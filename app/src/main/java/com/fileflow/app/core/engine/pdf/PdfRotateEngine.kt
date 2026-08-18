package com.fileflow.app.core.engine.pdf

import android.content.Context
import android.net.Uri
import com.fileflow.app.core.saf.StorageManager
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfRotateEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun rotatePdf(
        pdfUri: Uri,
        rotationDegrees: Int = 90
    ): File = withContext(Dispatchers.IO) {
        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFile = storageManager.createTempFile("FileFlow_Rotated_", ".pdf")

        var document: PDDocument? = null
        try {
            document = PDDocument.load(tempPdf)
            val pageCount = document.numberOfPages
            for (i in 0 until pageCount) {
                val page = document.getPage(i)
                val currentRotation = page.rotation
                page.rotation = (currentRotation + rotationDegrees) % 360
            }
            document.save(outputFile)
            outputFile
        } catch (e: Exception) {
            outputFile.delete()
            throw IllegalStateException("Failed to rotate PDF: ${e.message}", e)
        } finally {
            try {
                document?.close()
            } catch (_: Exception) {
            }
            tempPdf.delete()
        }
    }
}
