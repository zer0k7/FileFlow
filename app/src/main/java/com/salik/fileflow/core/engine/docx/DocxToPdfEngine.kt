package com.salik.fileflow.core.engine.docx

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.salik.fileflow.core.model.PageSizeOption
import com.salik.fileflow.core.saf.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import java.util.zip.ZipFile

class DocxToPdfEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun convert(
        docxUri: Uri,
        pageSize: PageSizeOption = PageSizeOption.A4,
        onProgress: (Int, Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val tempDocx = storageManager.copyUriToLocalTemp(docxUri, "docx")
        val outputFile = storageManager.createTempFile("FileFlow_DocxPdf_", ".pdf")
        val pdfDocument = PdfDocument()

        try {
            val paragraphs = extractParagraphsFromDocx(tempDocx)
            val totalParagraphs = paragraphs.size.coerceAtLeast(1)

            val pageWidth = if (pageSize.widthPoints > 0) pageSize.widthPoints else 595
            val pageHeight = if (pageSize.heightPoints > 0) pageSize.heightPoints else 842
            val margin = 50
            val contentWidth = pageWidth - (margin * 2)
            val contentHeight = pageHeight - (margin * 2)

            val textPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var currentPage = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = currentPage.canvas
            var currentY = margin.toFloat()

            paragraphs.forEachIndexed { index, paragraphText ->
                onProgress(index + 1, totalParagraphs)

                if (paragraphText.isBlank()) {
                    currentY += 12f
                    return@forEachIndexed
                }

                val layout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    StaticLayout.Builder.obtain(
                        paragraphText,
                        0,
                        paragraphText.length,
                        textPaint,
                        contentWidth
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(2f, 1f)
                        .setIncludePad(false)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    StaticLayout(
                        paragraphText,
                        textPaint,
                        contentWidth,
                        Layout.Alignment.ALIGN_NORMAL,
                        1f,
                        2f,
                        false
                    )
                }

                if (currentY + layout.height > pageHeight - margin) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    currentY = margin.toFloat()
                }

                canvas.save()
                canvas.translate(margin.toFloat(), currentY)
                layout.draw(canvas)
                canvas.restore()

                currentY += layout.height + 8f
            }

            pdfDocument.finishPage(currentPage)

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }

            outputFile
        } finally {
            pdfDocument.close()
            tempDocx.delete()
        }
    }

    private fun extractParagraphsFromDocx(file: File): List<String> {
        val paragraphs = mutableListOf<String>()
        val zipFile = ZipFile(file)
        try {
            val entry = zipFile.getEntry("word/document.xml") ?: return emptyList()
            val xmlContent = zipFile.getInputStream(entry).bufferedReader().use { it.readText() }

            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            val currentParagraph = StringBuilder()
            var inParagraph = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "p" || parser.name.endsWith(":p")) {
                            inParagraph = true
                            currentParagraph.setLength(0)
                        } else if (inParagraph && (parser.name == "t" || parser.name.endsWith(":t"))) {
                            val text = parser.nextText()
                            currentParagraph.append(text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "p" || parser.name.endsWith(":p")) {
                            inParagraph = false
                            paragraphs.add(currentParagraph.toString())
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {
        } finally {
            zipFile.close()
        }
        return paragraphs
    }
}
