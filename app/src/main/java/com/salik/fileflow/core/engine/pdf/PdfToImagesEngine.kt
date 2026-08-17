package com.salik.fileflow.core.engine.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.salik.fileflow.core.model.ImageFormatOption
import com.salik.fileflow.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfToImagesEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun convert(
        pdfUri: Uri,
        format: ImageFormatOption,
        qualityPercent: Int,
        renderScale: Float = 2.0f,
        onProgress: (Int, Int) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFiles = mutableListOf<File>()

        val pfd = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        try {
            val totalPages = renderer.pageCount
            for (i in 0 until totalPages) {
                onProgress(i + 1, totalPages)
                val page = renderer.openPage(i)

                val width = (page.width * renderScale).toInt()
                val height = (page.height * renderScale).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val compressFormat = when (format) {
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

                val imageFile = storageManager.createTempFile("FileFlow_Page_${i + 1}_", ".${format.extension}")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(compressFormat, qualityPercent, out)
                }
                bitmap.recycle()
                outputFiles.add(imageFile)
            }
        } finally {
            renderer.close()
            pfd.close()
            tempPdf.delete()
        }

        outputFiles
    }
}
