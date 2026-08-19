package com.fileflow.app.core.model

import android.net.Uri

enum class ToolType(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val iconName: String,
    val inputMimeTypes: Array<String>,
    val allowsMultipleFiles: Boolean
) {
    IMAGE_TO_PDF(
        id = "image_to_pdf",
        title = "Image to PDF",
        description = "Convert photos and scans into a multi-page PDF document",
        category = ToolCategory.CONVERT,
        iconName = "PictureAsPdf",
        inputMimeTypes = arrayOf("image/*"),
        allowsMultipleFiles = true
    ),
    PDF_TO_IMAGES(
        id = "pdf_to_images",
        title = "PDF to Images",
        description = "Extract pages as high-quality JPG, PNG, or WebP images",
        category = ToolCategory.EXTRACT,
        iconName = "Image",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    PDF_TO_DOCX(
        id = "pdf_to_docx",
        title = "PDF to DOCX",
        description = "Convert PDF text and structure into editable Word document",
        category = ToolCategory.CONVERT,
        iconName = "Description",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    DOCX_TO_PDF(
        id = "docx_to_pdf",
        title = "DOCX to PDF",
        description = "Convert Word documents (.docx) into clean PDF files",
        category = ToolCategory.CONVERT,
        iconName = "Article",
        inputMimeTypes = arrayOf(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/octet-stream"
        ),
        allowsMultipleFiles = false
    ),
    PDF_COMPRESSOR(
        id = "pdf_compressor",
        title = "PDF Compressor",
        description = "Reduce PDF file size while maintaining readability",
        category = ToolCategory.OPTIMIZE,
        iconName = "Compress",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    PDF_PASSWORD_REMOVER(
        id = "pdf_password_remover",
        title = "PDF Password Remover",
        description = "Unlock password-protected PDFs with your known password",
        category = ToolCategory.SECURITY,
        iconName = "LockOpen",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    PDF_MERGE(
        id = "pdf_merge",
        title = "PDF Merge",
        description = "Combine multiple PDF documents into a single document",
        category = ToolCategory.ORGANIZE,
        iconName = "CallMerge",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = true
    ),
    PDF_SPLIT(
        id = "pdf_split",
        title = "PDF Split / Extract",
        description = "Extract selected page ranges or split into individual files",
        category = ToolCategory.ORGANIZE,
        iconName = "CallSplit",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    IMAGE_COMPRESSOR(
        id = "image_compressor",
        title = "Image Compressor",
        description = "Compress and resize images with custom quality and format",
        category = ToolCategory.OPTIMIZE,
        iconName = "PhotoSizeSelectLarge",
        inputMimeTypes = arrayOf("image/*"),
        allowsMultipleFiles = true
    ),
    DOCUMENT_SCANNER(
        id = "document_scanner",
        title = "Document Scanner",
        description = "Capture documents, enhance contrast, crop, and export",
        category = ToolCategory.CREATE,
        iconName = "DocumentScanner",
        inputMimeTypes = arrayOf("image/*"),
        allowsMultipleFiles = true
    ),
    PDF_PROTECT(
        id = "pdf_protect",
        title = "PDF Password Protect",
        description = "Encrypt and lock your PDF document with a password",
        category = ToolCategory.SECURITY,
        iconName = "Lock",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    PDF_ROTATE(
        id = "pdf_rotate",
        title = "PDF Page Rotate",
        description = "Rotate PDF pages clockwise by 90°, 180°, or 270°",
        category = ToolCategory.ORGANIZE,
        iconName = "RotateRight",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    PDF_EXTRACT_TEXT(
        id = "pdf_extract_text",
        title = "Extract Text from PDF",
        description = "Extract selectable plain text into a TXT document",
        category = ToolCategory.EXTRACT,
        iconName = "TextFields",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    PDF_WATERMARK(
        id = "pdf_watermark",
        title = "PDF Watermark",
        description = "Stamp custom text watermark across document pages",
        category = ToolCategory.SECURITY,
        iconName = "BrandingWatermark",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    OCR_TEXT_EXTRACTOR(
        id = "ocr_text_extractor",
        title = "On-Device OCR",
        description = "Extract text from photos and scans 100% offline without cloud",
        category = ToolCategory.EXTRACT,
        iconName = "DocumentScanner",
        inputMimeTypes = arrayOf("image/*", "application/pdf"),
        allowsMultipleFiles = true
    ),
    PDF_SIGN_STAMP(
        id = "pdf_sign_stamp",
        title = "Sign & Stamp PDF",
        description = "Draw signature or apply official status stamps to PDF pages",
        category = ToolCategory.SECURITY,
        iconName = "Article",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    ),
    PDF_METADATA_EDITOR(
        id = "pdf_metadata_editor",
        title = "PDF Metadata Editor",
        description = "View and edit document title, author, subject, and keywords",
        category = ToolCategory.OPTIMIZE,
        iconName = "Description",
        inputMimeTypes = arrayOf("application/pdf"),
        allowsMultipleFiles = false
    );

    companion object {
        fun fromId(id: String): ToolType? = entries.find { it.id == id }
    }
}

enum class ToolCategory(val title: String) {
    CONVERT("Convert"),
    OPTIMIZE("Optimize"),
    ORGANIZE("Organize"),
    SECURITY("Security"),
    EXTRACT("Extract"),
    CREATE("Create")
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

enum class AccentColorMode(val title: String, val hexColor: String) {
    SYSTEM("Dynamic / System", "#0284C7"),
    BLUE("FileFlow Blue", "#0284C7"),
    INDIGO("Indigo", "#4F46E5"),
    PURPLE("Purple", "#9333EA"),
    VIOLET("Violet", "#7C3AED"),
    PINK("Pink", "#DB2777"),
    RED("Red", "#DC2626"),
    ORANGE("Orange", "#EA580C"),
    AMBER("Amber", "#D97706"),
    GREEN("Green", "#16A34A"),
    TEAL("Teal", "#0D9488"),
    CYAN("Cyan", "#0891B2"),
    LIME("Lime", "#65A30D"),
    CUSTOM("Custom", "#0284C7")
}

enum class UiDensity {
    COMFORTABLE,
    COMPACT
}

enum class ImageQualityOption(val title: String, val qualityPercent: Int) {
    ORIGINAL("Original (100%)", 100),
    HIGH("High (85%)", 85),
    MEDIUM("Medium (70%)", 70),
    LOW("Low (50%)", 50)
}

enum class ImageFormatOption(val extension: String, val mimeType: String) {
    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp")
}

enum class PageSizeOption(val title: String, val widthPoints: Int, val heightPoints: Int) {
    A4("A4 (595 x 842)", 595, 842),
    LETTER("Letter (612 x 792)", 612, 792),
    ORIGINAL("Original / Fit", 0, 0)
}

enum class OrientationOption(val title: String) {
    AUTO("Auto Detect"),
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

enum class CompressionLevel(val title: String, val factor: Float, val imageQuality: Int) {
    EXTREME("Extreme (Smallest Size)", 0.5f, 40),
    RECOMMENDED("Recommended (Balanced)", 0.75f, 70),
    LIGHT("Light (Best Quality)", 0.9f, 85)
}

data class SelectedFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String
)

data class ProcessResult(
    val success: Boolean,
    val outputUris: List<Uri> = emptyList(),
    val outputFilenames: List<String> = emptyList(),
    val outputTotalBytes: Long = 0L,
    val inputTotalBytes: Long = 0L,
    val message: String = ""
)

data class HistoryItem(
    val id: String,
    val toolType: ToolType,
    val inputFileName: String,
    val outputFileName: String,
    val outputUriString: String,
    val timestamp: Long,
    val sizeBytes: Long,
    val success: Boolean
)

data class ChangelogVersion(
    val version: String,
    val releaseDate: String,
    val added: List<String>,
    val changed: List<String>,
    val fixed: List<String>,
    val security: List<String> = emptyList()
)

data class PageItem(
    val id: String,
    val uri: Uri,
    val name: String,
    val rotationDegrees: Int = 0,
    val pageIndex: Int = 0
)

data class PdfMetadata(
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val keywords: String = "",
    val creator: String = "",
    val producer: String = "",
    val pageCount: Int = 0,
    val isEncrypted: Boolean = false
)

data class StampPreset(
    val title: String,
    val text: String,
    val colorHex: String
)

data class StorageAnalytics(
    val exportFileCount: Int = 0,
    val exportTotalBytes: Long = 0L,
    val tempCacheBytes: Long = 0L,
    val totalProcessedCount: Int = 0
)
