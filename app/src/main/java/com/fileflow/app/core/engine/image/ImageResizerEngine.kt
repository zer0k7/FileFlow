package com.fileflow.app.core.engine.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.fileflow.app.core.model.ResizeMode
import com.fileflow.app.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class ImageResizerEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {

    suspend fun resizeByDimensions(
        imageUri: Uri,
        targetWidth: Int,
        targetHeight: Int,
        qualityPercent: Int = 90
    ): File = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "resize_in")
        try {
            val bitmap = decodeBitmapWithOrientation(tempInput)
                ?: throw IllegalStateException("Failed to decode image")

            val safeW = targetWidth.coerceAtLeast(1)
            val safeH = targetHeight.coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(bitmap, safeW, safeH, true)
            if (scaled != bitmap) bitmap.recycle()

            val outputFile = storageManager.createTempFile("FileFlow_Resized_", ".jpg")
            FileOutputStream(outputFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, qualityPercent.coerceIn(1, 100), out)
            }
            scaled.recycle()
            outputFile
        } finally {
            tempInput.delete()
        }
    }

    suspend fun resizeByPercentage(
        imageUri: Uri,
        percentage: Int,
        qualityPercent: Int = 90
    ): File = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "resize_in")
        try {
            val bitmap = decodeBitmapWithOrientation(tempInput)
                ?: throw IllegalStateException("Failed to decode image")

            val factor = percentage.coerceIn(5, 400) / 100f
            val safeW = (bitmap.width * factor).roundToInt().coerceAtLeast(1)
            val safeH = (bitmap.height * factor).roundToInt().coerceAtLeast(1)

            val scaled = Bitmap.createScaledBitmap(bitmap, safeW, safeH, true)
            if (scaled != bitmap) bitmap.recycle()

            val outputFile = storageManager.createTempFile("FileFlow_Resized_", ".jpg")
            FileOutputStream(outputFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, qualityPercent.coerceIn(1, 100), out)
            }
            scaled.recycle()
            outputFile
        } finally {
            tempInput.delete()
        }
    }

    suspend fun resizeToTargetFileSize(
        imageUri: Uri,
        targetSizeBytes: Long
    ): File = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "resize_target_in")
        try {
            var currentBitmap = decodeBitmapWithOrientation(tempInput)
                ?: throw IllegalStateException("Failed to decode image")

            var bestBytes: ByteArray? = null
            var bestQuality = 85

            var lowQ = 10
            var highQ = 95

            while (lowQ <= highQ) {
                val midQ = (lowQ + highQ) / 2
                val stream = ByteArrayOutputStream()
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, midQ, stream)
                val bytes = stream.toByteArray()

                if (bytes.size <= targetSizeBytes) {
                    bestBytes = bytes
                    bestQuality = midQ
                    lowQ = midQ + 1
                } else {
                    highQ = midQ - 1
                }
            }

            if (bestBytes == null || bestBytes.size > targetSizeBytes) {
                var scaleFactor = 0.8f
                while (scaleFactor >= 0.15f) {
                    val w = (currentBitmap.width * scaleFactor).roundToInt().coerceAtLeast(1)
                    val h = (currentBitmap.height * scaleFactor).roundToInt().coerceAtLeast(1)
                    val scaled = Bitmap.createScaledBitmap(currentBitmap, w, h, true)

                    val stream = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    val bytes = stream.toByteArray()
                    scaled.recycle()

                    if (bytes.size <= targetSizeBytes) {
                        bestBytes = bytes
                        break
                    }
                    scaleFactor -= 0.15f
                }
            }

            if (bestBytes == null) {
                val stream = ByteArrayOutputStream()
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                bestBytes = stream.toByteArray()
            }

            currentBitmap.recycle()

            val outputFile = storageManager.createTempFile("FileFlow_TargetKB_", ".jpg")
            FileOutputStream(outputFile).use { out ->
                out.write(bestBytes)
            }
            outputFile
        } finally {
            tempInput.delete()
        }
    }

    suspend fun resizeBatch(
        imageUris: List<Uri>,
        mode: ResizeMode,
        targetWidth: Int = 1080,
        targetHeight: Int = 1080,
        percentage: Int = 50,
        targetSizeBytes: Long = 200 * 1024,
        qualityPercent: Int = 90,
        onProgress: (Int, Int) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val results = mutableListOf<File>()
        imageUris.forEachIndexed { index, uri ->
            onProgress(index + 1, imageUris.size)
            val file = when (mode) {
                ResizeMode.DIMENSIONS -> resizeByDimensions(uri, targetWidth, targetHeight, qualityPercent)
                ResizeMode.PERCENTAGE -> resizeByPercentage(uri, percentage, qualityPercent)
                ResizeMode.TARGET_FILE_SIZE -> resizeToTargetFileSize(uri, targetSizeBytes)
            }
            results.add(file)
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
