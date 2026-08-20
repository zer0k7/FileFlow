package com.fileflow.app.core.engine.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.fileflow.app.core.model.QrPayloadType
import com.fileflow.app.core.saf.StorageManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

class QrBarcodeGeneratorEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {

    fun buildPayload(
        type: QrPayloadType,
        url: String = "",
        wifiSsid: String = "",
        wifiPassword: String = "",
        wifiSecurity: String = "WPA",
        wifiHidden: Boolean = false,
        upiId: String = "",
        upiName: String = "",
        upiAmount: String = "",
        upiNote: String = "",
        vcardName: String = "",
        vcardPhone: String = "",
        vcardEmail: String = "",
        vcardCompany: String = "",
        text: String = "",
        email: String = "",
        phone: String = ""
    ): String {
        return when (type) {
            QrPayloadType.URL -> {
                val clean = url.trim()
                if (clean.startsWith("http://", ignoreCase = true) || clean.startsWith("https://", ignoreCase = true)) {
                    clean
                } else {
                    "https://$clean"
                }
            }

            QrPayloadType.WIFI -> {
                val sec = if (wifiSecurity.equals("NONE", ignoreCase = true) || wifiSecurity.equals("OPEN", ignoreCase = true)) "nopass" else wifiSecurity
                "WIFI:T:$sec;S:${wifiSsid.trim()};P:${wifiPassword.trim()};H:$wifiHidden;;"
            }

            QrPayloadType.UPI -> {
                val pa = upiId.trim()
                val pn = try { URLEncoder.encode(upiName.trim(), "UTF-8") } catch (_: Exception) { upiName.trim() }
                val am = upiAmount.trim()
                val tn = try { URLEncoder.encode(upiNote.trim(), "UTF-8") } catch (_: Exception) { upiNote.trim() }

                val builder = StringBuilder("upi://pay?pa=$pa&pn=$pn")
                if (am.isNotBlank()) builder.append("&am=$am")
                if (tn.isNotBlank()) builder.append("&tn=$tn")
                builder.append("&cu=INR")
                builder.toString()
            }

            QrPayloadType.VCARD -> {
                StringBuilder("BEGIN:VCARD\nVERSION:3.0\n")
                    .append("FN:${vcardName.trim()}\n")
                    .append("N:${vcardName.trim()};;;\n")
                    .apply {
                        if (vcardPhone.isNotBlank()) append("TEL;TYPE=CELL:${vcardPhone.trim()}\n")
                        if (vcardEmail.isNotBlank()) append("EMAIL;TYPE=INTERNET:${vcardEmail.trim()}\n")
                        if (vcardCompany.isNotBlank()) append("ORG:${vcardCompany.trim()}\n")
                    }
                    .append("END:VCARD")
                    .toString()
            }

            QrPayloadType.TEXT -> text.trim()

            QrPayloadType.EMAIL -> {
                val clean = email.trim()
                if (clean.startsWith("mailto:", ignoreCase = true)) clean else "mailto:$clean"
            }

            QrPayloadType.PHONE -> {
                val clean = phone.trim()
                if (clean.startsWith("tel:", ignoreCase = true)) clean else "tel:$clean"
            }
        }
    }

    fun generateQrBitmap(
        payload: String,
        sizePx: Int = 1024,
        fgColor: Int = Color.BLACK,
        bgColor: Int = Color.WHITE,
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): Bitmap {
        if (payload.isBlank()) {
            val emptyBitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            emptyBitmap.eraseColor(bgColor)
            return emptyBitmap
        }

        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to errorCorrection,
            EncodeHintType.MARGIN to 1
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) fgColor else bgColor
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    suspend fun exportQrCode(
        payload: String,
        sizePx: Int = 1024,
        fgColor: Int = Color.BLACK,
        bgColor: Int = Color.WHITE,
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): File = withContext(Dispatchers.IO) {
        val bitmap = generateQrBitmap(payload, sizePx, fgColor, bgColor, errorCorrection)
        val outputFile = storageManager.createTempFile("FileFlow_QR_", ".png")
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        outputFile
    }
}
