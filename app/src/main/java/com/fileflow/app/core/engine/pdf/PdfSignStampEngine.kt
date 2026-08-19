package com.fileflow.app.core.engine.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import com.fileflow.app.core.model.StampPreset
import com.fileflow.app.core.saf.StorageManager
import com.tomroush.pdfbox.pdmodel.PDDocument
import com.tomroush.pdfbox.pdmodel.PDPageContentStream
import com.tomroush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tomroush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tomroush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfSignStampEngine(private val context: Context, private val storageManager: StorageManager) {

    fun generateStampBitmap(preset: StampPreset): Bitmap {
        val width = 400
        val height = 160
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val parsedColor = try {
            Color.parseColor(preset.colorHex)
        } catch (_: Exception) {
            Color.RED
        }

        val borderPaint = Paint().apply {
            color = parsedColor
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
        }

        val fillPaint = Paint().apply {
            color = parsedColor
            alpha = 25
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = parsedColor
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }

        val rect = RectF(16f, 16f, width - 16f, height - 16f)
        canvas.drawRoundRect(rect, 24f, 24f, fillPaint)
        canvas.drawRoundRect(rect, 24f, 24f, borderPaint)

        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(preset.text, 0, preset.text.length, textBounds)
        val textY = (height / 2f) - textBounds.exactCenterY()
        canvas.drawText(preset.text, width / 2f, textY, textPaint)

        return bitmap
    }

    suspend fun applySignatureOrStamp(
        pdfUri: Uri,
        overlayBitmap: Bitmap,
        pageIndex: Int = 0,
        xPercent: Float = 0.5f,
        yPercent: Float = 0.2f,
        scaleFactor: Float = 0.35f,
        opacity: Float = 1.0f
    ): File = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val doc = PDDocument.load(tempInput)
        val outputFile = storageManager.createTempFile("signed_", ".pdf")

        try {
            val targetIndex = pageIndex.coerceIn(0, (doc.numberOfPages - 1).coerceAtLeast(0))
            val page = doc.getPage(targetIndex)
            val mediaBox = page.mediaBox
            val pageWidth = mediaBox.width
            val pageHeight = mediaBox.height

            val pdImage = LosslessFactory.createFromImage(doc, overlayBitmap)

            val stampWidth = pageWidth * scaleFactor
            val stampHeight = stampWidth * (overlayBitmap.height.toFloat() / overlayBitmap.width.toFloat())

            val targetX = (pageWidth * xPercent) - (stampWidth / 2f)
            val targetY = (pageHeight * yPercent) - (stampHeight / 2f)

            val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)

            if (opacity < 1.0f) {
                val graphicsState = PDExtendedGraphicsState().apply {
                    nonStrokingAlphaConstant = opacity
                }
                contentStream.setGraphicsStateParameters(graphicsState)
            }

            contentStream.drawImage(pdImage, targetX, targetY, stampWidth, stampHeight)
            contentStream.close()

            FileOutputStream(outputFile).use { out ->
                doc.save(out)
            }
        } finally {
            doc.close()
            tempInput.delete()
        }

        outputFile
    }
}
