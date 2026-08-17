package com.salik.fileflow.core.engine.docx

import android.content.Context
import android.net.Uri
import com.salik.fileflow.core.saf.StorageManager
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PdfToDocxEngine(
    private val context: Context,
    private val storageManager: StorageManager
) {
    suspend fun convert(
        pdfUri: Uri,
        onProgress: (Int, Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val tempPdf = storageManager.copyUriToLocalTemp(pdfUri, "pdf")
        val outputFile = storageManager.createTempFile("FileFlow_Doc_", ".docx")

        var document: PDDocument? = null
        try {
            document = PDDocument.load(tempPdf)
            val totalPages = document.numberOfPages
            val textStripper = PDFTextStripper()

            val paragraphs = mutableListOf<String>()

            for (i in 1..totalPages) {
                onProgress(i, totalPages)
                textStripper.startPage = i
                textStripper.endPage = i
                val pageText = textStripper.getText(document)
                val lines = pageText.split("\n")
                for (line in lines) {
                    val cleanLine = line.trim()
                    if (cleanLine.isNotEmpty()) {
                        paragraphs.add(cleanLine)
                    }
                }
            }

            val docXmlContent = buildDocumentXml(paragraphs)
            createDocxZip(outputFile, docXmlContent)

            outputFile
        } finally {
            try {
                document?.close()
            } catch (_: Exception) {
            }
            tempPdf.delete()
        }
    }

    private fun buildDocumentXml(paragraphs: List<String>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n")
        sb.append("<w:body>\n")

        for (p in paragraphs) {
            val escaped = escapeXml(p)
            sb.append("  <w:p>\n")
            sb.append("    <w:r>\n")
            sb.append("      <w:t xml:space=\"preserve\">").append(escaped).append("</w:t>\n")
            sb.append("    </w:r>\n")
            sb.append("  </w:p>\n")
        }

        sb.append("  <w:sectPr>\n")
        sb.append("    <w:pgSz w:w=\"11906\" w:h=\"16838\"/>\n")
        sb.append("    <w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>\n")
        sb.append("  </w:sectPr>\n")
        sb.append("</w:body>\n")
        sb.append("</w:document>")
        return sb.toString()
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun createDocxZip(zipFile: File, documentXml: String) {
        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

        val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

        val wordRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>"""

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            addZipEntry(zos, "[Content_Types].xml", contentTypesXml)
            addZipEntry(zos, "_rels/.rels", relsXml)
            addZipEntry(zos, "word/_rels/document.xml.rels", wordRelsXml)
            addZipEntry(zos, "word/document.xml", documentXml)
        }
    }

    private fun addZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }
}
