package com.fileflow.app.ui.screens.processing

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.fileflow.app.core.engine.docx.DocxToPdfEngine
import com.fileflow.app.core.engine.docx.PdfToDocxEngine
import com.fileflow.app.core.engine.image.ImageCompressorEngine
import com.fileflow.app.core.engine.image.ImageExifEngine
import com.fileflow.app.core.engine.image.ImageFormatConverterEngine
import com.fileflow.app.core.engine.image.ImagePaletteEngine
import com.fileflow.app.core.engine.image.ImageResizerEngine
import com.fileflow.app.core.engine.ocr.OcrEngine
import com.fileflow.app.core.engine.pdf.ImageToPdfEngine
import com.fileflow.app.core.engine.pdf.PdfCompressorEngine
import com.fileflow.app.core.engine.pdf.PdfExtractTextEngine
import com.fileflow.app.core.engine.pdf.PdfMergeEngine
import com.fileflow.app.core.engine.pdf.PdfMetadataEngine
import com.fileflow.app.core.engine.pdf.PdfPasswordEngine
import com.fileflow.app.core.engine.pdf.PdfPasswordProtectEngine
import com.fileflow.app.core.engine.pdf.PdfRotateEngine
import com.fileflow.app.core.engine.pdf.PdfSignStampEngine
import com.fileflow.app.core.engine.pdf.PdfSplitEngine
import com.fileflow.app.core.engine.pdf.PdfToImagesEngine
import com.fileflow.app.core.engine.pdf.PdfWatermarkEngine
import com.fileflow.app.core.engine.qr.QrBarcodeGeneratorEngine
import com.fileflow.app.core.engine.qr.QrBarcodeScannerEngine
import com.fileflow.app.core.engine.scanner.DocumentScannerEngine
import com.fileflow.app.core.engine.scanner.ScanFilter
import com.fileflow.app.core.engine.security.SecurityScannerEngine
import com.fileflow.app.core.history.HistoryRepository
import com.fileflow.app.core.model.CompressionLevel
import com.fileflow.app.core.model.EngineResult
import com.fileflow.app.core.model.ExifMetadataInfo
import com.fileflow.app.core.model.ImageFormatOption
import com.fileflow.app.core.model.ImageQualityOption
import com.fileflow.app.core.model.OrientationOption
import com.fileflow.app.core.model.PageItem
import com.fileflow.app.core.model.PageSizeOption
import com.fileflow.app.core.model.PaletteColor
import com.fileflow.app.core.model.PdfMetadata
import com.fileflow.app.core.model.ProcessResult
import com.fileflow.app.core.model.QrPayloadType
import com.fileflow.app.core.model.QrParsedResult
import com.fileflow.app.core.model.QrPayloadType
import com.fileflow.app.core.model.QrParsedResult
import com.fileflow.app.core.model.ResizeMode
import com.fileflow.app.core.model.SelectedFile
import com.fileflow.app.core.model.SecurityScanReport
import com.fileflow.app.core.model.SecurityServiceType
import com.fileflow.app.core.model.SecurityThreatVerdict
import com.fileflow.app.core.model.StampPreset
import com.fileflow.app.core.model.ToolType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.fileflow.app.core.saf.StorageManager
import com.fileflow.app.core.service.FileProcessingService
import com.fileflow.app.ui.components.FloatingTopAppBar
import com.fileflow.app.ui.components.ProcessProgressBar
import com.fileflow.app.ui.components.ResultCard
import com.fileflow.app.ui.components.SignatureCanvas
import com.fileflow.app.ui.components.VisualPageOrganizer
import com.fileflow.app.ui.components.getToolIcon
import com.fileflow.app.ui.components.rememberAppHaptics
import com.fileflow.app.ui.theme.ToolCardShape
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

import androidx.core.content.FileProvider
import androidx.compose.material.icons.rounded.CameraAlt

