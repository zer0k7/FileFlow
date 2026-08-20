package com.fileflow.app.core.engine.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import com.fileflow.app.core.model.PaletteColor
import com.fileflow.app.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.sqrt

class ImagePaletteEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {

    suspend fun extractPalette(
        imageUri: Uri,
        maxColors: Int = 8
    ): List<PaletteColor> = withContext(Dispatchers.IO) {
        val tempInput = storageManager.copyUriToLocalTemp(imageUri, "palette_in")
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(tempInput.absolutePath, options)

            val sampleSize = maxOf(1, maxOf(options.outWidth, options.outHeight) / 160)
            options.inJustDecodeBounds = false
            options.inSampleSize = sampleSize

            val bitmap = BitmapFactory.decodeFile(tempInput.absolutePath, options)
                ?: throw IllegalStateException("Failed to decode image for palette extraction")

            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            bitmap.recycle()

            val colorCounts = mutableMapOf<Int, Int>()
            for (pixel in pixels) {
                val alpha = Color.alpha(pixel)
                if (alpha < 128) continue

                val r = (Color.red(pixel) / 8) * 8
                val g = (Color.green(pixel) / 8) * 8
                val b = (Color.blue(pixel) / 8) * 8
                val quantized = Color.rgb(r, g, b)
                colorCounts[quantized] = (colorCounts[quantized] ?: 0) + 1
            }

            val totalPixels = colorCounts.values.sum().coerceAtLeast(1)
            val sortedByFrequency = colorCounts.entries.sortedByDescending { it.value }

            val distinctColors = mutableListOf<Map.Entry<Int, Int>>()
            for (entry in sortedByFrequency) {
                val color = entry.key
                val isDistinct = distinctColors.none { existing ->
                    colorDistance(color, existing.key) < 36.0
                }
                if (isDistinct) {
                    distinctColors.add(entry)
                    if (distinctColors.size >= maxColors) break
                }
            }

            distinctColors.map { entry ->
                val c = entry.key
                val r = Color.red(c)
                val g = Color.green(c)
                val b = Color.blue(c)
                val hex = String.format(Locale.US, "#%02X%02X%02X", r, g, b)
                val rgb = "rgb($r, $g, $b)"
                val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                val percent = (entry.value.toFloat() / totalPixels.toFloat()) * 100f

                PaletteColor(
                    hex = hex,
                    rgb = rgb,
                    red = r,
                    green = g,
                    blue = b,
                    population = entry.value,
                    percentage = percent,
                    isDark = luminance < 0.5
                )
            }
        } finally {
            tempInput.delete()
        }
    }

    suspend fun exportPaletteCard(
        palette: List<PaletteColor>,
        sourceFileName: String = "Image"
    ): File = withContext(Dispatchers.IO) {
        val width = 1080
        val swatchHeight = 160
        val headerHeight = 160
        val footerHeight = 80
        val totalHeight = headerHeight + (palette.size * swatchHeight) + footerHeight

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.parseColor("#0F172A"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Color Palette", 50f, 90f, paint)

        paint.apply {
            color = Color.parseColor("#94A3B8")
            textSize = 28f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText("Extracted from $sourceFileName", 50f, 135f, paint)

        var yOffset = headerHeight.toFloat()
        palette.forEachIndexed { index, paletteItem ->
            paint.color = Color.rgb(paletteItem.red, paletteItem.green, paletteItem.blue)
            val rect = RectF(50f, yOffset, 200f, yOffset + swatchHeight - 24f)
            canvas.drawRoundRect(rect, 20f, 20f, paint)

            paint.apply {
                color = Color.WHITE
                textSize = 36f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }
            canvas.drawText(paletteItem.hex, 230f, yOffset + 60f, paint)

            paint.apply {
                color = Color.parseColor("#94A3B8")
                textSize = 28f
                typeface = Typeface.DEFAULT
            }
            canvas.drawText("${paletteItem.rgb}  •  ${String.format(Locale.US, "%.1f", paletteItem.percentage)}%", 230f, yOffset + 105f, paint)

            yOffset += swatchHeight
        }

        paint.apply {
            color = Color.parseColor("#64748B")
            textSize = 24f
        }
        canvas.drawText("Generated with FileFlow • 100% Offline", 50f, totalHeight - 35f, paint)

        val outputFile = storageManager.createTempFile("FileFlow_Palette_", ".png")
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        outputFile
    }

    private fun colorDistance(c1: Int, c2: Int): Double {
        val rDiff = Color.red(c1) - Color.red(c2)
        val gDiff = Color.green(c1) - Color.green(c2)
        val bDiff = Color.blue(c1) - Color.blue(c2)
        return sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble())
    }
}
