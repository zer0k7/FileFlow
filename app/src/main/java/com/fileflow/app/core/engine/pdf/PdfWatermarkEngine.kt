package com.fileflow.app.core.engine.pdf

import android.content.Context
import android.net.Uri
import com.fileflow.app.core.saf.StorageManager
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import com.tom_roush.pdfbox.util.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfWatermarkEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun addWatermark(
        pdfUri: Uri,
        watermarkText: String,
        opacity: Float = 0.35f,
        fontSize: Float = 48f
    ): File = withContext(Dispatchers.IO) {
        require(watermarkText.isNotBlank()) { "Watermark text cannot be empty" }

        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFile = storageManager.createTempFile("FileFlow_Watermarked_", ".pdf")

        var document: PDDocument? = null
        try {
            document = PDDocument.load(tempPdf)
            val font = PDType1Font.HELVETICA_BOLD
            val pageCount = document.numberOfPages

            for (i in 0 until pageCount) {
                val page = document.getPage(i)
                val mediaBox = page.mediaBox
                val width = mediaBox.width
                val height = mediaBox.height

                val stringWidth = font.getStringWidth(watermarkText) / 1000f * fontSize
                val x = (width - stringWidth * 0.7f) / 2f
                val y = height / 2f

                PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
                ).use { contentStream ->
                    val graphicsState = PDExtendedGraphicsState().apply {
                        nonStrokingAlphaConstant = opacity
                    }
                    contentStream.setGraphicsStateParameters(graphicsState)
                    contentStream.setNonStrokingColor(150, 150, 150)
                    contentStream.beginText()
                    contentStream.setFont(font, fontSize)

                    val matrix = Matrix.getRotateInstance(Math.toRadians(45.0), width / 2f, height / 2f)
                    contentStream.setTextMatrix(matrix)
                    contentStream.showText(watermarkText)
                    contentStream.endText()
                }
            }

            document.save(outputFile)
            outputFile
        } catch (e: Exception) {
            outputFile.delete()
            throw IllegalStateException("Failed to add watermark: ${e.message}", e)
        } finally {
            try {
                document?.close()
            } catch (_: Exception) {
            }
            tempPdf.delete()
        }
    }
}
