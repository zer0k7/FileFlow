package com.salik.fileflow.core.engine.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import com.salik.fileflow.core.model.OrientationOption
import com.salik.fileflow.core.model.PageSizeOption
import com.salik.fileflow.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class ImageToPdfEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun convert(
        imageUris: List<Uri>,
        pageSize: PageSizeOption,
        orientation: OrientationOption,
        qualityPercent: Int,
        onProgress: (Int, Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val total = imageUris.size

        try {
            imageUris.forEachIndexed { index, uri ->
                onProgress(index + 1, total)

                val tempFile = storageManager.copyUriToLocalTemp(uri, "jpg")
                val bitmap = decodeSampledBitmap(tempFile, qualityPercent)

                val (pageWidth, pageHeight) = calculateDimensions(
                    bitmap.width,
                    bitmap.height,
                    pageSize,
                    orientation
                )

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                val srcRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                val dstRect = RectF(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat())

                val scale = min(dstRect.width() / srcRect.width(), dstRect.height() / srcRect.height())
                val scaledWidth = srcRect.width() * scale
                val scaledHeight = srcRect.height() * scale
                val left = (dstRect.width() - scaledWidth) / 2f
                val top = (dstRect.height() - scaledHeight) / 2f

                val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

                canvas.drawBitmap(bitmap, null, targetRect, paint)
                pdfDocument.finishPage(page)

                bitmap.recycle()
                tempFile.delete()
            }

            val outputFile = storageManager.createTempFile("FileFlow_ImgPdf_", ".pdf")
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            outputFile
        } finally {
            pdfDocument.close()
        }
    }

    private fun decodeSampledBitmap(file: File, quality: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)

        var sampleSize = 1
        val maxDim = if (quality < 60) 1200 else if (quality < 80) 1800 else 2400
        while ((options.outWidth / sampleSize) > maxDim || (options.outHeight / sampleSize) > maxDim) {
            sampleSize *= 2
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = sampleSize
        var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            ?: throw IllegalStateException("Could not decode image")

        val orientation = try {
            val exif = ExifInterface(file.absolutePath)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        if (!matrix.isIdentity) {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            bitmap = rotated
        }

        return bitmap
    }

    private fun calculateDimensions(
        bitmapWidth: Int,
        bitmapHeight: Int,
        pageSize: PageSizeOption,
        orientation: OrientationOption
    ): Pair<Int, Int> {
        val baseWidth: Int
        val baseHeight: Int

        when (pageSize) {
            PageSizeOption.A4 -> {
                baseWidth = PageSizeOption.A4.widthPoints
                baseHeight = PageSizeOption.A4.heightPoints
            }
            PageSizeOption.LETTER -> {
                baseWidth = PageSizeOption.LETTER.widthPoints
                baseHeight = PageSizeOption.LETTER.heightPoints
            }
            PageSizeOption.ORIGINAL -> {
                baseWidth = bitmapWidth
                baseHeight = bitmapHeight
            }
        }

        return when (orientation) {
            OrientationOption.PORTRAIT -> {
                val w = min(baseWidth, baseHeight)
                val h = maxOf(baseWidth, baseHeight)
                Pair(w, h)
            }
            OrientationOption.LANDSCAPE -> {
                val w = maxOf(baseWidth, baseHeight)
                val h = min(baseWidth, baseHeight)
                Pair(w, h)
            }
            OrientationOption.AUTO -> {
                if (bitmapWidth > bitmapHeight) {
                    Pair(maxOf(baseWidth, baseHeight), min(baseWidth, baseHeight))
                } else {
                    Pair(min(baseWidth, baseHeight), maxOf(baseWidth, baseHeight))
                }
            }
        }
    }
}
