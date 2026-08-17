package com.salik.fileflow.core.engine.pdf

import android.content.Context
import android.net.Uri
import com.salik.fileflow.core.saf.StorageManager
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfSplitEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun extractPages(
        pdfUri: Uri,
        pageRangeString: String,
        onProgress: (Int, Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFile = storageManager.createTempFile("FileFlow_Extracted_", ".pdf")

        var sourceDoc: PDDocument? = null
        var newDoc: PDDocument? = null

        try {
            sourceDoc = PDDocument.load(tempPdf)
            newDoc = PDDocument()
            val totalPages = sourceDoc.numberOfPages
            val pagesToExtract = parsePageRange(pageRangeString, totalPages)

            if (pagesToExtract.isEmpty()) {
                throw IllegalArgumentException("No valid pages selected for extraction")
            }

            pagesToExtract.forEachIndexed { index, pageNum ->
                onProgress(index + 1, pagesToExtract.size)
                val page = sourceDoc.getPage(pageNum - 1)
                newDoc.addPage(page)
            }

            newDoc.save(outputFile)
            outputFile
        } finally {
            try {
                sourceDoc?.close()
                newDoc?.close()
            } catch (_: Exception) {
            }
            tempPdf.delete()
        }
    }

    suspend fun splitAllPages(
        pdfUri: Uri,
        onProgress: (Int, Int) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFiles = mutableListOf<File>()
        var sourceDoc: PDDocument? = null

        try {
            sourceDoc = PDDocument.load(tempPdf)
            val totalPages = sourceDoc.numberOfPages

            for (i in 0 until totalPages) {
                onProgress(i + 1, totalPages)
                val singleDoc = PDDocument()
                try {
                    singleDoc.addPage(sourceDoc.getPage(i))
                    val singleFile = storageManager.createTempFile("FileFlow_Page_${i + 1}_", ".pdf")
                    singleDoc.save(singleFile)
                    outputFiles.add(singleFile)
                } finally {
                    singleDoc.close()
                }
            }
            outputFiles
        } finally {
            try {
                sourceDoc?.close()
            } catch (_: Exception) {
            }
            tempPdf.delete()
        }
    }

    fun parsePageRange(rangeStr: String, maxPage: Int): List<Int> {
        val clean = rangeStr.trim()
        if (clean.isBlank() || clean.equals("all", ignoreCase = true)) {
            return (1..maxPage).toList()
        }

        val result = mutableSetOf<Int>()
        val parts = clean.split(",")

        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                val rangeParts = trimmed.split("-")
                if (rangeParts.size == 2) {
                    val start = rangeParts[0].trim().toIntOrNull() ?: 1
                    val end = rangeParts[1].trim().toIntOrNull() ?: maxPage
                    val s = start.coerceIn(1, maxPage)
                    val e = end.coerceIn(1, maxPage)
                    if (s <= e) {
                        for (p in s..e) result.add(p)
                    } else {
                        for (p in s downTo e) result.add(p)
                    }
                }
            } else {
                val p = trimmed.toIntOrNull()
                if (p != null && p in 1..maxPage) {
                    result.add(p)
                }
            }
        }
        return result.toList().sorted()
    }
}
