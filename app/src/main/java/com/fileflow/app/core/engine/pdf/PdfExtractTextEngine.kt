package com.fileflow.app.core.engine.pdf

import android.content.Context
import android.net.Uri
import com.fileflow.app.core.saf.StorageManager
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfExtractTextEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun extractText(pdfUri: Uri): File = withContext(Dispatchers.IO) {
        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFile = storageManager.createTempFile("FileFlow_ExtractedText_", ".txt")

        var document: PDDocument? = null
        try {
            document = PDDocument.load(tempPdf)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)

            val outputContent = if (text.isBlank()) {
                "No selectable text found in this PDF document.\n(If this is a scanned document, text cannot be extracted without OCR)."
            } else {
                text
            }

            outputFile.writeText(outputContent, Charsets.UTF_8)
            outputFile
        } catch (e: Exception) {
            outputFile.delete()
            throw IllegalStateException("Failed to extract text from PDF: ${e.message}", e)
        } finally {
            try {
                document?.close()
            } catch (_: Exception) {
            }
            tempPdf.delete()
        }
    }
}
