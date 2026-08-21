package com.fileflow.app.core.engine.security

import android.content.Context
import android.net.Uri
import com.fileflow.app.core.model.EngineResult
import com.fileflow.app.core.model.SecurityScanReport
import com.fileflow.app.core.model.SecurityServiceType
import com.fileflow.app.core.model.SecurityThreatVerdict
import com.fileflow.app.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecurityScannerEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {

    suspend fun computeFileHashes(uri: Uri): Pair<String, String> = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(uri, "hash_chk")
        try {
            val sha256 = calculateHash(tempInput, "SHA-256")
            val md5 = calculateHash(tempInput, "MD5")
            Pair(sha256, md5)
        } finally {
            tempInput.delete()
        }
    }

    suspend fun scanFile(
        uri: Uri,
        service: SecurityServiceType,
        apiKey: String,
        allowUpload: Boolean = false,
        onProgress: (String) -> Unit = {}
    ): SecurityScanReport = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(uri, "sec_scan")
        try {
            val fileName = storageManager.getFileName(uri)
            val fileSize = tempInput.length()
            val sha256 = calculateHash(tempInput, "SHA-256")
            val md5 = calculateHash(tempInput, "MD5")

            when (service) {
                SecurityServiceType.VIRUSTOTAL -> {
                    require(apiKey.isNotBlank()) { "Please enter your free VirusTotal API key in Options & Settings" }
                    onProgress("Querying VirusTotal multi-engine database...")
                    val report = checkVirusTotalHash(apiKey, sha256, md5, fileName, fileSize)
                    if (report != null) {
                        report
                    } else if (allowUpload) {
                        onProgress("File hash not in database. Uploading to VirusTotal...")
                        uploadAndScanVirusTotal(apiKey, tempInput, sha256, md5, fileName, fileSize, onProgress)
                    } else {
                        SecurityScanReport(
                            serviceName = "VirusTotal",
                            fileName = fileName,
                            fileSize = fileSize,
                            sha256 = sha256,
                            md5 = md5,
                            verdict = SecurityThreatVerdict.UNKNOWN,
                            threatScoreText = "Not yet analyzed in VirusTotal database. You can upload it for a live multi-engine scan.",
                            maliciousEngines = 0,
                            suspiciousEngines = 0,
                            totalEngines = 0,
                            webReportUrl = "https://www.virustotal.com/gui/file/$sha256",
                            scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                        )
                    }
                }

                SecurityServiceType.MALWAREBAZAAR -> {
                    onProgress("Querying Abuse.ch MalwareBazaar threat intelligence...")
                    checkMalwareBazaarHash(sha256, md5, fileName, fileSize)
                }

                SecurityServiceType.HYBRID_ANALYSIS -> {
                    require(apiKey.isNotBlank()) { "Please enter your free Hybrid Analysis API key in Options & Settings" }
                    onProgress("Querying CrowdStrike Falcon Sandbox intelligence...")
                    checkHybridAnalysisHash(apiKey, sha256, md5, fileName, fileSize)
                }
            }
        } finally {
            tempInput.delete()
        }
    }

    suspend fun testApiKey(service: SecurityServiceType, apiKey: String): String = withContext(Dispatchers.IO) {
        when (service) {
            SecurityServiceType.VIRUSTOTAL -> {
                val cleanKey = apiKey.trim()
                if (cleanKey.length != 64) {
                    throw IllegalArgumentException("VirusTotal API key must be a 64-character hex string.")
                }
                val url = URL("https://www.virustotal.com/api/v3/users/$cleanKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("x-apikey", cleanKey)
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                val code = conn.responseCode
                if (code == 200) {
                    val resp = readResponse(conn)
                    val json = JSONObject(resp)
                    val username = json.optJSONObject("data")?.optJSONObject("attributes")?.optString("username", "User") ?: "User"
                    "Connected successfully as '$username'"
                } else if (code == 401 || code == 403) {
                    throw IllegalStateException("Invalid VirusTotal API Key (HTTP $code).")
                } else {
                    throw IllegalStateException("VirusTotal connection test failed with status HTTP $code.")
                }
            }

            SecurityServiceType.HYBRID_ANALYSIS -> {
                val cleanKey = apiKey.trim()
                val url = URL("https://www.hybrid-analysis.com/api/v2/key/current")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("api-key", cleanKey)
                conn.setRequestProperty("User-Agent", "Falcon Sandbox")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                val code = conn.responseCode
                if (code == 200) {
                    "Connected successfully to Hybrid Analysis"
                } else if (code == 401 || code == 403) {
                    throw IllegalStateException("Invalid Hybrid Analysis API key (HTTP $code).")
                } else {
                    throw IllegalStateException("Hybrid Analysis connection failed with status HTTP $code.")
                }
            }

            SecurityServiceType.MALWAREBAZAAR -> {
                "MalwareBazaar is public & active (No API key required)."
            }
        }
    }

    private fun checkVirusTotalHash(
        apiKey: String,
        sha256: String,
        md5: String,
        fileName: String,
        fileSize: Long
    ): SecurityScanReport? {
        val url = URL("https://www.virustotal.com/api/v3/files/$sha256")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("x-apikey", apiKey.trim())
            connectTimeout = 12000
            readTimeout = 12000
        }

        val code = conn.responseCode
        if (code == 404) {
            return null
        }
        if (code == 401 || code == 403) {
            throw IllegalStateException("Invalid VirusTotal API Key. Please verify in settings.")
        }
        if (code == 429) {
            throw IllegalStateException("VirusTotal Free Rate Limit reached (Max 4 requests/min). Please wait a moment.")
        }
        if (code != 200) {
            throw IllegalStateException("VirusTotal error: HTTP $code")
        }

        val response = readResponse(conn)
        val json = JSONObject(response)
        val attributes = json.getJSONObject("data").getJSONObject("attributes")

        val stats = attributes.getJSONObject("last_analysis_stats")
        val malicious = stats.optInt("malicious", 0)
        val suspicious = stats.optInt("suspicious", 0)
        val harmless = stats.optInt("harmless", 0)
        val undetected = stats.optInt("undetected", 0)
        val total = malicious + suspicious + harmless + undetected

        val detections = mutableListOf<EngineResult>()
        val resultsObj = attributes.optJSONObject("last_analysis_results")
        if (resultsObj != null) {
            val keys = resultsObj.keys()
            while (keys.hasNext()) {
                val engineKey = keys.next()
                val engineData = resultsObj.getJSONObject(engineKey)
                val category = engineData.optString("category", "undetected")
                val result = engineData.optString("result", null)
                detections.add(
                    EngineResult(
                        engineName = engineData.optString("engine_name", engineKey),
                        category = category,
                        threatName = if (result != "null" && result.isNotBlank()) result else null
                    )
                )
            }
        }

        // Prioritize malicious/suspicious engines at the top
        detections.sortBy {
            when (it.category) {
                "malicious" -> 0
                "suspicious" -> 1
                "harmless" -> 2
                else -> 3
            }
        }

        val tags = mutableListOf<String>()
        val tagsArray = attributes.optJSONArray("tags")
        if (tagsArray != null) {
            for (i in 0 until tagsArray.length()) {
                tags.add(tagsArray.getString(i))
            }
        }

        val verdict = when {
            malicious > 0 -> SecurityThreatVerdict.MALICIOUS
            suspicious > 0 -> SecurityThreatVerdict.SUSPICIOUS
            total > 0 -> SecurityThreatVerdict.CLEAN
            else -> SecurityThreatVerdict.UNKNOWN
        }

        val threatScoreText = when (verdict) {
            SecurityThreatVerdict.CLEAN -> "Clean / Safe (0 of $total security engines detected threats)"
            SecurityThreatVerdict.SUSPICIOUS -> "Suspicious ($suspicious of $total engines flagged as suspicious)"
            SecurityThreatVerdict.MALICIOUS -> "Malicious ($malicious of $total antivirus engines flagged threats)"
            SecurityThreatVerdict.UNKNOWN -> "Unknown status"
        }

        return SecurityScanReport(
            serviceName = "VirusTotal",
            fileName = fileName,
            fileSize = fileSize,
            sha256 = sha256,
            md5 = md5,
            verdict = verdict,
            threatScoreText = threatScoreText,
            maliciousEngines = malicious,
            suspiciousEngines = suspicious,
            totalEngines = total,
            engineDetections = detections,
            threatTags = tags,
            webReportUrl = "https://www.virustotal.com/gui/file/$sha256",
            scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
            rawDetails = response
        )
    }

    private suspend fun uploadAndScanVirusTotal(
        apiKey: String,
        file: File,
        sha256: String,
        md5: String,
        fileName: String,
        fileSize: Long,
        onProgress: (String) -> Unit
    ): SecurityScanReport {
        if (file.length() > 32 * 1024 * 1024) {
            throw IllegalArgumentException("File exceeds 32 MB free direct upload limit for VirusTotal.")
        }

        val boundary = "===FileFlowSecurityBoundary${System.currentTimeMillis()}==="
        val uploadUrl = URL("https://www.virustotal.com/api/v3/files")
        val conn = (uploadUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("x-apikey", apiKey.trim())
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connectTimeout = 30000
            readTimeout = 60000
        }

        DataOutputStream(conn.outputStream).use { out ->
            out.writeBytes("--$boundary\r\n")
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
            out.writeBytes("Content-Type: application/octet-stream\r\n\r\n")

            FileInputStream(file).use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
            }
            out.writeBytes("\r\n--$boundary--\r\n")
            out.flush()
        }

        val code = conn.responseCode
        if (code != 200 && code != 201) {
            throw IllegalStateException("Failed to upload file to VirusTotal (HTTP $code).")
        }

        val uploadResp = readResponse(conn)
        val uploadJson = JSONObject(uploadResp)
        val analysisId = uploadJson.getJSONObject("data").getString("id")

        onProgress("Analysis queued. Waiting for multi-engine scan results...")

        // Poll analysis status
        var attempts = 0
        while (attempts < 12) {
            delay(5000)
            attempts++
            onProgress("Analyzing across 70+ antivirus engines (Attempt $attempts/12)...")

            val checkUrl = URL("https://www.virustotal.com/api/v3/analyses/$analysisId")
            val checkConn = (checkUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("x-apikey", apiKey.trim())
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (checkConn.responseCode == 200) {
                val checkResp = readResponse(checkConn)
                val checkJson = JSONObject(checkResp)
                val status = checkJson.getJSONObject("data").getJSONObject("attributes").getString("status")
                if (status == "completed") {
                    val report = checkVirusTotalHash(apiKey, sha256, md5, fileName, fileSize)
                    if (report != null) return report
                }
            }
        }

        return SecurityScanReport(
            serviceName = "VirusTotal",
            fileName = fileName,
            fileSize = fileSize,
            sha256 = sha256,
            md5 = md5,
            verdict = SecurityThreatVerdict.UNKNOWN,
            threatScoreText = "Analysis is currently processing in VirusTotal cloud. View live progress on web.",
            webReportUrl = "https://www.virustotal.com/gui/file/$sha256",
            scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        )
    }

    private fun checkMalwareBazaarHash(
        sha256: String,
        md5: String,
        fileName: String,
        fileSize: Long
    ): SecurityScanReport {
        val url = URL("https://mb-api.abuse.ch/api/v1/")
        val postData = "query=get_info&hash=" + URLEncoder.encode(sha256, "UTF-8")

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connectTimeout = 12000
            readTimeout = 12000
        }

        conn.outputStream.use { out ->
            out.write(postData.toByteArray(Charsets.UTF_8))
            out.flush()
        }

        val code = conn.responseCode
        if (code != 200) {
            throw IllegalStateException("MalwareBazaar error: HTTP $code")
        }

        val response = readResponse(conn)
        val json = JSONObject(response)
        val queryStatus = json.optString("query_status", "hash_not_found")

        if (queryStatus == "hash_not_found" || queryStatus == "no_results") {
            return SecurityScanReport(
                serviceName = "MalwareBazaar (Abuse.ch)",
                fileName = fileName,
                fileSize = fileSize,
                sha256 = sha256,
                md5 = md5,
                verdict = SecurityThreatVerdict.CLEAN,
                threatScoreText = "Clean / Not Listed in Abuse.ch Malware Threat Database",
                maliciousEngines = 0,
                totalEngines = 1,
                threatTags = listOf("MalwareBazaar: Clean"),
                webReportUrl = "https://bazaar.abuse.ch/sample/$sha256/",
                scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
        }

        if (queryStatus == "ok") {
            val dataArray = json.optJSONArray("data")
            val item = dataArray?.optJSONObject(0)
            val signature = item?.optString("signature", "Generic Malware") ?: "Generic Malware"
            val fileType = item?.optString("file_type", "Unknown") ?: "Unknown"
            val reporter = item?.optString("reporter", "Community") ?: "Community"

            val tags = mutableListOf<String>()
            val tagsArray = item?.optJSONArray("tags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(i))
                }
            }

            val detections = listOf(
                EngineResult("Abuse.ch Signature", "malicious", signature),
                EngineResult("File Classification", "malicious", fileType),
                EngineResult("Threat Reporter", "malicious", reporter)
            )

            return SecurityScanReport(
                serviceName = "MalwareBazaar (Abuse.ch)",
                fileName = fileName,
                fileSize = fileSize,
                sha256 = sha256,
                md5 = md5,
                verdict = SecurityThreatVerdict.MALICIOUS,
                threatScoreText = "Malware Detected: $signature ($fileType)",
                maliciousEngines = 1,
                totalEngines = 1,
                engineDetections = detections,
                threatTags = tags,
                webReportUrl = "https://bazaar.abuse.ch/sample/$sha256/",
                scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
        }

        return SecurityScanReport(
            serviceName = "MalwareBazaar",
            fileName = fileName,
            fileSize = fileSize,
            sha256 = sha256,
            md5 = md5,
            verdict = SecurityThreatVerdict.UNKNOWN,
            threatScoreText = "MalwareBazaar status: $queryStatus",
            webReportUrl = "https://bazaar.abuse.ch/sample/$sha256/",
            scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        )
    }

    private fun checkHybridAnalysisHash(
        apiKey: String,
        sha256: String,
        md5: String,
        fileName: String,
        fileSize: Long
    ): SecurityScanReport {
        val url = URL("https://www.hybrid-analysis.com/api/v2/search/hash")
        val postData = "hash=" + URLEncoder.encode(sha256, "UTF-8")

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("api-key", apiKey.trim())
            setRequestProperty("User-Agent", "Falcon Sandbox")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connectTimeout = 12000
            readTimeout = 12000
        }

        conn.outputStream.use { out ->
            out.write(postData.toByteArray(Charsets.UTF_8))
            out.flush()
        }

        val code = conn.responseCode
        if (code == 404) {
            return SecurityScanReport(
                serviceName = "Hybrid Analysis",
                fileName = fileName,
                fileSize = fileSize,
                sha256 = sha256,
                md5 = md5,
                verdict = SecurityThreatVerdict.UNKNOWN,
                threatScoreText = "File hash not yet indexed in CrowdStrike Falcon database.",
                webReportUrl = "https://www.hybrid-analysis.com/sample/$sha256",
                scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
        }
        if (code == 401 || code == 403) {
            throw IllegalStateException("Invalid Hybrid Analysis API Key. Please verify in settings.")
        }
        if (code != 200) {
            throw IllegalStateException("Hybrid Analysis error: HTTP $code")
        }

        val response = readResponse(conn)
        val jsonArray = org.json.JSONArray(response)
        if (jsonArray.length() == 0) {
            return SecurityScanReport(
                serviceName = "Hybrid Analysis",
                fileName = fileName,
                fileSize = fileSize,
                sha256 = sha256,
                md5 = md5,
                verdict = SecurityThreatVerdict.UNKNOWN,
                threatScoreText = "No previous sandbox report for this hash.",
                webReportUrl = "https://www.hybrid-analysis.com/sample/$sha256",
                scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
        }

        val firstReport = jsonArray.getJSONObject(0)
        val verdictStr = firstReport.optString("verdict", "no specific threat").lowercase()
        val threatScore = firstReport.optInt("threat_score", 0)
        val avDetect = firstReport.optInt("av_detect", 0)
        val env = firstReport.optString("environment_description", "Android Sandbox")

        val verdict = when {
            verdictStr.contains("malicious") || threatScore >= 70 -> SecurityThreatVerdict.MALICIOUS
            verdictStr.contains("suspicious") || threatScore >= 30 -> SecurityThreatVerdict.SUSPICIOUS
            else -> SecurityThreatVerdict.CLEAN
        }

        val detections = listOf(
            EngineResult("Falcon Threat Score", if (threatScore > 30) "malicious" else "harmless", "$threatScore / 100"),
            EngineResult("AV Detections", if (avDetect > 0) "malicious" else "harmless", "$avDetect engines flagged"),
            EngineResult("Sandbox Environment", "undetected", env)
        )

        return SecurityScanReport(
            serviceName = "Hybrid Analysis (CrowdStrike)",
            fileName = fileName,
            fileSize = fileSize,
            sha256 = sha256,
            md5 = md5,
            verdict = verdict,
            threatScoreText = "Falcon Verdict: ${verdictStr.replaceFirstChar { it.uppercase() }} (Threat Score: $threatScore/100)",
            maliciousEngines = avDetect,
            totalEngines = 100,
            engineDetections = detections,
            webReportUrl = "https://www.hybrid-analysis.com/sample/$sha256",
            scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
            rawDetails = response
        )
    }

    private fun calculateHash(file: File, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val bytes = digest.digest()
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val reader = BufferedReader(
            InputStreamReader(
                if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream ?: conn.inputStream,
                Charsets.UTF_8
            )
        )
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        reader.close()
        return sb.toString()
    }
}
