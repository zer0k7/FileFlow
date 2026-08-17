package com.fileflow.app.core.engine.pdf

import android.content.Context
import android.net.Uri
import com.fileflow.app.core.saf.StorageManager
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfPasswordEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun removePassword(
        pdfUri: Uri,
        password: String
    ): File = withContext(Dispatchers.IO) {
        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFile = storageManager.createTempFile("FileFlow_Unlocked_", ".pdf")

        var document: PDDocument? = null
        try {
            document = PDDocument.load(tempPdf, password)
            if (document.isEncrypted) {
                document.setAllSecurityToBeRemoved(true)
            }
            document.save(outputFile)
            outputFile
        } catch (e: Exception) {
            outputFile.delete()
            throw IllegalArgumentException("Invalid password or corrupted PDF: ${e.message}", e)
        } finally {
            try {
                document?.close()
            } catch (_: Exception) {
            }
            tempPdf.delete()
        }
    }
}
