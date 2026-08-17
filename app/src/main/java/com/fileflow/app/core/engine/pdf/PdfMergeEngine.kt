package com.fileflow.app.core.engine.pdf

import android.content.Context
import android.net.Uri
import com.fileflow.app.core.saf.StorageManager
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfMergeEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun merge(
        pdfUris: List<Uri>,
        onProgress: (Int, Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val merger = PDFMergerUtility()
        val tempFiles = mutableListOf<File>()
        val outputFile = storageManager.createTempFile("FileFlow_Merged_", ".pdf")

        try {
            val total = pdfUris.size
            pdfUris.forEachIndexed { index, uri ->
                onProgress(index + 1, total)
                val temp = storageManager.copyUriToLocalTemp(uri, "pdf")
                tempFiles.add(temp)
                merger.addSource(temp)
            }

            FileOutputStream(outputFile).use { outStream ->
                merger.destinationStream = outStream
                merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())
            }

            outputFile
        } finally {
            tempFiles.forEach { it.delete() }
        }
    }
}
