package com.fileflow.app.core.engine.pdf

import android.content.Context
import android.net.Uri
import com.fileflow.app.core.saf.StorageManager
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfPasswordProtectEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun protectPdf(
        pdfUri: Uri,
        userPassword: String,
        ownerPassword: String = userPassword
    ): File = withContext(Dispatchers.IO) {
        require(userPassword.isNotBlank()) { "Password cannot be empty" }

        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFile = storageManager.createTempFile("FileFlow_Protected_", ".pdf")

        var document: PDDocument? = null
        try {
            document = PDDocument.load(tempPdf)
            val permissions = AccessPermission()
            permissions.setCanPrint(true)
            permissions.setCanExtractContent(true)

            val protectionPolicy = StandardProtectionPolicy(
                ownerPassword,
                userPassword,
                permissions
            ).apply {
                encryptionKeyLength = 128
                this.permissions = permissions
            }

            document.protect(protectionPolicy)
            document.save(outputFile)
            outputFile
        } catch (e: Exception) {
            outputFile.delete()
            throw IllegalStateException("Failed to protect PDF: ${e.message}", e)
        } finally {
            try {
                document?.close()
            } catch (_: Exception) {
            }
            tempPdf.delete()
        }
    }
}
