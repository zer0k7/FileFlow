package com.fileflow.app.core.engine.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import com.fileflow.app.core.model.ImageFormatOption
import com.fileflow.app.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ImageFormatConverterEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun convert(
        imageUri: Uri,
        targetFormat: ImageFormatOption,
        qualityPercent: Int = 90
    ): File = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "conv_in")
        try {
            val bitmap = decodeBitmapWithOrientation(tempInput)
                ?: throw IllegalStateException("Failed to decode image")

            val outputFile = storageManager.createTempFile("FileFlow_Converted_", ".${targetFormat.extension}")
            val compressFormat = when (targetFormat) {
                ImageFormatOption.PNG -> Bitmap.CompressFormat.PNG
                ImageFormatOption.WEBP -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSY
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.WEBP
                    }
                }
                ImageFormatOption.JPG -> Bitmap.CompressFormat.JPEG
            }

            FileOutputStream(outputFile).use { out ->
                bitmap.compress(compressFormat, qualityPercent.coerceIn(1, 100), out)
            }
            bitmap.recycle()
            outputFile
        } finally {
            tempInput.delete()
        }
    }

    suspend fun convertBatch(
        imageUris: List<Uri>,
        targetFormat: ImageFormatOption,
        qualityPercent: Int = 90,
        onProgress: (current: Int, total: Int) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val results = mutableListOf<File>()
        imageUris.forEachIndexed { index, uri ->
            onProgress(index + 1, imageUris.size)
            val converted = convert(uri, targetFormat, qualityPercent)
            results.add(converted)
        }
        results
    }

    private fun decodeBitmapWithOrientation(file: File): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        val orientation = try {
            val exif = ExifInterface(file.absolutePath)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) {
            return bitmap
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        }

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }
}