@Composable
fun ToolExecutionScreen(
    tool: ToolType,
    defaultSaveUri: String?,
    namingPrefix: String,
    askBeforeReplace: Boolean,
    defaultQuality: ImageQualityOption = ImageQualityOption.HIGH,
    defaultPdfCompression: CompressionLevel = CompressionLevel.RECOMMENDED,
    defaultFormat: ImageFormatOption = ImageFormatOption.JPG,
    defaultPageSize: PageSizeOption = PageSizeOption.A4,
    defaultOrientation: OrientationOption = OrientationOption.AUTO,
    autoDeleteTemp: Boolean = true,
    floatingTopBar: Boolean = true,
    initialFiles: List<SelectedFile> = emptyList(),
    storageManager: StorageManager,
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberAppHaptics()
    val uriHandler = LocalUriHandler.current

    var selectedFiles by remember { mutableStateOf<List<SelectedFile>>(initialFiles) }
    var pageItems by remember {
        mutableStateOf<List<PageItem>>(
            initialFiles.mapIndexed { idx, f -> PageItem(UUID.randomUUID().toString(), f.uri, f.name, 0, idx) }
        )
    }
    var isProcessing by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(0) }
    var totalSteps by remember { mutableIntStateOf(1) }
    var progressStatus by remember { mutableStateOf("Processing...") }
    var processResult by remember { mutableStateOf<ProcessResult?>(null) }

    // Camera capture states
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    var cameraTempFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraTempUri != null && cameraTempFile != null) {
            val name = cameraTempFile!!.name
            val size = cameraTempFile!!.length()
            val newFile = SelectedFile(cameraTempUri!!, name, size, "image/jpeg")
            selectedFiles = if (tool.allowsMultipleFiles) selectedFiles + newFile else listOf(newFile)
            val newPageItem = PageItem(UUID.randomUUID().toString(), cameraTempUri!!, name, 0, pageItems.size)
            pageItems = pageItems + newPageItem
        }
    }

    fun launchCamera() {
        try {
            val tempFile = storageManager.createTempFile("scan_capture_", ".jpg")
            cameraTempFile = tempFile
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
            cameraTempUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to capture documents", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Tool options initialized from user preferences
    var selectedPageSize by remember { mutableStateOf(defaultPageSize) }
    var selectedOrientation by remember { mutableStateOf(defaultOrientation) }
    var selectedQuality by remember { mutableStateOf(defaultQuality) }
    var selectedFormat by remember { mutableStateOf(defaultFormat) }
    var selectedCompression by remember { mutableStateOf(defaultPdfCompression) }
    var passwordText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var pageRangeText by remember { mutableStateOf("1") }
    var splitAllPages by remember { mutableStateOf(false) }
    var imageQualitySlider by remember { mutableFloatStateOf(80f) }
    var scanFilter by remember { mutableStateOf(ScanFilter.MAGIC_COLOR) }
    var protectPasswordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var isProtectPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRotationAngle by remember { mutableIntStateOf(90) }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var watermarkOpacity by remember { mutableFloatStateOf(0.35f) }

    // Advanced tool states
    var ocrMode by remember { mutableStateOf("TXT") } // "TXT" or "SEARCHABLE_PDF"
    var extractedOcrText by remember { mutableStateOf("") }

    var signStampMode by remember { mutableStateOf("SIGN") } // "SIGN" or "STAMP"
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedPresetStamp by remember { mutableStateOf("APPROVED") }
    var signPageNumber by remember { mutableIntStateOf(1) }
    var signXPercent by remember { mutableFloatStateOf(0.5f) }
    var signYPercent by remember { mutableFloatStateOf(0.2f) }
    var signScale by remember { mutableFloatStateOf(0.35f) }
    var signOpacity by remember { mutableFloatStateOf(1.0f) }

    var pdfMetadata by remember { mutableStateOf(PdfMetadata()) }

    var targetFormatOption by remember { mutableStateOf(ImageFormatOption.JPG) }
    var formatQualityPercent by remember { mutableFloatStateOf(90f) }

    var inspectedExif by remember { mutableStateOf<ExifMetadataInfo?>(null) }
    var stripOnlyGps by remember { mutableStateOf(false) }
    var isLoadingExif by remember { mutableStateOf(false) }

    var resizeMode by remember { mutableStateOf(ResizeMode.PERCENTAGE) }
    var resizeWidthPx by remember { mutableStateOf("1080") }
    var resizeHeightPx by remember { mutableStateOf("1080") }
    var lockAspectRatio by remember { mutableStateOf(true) }
    var resizePercentage by remember { mutableFloatStateOf(50f) }
    var targetKbPreset by remember { mutableStateOf("200") }
    var customTargetKb by remember { mutableStateOf("200") }

    var extractedPalette by remember { mutableStateOf<List<PaletteColor>>(emptyList()) }
    var isExtractingPalette by remember { mutableStateOf(false) }

    // QR & Barcode Tool States
    var qrPayloadType by remember { mutableStateOf(QrPayloadType.URL) }
    var qrInputUrl by remember { mutableStateOf("https://") }
    var qrInputWifiSsid by remember { mutableStateOf("") }
    var qrInputWifiPass by remember { mutableStateOf("") }
    var qrInputWifiSecurity by remember { mutableStateOf("WPA") }
    var qrInputWifiHidden by remember { mutableStateOf(false) }
    var qrInputUpiId by remember { mutableStateOf("") }
    var qrInputUpiName by remember { mutableStateOf("") }
    var qrInputUpiAmount by remember { mutableStateOf("") }
    var qrInputUpiNote by remember { mutableStateOf("") }
    var qrInputVcardName by remember { mutableStateOf("") }
    var qrInputVcardPhone by remember { mutableStateOf("") }
    var qrInputVcardEmail by remember { mutableStateOf("") }
    var qrInputVcardCompany by remember { mutableStateOf("") }
    var qrInputText by remember { mutableStateOf("") }
    var qrInputEmail by remember { mutableStateOf("") }
    var qrInputPhone by remember { mutableStateOf("") }
    var qrFgColorHex by remember { mutableStateOf("#000000") }
    var qrBgColorHex by remember { mutableStateOf("#FFFFFF") }
    var qrErrorCorrection by remember { mutableStateOf(ErrorCorrectionLevel.M) }
    var qrResolutionPx by remember { mutableIntStateOf(1024) }

    var scannedQrResult by remember { mutableStateOf<QrParsedResult?>(null) }
    var isScanningQr by remember { mutableStateOf(false) }

    // Security Scanner Tool States
    val savedVtApiKey by preferencesManager.virusTotalApiKey.collectAsState(initial = "")
    val savedHaApiKey by preferencesManager.hybridAnalysisApiKey.collectAsState(initial = "")
    var securityService by remember { mutableStateOf(SecurityServiceType.VIRUSTOTAL) }
    var localSha256 by remember { mutableStateOf("") }
    var localMd5 by remember { mutableStateOf("") }
    var isComputingHash by remember { mutableStateOf(false) }
    var securityReport by remember { mutableStateOf<SecurityScanReport?>(null) }
    var customApiKeyInput by remember { mutableStateOf("") }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var showSetupGuide by remember { mutableStateOf(false) }
    var isTestingKey by remember { mutableStateOf(false) }
    var keyTestMessage by remember { mutableStateOf<String?>(null) }
    var allowUploadScan by remember { mutableStateOf(false) }
    var engineSearchQuery by remember { mutableStateOf("") }

    val liveQrBitmap = remember(
        tool, qrPayloadType, qrInputUrl, qrInputWifiSsid, qrInputWifiPass, qrInputWifiSecurity, qrInputWifiHidden,
        qrInputUpiId, qrInputUpiName, qrInputUpiAmount, qrInputUpiNote, qrInputVcardName, qrInputVcardPhone,
        qrInputVcardEmail, qrInputVcardCompany, qrInputText, qrInputEmail, qrInputPhone, qrFgColorHex, qrBgColorHex, qrErrorCorrection
    ) {
        if (tool == ToolType.QR_BARCODE_GENERATOR) {
            try {
                val engine = QrBarcodeGeneratorEngine(context, storageManager)
                val payload = engine.buildPayload(
                    type = qrPayloadType,
                    url = qrInputUrl,
                    wifiSsid = qrInputWifiSsid,
                    wifiPassword = qrInputWifiPass,
                    wifiSecurity = qrInputWifiSecurity,
                    wifiHidden = qrInputWifiHidden,
                    upiId = qrInputUpiId,
                    upiName = qrInputUpiName,
                    upiAmount = qrInputUpiAmount,
                    upiNote = qrInputUpiNote,
                    vcardName = qrInputVcardName,
                    vcardPhone = qrInputVcardPhone,
                    vcardEmail = qrInputVcardEmail,
                    vcardCompany = qrInputVcardCompany,
                    text = qrInputText,
                    email = qrInputEmail,
                    phone = qrInputPhone
                )
                val fg = try { android.graphics.Color.parseColor(qrFgColorHex) } catch (_: Exception) { android.graphics.Color.BLACK }
                val bg = try { android.graphics.Color.parseColor(qrBgColorHex) } catch (_: Exception) { android.graphics.Color.WHITE }
                engine.generateQrBitmap(payload, 512, fg, bg, qrErrorCorrection)
            } catch (_: Exception) {
                null
            }
        } else null
    }

    LaunchedEffect(selectedFiles, tool) {
        if (selectedFiles.isNotEmpty()) {
            val firstUri = selectedFiles.first().uri
            if (tool == ToolType.IMAGE_EXIF_STRIPPER) {
                isLoadingExif = true
                try {
                    val engine = ImageExifEngine(context, storageManager)
                    inspectedExif = engine.readExif(firstUri)
                } catch (_: Exception) {
                    inspectedExif = null
                } finally {
                    isLoadingExif = false
                }
            } else if (tool == ToolType.IMAGE_PALETTE_EXTRACTOR) {
                isExtractingPalette = true
                try {
                    val engine = ImagePaletteEngine(context, storageManager)
                    extractedPalette = engine.extractPalette(firstUri)
                } catch (_: Exception) {
                    extractedPalette = emptyList()
                } finally {
                    isExtractingPalette = false
                }
            } else if (tool == ToolType.IMAGE_RESIZER) {
                try {
                    val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    val temp = storageManager.copyUriToLocalTemp(firstUri, "bounds_chk")
                    android.graphics.BitmapFactory.decodeFile(temp.absolutePath, options)
                    temp.delete()
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        resizeWidthPx = options.outWidth.toString()
                        resizeHeightPx = options.outHeight.toString()
                    }
                } catch (_: Exception) {}
            } else if (tool == ToolType.QR_BARCODE_SCANNER) {
                isScanningQr = true
                try {
                    val engine = QrBarcodeScannerEngine(context, storageManager)
                    scannedQrResult = engine.scanImage(firstUri)
                } catch (e: Exception) {
                    scannedQrResult = null
                    Toast.makeText(context, e.localizedMessage ?: "No readable QR code found", Toast.LENGTH_SHORT).show()
                } finally {
                    isScanningQr = false
                }
            } else if (tool == ToolType.SECURITY_SCANNER) {
                isComputingHash = true
                securityReport = null
                try {
                    val engine = SecurityScannerEngine(context, storageManager)
                    val hashes = engine.computeFileHashes(firstUri)
                    localSha256 = hashes.first
                    localMd5 = hashes.second
                } catch (_: Exception) {
                    localSha256 = ""
                    localMd5 = ""
                } finally {
                    isComputingHash = false
                }
            }
        } else {
            inspectedExif = null
            extractedPalette = emptyList()
            scannedQrResult = null
            securityReport = null
            localSha256 = ""
            localMd5 = ""
        }
    }

    val presetStamps = remember {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        listOf(
            StampPreset("Approved", "APPROVED", "#16A34A"),
            StampPreset("Confidential", "CONFIDENTIAL", "#DC2626"),
            StampPreset("Draft", "DRAFT", "#EA580C"),
            StampPreset("Paid", "PAID", "#0284C7"),
            StampPreset("Void", "VOID", "#64748B"),
            StampPreset("Date", today, "#4F46E5")
        )
    }

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = storageManager.getFileName(uri)
            val size = storageManager.getFileSize(uri)
            selectedFiles = listOf(SelectedFile(uri, name, size, tool.inputMimeTypes.firstOrNull() ?: "*/*"))
            pageItems = listOf(PageItem(UUID.randomUUID().toString(), uri, name, 0, 0))

            if (tool == ToolType.PDF_METADATA_EDITOR) {
                scope.launch {
                    try {
                        val engine = PdfMetadataEngine(context, storageManager)
                        pdfMetadata = engine.readMetadata(uri)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newFiles = uris.map { uri ->
                val name = storageManager.getFileName(uri)
                val size = storageManager.getFileSize(uri)
                SelectedFile(uri, name, size, tool.inputMimeTypes.firstOrNull() ?: "*/*")
            }
            selectedFiles = if (tool.allowsMultipleFiles) selectedFiles + newFiles else newFiles
            pageItems = selectedFiles.mapIndexed { idx, f ->
                PageItem(UUID.randomUUID().toString(), f.uri, f.name, 0, idx)
            }
        }
    }

    fun openFilePicker() {
        if (tool.allowsMultipleFiles) {
            multiPickerLauncher.launch(tool.inputMimeTypes)
        } else {
            singlePickerLauncher.launch(tool.inputMimeTypes)
        }
    }

    fun startProcessing() {
        if (selectedFiles.isEmpty() && tool != ToolType.QR_BARCODE_GENERATOR) return
        isProcessing = true
        processResult = null
        currentStep = 0
        totalSteps = if (selectedFiles.isNotEmpty()) selectedFiles.size else 1
        FileProcessingService.start(context, "Processing ${tool.title}...")

        scope.launch {
            try {
                when (tool) {
                    ToolType.IMAGE_TO_PDF -> {
                        val engine = ImageToPdfEngine(context, storageManager)
                        val urisToProcess = if (pageItems.isNotEmpty()) pageItems.map { it.uri } else selectedFiles.map { it.uri }
                        val outputFile = engine.convert(
                            imageUris = urisToProcess,
                            pageSize = selectedPageSize,
                            orientation = selectedOrientation,
                            qualityPercent = selectedQuality.qualityPercent,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Rendering page $c of $t"
                                FileProcessingService.updateProgress(context, "Rendering page $c of $t", c, t)
                            }
                        )
                        val finalFilename = storageManager.generateFileName(namingPrefix, "pdf")
                        val savedUri = storageManager.saveToTarget(
                            outputFile,
                            finalFilename,
                            "application/pdf",
                            defaultSaveUri,
                            askBeforeReplace
                        )
                        historyRepository.recordItem(
                            tool,
                            selectedFiles.first().name,
                            finalFilename,
                            savedUri.toString(),
                            outputFile.length(),
                            true
                        )
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(savedUri),
                            outputFilenames = listOf(finalFilename),
                            outputTotalBytes = outputFile.length(),
                            message = "PDF created successfully."
                        )
                    }

                    ToolType.PDF_TO_IMAGES -> {
                        val engine = PdfToImagesEngine(context, storageManager)
                        val outputFiles = engine.convert(
                            pdfUri = selectedFiles.first().uri,
                            format = selectedFormat,
                            qualityPercent = selectedQuality.qualityPercent,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Extracting page $c of $t"
                                FileProcessingService.updateProgress(context, "Extracting page $c of $t", c, t)
                            }
                        )
                        val savedUris = mutableListOf<Uri>()
                        val savedNames = mutableListOf<String>()
                        outputFiles.forEachIndexed { idx, file ->
                            val name = storageManager.generateFileName("${namingPrefix}_Page_${idx + 1}", selectedFormat.extension)
                            val uri = storageManager.saveToTarget(file, name, selectedFormat.mimeType, defaultSaveUri, askBeforeReplace)
                            savedUris.add(uri)
                            savedNames.add(name)
                        }
                        historyRepository.recordItem(
                            tool,
                            selectedFiles.first().name,
                            "${outputFiles.size} images",
                            savedUris.firstOrNull().toString(),
                            outputFiles.sumOf { it.length() },
                            true
                        )
                        processResult = ProcessResult(
                            success = true,
                            outputUris = savedUris,
                            outputFilenames = savedNames,
                            outputTotalBytes = outputFiles.sumOf { it.length() },
                            message = "Extracted ${outputFiles.size} images."
                        )
                    }

                    ToolType.OCR_TEXT_EXTRACTOR -> {
                        val ocrEngine = OcrEngine(context, storageManager)
                        val isPdf = selectedFiles.first().name.endsWith(".pdf", ignoreCase = true)

                        if (isPdf) {
                            val text = ocrEngine.extractTextFromPdfWithOcr(
                                selectedFiles.first().uri,
                                onProgress = { c, t ->
                                    currentStep = c
                                    totalSteps = t
                                    progressStatus = "Running OCR on page $c of $t"
                                }
                            )
                            extractedOcrText = text
                            val tempFile = storageManager.createTempFile("ocr_text_", ".txt")
                            tempFile.writeText(text)
                            val name = storageManager.generateFileName("${namingPrefix}_OCR", "txt")
                            val uri = storageManager.saveToTarget(tempFile, name, "text/plain", defaultSaveUri, askBeforeReplace)
                            historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), tempFile.length(), true)
                            processResult = ProcessResult(
                                success = true,
                                outputUris = listOf(uri),
                                outputFilenames = listOf(name),
                                outputTotalBytes = tempFile.length(),
                                message = "OCR completed! Extracted text saved to file."
                            )
                        } else {
                            if (ocrMode == "SEARCHABLE_PDF") {
                                val uris = selectedFiles.map { it.uri }
                                val file = ocrEngine.createSearchablePdf(uris) { c, t ->
                                    currentStep = c
                                    totalSteps = t
                                    progressStatus = "Layering OCR on page $c of $t"
                                }
                                val name = storageManager.generateFileName("${namingPrefix}_Searchable", "pdf")
                                val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                                historyRepository.recordItem(tool, "${selectedFiles.size} images", name, uri.toString(), file.length(), true)
                                processResult = ProcessResult(
                                    success = true,
                                    outputUris = listOf(uri),
                                    outputFilenames = listOf(name),
                                    outputTotalBytes = file.length(),
                                    message = "Searchable PDF created successfully with selectable OCR text!"
                                )
                            } else {
                                val sb = StringBuilder()
                                selectedFiles.forEachIndexed { idx, fileItem ->
                                    currentStep = idx + 1
                                    totalSteps = selectedFiles.size
                                    progressStatus = "Recognizing text in image ${idx + 1} of ${selectedFiles.size}"
                                    val text = ocrEngine.recognizeTextFromImageUri(fileItem.uri)
                                    sb.append("--- Image ${idx + 1}: ${fileItem.name} ---\n")
                                    sb.append(text).append("\n\n")
                                }
                                extractedOcrText = sb.toString()
                                val tempFile = storageManager.createTempFile("ocr_extracted_", ".txt")
                                tempFile.writeText(extractedOcrText)
                                val name = storageManager.generateFileName("${namingPrefix}_OCR", "txt")
                                val uri = storageManager.saveToTarget(tempFile, name, "text/plain", defaultSaveUri, askBeforeReplace)
                                historyRepository.recordItem(tool, "${selectedFiles.size} images", name, uri.toString(), tempFile.length(), true)
                                processResult = ProcessResult(
                                    success = true,
                                    outputUris = listOf(uri),
                                    outputFilenames = listOf(name),
                                    outputTotalBytes = tempFile.length(),
                                    message = "OCR text extraction completed successfully."
                                )
                            }
                        }
                    }

                    ToolType.PDF_SIGN_STAMP -> {
                        val engine = PdfSignStampEngine(context, storageManager)
                        val stampBitmap = if (signStampMode == "SIGN") {
                            requireNotNull(signatureBitmap) { "Please draw your signature on the pad above" }
                        } else {
                            val preset = presetStamps.find { it.text == selectedPresetStamp } ?: presetStamps.first()
                            engine.generateStampBitmap(preset)
                        }

                        progressStatus = "Applying signature to document..."
                        val outputFile = engine.applySignatureOrStamp(
                            pdfUri = selectedFiles.first().uri,
                            overlayBitmap = stampBitmap,
                            pageIndex = (signPageNumber - 1).coerceAtLeast(0),
                            xPercent = signXPercent,
                            yPercent = signYPercent,
                            scaleFactor = signScale,
                            opacity = signOpacity
                        )
                        val name = storageManager.generateFileName("${namingPrefix}_Signed", "pdf")
                        val uri = storageManager.saveToTarget(outputFile, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), outputFile.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = outputFile.length(),
                            message = "Signature & stamp successfully embedded into PDF."
                        )
                    }

                    ToolType.PDF_METADATA_EDITOR -> {
                        val engine = PdfMetadataEngine(context, storageManager)
                        progressStatus = "Saving PDF metadata..."
                        val outputFile = engine.updateMetadata(selectedFiles.first().uri, pdfMetadata)
                        val name = storageManager.generateFileName("${namingPrefix}_Updated", "pdf")
                        val uri = storageManager.saveToTarget(outputFile, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), outputFile.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = outputFile.length(),
                            message = "Document metadata updated successfully."
                        )
                    }

                    ToolType.PDF_TO_DOCX -> {
                        val engine = PdfToDocxEngine(context, storageManager)
                        val file = engine.convert(
                            pdfUri = selectedFiles.first().uri,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Converting page $c of $t"
                            }
                        )
                        val name = storageManager.generateFileName(namingPrefix, "docx")
                        val uri = storageManager.saveToTarget(file, name, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "DOCX document created successfully."
                        )
                    }

                    ToolType.DOCX_TO_PDF -> {
                        val engine = DocxToPdfEngine(context, storageManager)
                        val file = engine.convert(
                            docxUri = selectedFiles.first().uri,
                            pageSize = selectedPageSize,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Formatting paragraph $c of $t"
                            }
                        )
                        val name = storageManager.generateFileName(namingPrefix, "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "PDF document created successfully."
                        )
                    }

                    ToolType.PDF_COMPRESSOR -> {
                        val engine = PdfCompressorEngine(context, storageManager)
                        val file = engine.compress(
                            pdfUri = selectedFiles.first().uri,
                            level = selectedCompression,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Compressing page $c of $t"
                            }
                        )
                        val name = storageManager.generateFileName("${namingPrefix}_Compressed", "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            inputTotalBytes = selectedFiles.first().sizeBytes,
                            message = "PDF compressed successfully."
                        )
                    }

                    ToolType.PDF_PASSWORD_REMOVER -> {
                        val engine = PdfPasswordEngine(context, storageManager)
                        progressStatus = "Unlocking PDF..."
                        val file = engine.removePassword(selectedFiles.first().uri, passwordText)
                        val name = storageManager.generateFileName("${namingPrefix}_Unlocked", "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "Password removed and unlocked PDF saved."
                        )
                    }

                    ToolType.PDF_MERGE -> {
                        val engine = PdfMergeEngine(context, storageManager)
                        val uris = if (pageItems.isNotEmpty()) pageItems.map { it.uri } else selectedFiles.map { it.uri }
                        val file = engine.merge(
                            pdfUris = uris,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Merging file $c of $t"
                            }
                        )
                        val name = storageManager.generateFileName("${namingPrefix}_Merged", "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, "${uris.size} PDFs", name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "Merged ${uris.size} PDF files into one."
                        )
                    }

                    ToolType.PDF_SPLIT -> {
                        val engine = PdfSplitEngine(context, storageManager)
                        if (splitAllPages) {
                            val files = engine.splitAllPages(
                                selectedFiles.first().uri,
                                onProgress = { c, t ->
                                    currentStep = c
                                    totalSteps = t
                                    progressStatus = "Splitting page $c of $t"
                                }
                            )
                            val savedUris = mutableListOf<Uri>()
                            val savedNames = mutableListOf<String>()
                            files.forEachIndexed { idx, file ->
                                val name = storageManager.generateFileName("${namingPrefix}_Page_${idx + 1}", "pdf")
                                val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                                savedUris.add(uri)
                                savedNames.add(name)
                            }
                            historyRepository.recordItem(tool, selectedFiles.first().name, "${files.size} pages", savedUris.first().toString(), files.sumOf { it.length() }, true)
                            processResult = ProcessResult(
                                success = true,
                                outputUris = savedUris,
                                outputFilenames = savedNames,
                                outputTotalBytes = files.sumOf { it.length() },
                                message = "Split into ${files.size} separate PDF files."
                            )
                        } else {
                            val file = engine.extractPages(
                                selectedFiles.first().uri,
                                pageRangeText,
                                onProgress = { c, t ->
                                    currentStep = c
                                    totalSteps = t
                                    progressStatus = "Extracting page $c of $t"
                                }
                            )
                            val name = storageManager.generateFileName("${namingPrefix}_Extracted", "pdf")
                            val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                            historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                            processResult = ProcessResult(
                                success = true,
                                outputUris = listOf(uri),
                                outputFilenames = listOf(name),
                                outputTotalBytes = file.length(),
                                message = "Extracted selected pages into new PDF."
                            )
                        }
                    }

                    ToolType.IMAGE_COMPRESSOR -> {
                        val engine = ImageCompressorEngine(context, storageManager)
                        val savedUris = mutableListOf<Uri>()
                        val savedNames = mutableListOf<String>()
                        var totalOutBytes = 0L

                        selectedFiles.forEachIndexed { idx, fileItem ->
                            currentStep = idx + 1
                            totalSteps = selectedFiles.size
                            progressStatus = "Compressing image ${idx + 1} of ${selectedFiles.size}"
                            val compressedFile = engine.compress(
                                fileItem.uri,
                                imageQualitySlider.toInt(),
                                selectedFormat
                            )
                            val name = storageManager.generateFileName("${namingPrefix}_Compressed_${idx + 1}", selectedFormat.extension)
                            val uri = storageManager.saveToTarget(compressedFile, name, selectedFormat.mimeType, defaultSaveUri, askBeforeReplace)
                            savedUris.add(uri)
                            savedNames.add(name)
                            totalOutBytes += compressedFile.length()
                        }
                        historyRepository.recordItem(tool, "${selectedFiles.size} images", savedNames.first(), savedUris.first().toString(), totalOutBytes, true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = savedUris,
                            outputFilenames = savedNames,
                            outputTotalBytes = totalOutBytes,
                            inputTotalBytes = selectedFiles.sumOf { it.sizeBytes },
                            message = "Images compressed successfully."
                        )
                    }

                    ToolType.DOCUMENT_SCANNER -> {
                        val engine = DocumentScannerEngine(context, storageManager)
                        val processedBitmaps = mutableListOf<android.graphics.Bitmap>()
                        val filesToScan = if (pageItems.isNotEmpty()) pageItems.map { it.uri } else selectedFiles.map { it.uri }

                        filesToScan.forEachIndexed { idx, uri ->
                            currentStep = idx + 1
                            totalSteps = filesToScan.size
                            progressStatus = "Filtering scan ${idx + 1} of ${filesToScan.size}"
                            val bmp = engine.applyFilter(uri, scanFilter)
                            processedBitmaps.add(bmp)
                        }

                        val file = engine.exportScannedPagesToPdf(
                            processedBitmaps,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Generating PDF page $c of $t"
                            }
                        )
                        processedBitmaps.forEach { it.recycle() }

                        val name = storageManager.generateFileName("${namingPrefix}_Scan", "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, "${filesToScan.size} scans", name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "Scanned document saved as PDF."
                        )
                    }

                    ToolType.PDF_PROTECT -> {
                        require(protectPasswordText.isNotBlank()) { "Please enter a password" }
                        require(protectPasswordText == confirmPasswordText) { "Passwords do not match" }
                        val engine = PdfPasswordProtectEngine(context, storageManager)
                        progressStatus = "Encrypting and locking PDF..."
                        val file = engine.protectPdf(selectedFiles.first().uri, protectPasswordText)
                        val name = storageManager.generateFileName("${namingPrefix}_Protected", "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "PDF locked with password successfully."
                        )
                    }

                    ToolType.PDF_ROTATE -> {
                        val engine = PdfRotateEngine(context, storageManager)
                        progressStatus = "Rotating PDF pages by ${selectedRotationAngle}°..."
                        val file = engine.rotatePdf(selectedFiles.first().uri, selectedRotationAngle)
                        val name = storageManager.generateFileName("${namingPrefix}_Rotated", "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "PDF pages rotated by ${selectedRotationAngle}°."
                        )
                    }

                    ToolType.PDF_EXTRACT_TEXT -> {
                        val engine = PdfExtractTextEngine(context, storageManager)
                        progressStatus = "Extracting selectable text..."
                        val file = engine.extractText(selectedFiles.first().uri)
                        val name = storageManager.generateFileName("${namingPrefix}_Text", "txt")
                        val uri = storageManager.saveToTarget(file, name, "text/plain", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "Extracted text saved to TXT file."
                        )
                    }

                    ToolType.PDF_WATERMARK -> {
                        require(watermarkText.isNotBlank()) { "Please enter watermark text" }
                        val engine = PdfWatermarkEngine(context, storageManager)
                        progressStatus = "Applying watermark..."
                        val file = engine.addWatermark(selectedFiles.first().uri, watermarkText, watermarkOpacity)
                        val name = storageManager.generateFileName("${namingPrefix}_Watermarked", "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "Watermark stamped across all pages."
                        )
                    }

                    ToolType.IMAGE_FORMAT_CONVERTER -> {
                        val engine = ImageFormatConverterEngine(context, storageManager)
                        val uris = selectedFiles.map { it.uri }
                        val outputFiles = engine.convertBatch(
                            imageUris = uris,
                            targetFormat = targetFormatOption,
                            qualityPercent = formatQualityPercent.toInt(),
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Converting image $c of $t"
                                FileProcessingService.updateProgress(context, "Converting image $c of $t", c, t)
                            }
                        )
                        val savedUris = mutableListOf<Uri>()
                        val savedNames = mutableListOf<String>()
                        var totalBytes = 0L
                        outputFiles.forEachIndexed { idx, file ->
                            val name = storageManager.generateFileName("${namingPrefix}_Converted_${idx + 1}", targetFormatOption.extension)
                            val uri = storageManager.saveToTarget(file, name, targetFormatOption.mimeType, defaultSaveUri, askBeforeReplace)
                            savedUris.add(uri)
                            savedNames.add(name)
                            totalBytes += file.length()
                        }
                        historyRepository.recordItem(tool, "${selectedFiles.size} images", savedNames.first(), savedUris.first().toString(), totalBytes, true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = savedUris,
                            outputFilenames = savedNames,
                            outputTotalBytes = totalBytes,
                            inputTotalBytes = selectedFiles.sumOf { it.sizeBytes },
                            message = "Converted ${selectedFiles.size} image(s) to ${targetFormatOption.name} successfully."
                        )
                    }

                    ToolType.IMAGE_EXIF_STRIPPER -> {
                        val engine = ImageExifEngine(context, storageManager)
                        val uris = selectedFiles.map { it.uri }
                        val outputFiles = engine.stripExifBatch(
                            imageUris = uris,
                            stripOnlyGps = stripOnlyGps,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Cleaning metadata from image $c of $t"
                                FileProcessingService.updateProgress(context, "Cleaning metadata $c of $t", c, t)
                            }
                        )
                        val savedUris = mutableListOf<Uri>()
                        val savedNames = mutableListOf<String>()
                        var totalBytes = 0L
                        outputFiles.forEachIndexed { idx, file ->
                            val name = storageManager.generateFileName("${namingPrefix}_Cleaned_${idx + 1}", "jpg")
                            val uri = storageManager.saveToTarget(file, name, "image/jpeg", defaultSaveUri, askBeforeReplace)
                            savedUris.add(uri)
                            savedNames.add(name)
                            totalBytes += file.length()
                        }
                        val actionDesc = if (stripOnlyGps) "GPS location removed" else "All EXIF metadata stripped"
                        historyRepository.recordItem(tool, "${selectedFiles.size} images", savedNames.first(), savedUris.first().toString(), totalBytes, true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = savedUris,
                            outputFilenames = savedNames,
                            outputTotalBytes = totalBytes,
                            inputTotalBytes = selectedFiles.sumOf { it.sizeBytes },
                            message = "$actionDesc from ${selectedFiles.size} image(s) successfully."
                        )
                    }

                    ToolType.IMAGE_RESIZER -> {
                        val engine = ImageResizerEngine(context, storageManager)
                        val uris = selectedFiles.map { it.uri }
                        val targetKB = (if (targetKbPreset == "custom") customTargetKb.toLongOrNull() ?: 200L else targetKbPreset.toLongOrNull() ?: 200L) * 1024L
                        val targetW = resizeWidthPx.toIntOrNull() ?: 1080
                        val targetH = resizeHeightPx.toIntOrNull() ?: 1080
                        val outputFiles = engine.resizeBatch(
                            imageUris = uris,
                            mode = resizeMode,
                            targetWidth = targetW,
                            targetHeight = targetH,
                            percentage = resizePercentage.toInt(),
                            targetSizeBytes = targetKB,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Resizing image $c of $t"
                                FileProcessingService.updateProgress(context, "Resizing image $c of $t", c, t)
                            }
                        )
                        val savedUris = mutableListOf<Uri>()
                        val savedNames = mutableListOf<String>()
                        var totalBytes = 0L
                        outputFiles.forEachIndexed { idx, file ->
                            val name = storageManager.generateFileName("${namingPrefix}_Resized_${idx + 1}", "jpg")
                            val uri = storageManager.saveToTarget(file, name, "image/jpeg", defaultSaveUri, askBeforeReplace)
                            savedUris.add(uri)
                            savedNames.add(name)
                            totalBytes += file.length()
                        }
                        historyRepository.recordItem(tool, "${selectedFiles.size} images", savedNames.first(), savedUris.first().toString(), totalBytes, true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = savedUris,
                            outputFilenames = savedNames,
                            outputTotalBytes = totalBytes,
                            inputTotalBytes = selectedFiles.sumOf { it.sizeBytes },
                            message = "Resized ${selectedFiles.size} image(s) successfully."
                        )
                    }

                    ToolType.IMAGE_PALETTE_EXTRACTOR -> {
                        val engine = ImagePaletteEngine(context, storageManager)
                        val palette = if (extractedPalette.isNotEmpty()) extractedPalette else engine.extractPalette(selectedFiles.first().uri)
                        val file = engine.exportPaletteCard(palette, selectedFiles.first().name)
                        val name = storageManager.generateFileName("${namingPrefix}_PaletteCard", "png")
                        val uri = storageManager.saveToTarget(file, name, "image/png", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "Color palette card exported successfully."
                        )
                    }

                    ToolType.QR_BARCODE_GENERATOR -> {
                        val engine = QrBarcodeGeneratorEngine(context, storageManager)
                        val payload = engine.buildPayload(
                            type = qrPayloadType,
                            url = qrInputUrl,
                            wifiSsid = qrInputWifiSsid,
                            wifiPassword = qrInputWifiPass,
                            wifiSecurity = qrInputWifiSecurity,
                            wifiHidden = qrInputWifiHidden,
                            upiId = qrInputUpiId,
                            upiName = qrInputUpiName,
                            upiAmount = qrInputUpiAmount,
                            upiNote = qrInputUpiNote,
                            vcardName = qrInputVcardName,
                            vcardPhone = qrInputVcardPhone,
                            vcardEmail = qrInputVcardEmail,
                            vcardCompany = qrInputVcardCompany,
                            text = qrInputText,
                            email = qrInputEmail,
                            phone = qrInputPhone
                        )
                        require(payload.isNotBlank()) { "Please enter content to generate QR Code" }
                        val fg = try { android.graphics.Color.parseColor(qrFgColorHex) } catch (_: Exception) { android.graphics.Color.BLACK }
                        val bg = try { android.graphics.Color.parseColor(qrBgColorHex) } catch (_: Exception) { android.graphics.Color.WHITE }
                        progressStatus = "Generating high-resolution QR Code..."
                        val file = engine.exportQrCode(payload, qrResolutionPx, fg, bg, qrErrorCorrection)
                        val name = storageManager.generateFileName("${namingPrefix}_QRCode", "png")
                        val uri = storageManager.saveToTarget(file, name, "image/png", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, "${qrPayloadType.title} QR", name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "${qrPayloadType.title} QR Code saved as high-resolution PNG."
                        )
                    }

                    ToolType.QR_BARCODE_SCANNER -> {
                        val engine = QrBarcodeScannerEngine(context, storageManager)
                        progressStatus = "Scanning barcode payload..."
                        val result = if (scannedQrResult != null) scannedQrResult!! else engine.scanImage(selectedFiles.first().uri)
                        val name = storageManager.generateFileName("${namingPrefix}_ScanResult", "txt")
                        val tempTxt = storageManager.createTempFile("scan_result_", ".txt")
                        tempTxt.writeText("Type: ${result.type}\nFormat: ${result.formatName}\nTitle: ${result.displayTitle}\nContent:\n${result.rawText}\n")
                        val uri = storageManager.saveToTarget(tempTxt, name, "text/plain", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), tempTxt.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = tempTxt.length(),
                            message = "Decoded ${result.formatName}: ${result.displayTitle}"
                        )
                    }

                    ToolType.SECURITY_SCANNER -> {
                        val engine = SecurityScannerEngine(context, storageManager)
                        val apiKeyToUse = if (securityService == SecurityServiceType.VIRUSTOTAL) {
                            if (customApiKeyInput.isNotBlank()) customApiKeyInput.trim() else savedVtApiKey.trim()
                        } else if (securityService == SecurityServiceType.HYBRID_ANALYSIS) {
                            if (customApiKeyInput.isNotBlank()) customApiKeyInput.trim() else savedHaApiKey.trim()
                        } else ""

                        progressStatus = "Scanning ${securityService.title} threat intelligence..."
                        val report = engine.scanFile(
                            uri = selectedFiles.first().uri,
                            service = securityService,
                            apiKey = apiKeyToUse,
                            allowUpload = allowUploadScan,
                            onProgress = { status ->
                                progressStatus = status
                            }
                        )
                        securityReport = report

                        val name = storageManager.generateFileName("${namingPrefix}_SecurityReport", "txt")
                        val tempTxt = storageManager.createTempFile("sec_report_", ".txt")
                        val sb = StringBuilder()
                        sb.append("FileFlow Security Report - ${report.serviceName}\n")
                        sb.append("========================================\n")
                        sb.append("File: ${report.fileName} (${storageManager.formatFileSize(report.fileSize)})\n")
                        sb.append("SHA-256: ${report.sha256}\n")
                        if (report.md5.isNotBlank()) sb.append("MD5: ${report.md5}\n")
                        sb.append("Verdict: ${report.verdict.label}\n")
                        sb.append("Summary: ${report.threatScoreText}\n")
                        if (report.webReportUrl != null) sb.append("Web Report: ${report.webReportUrl}\n")
                        sb.append("Scan Date: ${report.scanDate}\n\n")
                        if (report.engineDetections.isNotEmpty()) {
                            sb.append("Engine Detections:\n")
                            report.engineDetections.forEach { eng ->
                                sb.append("- ${eng.engineName}: ${eng.category.uppercase()} ${eng.threatName?.let { "($it)" } ?: ""}\n")
                            }
                        }
                        tempTxt.writeText(sb.toString())
                        val uri = storageManager.saveToTarget(tempTxt, name, "text/plain", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, selectedFiles.first().name, name, uri.toString(), tempTxt.length(), true)

                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = tempTxt.length(),
                            message = "${report.serviceName}: ${report.threatScoreText}"
                        )
                    }
                }
            } catch (e: Exception) {
                haptics.heavyTap()
                processResult = ProcessResult(
                    success = false,
                    message = e.localizedMessage ?: "An error occurred during processing."
                )
            } finally {
                isProcessing = false
                FileProcessingService.stop(context)
                if (autoDeleteTemp) {
                    storageManager.clearTempFiles()
                }
                if (processResult?.success == true) {
                    haptics.heavyTap()
                }
            }
        }
    }

    fun openResultFile(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun shareResultFile(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = context.contentResolver.getType(uri) ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        } catch (_: Exception) {
        }
    }

    fun copyTextToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FileFlow Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun openUriAction(uriString: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No app available to handle this link", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        FloatingTopAppBar(
            title = tool.title,
            subtitle = tool.category.title,
            isFloating = floatingTopBar,
            navigationIcon = {
                IconButton(onClick = {
                    haptics.tap()
                    onBack()
                }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (processResult != null) {
                item {
                    ResultCard(
                        result = processResult!!,
                        onOpen = { processResult?.outputUris?.firstOrNull()?.let { openResultFile(it) } },
                        onShare = { processResult?.outputUris?.firstOrNull()?.let { shareResultFile(it) } },
                        onSaveAs = { openFilePicker() },
                        onReset = {
                            processResult = null
                            selectedFiles = emptyList()
                            pageItems = emptyList()
                        }
                    )
                }

                if (extractedOcrText.isNotBlank()) {
                    item {
                        Surface(
                            shape = ToolCardShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recognized Text Content",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(onClick = {
                                        haptics.tap()
                                        copyTextToClipboard(extractedOcrText)
                                    }) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy Text", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = extractedOcrText.take(1000) + if (extractedOcrText.length > 1000) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (isProcessing) {
                item {
                    ProcessProgressBar(
                        currentStep = currentStep,
                        totalSteps = totalSteps,
                        statusText = progressStatus,
                        onCancel = {
                            isProcessing = false
                            FileProcessingService.stop(context)
                        }
                    )
                }
            } else {
                if (tool == ToolType.QR_BARCODE_GENERATOR) {
                    item {
                        if (liveQrBitmap != null) {
                            Surface(
                                shape = ToolCardShape,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Live Interactive Preview",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(190.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(androidx.compose.ui.graphics.Color.White)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = liveQrBitmap.asImageBitmap(),
                                            contentDescription = "Live QR Code",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Generates instantly as you customize details below",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // File Picker Box
                    item {
                        Surface(
                            shape = ToolCardShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (tool == ToolType.QR_BARCODE_SCANNER) "Select Code Image" else if (selectedFiles.isEmpty()) "Select Input" else "Selected Files (${selectedFiles.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (tool.inputMimeTypes.contains("image/*") || tool == ToolType.DOCUMENT_SCANNER || tool == ToolType.IMAGE_TO_PDF || tool == ToolType.OCR_TEXT_EXTRACTOR || tool == ToolType.QR_BARCODE_SCANNER) {
                                            FilledTonalButton(
                                                onClick = {
                                                    haptics.tap()
                                                    requestCameraAndLaunch()
                                                },
                                                shape = CircleShape,
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CameraAlt,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Camera", maxLines = 1)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                haptics.tap()
                                                openFilePicker()
                                            },
                                            shape = CircleShape,
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (selectedFiles.isEmpty()) Icons.Rounded.FileOpen else Icons.Rounded.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (selectedFiles.isEmpty()) "Browse" else "Add", maxLines = 1)
                                        }
                                    }
                                }

                                if (selectedFiles.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    selectedFiles.forEachIndexed { index, file ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = getToolIcon(tool.iconName),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = file.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            IconButton(
                                                onClick = {
                                                    haptics.tap()
                                                    selectedFiles = selectedFiles.filterIndexed { i, _ -> i != index }
                                                    pageItems = pageItems.filterIndexed { i, _ -> i != index }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Scanned QR / Barcode Card
                if (tool == ToolType.QR_BARCODE_SCANNER && (isScanningQr || scannedQrResult != null)) {
                    item {
                        Surface(
                            shape = ToolCardShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (isScanningQr) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Decoding QR / Barcode...", style = MaterialTheme.typography.bodyMedium)
                                    }
                                } else if (scannedQrResult != null) {
                                    val result = scannedQrResult!!
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = result.displayTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = result.formatName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    if (result.displaySubtitle.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = result.displaySubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (result.details.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                result.details.forEach { (k, v) ->
                                                    Row {
                                                        Text("$k: ", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                                        Text(v, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (result.actionUrl != null) {
                                            Button(
                                                onClick = {
                                                    haptics.tap()
                                                    openUriAction(result.actionUrl)
                                                },
                                                shape = CircleShape,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    when (result.type) {
                                                        QrContentType.URL -> "Open Link"
                                                        QrContentType.UPI -> "Pay in App"
                                                        QrContentType.EMAIL -> "Send Email"
                                                        QrContentType.PHONE -> "Call"
                                                        QrContentType.SMS -> "Send SMS"
                                                        else -> "Open"
                                                    }
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                haptics.tap()
                                                copyTextToClipboard(result.details["Password"] ?: result.details["UPI ID"] ?: result.rawText)
                                            },
                                            shape = CircleShape,
                                            modifier = if (result.actionUrl != null) Modifier.weight(1f) else Modifier.fillMaxWidth()
                                        ) {
                                            Text(if (result.type == QrContentType.WIFI && result.details.containsKey("Password")) "Copy Password" else "Copy Text")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Security Hashes Card
                if (tool == ToolType.SECURITY_SCANNER && selectedFiles.isNotEmpty()) {
                    item {
                        Surface(
                            shape = ToolCardShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "On-Device Cryptographic Hashes",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isComputingHash) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                if (localSha256.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("SHA-256 (Primary Security Identifier):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                                    Text(
                                                        text = localSha256,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        haptics.tap()
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("SHA-256", localSha256))
                                                        Toast.makeText(context, "SHA-256 copied to clipboard", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy SHA-256", modifier = Modifier.size(18.dp))
                                                }
                                            }

                                            if (localMd5.isNotBlank()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("MD5:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                                        Text(
                                                            text = localMd5,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            haptics.tap()
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("MD5", localMd5))
                                                            Toast.makeText(context, "MD5 copied to clipboard", Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy MD5", modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Security Threat Scan Report Card
                if (tool == ToolType.SECURITY_SCANNER && securityReport != null) {
                    val report = securityReport!!
                    item {
                        Surface(
                            shape = ToolCardShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val verdictColor = when (report.verdict) {
                                    SecurityThreatVerdict.CLEAN -> androidx.compose.ui.graphics.Color(0xFF16A34A)
                                    SecurityThreatVerdict.SUSPICIOUS -> androidx.compose.ui.graphics.Color(0xFFEA580C)
                                    SecurityThreatVerdict.MALICIOUS -> androidx.compose.ui.graphics.Color(0xFFDC2626)
                                    SecurityThreatVerdict.UNKNOWN -> androidx.compose.ui.graphics.Color(0xFF64748B)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = verdictColor.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (report.verdict) {
                                                SecurityThreatVerdict.CLEAN -> Icons.Rounded.CheckCircle
                                                SecurityThreatVerdict.SUSPICIOUS -> Icons.Rounded.PrivacyTip
                                                SecurityThreatVerdict.MALICIOUS -> Icons.Rounded.ErrorOutline
                                                SecurityThreatVerdict.UNKNOWN -> Icons.Rounded.Description
                                            },
                                            contentDescription = null,
                                            tint = verdictColor,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = report.verdict.label,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = verdictColor
                                            )
                                            Text(
                                                text = report.threatScoreText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                if (report.threatTags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(report.threatTags) { tag ->
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = tag,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (report.engineDetections.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Antivirus Engine Breakdown (${report.engineDetections.size})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = engineSearchQuery,
                                        onValueChange = { engineSearchQuery = it },
                                        placeholder = { Text("Filter engines (e.g. Microsoft, Kaspersky)...", style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = CircleShape,
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val filteredEngines = remember(report.engineDetections, engineSearchQuery) {
                                        if (engineSearchQuery.isBlank()) report.engineDetections
                                        else report.engineDetections.filter { it.engineName.contains(engineSearchQuery, ignoreCase = true) || (it.threatName?.contains(engineSearchQuery, ignoreCase = true) == true) }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 240.dp)
                                    ) {
                                        LazyColumn(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            items(filteredEngines) { eng ->
                                                val isFlagged = eng.category == "malicious" || eng.category == "suspicious"
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = eng.engineName,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isFlagged) FontWeight.Bold else FontWeight.Normal),
                                                        color = if (isFlagged) androidx.compose.ui.graphics.Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = eng.threatName ?: if (eng.category == "undetected") "Undetected / Clean" else eng.category,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isFlagged) androidx.compose.ui.graphics.Color(0xFFDC2626) else androidx.compose.ui.graphics.Color(0xFF16A34A)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (report.webReportUrl != null) {
                                        Button(
                                            onClick = {
                                                haptics.tap()
                                                try {
                                                    uriHandler.openUri(report.webReportUrl)
                                                } catch (_: Exception) {}
                                            },
                                            shape = CircleShape,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Online Report", maxLines = 1)
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            haptics.tap()
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Security Report", "${report.serviceName} Report for ${report.fileName}\nVerdict: ${report.verdict.label}\nSHA-256: ${report.sha256}\nSummary: ${report.threatScoreText}\n${report.webReportUrl ?: ""}"))
                                            Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = CircleShape,
                                        modifier = if (report.webReportUrl != null) Modifier.weight(1f) else Modifier.fillMaxWidth()
                                    ) {
                                        Text("Copy Summary", maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Visual Page Organizer (for image-to-pdf, merge, and scanner)
                if (pageItems.size > 1 && (tool == ToolType.IMAGE_TO_PDF || tool == ToolType.PDF_MERGE || tool == ToolType.DOCUMENT_SCANNER)) {
                    item {
                        VisualPageOrganizer(
                            pages = pageItems,
                            onReorder = { from, to ->
                                val mutable = pageItems.toMutableList()
                                val item = mutable.removeAt(from)
                                mutable.add(to, item)
                                pageItems = mutable
                            },
                            onRotate = { index ->
                                val item = pageItems[index]
                                val newDegrees = (item.rotationDegrees + 90) % 360
                                pageItems = pageItems.toMutableList().apply {
                                    set(index, item.copy(rotationDegrees = newDegrees))
                                }
                            },
                            onDelete = { index ->
                                pageItems = pageItems.filterIndexed { i, _ -> i != index }
                                selectedFiles = selectedFiles.filterIndexed { i, _ -> i != index }
                            }
                        )
                    }
                }

                // Tool Specific Configuration Panels
                item {
                    Surface(
                        shape = ToolCardShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Options & Settings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            when (tool) {
                                ToolType.IMAGE_TO_PDF -> {
                                    Text("Page Size", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(PageSizeOption.entries) { opt ->
                                            FilterChip(
                                                selected = selectedPageSize == opt,
                                                onClick = {
                                                    haptics.tap()
                                                    selectedPageSize = opt
                                                },
                                                label = { Text(opt.title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Orientation", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(OrientationOption.entries) { opt ->
                                            FilterChip(
                                                selected = selectedOrientation == opt,
                                                onClick = {
                                                    haptics.tap()
                                                    selectedOrientation = opt
                                                },
                                                label = { Text(opt.title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Quality", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(ImageQualityOption.entries) { opt ->
                                            FilterChip(
                                                selected = selectedQuality == opt,
                                                onClick = {
                                                    haptics.tap()
                                                    selectedQuality = opt
                                                },
                                                label = { Text(opt.title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }

                                ToolType.OCR_TEXT_EXTRACTOR -> {
                                    Text("OCR Output Mode", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = ocrMode == "TXT",
                                            onClick = {
                                                haptics.tap()
                                                ocrMode = "TXT"
                                            },
                                            label = { Text("Extract Text (.txt)") },
                                            shape = CircleShape
                                        )
                                        FilterChip(
                                            selected = ocrMode == "SEARCHABLE_PDF",
                                            onClick = {
                                                haptics.tap()
                                                ocrMode = "SEARCHABLE_PDF"
                                            },
                                            label = { Text("Searchable PDF (.pdf)") },
                                            shape = CircleShape
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "100% offline ML Kit text recognition. No internet or data upload required.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                ToolType.PDF_SIGN_STAMP -> {
                                    Text("Mode", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = signStampMode == "SIGN",
                                            onClick = {
                                                haptics.tap()
                                                signStampMode = "SIGN"
                                            },
                                            label = { Text("Draw Signature") },
                                            shape = CircleShape
                                        )
                                        FilterChip(
                                            selected = signStampMode == "STAMP",
                                            onClick = {
                                                haptics.tap()
                                                signStampMode = "STAMP"
                                            },
                                            label = { Text("Document Stamp") },
                                            shape = CircleShape
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (signStampMode == "SIGN") {
                                        SignatureCanvas(
                                            onSignatureChanged = { signatureBitmap = it }
                                        )
                                    } else {
                                        Text("Select Official Stamp", style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(presetStamps) { preset ->
                                                FilterChip(
                                                    selected = selectedPresetStamp == preset.text,
                                                    onClick = {
                                                        haptics.tap()
                                                        selectedPresetStamp = preset.text
                                                    },
                                                    label = { Text(preset.title) },
                                                    shape = CircleShape
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Page Number: $signPageNumber", style = MaterialTheme.typography.labelMedium)
                                    Slider(
                                        value = signPageNumber.toFloat(),
                                        onValueChange = { signPageNumber = it.toInt() },
                                        valueRange = 1f..10f,
                                        steps = 8
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Vertical Position: ${(signYPercent * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                                    Slider(
                                        value = signYPercent,
                                        onValueChange = { signYPercent = it },
                                        valueRange = 0.05f..0.95f
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Scale: ${(signScale * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                                    Slider(
                                        value = signScale,
                                        onValueChange = { signScale = it },
                                        valueRange = 0.15f..0.75f
                                    )
                                }

                                ToolType.PDF_METADATA_EDITOR -> {
                                    OutlinedTextField(
                                        value = pdfMetadata.title,
                                        onValueChange = { pdfMetadata = pdfMetadata.copy(title = it) },
                                        label = { Text("Title") },
                                        shape = CircleShape,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = pdfMetadata.author,
                                        onValueChange = { pdfMetadata = pdfMetadata.copy(author = it) },
                                        label = { Text("Author") },
                                        shape = CircleShape,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = pdfMetadata.subject,
                                        onValueChange = { pdfMetadata = pdfMetadata.copy(subject = it) },
                                        label = { Text("Subject") },
                                        shape = CircleShape,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = pdfMetadata.keywords,
                                        onValueChange = { pdfMetadata = pdfMetadata.copy(keywords = it) },
                                        label = { Text("Keywords (comma separated)") },
                                        shape = CircleShape,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                ToolType.PDF_TO_IMAGES -> {
                                    Text("Output Image Format", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(ImageFormatOption.entries) { opt ->
                                            FilterChip(
                                                selected = selectedFormat == opt,
                                                onClick = {
                                                    haptics.tap()
                                                    selectedFormat = opt
                                                },
                                                label = { Text(opt.name) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }

                                ToolType.PDF_COMPRESSOR -> {
                                    Text("Compression Level", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(CompressionLevel.entries) { opt ->
                                            FilterChip(
                                                selected = selectedCompression == opt,
                                                onClick = {
                                                    haptics.tap()
                                                    selectedCompression = opt
                                                },
                                                label = { Text(opt.title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }

                                ToolType.PDF_PASSWORD_REMOVER -> {
                                    OutlinedTextField(
                                        value = passwordText,
                                        onValueChange = { passwordText = it },
                                        label = { Text("PDF Password") },
                                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = CircleShape,
                                        singleLine = true
                                    )
                                }

                                ToolType.PDF_SPLIT -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Extract all pages individually", style = MaterialTheme.typography.bodyMedium)
                                        Switch(
                                            checked = splitAllPages,
                                            onCheckedChange = {
                                                haptics.tap()
                                                splitAllPages = it
                                            }
                                        )
                                    }

                                    if (!splitAllPages) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = pageRangeText,
                                            onValueChange = { pageRangeText = it },
                                            label = { Text("Page Range (e.g. 1-3, 5)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = CircleShape,
                                            singleLine = true
                                        )
                                    }
                                }

                                ToolType.IMAGE_COMPRESSOR -> {
                                    Text("Quality: ${imageQualitySlider.toInt()}%", style = MaterialTheme.typography.labelMedium)
                                    Slider(
                                        value = imageQualitySlider,
                                        onValueChange = { imageQualitySlider = it },
                                        valueRange = 10f..100f,
                                        steps = 18
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Format", style = MaterialTheme.typography.labelMedium)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(ImageFormatOption.entries) { opt ->
                                            FilterChip(
                                                selected = selectedFormat == opt,
                                                onClick = {
                                                    haptics.tap()
                                                    selectedFormat = opt
                                                },
                                                label = { Text(opt.name) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }

                                ToolType.DOCUMENT_SCANNER -> {
                                    Text("Enhancement Filter", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(ScanFilter.entries) { opt ->
                                            FilterChip(
                                                selected = scanFilter == opt,
                                                onClick = {
                                                    haptics.tap()
                                                    scanFilter = opt
                                                },
                                                label = { Text(opt.title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }

                                ToolType.PDF_PROTECT -> {
                                    OutlinedTextField(
                                        value = protectPasswordText,
                                        onValueChange = { protectPasswordText = it },
                                        label = { Text("Set Password") },
                                        visualTransformation = if (isProtectPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { isProtectPasswordVisible = !isProtectPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isProtectPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = CircleShape,
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = confirmPasswordText,
                                        onValueChange = { confirmPasswordText = it },
                                        label = { Text("Confirm Password") },
                                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isConfirmPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = CircleShape,
                                        singleLine = true
                                    )
                                }

                                ToolType.PDF_ROTATE -> {
                                    Text("Rotate Clockwise", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(listOf(90, 180, 270)) { angle ->
                                            FilterChip(
                                                selected = selectedRotationAngle == angle,
                                                onClick = {
                                                    haptics.tap()
                                                    selectedRotationAngle = angle
                                                },
                                                label = { Text("${angle}°") },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }

                                ToolType.PDF_EXTRACT_TEXT -> {
                                    Text(
                                        text = "Extracts all selectable text into a clean .txt file without altering the original PDF.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                ToolType.PDF_WATERMARK -> {
                                    OutlinedTextField(
                                        value = watermarkText,
                                        onValueChange = { watermarkText = it },
                                        label = { Text("Watermark Text") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = CircleShape,
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Quick Presets", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(listOf("CONFIDENTIAL", "DRAFT", "DO NOT COPY", "SAMPLE", "INTERNAL")) { preset ->
                                            FilterChip(
                                                selected = watermarkText == preset,
                                                onClick = {
                                                    haptics.tap()
                                                    watermarkText = preset
                                                },
                                                label = { Text(preset) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Opacity: ${(watermarkOpacity * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                                    Slider(
                                        value = watermarkOpacity,
                                        onValueChange = { watermarkOpacity = it },
                                        valueRange = 0.10f..0.80f,
                                        steps = 7
                                    )
                                }

                                ToolType.IMAGE_FORMAT_CONVERTER -> {
                                    Text("Target Format", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(ImageFormatOption.JPG, ImageFormatOption.PNG, ImageFormatOption.WEBP).forEach { fmt ->
                                            FilterChip(
                                                selected = targetFormatOption == fmt,
                                                onClick = {
                                                    haptics.tap()
                                                    targetFormatOption = fmt
                                                },
                                                label = { Text(fmt.name) },
                                                shape = CircleShape
                                            )
                                        }
                                    }

                                    if (targetFormatOption != ImageFormatOption.PNG) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Quality: ${formatQualityPercent.toInt()}%", style = MaterialTheme.typography.labelMedium)
                                        Slider(
                                            value = formatQualityPercent,
                                            onValueChange = { formatQualityPercent = it },
                                            valueRange = 10f..100f,
                                            steps = 18
                                        )
                                    }
                                }

                                ToolType.IMAGE_EXIF_STRIPPER -> {
                                    if (isLoadingExif) {
                                        Text("Inspecting EXIF metadata...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    } else if (inspectedExif != null) {
                                        val exif = inspectedExif!!
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (exif.hasGps) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (exif.hasGps) "GPS Location Found" else "No GPS Location",
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (exif.hasGps) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                                    )
                                                    if (exif.hasGps && exif.formattedCoordinates.isNotBlank()) {
                                                        OutlinedButton(
                                                            onClick = {
                                                                haptics.tap()
                                                                copyTextToClipboard(exif.formattedCoordinates)
                                                            },
                                                            shape = CircleShape
                                                        ) {
                                                            Text("Copy GPS", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }

                                                if (exif.hasGps && exif.formattedCoordinates.isNotBlank()) {
                                                    Text(
                                                        text = "Coordinates: ${exif.formattedCoordinates}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                if (exif.cameraMake.isNotBlank() || exif.cameraModel.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Device: ${exif.cameraMake} ${exif.cameraModel}".trim(),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                if (exif.dateTimeOriginal.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Captured: ${exif.dateTimeOriginal}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                if (exif.allTags.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    OutlinedButton(
                                                        onClick = {
                                                            haptics.tap()
                                                            val allText = exif.allTags.joinToString("\n") { "[${it.category}] ${it.label}: ${it.value}" }
                                                            copyTextToClipboard(allText)
                                                        },
                                                        shape = CircleShape
                                                    ) {
                                                        Text("Copy All EXIF Info (${exif.allTags.size} tags)", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    Text("Stripping Mode", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = !stripOnlyGps,
                                            onClick = {
                                                haptics.tap()
                                                stripOnlyGps = false
                                            },
                                            label = { Text("Strip All Metadata") },
                                            shape = CircleShape
                                        )
                                        FilterChip(
                                            selected = stripOnlyGps,
                                            onClick = {
                                                haptics.tap()
                                                stripOnlyGps = true
                                            },
                                            label = { Text("Strip GPS Only") },
                                            shape = CircleShape
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (!stripOnlyGps) "Completely removes GPS, device fingerprints, camera models, and timestamps for 100% privacy." else "Removes GPS coordinates while keeping camera and exposure settings intact.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                ToolType.IMAGE_RESIZER -> {
                                    Text("Resize Mode", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = resizeMode == ResizeMode.PERCENTAGE,
                                            onClick = {
                                                haptics.tap()
                                                resizeMode = ResizeMode.PERCENTAGE
                                            },
                                            label = { Text("Percentage") },
                                            shape = CircleShape
                                        )
                                        FilterChip(
                                            selected = resizeMode == ResizeMode.DIMENSIONS,
                                            onClick = {
                                                haptics.tap()
                                                resizeMode = ResizeMode.DIMENSIONS
                                            },
                                            label = { Text("Dimensions") },
                                            shape = CircleShape
                                        )
                                        FilterChip(
                                            selected = resizeMode == ResizeMode.TARGET_FILE_SIZE,
                                            onClick = {
                                                haptics.tap()
                                                resizeMode = ResizeMode.TARGET_FILE_SIZE
                                            },
                                            label = { Text("Target Size (KB)") },
                                            shape = CircleShape
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    when (resizeMode) {
                                        ResizeMode.PERCENTAGE -> {
                                            Text("Scale: ${resizePercentage.toInt()}%", style = MaterialTheme.typography.labelMedium)
                                            Slider(
                                                value = resizePercentage,
                                                onValueChange = { resizePercentage = it },
                                                valueRange = 10f..200f,
                                                steps = 18
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                listOf(25f, 50f, 75f).forEach { pct ->
                                                    FilterChip(
                                                        selected = resizePercentage == pct,
                                                        onClick = {
                                                            haptics.tap()
                                                            resizePercentage = pct
                                                        },
                                                        label = { Text("${pct.toInt()}%") },
                                                        shape = CircleShape
                                                    )
                                                }
                                            }
                                        }

                                        ResizeMode.DIMENSIONS -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = resizeWidthPx,
                                                    onValueChange = { resizeWidthPx = it.filter { ch -> ch.isDigit() } },
                                                    label = { Text("Width (px)") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = CircleShape,
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                )
                                                OutlinedTextField(
                                                    value = resizeHeightPx,
                                                    onValueChange = { resizeHeightPx = it.filter { ch -> ch.isDigit() } },
                                                    label = { Text("Height (px)") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = CircleShape,
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                )
                                            }
                                        }

                                        ResizeMode.TARGET_FILE_SIZE -> {
                                            Text("Target File Size Preset", style = MaterialTheme.typography.labelMedium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(listOf("100", "200", "500", "1000", "custom")) { preset ->
                                                    FilterChip(
                                                        selected = targetKbPreset == preset,
                                                        onClick = {
                                                            haptics.tap()
                                                            targetKbPreset = preset
                                                        },
                                                        label = { Text(if (preset == "custom") "Custom" else if (preset == "1000") "1 MB" else "$preset KB") },
                                                        shape = CircleShape
                                                    )
                                                }
                                            }
                                            if (targetKbPreset == "custom") {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = customTargetKb,
                                                    onValueChange = { customTargetKb = it.filter { ch -> ch.isDigit() } },
                                                    label = { Text("Max Size in KB (e.g. 150)") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = CircleShape,
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Iteratively scales and optimizes image to stay strictly under the specified file size limit.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                ToolType.IMAGE_PALETTE_EXTRACTOR -> {
                                    if (isExtractingPalette) {
                                        Text("Analyzing image colors...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    } else if (extractedPalette.isNotEmpty()) {
                                        Text("Extracted Palette (${extractedPalette.size} colors)", style = MaterialTheme.typography.labelMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            extractedPalette.forEach { color ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            haptics.tap()
                                                            copyTextToClipboard(color.hex)
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(androidx.compose.ui.graphics.Color(color.red, color.green, color.blue))
                                                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = color.hex,
                                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                            )
                                                            Text(
                                                                text = "${color.rgb} • ${String.format(Locale.US, "%.1f", color.percentage)}%",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        Text("Copy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                haptics.tap()
                                                val allHex = extractedPalette.joinToString(", ") { it.hex }
                                                copyTextToClipboard(allHex)
                                            },
                                            shape = CircleShape,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Copy All Hex Codes")
                                        }
                                    }
                                }

                                ToolType.QR_BARCODE_GENERATOR -> {
                                    Text("Payload Type", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(QrPayloadType.entries) { type ->
                                            FilterChip(
                                                selected = qrPayloadType == type,
                                                onClick = {
                                                    haptics.tap()
                                                    qrPayloadType = type
                                                },
                                                label = { Text(type.title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    when (qrPayloadType) {
                                        QrPayloadType.URL -> {
                                            OutlinedTextField(
                                                value = qrInputUrl,
                                                onValueChange = { qrInputUrl = it },
                                                label = { Text("Website URL") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true
                                            )
                                        }

                                        QrPayloadType.WIFI -> {
                                            OutlinedTextField(
                                                value = qrInputWifiSsid,
                                                onValueChange = { qrInputWifiSsid = it },
                                                label = { Text("Network SSID (Name)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = qrInputWifiPass,
                                                onValueChange = { qrInputWifiPass = it },
                                                label = { Text("Password") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Security Type", style = MaterialTheme.typography.labelSmall)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                listOf("WPA", "WEP", "OPEN").forEach { sec ->
                                                    FilterChip(
                                                        selected = qrInputWifiSecurity == sec,
                                                        onClick = {
                                                            haptics.tap()
                                                            qrInputWifiSecurity = sec
                                                        },
                                                        label = { Text(if (sec == "WPA") "WPA/WPA2/WPA3" else sec) },
                                                        shape = CircleShape
                                                    )
                                                }
                                            }
                                        }

                                        QrPayloadType.UPI -> {
                                            OutlinedTextField(
                                                value = qrInputUpiId,
                                                onValueChange = { qrInputUpiId = it },
                                                label = { Text("Payee UPI ID (e.g. merchant@upi)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = qrInputUpiName,
                                                onValueChange = { qrInputUpiName = it },
                                                label = { Text("Payee / Business Name") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = qrInputUpiAmount,
                                                    onValueChange = { qrInputUpiAmount = it },
                                                    label = { Text("Amount (₹ Optional)") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = CircleShape,
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                )
                                                OutlinedTextField(
                                                    value = qrInputUpiNote,
                                                    onValueChange = { qrInputUpiNote = it },
                                                    label = { Text("Note (Optional)") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = CircleShape,
                                                    singleLine = true
                                                )
                                            }
                                        }

                                        QrPayloadType.VCARD -> {
                                            OutlinedTextField(
                                                value = qrInputVcardName,
                                                onValueChange = { qrInputVcardName = it },
                                                label = { Text("Full Name") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = qrInputVcardPhone,
                                                onValueChange = { qrInputVcardPhone = it },
                                                label = { Text("Phone Number") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = qrInputVcardEmail,
                                                onValueChange = { qrInputVcardEmail = it },
                                                label = { Text("Email Address") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = qrInputVcardCompany,
                                                onValueChange = { qrInputVcardCompany = it },
                                                label = { Text("Organization / Company") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true
                                            )
                                        }

                                        QrPayloadType.TEXT -> {
                                            OutlinedTextField(
                                                value = qrInputText,
                                                onValueChange = { qrInputText = it },
                                                label = { Text("Custom Text or Note") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                minLines = 3,
                                                maxLines = 6
                                            )
                                        }

                                        QrPayloadType.EMAIL -> {
                                            OutlinedTextField(
                                                value = qrInputEmail,
                                                onValueChange = { qrInputEmail = it },
                                                label = { Text("Recipient Email") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                            )
                                        }

                                        QrPayloadType.PHONE -> {
                                            OutlinedTextField(
                                                value = qrInputPhone,
                                                onValueChange = { qrInputPhone = it },
                                                label = { Text("Phone Number to Dial") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = CircleShape,
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("QR Code Color", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val qrColors = listOf(
                                        "#000000" to "Black",
                                        "#0284C7" to "Blue",
                                        "#059669" to "Emerald",
                                        "#4F46E5" to "Indigo",
                                        "#9333EA" to "Purple",
                                        "#DC2626" to "Red",
                                        "#EA580C" to "Orange"
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(qrColors) { (hex, name) ->
                                            FilterChip(
                                                selected = qrFgColorHex.equals(hex, ignoreCase = true),
                                                onClick = {
                                                    haptics.tap()
                                                    qrFgColorHex = hex
                                                },
                                                label = { Text(name) },
                                                shape = CircleShape
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Error Correction", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(
                                            ErrorCorrectionLevel.L to "L (~7%)",
                                            ErrorCorrectionLevel.M to "M (~15%)",
                                            ErrorCorrectionLevel.Q to "Q (~25%)",
                                            ErrorCorrectionLevel.H to "H (~30%)"
                                        ).forEach { (level, title) ->
                                            FilterChip(
                                                selected = qrErrorCorrection == level,
                                                onClick = {
                                                    haptics.tap()
                                                    qrErrorCorrection = level
                                                },
                                                label = { Text(title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }

                                ToolType.QR_BARCODE_SCANNER -> {
                                    Text(
                                        text = "Point camera at any QR Code, UPC barcode, EAN-13, or select an image from your gallery.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                ToolType.SECURITY_SCANNER -> {
                                    Text("Threat Intelligence Service", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(SecurityServiceType.entries) { srv ->
                                            FilterChip(
                                                selected = securityService == srv,
                                                onClick = {
                                                    haptics.tap()
                                                    securityService = srv
                                                    keyTestMessage = null
                                                },
                                                label = { Text(srv.title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = securityService.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (securityService == SecurityServiceType.VIRUSTOTAL) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val activeKey = if (customApiKeyInput.isNotBlank()) customApiKeyInput.trim() else savedVtApiKey.trim()
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (activeKey.isNotBlank()) "VirusTotal API Key (Active)" else "VirusTotal API Key (Required)",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (activeKey.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                            FilledTonalButton(
                                                onClick = {
                                                    haptics.tap()
                                                    showSetupGuide = !showSetupGuide
                                                },
                                                shape = CircleShape,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 28.dp)
                                            ) {
                                                Text(if (showSetupGuide) "Hide Guide" else "How to get Free Key?", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }

                                        if (showSetupGuide) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text("How to get a Free VirusTotal Key:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                                    Text("1. Sign up for a free account at virustotal.com", style = MaterialTheme.typography.bodySmall)
                                                    Text("2. Click your profile avatar (top-right) & choose 'API key'", style = MaterialTheme.typography.bodySmall)
                                                    Text("3. Copy your 64-character Personal API key & paste below", style = MaterialTheme.typography.bodySmall)
                                                    Text("4. Tap 'Test & Save Key'. Free tier includes 500 scans/day!", style = MaterialTheme.typography.bodySmall)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Button(
                                                        onClick = {
                                                            haptics.tap()
                                                            try {
                                                                uriHandler.openUri("https://www.virustotal.com/gui/join-us")
                                                            } catch (_: Exception) {}
                                                        },
                                                        shape = CircleShape,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Open VirusTotal Sign Up", maxLines = 1)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = if (customApiKeyInput.isNotBlank()) customApiKeyInput else savedVtApiKey,
                                            onValueChange = {
                                                customApiKeyInput = it
                                                keyTestMessage = null
                                            },
                                            placeholder = { Text("Paste 64-char VirusTotal API Key...") },
                                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            trailingIcon = {
                                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                                    Icon(
                                                        imageVector = if (isApiKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                        contentDescription = null
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            FilledTonalButton(
                                                onClick = {
                                                    haptics.tap()
                                                    isTestingKey = true
                                                    keyTestMessage = null
                                                    scope.launch {
                                                        try {
                                                            val engine = SecurityScannerEngine(context, storageManager)
                                                            val key = if (customApiKeyInput.isNotBlank()) customApiKeyInput.trim() else savedVtApiKey.trim()
                                                            val msg = engine.testApiKey(SecurityServiceType.VIRUSTOTAL, key)
                                                            preferencesManager.setVirusTotalApiKey(key)
                                                            keyTestMessage = msg
                                                        } catch (e: Exception) {
                                                            keyTestMessage = "Error: ${e.localizedMessage}"
                                                        } finally {
                                                            isTestingKey = false
                                                        }
                                                    }
                                                },
                                                shape = CircleShape,
                                                modifier = Modifier.weight(1f),
                                                enabled = (customApiKeyInput.isNotBlank() || savedVtApiKey.isNotBlank()) && !isTestingKey
                                            ) {
                                                if (isTestingKey) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                } else {
                                                    Text("Test & Save Key")
                                                }
                                            }
                                        }

                                        if (keyTestMessage != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = keyTestMessage!!,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (keyTestMessage!!.startsWith("Error")) MaterialTheme.colorScheme.error else androidx.compose.ui.graphics.Color(0xFF16A34A)
                                            )
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Upload & Scan If Hash Unknown", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                                Text("Upload file to VirusTotal cloud if hash has no prior scan (Up to 32MB)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Switch(
                                                checked = allowUploadScan,
                                                onCheckedChange = {
                                                    haptics.tap()
                                                    allowUploadScan = it
                                                }
                                            )
                                        }
                                    } else if (securityService == SecurityServiceType.HYBRID_ANALYSIS) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val activeKey = if (customApiKeyInput.isNotBlank()) customApiKeyInput.trim() else savedHaApiKey.trim()
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (activeKey.isNotBlank()) "Hybrid Analysis Key (Active)" else "Hybrid Analysis Key (Required)",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (activeKey.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                            FilledTonalButton(
                                                onClick = {
                                                    haptics.tap()
                                                    showSetupGuide = !showSetupGuide
                                                },
                                                shape = CircleShape,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 28.dp)
                                            ) {
                                                Text(if (showSetupGuide) "Hide Guide" else "How to get Key?", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }

                                        if (showSetupGuide) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text("How to get a Free Hybrid Analysis Key:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                                    Text("1. Sign up at hybrid-analysis.com/signup", style = MaterialTheme.typography.bodySmall)
                                                    Text("2. Go to Profile & settings &rarr; API key", style = MaterialTheme.typography.bodySmall)
                                                    Text("3. Copy API key and paste below", style = MaterialTheme.typography.bodySmall)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Button(
                                                        onClick = {
                                                            haptics.tap()
                                                            try {
                                                                uriHandler.openUri("https://www.hybrid-analysis.com/signup")
                                                            } catch (_: Exception) {}
                                                        },
                                                        shape = CircleShape,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Open Hybrid Analysis Signup", maxLines = 1)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = if (customApiKeyInput.isNotBlank()) customApiKeyInput else savedHaApiKey,
                                            onValueChange = {
                                                customApiKeyInput = it
                                                keyTestMessage = null
                                            },
                                            placeholder = { Text("Paste Hybrid Analysis API Key...") },
                                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            trailingIcon = {
                                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                                    Icon(
                                                        imageVector = if (isApiKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                        contentDescription = null
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            FilledTonalButton(
                                                onClick = {
                                                    haptics.tap()
                                                    isTestingKey = true
                                                    keyTestMessage = null
                                                    scope.launch {
                                                        try {
                                                            val engine = SecurityScannerEngine(context, storageManager)
                                                            val key = if (customApiKeyInput.isNotBlank()) customApiKeyInput.trim() else savedHaApiKey.trim()
                                                            val msg = engine.testApiKey(SecurityServiceType.HYBRID_ANALYSIS, key)
                                                            preferencesManager.setHybridAnalysisApiKey(key)
                                                            keyTestMessage = msg
                                                        } catch (e: Exception) {
                                                            keyTestMessage = "Error: ${e.localizedMessage}"
                                                        } finally {
                                                            isTestingKey = false
                                                        }
                                                    }
                                                },
                                                shape = CircleShape,
                                                modifier = Modifier.weight(1f),
                                                enabled = (customApiKeyInput.isNotBlank() || savedHaApiKey.isNotBlank()) && !isTestingKey
                                            ) {
                                                if (isTestingKey) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                } else {
                                                    Text("Test & Save Key")
                                                }
                                            }
                                        }

                                        if (keyTestMessage != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = keyTestMessage!!,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (keyTestMessage!!.startsWith("Error")) MaterialTheme.colorScheme.error else androidx.compose.ui.graphics.Color(0xFF16A34A)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Open Community Database", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "MalwareBazaar by Abuse.ch is 100% free and open to the public. Instant signature lookups work immediately with zero API key configuration.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    Text("Default local processing pipeline configured.", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                val isQrGen = tool == ToolType.QR_BARCODE_GENERATOR
                val isSecurity = tool == ToolType.SECURITY_SCANNER
                val hasSecurityKey = if (isSecurity) {
                    if (securityService == SecurityServiceType.VIRUSTOTAL) {
                        customApiKeyInput.isNotBlank() || savedVtApiKey.isNotBlank()
                    } else if (securityService == SecurityServiceType.HYBRID_ANALYSIS) {
                        customApiKeyInput.isNotBlank() || savedHaApiKey.isNotBlank()
                    } else true
                } else true

                val canSubmit = if (isQrGen) {
                    when (qrPayloadType) {
                        QrPayloadType.URL -> qrInputUrl.isNotBlank() && qrInputUrl != "https://"
                        QrPayloadType.WIFI -> qrInputWifiSsid.isNotBlank()
                        QrPayloadType.UPI -> qrInputUpiId.isNotBlank()
                        QrPayloadType.VCARD -> qrInputVcardName.isNotBlank()
                        QrPayloadType.TEXT -> qrInputText.isNotBlank()
                        QrPayloadType.EMAIL -> qrInputEmail.isNotBlank()
                        QrPayloadType.PHONE -> qrInputPhone.isNotBlank()
                    } && !isProcessing
                } else if (isSecurity) {
                    selectedFiles.isNotEmpty() && hasSecurityKey && !isProcessing
                } else {
                    selectedFiles.isNotEmpty() && !isProcessing
                }

                item {
                    Button(
                        onClick = {
                            haptics.tap()
                            startProcessing()
                        },
                        enabled = canSubmit,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isQrGen) "Export QR Code (PNG)"
                                else if (isSecurity) {
                                    if (selectedFiles.isEmpty()) "Select File First"
                                    else if (!hasSecurityKey) "Enter API Key to Scan"
                                    else "Scan File Security"
                                }
                                else if (selectedFiles.isEmpty()) "Select Files First"
                                else "Start Processing",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
