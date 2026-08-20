package com.fileflow.app.core.engine.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.fileflow.app.core.model.ExifMetadataInfo
import com.fileflow.app.core.model.ExifTagItem
import com.fileflow.app.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class ImageExifEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {

    suspend fun readExif(imageUri: Uri): ExifMetadataInfo = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "exif_inspect")
        try {
            val exif = ExifInterface(tempInput.absolutePath)
            parseExifInfo(exif, tempInput)
        } finally {
            tempInput.delete()
        }
    }

    suspend fun stripExif(
        imageUri: Uri,
        stripOnlyGps: Boolean = false
    ): File = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "exif_strip_in")
        try {
            if (stripOnlyGps) {
                val outputFile = storageManager.createTempFile("FileFlow_NoGPS_", ".jpg")
                tempInput.copyTo(outputFile, overwrite = true)
                val exif = ExifInterface(outputFile.absolutePath)
                removeGpsAttributes(exif)
                exif.saveAttributes()
                outputFile
            } else {
                val orientation = try {
                    val exif = ExifInterface(tempInput.absolutePath)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } catch (_: Exception) {
                    ExifInterface.ORIENTATION_NORMAL
                }

                val bitmap = BitmapFactory.decodeFile(tempInput.absolutePath)
                    ?: throw IllegalStateException("Failed to decode image for EXIF stripping")

                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                }

                val orientedBitmap = if (!matrix.isIdentity) {
                    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    if (rotated != bitmap) bitmap.recycle()
                    rotated
                } else {
                    bitmap
                }

                val cleanBitmap = orientedBitmap.copy(Bitmap.Config.ARGB_8888, false)
                if (cleanBitmap != orientedBitmap) orientedBitmap.recycle()

                val outputFile = storageManager.createTempFile("FileFlow_Clean_", ".jpg")
                FileOutputStream(outputFile).use { out ->
                    cleanBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                cleanBitmap.recycle()
                outputFile
            }
        } finally {
            tempInput.delete()
        }
    }

    suspend fun stripExifBatch(
        imageUris: List<Uri>,
        stripOnlyGps: Boolean = false,
        onProgress: (current: Int, total: Int) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val results = mutableListOf<File>()
        imageUris.forEachIndexed { index, uri ->
            onProgress(index + 1, imageUris.size)
            val cleaned = stripExif(uri, stripOnlyGps)
            results.add(cleaned)
        }
        results
    }

    private fun parseExifInfo(exif: ExifInterface, file: File): ExifMetadataInfo {
        val tags = mutableListOf<ExifTagItem>()

        val latLong = FloatArray(2)
        val hasGps = exif.getLatLong(latLong)
        val lat = if (hasGps) latLong[0].toDouble() else null
        val lng = if (hasGps) latLong[1].toDouble() else null

        val formattedCoords = if (hasGps && lat != null && lng != null) {
            String.format(Locale.US, "%.6f°, %.6f°", lat, lng)
        } else {
            ""
        }

        if (hasGps && lat != null && lng != null) {
            tags.add(ExifTagItem("GPS Location", "Latitude", String.format(Locale.US, "%.6f°", lat)))
            tags.add(ExifTagItem("GPS Location", "Longitude", String.format(Locale.US, "%.6f°", lng)))
            exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)?.let {
                tags.add(ExifTagItem("GPS Location", "Altitude", "$it m"))
            }
            exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP)?.let {
                tags.add(ExifTagItem("GPS Location", "GPS Date", it))
            }
            exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP)?.let {
                tags.add(ExifTagItem("GPS Location", "GPS Time", it))
            }
        }

        val make = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim().orEmpty()
        val model = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim().orEmpty()
        val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)?.trim().orEmpty()
        val lensModel = exif.getAttribute(ExifInterface.TAG_LENS_MODEL)?.trim().orEmpty()

        if (make.isNotEmpty()) tags.add(ExifTagItem("Device & Camera", "Manufacturer", make))
        if (model.isNotEmpty()) tags.add(ExifTagItem("Device & Camera", "Model", model))
        if (lensModel.isNotEmpty()) tags.add(ExifTagItem("Device & Camera", "Lens", lensModel))
        if (software.isNotEmpty()) tags.add(ExifTagItem("Device & Camera", "Software", software))

        val dateOriginal = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)?.trim().orEmpty()
        val dateDigitized = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)?.trim().orEmpty()
        val exposure = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.trim().orEmpty()
        val fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.trim().orEmpty()
        val iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)?.trim().orEmpty()
        val focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.trim().orEmpty()
        val flash = exif.getAttribute(ExifInterface.TAG_FLASH)?.trim().orEmpty()

        if (dateOriginal.isNotEmpty()) tags.add(ExifTagItem("Capture & Time", "Date Original", dateOriginal))
        if (dateDigitized.isNotEmpty()) tags.add(ExifTagItem("Capture & Time", "Date Digitized", dateDigitized))
        if (exposure.isNotEmpty()) tags.add(ExifTagItem("Capture & Time", "Exposure Time", "$exposure s"))
        if (fNumber.isNotEmpty()) tags.add(ExifTagItem("Capture & Time", "Aperture", "f/$fNumber"))
        if (iso.isNotEmpty()) tags.add(ExifTagItem("Capture & Time", "ISO Speed", "ISO $iso"))
        if (focalLength.isNotEmpty()) tags.add(ExifTagItem("Capture & Time", "Focal Length", "$focalLength mm"))
        if (flash.isNotEmpty()) tags.add(ExifTagItem("Capture & Time", "Flash", if (flash == "0") "No Flash" else "Fired"))

        val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
        val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
        if (width > 0 && height > 0) {
            tags.add(ExifTagItem("Image Dimensions", "Resolution", "${width} × ${height} px"))
        }

        return ExifMetadataInfo(
            hasGps = hasGps,
            latitude = lat,
            longitude = lng,
            formattedCoordinates = formattedCoords,
            cameraMake = make,
            cameraModel = model,
            lensModel = lensModel,
            software = software,
            dateTimeOriginal = dateOriginal,
            exposureTime = exposure,
            fNumber = fNumber,
            iso = iso,
            focalLength = focalLength,
            flash = flash,
            imageWidth = width,
            imageHeight = height,
            allTags = tags
        )
    }

    private fun removeGpsAttributes(exif: ExifInterface) {
        val gpsTags = listOf(
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_AREA_INFORMATION,
            ExifInterface.TAG_GPS_DOP,
            ExifInterface.TAG_GPS_SPEED,
            ExifInterface.TAG_GPS_SPEED_REF,
            ExifInterface.TAG_GPS_TRACK,
            ExifInterface.TAG_GPS_TRACK_REF,
            ExifInterface.TAG_GPS_IMG_DIRECTION,
            ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
            ExifInterface.TAG_GPS_DEST_LATITUDE,
            ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
            ExifInterface.TAG_GPS_DEST_LONGITUDE,
            ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
            ExifInterface.TAG_GPS_DEST_BEARING,
            ExifInterface.TAG_GPS_DEST_BEARING_REF,
            ExifInterface.TAG_GPS_DEST_DISTANCE,
            ExifInterface.TAG_GPS_DEST_DISTANCE_REF
        )
        for (tag in gpsTags) {
            exif.setAttribute(tag, null)
        }
    }
}
