package com.fileflow.app.core.engine.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.fileflow.app.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ScanFilter(val title: String) {
    ORIGINAL("Original"),
    MAGIC_COLOR("Magic Color"),
    GRAYSCALE("Grayscale"),
    BLACK_AND_WHITE("B & W Clean"),
    BRIGHTEN("Brighten")
}

class DocumentScannerEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun applyFilter(
        inputUri: Uri,
        filter: ScanFilter
    ): Bitmap = withContext(Dispatchers.IO) {
        val temp = storageManager.copyUriToLocalTemp(inputUri, "img")
        val bitmap = BitmapFactory.decodeFile(temp.absolutePath)
        temp.delete()
        processFilter(bitmap, filter)
    }

    fun processFilter(bitmap: Bitmap, filter: ScanFilter): Bitmap {
        if (filter == ScanFilter.ORIGINAL) {
            return bitmap
        }

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (filter) {
            ScanFilter.ORIGINAL -> return bitmap
            ScanFilter.GRAYSCALE -> {
                val matrix = ColorMatrix().apply { setSaturation(0f) }
                paint.colorFilter = ColorMatrixColorFilter(matrix)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            ScanFilter.MAGIC_COLOR -> {
                val contrast = 1.3f
                val brightness = 15f
                val cm = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            ScanFilter.BRIGHTEN -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.1f, 0f, 0f, 0f, 30f,
                        0f, 1.1f, 0f, 0f, 30f,
                        0f, 0f, 1.1f, 0f, 30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            ScanFilter.BLACK_AND_WHITE -> {
                val grayBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val grayCanvas = Canvas(grayBitmap)
                val grayPaint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
                }
                grayCanvas.drawBitmap(bitmap, 0f, 0f, grayPaint)

                val pixels = IntArray(grayBitmap.width * grayBitmap.height)
                grayBitmap.getPixels(pixels, 0, grayBitmap.width, 0, 0, grayBitmap.width, grayBitmap.height)

                for (i in pixels.indices) {
                    val p = pixels[i]
                    val red = Color.red(p)
                    val green = Color.green(p)
                    val blue = Color.blue(p)
                    val lum = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
                    val threshold = 135
                    val newColor = if (lum > threshold) Color.WHITE else Color.BLACK
                    pixels[i] = newColor
                }

                result.setPixels(pixels, 0, grayBitmap.width, 0, 0, grayBitmap.width, grayBitmap.height)
                grayBitmap.recycle()
            }
        }

        return result
    }

    suspend fun exportScannedPagesToPdf(
        bitmaps: List<Bitmap>,
        onProgress: (Int, Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val total = bitmaps.size

        try {
            bitmaps.forEachIndexed { index, bitmap ->
                onProgress(index + 1, total)
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
                pdfDocument.finishPage(page)
            }

            val outputFile = storageManager.createTempFile("FileFlow_Scan_", ".pdf")
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            outputFile
        } finally {
            pdfDocument.close()
        }
    }
}
