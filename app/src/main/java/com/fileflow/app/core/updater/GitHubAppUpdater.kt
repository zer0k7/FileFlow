package com.fileflow.app.core.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val isAvailable: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeEstimate: Long
)

class GitHubAppUpdater(
    private val context: Context,
    private val githubRepo: String = "zer0k7/FileFlow"
) {

    suspend fun checkForUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$githubRepo/releases/latest")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "FileFlow-Android-App")
                connectTimeout = 15000
                readTimeout = 15000
            }

            if (connection.responseCode != 200) {
                return@withContext null
            }

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)

            val latestTag = json.getString("tag_name").removePrefix("v").trim()
            val releaseNotes = json.optString("body", "").trim()
            val currentVersion = getAppVersionName()

            var apkUrl: String? = null
            var apkFileName = "FileFlow-v$latestTag.apk"
            var apkSize = 0L

            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.getString("browser_download_url")
                        apkFileName = name
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            if (apkUrl == null) {
                apkUrl = json.optString("html_url", "https://github.com/$githubRepo/releases/latest")
            }

            val isNewer = compareVersions(latestTag, currentVersion) > 0

            AppUpdateInfo(
                isAvailable = isNewer,
                currentVersion = currentVersion,
                latestVersion = latestTag,
                releaseNotes = releaseNotes,
                apkDownloadUrl = apkUrl,
                apkFileName = apkFileName,
                apkSizeEstimate = apkSize
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun downloadApk(
        downloadUrl: String,
        targetFileName: String = "FileFlow_update.apk",
        onProgress: (percentage: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val destinationFile = File(cacheDir, targetFileName)

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "FileFlow-Android-App")
                connect()
            }

            val totalBytes = connection.contentLength.toLong()
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (totalBytes > 0) {
                            val percent = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                            withContext(Dispatchers.Main) {
                                onProgress(percent, downloadedBytes, totalBytes)
                            }
                        }
                    }
                    output.flush()
                }
            }

            destinationFile
        } catch (_: Exception) {
            null
        }
    }

    fun installApk(apkFile: File): Boolean {
        return try {
            if (!apkFile.exists()) return false

            // Check if app can install packages on Android 8.0+ (API 26)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                }
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(apkFile.absolutePath)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun getAppVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLen) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 != num2) return num1.compareTo(num2)
        }
        return 0
    }
}
