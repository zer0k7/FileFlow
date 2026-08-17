package com.salik.fileflow.ui.screens.processing

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salik.fileflow.core.engine.docx.DocxToPdfEngine
import com.salik.fileflow.core.engine.docx.PdfToDocxEngine
import com.salik.fileflow.core.engine.image.ImageCompressorEngine
import com.salik.fileflow.core.engine.pdf.ImageToPdfEngine
import com.salik.fileflow.core.engine.pdf.PdfCompressorEngine
import com.salik.fileflow.core.engine.pdf.PdfMergeEngine
import com.salik.fileflow.core.engine.pdf.PdfPasswordEngine
import com.salik.fileflow.core.engine.pdf.PdfSplitEngine
import com.salik.fileflow.core.engine.pdf.PdfToImagesEngine
import com.salik.fileflow.core.engine.scanner.DocumentScannerEngine
import com.salik.fileflow.core.engine.scanner.ScanFilter
import com.salik.fileflow.core.history.HistoryRepository
import com.salik.fileflow.core.model.CompressionLevel
import com.salik.fileflow.core.model.ImageFormatOption
import com.salik.fileflow.core.model.ImageQualityOption
import com.salik.fileflow.core.model.OrientationOption
import com.salik.fileflow.core.model.PageSizeOption
import com.salik.fileflow.core.model.ProcessResult
import com.salik.fileflow.core.model.SelectedFile
import com.salik.fileflow.core.model.ToolType
import com.salik.fileflow.core.saf.StorageManager
import com.salik.fileflow.ui.components.FloatingTopAppBar
import com.salik.fileflow.ui.components.ProcessProgressBar
import com.salik.fileflow.ui.components.ResultCard
import com.salik.fileflow.ui.components.getToolIcon
import com.salik.fileflow.ui.theme.ToolCardShape
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ToolExecutionScreen(
    tool: ToolType,
    defaultSaveUri: String?,
    namingPrefix: String,
    askBeforeReplace: Boolean,
    storageManager: StorageManager,
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFiles by remember { mutableStateOf<List<SelectedFile>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(0) }
    var totalSteps by remember { mutableIntStateOf(1) }
    var progressStatus by remember { mutableStateOf("Processing...") }
    var processResult by remember { mutableStateOf<ProcessResult?>(null) }

    // Tool options
    var selectedPageSize by remember { mutableStateOf(PageSizeOption.A4) }
    var selectedOrientation by remember { mutableStateOf(OrientationOption.AUTO) }
    var selectedQuality by remember { mutableStateOf(ImageQualityOption.HIGH) }
    var selectedFormat by remember { mutableStateOf(ImageFormatOption.JPG) }
    var selectedCompression by remember { mutableStateOf(CompressionLevel.RECOMMENDED) }
    var passwordText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var pageRangeText by remember { mutableStateOf("1") }
    var splitAllPages by remember { mutableStateOf(false) }
    var imageQualitySlider by remember { mutableFloatStateOf(80f) }
    var scanFilter by remember { mutableStateOf(ScanFilter.MAGIC_COLOR) }

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = storageManager.getFileName(uri)
            val size = storageManager.getFileSize(uri)
            selectedFiles = listOf(SelectedFile(uri, name, size, tool.inputMimeTypes.firstOrNull() ?: "*/*"))
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
        if (selectedFiles.isEmpty()) return
        isProcessing = true
        processResult = null
        currentStep = 0
        totalSteps = selectedFiles.size

        scope.launch {
            try {
                when (tool) {
                    ToolType.IMAGE_TO_PDF -> {
                        val engine = ImageToPdfEngine(context, storageManager)
                        val outputFile = engine.convert(
                            imageUris = selectedFiles.map { it.uri },
                            pageSize = selectedPageSize,
                            orientation = selectedOrientation,
                            qualityPercent = selectedQuality.qualityPercent,
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Rendering page $c of $t"
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
                        val file = engine.merge(
                            pdfUris = selectedFiles.map { it.uri },
                            onProgress = { c, t ->
                                currentStep = c
                                totalSteps = t
                                progressStatus = "Merging file $c of $t"
                            }
                        )
                        val name = storageManager.generateFileName("${namingPrefix}_Merged", "pdf")
                        val uri = storageManager.saveToTarget(file, name, "application/pdf", defaultSaveUri, askBeforeReplace)
                        historyRepository.recordItem(tool, "${selectedFiles.size} PDFs", name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "Merged ${selectedFiles.size} PDF files into one."
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

                        selectedFiles.forEachIndexed { idx, fileItem ->
                            currentStep = idx + 1
                            totalSteps = selectedFiles.size
                            progressStatus = "Filtering scan ${idx + 1} of ${selectedFiles.size}"
                            val bmp = engine.applyFilter(fileItem.uri, scanFilter)
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
                        historyRepository.recordItem(tool, "${selectedFiles.size} scans", name, uri.toString(), file.length(), true)
                        processResult = ProcessResult(
                            success = true,
                            outputUris = listOf(uri),
                            outputFilenames = listOf(name),
                            outputTotalBytes = file.length(),
                            message = "Scanned document saved as PDF."
                        )
                    }
                }
            } catch (e: Exception) {
                processResult = ProcessResult(
                    success = false,
                    message = e.localizedMessage ?: "An error occurred during processing."
                )
            } finally {
                isProcessing = false
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

    Column(modifier = modifier.fillMaxSize()) {
        FloatingTopAppBar(
            title = tool.title,
            subtitle = tool.category.title,
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                        }
                    )
                }
            } else if (isProcessing) {
                item {
                    ProcessProgressBar(
                        currentStep = currentStep,
                        totalSteps = totalSteps,
                        statusText = progressStatus,
                        onCancel = { isProcessing = false }
                    )
                }
            } else {
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
                                verticalAlignment = Alignment.CenterVertCenter
                            ) {
                                Text(
                                    text = if (selectedFiles.isEmpty()) "Select Input" else "Selected Files (${selectedFiles.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedButton(
                                    onClick = { openFilePicker() },
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = if (selectedFiles.isEmpty()) Icons.Rounded.FileOpen else Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (selectedFiles.isEmpty()) "Browse Files" else "Add More")
                                }
                            }

                            if (selectedFiles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                selectedFiles.forEachIndexed { index, file ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertCenter
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
                                                selectedFiles = selectedFiles.filterIndexed { i, _ -> i != index }
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
                                                onClick = { selectedPageSize = opt },
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
                                                onClick = { selectedOrientation = opt },
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
                                                onClick = { selectedQuality = opt },
                                                label = { Text(opt.title) },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }

                                ToolType.PDF_TO_IMAGES -> {
                                    Text("Output Image Format", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(ImageFormatOption.entries) { opt ->
                                            FilterChip(
                                                selected = selectedFormat == opt,
                                                onClick = { selectedFormat = opt },
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
                                                onClick = { selectedCompression = opt },
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
                                        verticalAlignment = Alignment.CenterVertCenter
                                    ) {
                                        Text("Extract all pages individually", style = MaterialTheme.typography.bodyMedium)
                                        Switch(
                                            checked = splitAllPages,
                                            onCheckedChange = { splitAllPages = it }
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
                                                onClick = { selectedFormat = opt },
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
                                                onClick = { scanFilter = opt },
                                                label = { Text(opt.title) },
                                                shape = CircleShape
                                            )
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

                item {
                    Button(
                        onClick = { startProcessing() },
                        enabled = selectedFiles.isNotEmpty() && !isProcessing,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedFiles.isEmpty()) "Select Files First" else "Start Processing",
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
