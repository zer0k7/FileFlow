package com.salik.fileflow.core.engine.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.salik.fileflow.core.model.CompressionLevel
import com.salik.fileflow.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PdfCompressorEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun compress(
        pdfUri: Uri,
        level: CompressionLevel,
        onProgress: (Int, Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val pfd = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val pdfDocument = PdfDocument()

        try {
            val totalPages = renderer.pageCount
            val scale = when (level) {
                CompressionLevel.EXTREME -> 1.0f
                CompressionLevel.RECOMMENDED -> 1.5f
                CompressionLevel.LIGHT -> 2.0f
            }

            for (i in 0 until totalPages) {
                onProgress(i + 1, totalPages)
                val page = renderer.openPage(i)

                val renderWidth = (page.width * scale).toInt()
                val renderHeight = (page.height * scale).toInt()

                val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                page.close()

                val compressedStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, level.imageQuality, compressedStream)
                val compressedBytes = compressedStream.toByteArray()
                bitmap.recycle()

                val reloadedBitmap = android.graphics.BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

                val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
                val newPage = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = newPage.canvas
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

                canvas.drawBitmap(
                    reloadedBitmap,
                    null,
                    RectF(0f, 0f, page.width.toFloat(), page.height.toFloat()),
                    paint
                )
                pdfDocument.finishPage(newPage)
                reloadedBitmap.recycle()
            }

            val outputFile = storageManager.createTempFile("FileFlow_Compressed_", ".pdf")
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            outputFile
        } finally {
            renderer.close()
            pfd.close()
            pdfDocument.close()
            tempPdf.delete()
        }
    }
}
