package com.fileflow.app.core.engine.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.fileflow.app.core.model.ImageFormatOption
import com.fileflow.app.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class ImageCompressorEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun compress(
        imageUri: Uri,
        qualityPercent: Int,
        targetFormat: ImageFormatOption,
        maxDimension: Int = 1920
    ): File = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "img")

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(tempInput.absolutePath, options)

        val srcWidth = options.outWidth
        val srcHeight = options.outHeight

        var sampleSize = 1
        while ((srcWidth / sampleSize) > maxDimension * 2 || (srcHeight / sampleSize) > maxDimension * 2) {
            sampleSize *= 2
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = sampleSize
        var bitmap = BitmapFactory.decodeFile(tempInput.absolutePath, options)
            ?: throw IllegalStateException("Failed to decode image for compression")

        val orientation = try {
            val exif = ExifInterface(tempInput.absolutePath)
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

        if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            matrix.postScale(scale, scale)
        }

        val processedBitmap = if (!matrix.isIdentity) {
            val transformed = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            bitmap.recycle()
            transformed
        } else {
            bitmap
        }

        val compressFormat = when (targetFormat) {
            ImageFormatOption.PNG -> Bitmap.CompressFormat.PNG
            ImageFormatOption.WEBP -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
            ImageFormatOption.JPG -> Bitmap.CompressFormat.JPEG
        }

        val outputFile = storageManager.createTempFile("FileFlow_Compressed_", ".${targetFormat.extension}")
        FileOutputStream(outputFile).use { out ->
            processedBitmap.compress(compressFormat, qualityPercent, out)
        }

        processedBitmap.recycle()
        tempInput.delete()

        outputFile
    }
}
