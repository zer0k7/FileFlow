package com.fileflow.app.core.engine.pdf

import android.content.Context
import android.net.Uri
import com.fileflow.app.core.model.PdfMetadata
import com.fileflow.app.core.saf.StorageManager
import com.tomroush.pdfbox.pdmodel.PDDocument
import com.tomroush.pdfbox.pdmodel.PDDocumentInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfMetadataEngine(private val context: Context, private val storageManager: StorageManager) {

    suspend fun readMetadata(pdfUri: Uri): PdfMetadata = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val doc = PDDocument.load(tempInput)
        try {
            val info = doc.documentInformation
            PdfMetadata(
                title = info?.title ?: "",
                author = info?.author ?: "",
                subject = info?.subject ?: "",
                keywords = info?.keywords ?: "",
                creator = info?.creator ?: "",
                producer = info?.producer ?: "",
                pageCount = doc.numberOfPages,
                isEncrypted = doc.isEncrypted
            )
        } finally {
            doc.close()
            tempInput.delete()
        }
    }

    suspend fun updateMetadata(
        pdfUri: Uri,
        newMetadata: PdfMetadata
    ): File = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val doc = PDDocument.load(tempInput)
        val outputFile = storageManager.createTempFile("meta_updated_", ".pdf")

        try {
            var info = doc.documentInformation
            if (info == null) {
                info = PDDocumentInformation()
                doc.documentInformation = info
            }

            info.title = newMetadata.title.trim()
            info.author = newMetadata.author.trim()
            info.subject = newMetadata.subject.trim()
            info.keywords = newMetadata.keywords.trim()
            if (newMetadata.creator.isNotBlank()) {
                info.creator = newMetadata.creator.trim()
            }

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
