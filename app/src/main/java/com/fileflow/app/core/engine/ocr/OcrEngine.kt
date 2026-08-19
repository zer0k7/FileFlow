package com.fileflow.app.core.engine.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.fileflow.app.core.saf.StorageManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.state.RenderingMode
import com.tom_roush.pdfbox.rendering.PDFRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OcrEngine(private val context: Context, private val storageManager: StorageManager) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { textResult ->
                    continuation.resume(textResult.text)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    suspend fun recognizeTextFromImageUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open image input stream")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        try {
            recognizeTextFromBitmap(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    suspend fun extractTextFromPdfWithOcr(
        pdfUri: Uri,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): String = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val doc = PDDocument.load(tempInput)
        val renderer = PDFRenderer(doc)
        val stringBuilder = StringBuilder()
        val totalPages = doc.numberOfPages

        try {
            for (i in 0 until totalPages) {
                onProgress(i + 1, totalPages)
                val pageBitmap = renderer.renderImageWithDPI(i, 200f)
                val pageText = recognizeTextFromBitmap(pageBitmap)
                pageBitmap.recycle()

                stringBuilder.append("--- Page ${i + 1} ---\n")
                stringBuilder.append(pageText)
                stringBuilder.append("\n\n")
            }
        } finally {
            doc.close()
            tempInput.delete()
        }

        stringBuilder.toString().trim()
    }

    suspend fun createSearchablePdf(
        imageUris: List<Uri>,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): File = withContext(Dispatchers.IO) {
        val doc = PDDocument()
        val outputFile = storageManager.createTempFile("ocr_searchable_", ".pdf")

        try {
            imageUris.forEachIndexed { index, uri ->
                onProgress(index + 1, imageUris.size)
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot read image")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                val recognizedText = recognizeTextFromBitmap(bitmap)

                val pageWidth = bitmap.width.toFloat()
                val pageHeight = bitmap.height.toFloat()
                val page = PDPage(PDRectangle(pageWidth, pageHeight))
                doc.addPage(page)

                val pdImage = JPEGFactory.createFromImage(doc, bitmap, 0.85f)
                val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.OVERWRITE, true, true)

                contentStream.drawImage(pdImage, 0f, 0f, pageWidth, pageHeight)

                if (recognizedText.isNotBlank()) {
                    contentStream.setRenderingMode(RenderingMode.NEITHER)
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA, 10f)
                    contentStream.newLineAtOffset(20f, pageHeight - 30f)

                    val lines = recognizedText.split("\n")
                    for (line in lines) {
                        val sanitized = line.replace(Regex("[^\\x20-\\x7E]"), " ").trim()
                        if (sanitized.isNotBlank()) {
                            try {
                                contentStream.showText(sanitized)
                                contentStream.newLineAtOffset(0f, -12f)
                            } catch (_: Exception) {
                            }
                        }
                    }
                    contentStream.endText()
                }

                contentStream.close()
                bitmap.recycle()
            }

            FileOutputStream(outputFile).use { out: FileOutputStream ->
                doc.save(out)
            }
        } finally {
            doc.close()
        }

        outputFile
    }
}
