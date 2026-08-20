package com.fileflow.app.core.engine.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.fileflow.app.core.model.QrContentType
import com.fileflow.app.core.model.QrParsedResult
import com.fileflow.app.core.saf.StorageManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder

class QrBarcodeScannerEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {

    suspend fun scanImage(imageUri: Uri): QrParsedResult = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "qr_scan_in")
        try {
            val bitmap = decodeBitmapWithOrientation(tempInput)
                ?: throw IllegalStateException("Failed to decode image for barcode scan")

            val rawResult = scanBitmap(bitmap)
                ?: throw IllegalStateException("No readable QR code or barcode found in this image.")
            bitmap.recycle()

            parsePayload(rawResult.text, rawResult.barcodeFormat.name)
        } finally {
            tempInput.delete()
        }
    }

    private fun scanBitmap(bitmap: Bitmap): com.google.zxing.Result? {
        val directResult = decodeSingleBitmap(bitmap)
        if (directResult != null) return directResult

        // Fallback: Try 90 degree rotation (common for photos taken in portrait/landscape orientation)
        val matrix = Matrix().apply { postRotate(90f) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val rotatedResult = decodeSingleBitmap(rotated)
        if (rotated != bitmap) rotated.recycle()
        return rotatedResult
    }

    private fun decodeSingleBitmap(bitmap: Bitmap): com.google.zxing.Result? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val hints = mapOf(
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.entries
        )

        val reader = MultiFormatReader()
        reader.setHints(hints)

        return try {
            reader.decodeWithState(binaryBitmap)
        } catch (_: Exception) {
            try {
                val inverted = BinaryBitmap(HybridBinarizer(source.invert()))
                reader.decodeWithState(inverted)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun parsePayload(rawText: String, formatName: String): QrParsedResult {
        val trimmed = rawText.trim()

        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            val wifiDetails = parseWifiString(trimmed)
            val ssid = wifiDetails["SSID"] ?: "Wi-Fi Network"
            val pass = wifiDetails["Password"] ?: "None"
            val security = wifiDetails["Security"] ?: "WPA/WPA2"
            return QrParsedResult(
                rawText = trimmed,
                formatName = formatName,
                type = QrContentType.WIFI,
                displayTitle = ssid,
                displaySubtitle = "Security: $security • Password: $pass",
                details = wifiDetails
            )
        }

        if (trimmed.startsWith("upi://pay", ignoreCase = true) || (trimmed.contains("pa=") && trimmed.contains("pn="))) {
            val upiDetails = parseUpiString(trimmed)
            val pa = upiDetails["UPI ID"] ?: "Unknown"
            val pn = upiDetails["Payee Name"] ?: "Merchant"
            val am = upiDetails["Amount"]
            val subtitle = if (!am.isNullOrBlank()) "Payee: $pn • Amount: ₹$am" else "Payee: $pn"
            return QrParsedResult(
                rawText = trimmed,
                formatName = formatName,
                type = QrContentType.UPI,
                displayTitle = pa,
                displaySubtitle = subtitle,
                details = upiDetails,
                actionUrl = trimmed
            )
        }

        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true)) {
            val contactDetails = parseVCardString(trimmed)
            val name = contactDetails["Name"] ?: "Contact"
            val phone = contactDetails["Phone"] ?: ""
            val email = contactDetails["Email"] ?: ""
            return QrParsedResult(
                rawText = trimmed,
                formatName = formatName,
                type = QrContentType.VCARD,
                displayTitle = name,
                displaySubtitle = if (phone.isNotEmpty()) phone else email,
                details = contactDetails
            )
        }

        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return QrParsedResult(
                rawText = trimmed,
                formatName = formatName,
                type = QrContentType.URL,
                displayTitle = trimmed,
                displaySubtitle = "Web Link",
                details = mapOf("URL" to trimmed),
                actionUrl = trimmed
            )
        }

        if (trimmed.startsWith("mailto:", ignoreCase = true)) {
            val email = trimmed.removePrefix("mailto:")
            return QrParsedResult(
                rawText = trimmed,
                formatName = formatName,
                type = QrContentType.EMAIL,
                displayTitle = email,
                displaySubtitle = "Email Address",
                details = mapOf("Email" to email),
                actionUrl = trimmed
            )
        }

        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.removePrefix("tel:")
            return QrParsedResult(
                rawText = trimmed,
                formatName = formatName,
                type = QrContentType.PHONE,
                displayTitle = phone,
                displaySubtitle = "Phone Number",
                details = mapOf("Phone" to phone),
                actionUrl = trimmed
            )
        }

        if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
            val number = trimmed.substringAfter(":").substringBefore(":")
            return QrParsedResult(
                rawText = trimmed,
                formatName = formatName,
                type = QrContentType.SMS,
                displayTitle = number,
                displaySubtitle = "SMS Message",
                details = mapOf("Number" to number, "Payload" to trimmed),
                actionUrl = trimmed
            )
        }

        return QrParsedResult(
            rawText = trimmed,
            formatName = formatName,
            type = if (formatName == "QR_CODE") QrContentType.TEXT else QrContentType.BARCODE,
            displayTitle = trimmed.take(80) + if (trimmed.length > 80) "..." else "",
            displaySubtitle = if (formatName == "QR_CODE") "Text Content" else "Barcode ($formatName)",
            details = mapOf("Content" to trimmed, "Format" to formatName)
        )
    }

    private fun parseWifiString(wifi: String): Map<String, String> {
        val clean = wifi.removePrefix("WIFI:").removePrefix("wifi:")
        val parts = clean.split(";")
        val map = mutableMapOf<String, String>()
        for (part in parts) {
            when {
                part.startsWith("S:", ignoreCase = true) -> map["SSID"] = part.substring(2)
                part.startsWith("P:", ignoreCase = true) -> map["Password"] = part.substring(2)
                part.startsWith("T:", ignoreCase = true) -> map["Security"] = part.substring(2)
                part.startsWith("H:", ignoreCase = true) -> map["Hidden"] = if (part.substring(2) == "true") "Yes" else "No"
            }
        }
        return map
    }

    private fun parseUpiString(upi: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val query = upi.substringAfter("?")
        val params = query.split("&")
        for (param in params) {
            val pair = param.split("=")
            if (pair.size == 2) {
                val key = pair[0]
                val value = try { URLDecoder.decode(pair[1], "UTF-8") } catch (_: Exception) { pair[1] }
                when (key) {
                    "pa" -> map["UPI ID"] = value
                    "pn" -> map["Payee Name"] = value
                    "am" -> map["Amount"] = value
                    "tn" -> map["Note"] = value
                    "cu" -> map["Currency"] = value
                }
            }
        }
        return map
    }

    private fun parseVCardString(vcard: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val lines = vcard.split("\n", "\r\n")
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("FN:", ignoreCase = true) -> map["Name"] = trimmed.substring(3)
                trimmed.startsWith("N:", ignoreCase = true) && !map.containsKey("Name") -> map["Name"] = trimmed.substring(2).replace(";", " ")
                trimmed.startsWith("TEL", ignoreCase = true) -> map["Phone"] = trimmed.substringAfter(":")
                trimmed.startsWith("EMAIL", ignoreCase = true) -> map["Email"] = trimmed.substringAfter(":")
                trimmed.startsWith("ORG:", ignoreCase = true) -> map["Company"] = trimmed.substring(4)
                trimmed.startsWith("TITLE:", ignoreCase = true) -> map["Job Title"] = trimmed.substring(6)
            }
        }
        return map
    }

    private fun decodeBitmapWithOrientation(file: File): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        val orientation = try {
            val exif = ExifInterface(file.absolutePath)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) {
            return bitmap
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        }

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }
}
