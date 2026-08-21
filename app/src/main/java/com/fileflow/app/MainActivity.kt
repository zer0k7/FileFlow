package com.fileflow.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.fileflow.app.core.datastore.PreferencesManager
import com.fileflow.app.core.history.HistoryRepository
import com.fileflow.app.core.model.AccentColorMode
import com.fileflow.app.core.model.CompressionLevel
import com.fileflow.app.core.model.HistoryItem
import com.fileflow.app.core.model.ImageFormatOption
import com.fileflow.app.core.model.ImageQualityOption
import com.fileflow.app.core.model.OrientationOption
import com.fileflow.app.core.model.PageSizeOption
import com.fileflow.app.core.model.ThemeMode
import com.fileflow.app.core.model.ToolType
import com.fileflow.app.core.model.UiDensity
import com.fileflow.app.core.saf.StorageManager
import com.fileflow.app.core.updater.AppUpdateInfo
import com.fileflow.app.core.updater.GitHubAppUpdater
import com.fileflow.app.ui.components.FloatingBottomNavBar
import com.fileflow.app.ui.components.LocalHapticEnabled
import com.fileflow.app.ui.components.NavScreen
import com.fileflow.app.ui.components.UpdateDialog
import com.fileflow.app.ui.screens.history.HistoryScreen
import com.fileflow.app.ui.screens.home.HomeScreen
import com.fileflow.app.ui.screens.processing.ToolExecutionScreen
import com.fileflow.app.ui.screens.settings.ChangelogScreen
import com.fileflow.app.ui.screens.settings.SettingsScreen
import com.fileflow.app.ui.screens.tools.ToolsScreen
import com.fileflow.app.ui.theme.FileFlowTheme
import kotlinx.coroutines.launch

import androidx.fragment.app.FragmentActivity
import com.fileflow.app.core.auth.BiometricAuthManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.fileflow.app.core.model.SelectedFile
import com.fileflow.app.ui.components.getToolIcon
import com.fileflow.app.ui.theme.ToolCardShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextOverflow

class MainActivity : FragmentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var storageManager: StorageManager
    private lateinit var historyRepository: HistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(applicationContext)
        storageManager = StorageManager(applicationContext)
        historyRepository = HistoryRepository(applicationContext)

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.LIGHT)
            val accentColor by preferencesManager.accentColor.collectAsState(initial = AccentColorMode.BLUE)
            val customAccentHex by preferencesManager.customAccentHex.collectAsState(initial = "#0284C7")
            val keepScreenAwake by preferencesManager.keepScreenAwake.collectAsState(initial = false)

            LaunchedEffect(keepScreenAwake) {
                if (keepScreenAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            LaunchedEffect(Unit) {
                historyRepository.init()
            }

            FileFlowTheme(
                themeMode = themeMode,
                accentColorMode = accentColor,
                customAccentHex = customAccentHex
            ) {
                MainAppContainer(
                    preferencesManager = preferencesManager,
                    storageManager = storageManager,
                    historyRepository = historyRepository
                )
            }
        }
    }
}

@Composable
fun MainAppContainer(
    preferencesManager: PreferencesManager,
    storageManager: StorageManager,
    historyRepository: HistoryRepository
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()
    val appUpdater = remember { GitHubAppUpdater(context, "zer0k7/FileFlow") }

    val defaultSaveUri by preferencesManager.defaultSaveFolderUri.collectAsState(initial = null)
    val defaultFolderName by preferencesManager.defaultSaveFolderName.collectAsState(initial = null)
    val namingPrefix by preferencesManager.fileNamingPrefix.collectAsState(initial = "FileFlow")
    val filenameTemplate by preferencesManager.filenameTemplate.collectAsState(initial = "{PREFIX}_{DATE}_{TIME}")
    val askBeforeReplace by preferencesManager.askBeforeReplace.collectAsState(initial = true)
    val autoDeleteTemp by preferencesManager.autoDeleteTemp.collectAsState(initial = true)
    val keepScreenAwake by preferencesManager.keepScreenAwake.collectAsState(initial = false)

    val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.LIGHT)
    val accentColor by preferencesManager.accentColor.collectAsState(initial = AccentColorMode.BLUE)
    val customAccentHex by preferencesManager.customAccentHex.collectAsState(initial = "#0284C7")
    val uiDensity by preferencesManager.uiDensity.collectAsState(initial = UiDensity.COMFORTABLE)
    val floatingNavBar by preferencesManager.floatingNavBar.collectAsState(initial = true)
    val floatingTopBar by preferencesManager.floatingTopBar.collectAsState(initial = true)
    val reduceAnimations by preferencesManager.reduceAnimations.collectAsState(initial = false)
    val hapticFeedback by preferencesManager.hapticFeedback.collectAsState(initial = true)

    val imageQuality by preferencesManager.imageQuality.collectAsState(initial = ImageQualityOption.HIGH)
    val pdfCompression by preferencesManager.pdfCompression.collectAsState(initial = CompressionLevel.RECOMMENDED)
    val defaultImageFormat by preferencesManager.defaultImageFormat.collectAsState(initial = ImageFormatOption.JPG)
    val defaultPageSize by preferencesManager.defaultPageSize.collectAsState(initial = PageSizeOption.A4)
    val defaultOrientation by preferencesManager.defaultOrientation.collectAsState(initial = OrientationOption.AUTO)

    val appLockEnabled by preferencesManager.appLockEnabled.collectAsState(initial = false)
    var isUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(appLockEnabled) {
        if (appLockEnabled && !isUnlocked && activity != null) {
            BiometricAuthManager.authenticate(
                activity = activity,
                onSuccess = { isUnlocked = true },
                onError = { }
            )
        }
    }
    val favoriteToolIds by preferencesManager.favoriteTools.collectAsState(initial = setOf("image_to_pdf", "pdf_compressor", "document_scanner"))
    val recentToolIds by preferencesManager.recentTools.collectAsState(initial = emptyList())
    val historyItems by historyRepository.historyFlow.collectAsState(initial = emptyList())

    var currentScreen by remember { mutableStateOf(NavScreen.HOME) }
    var activeTool by remember { mutableStateOf<ToolType?>(null) }
    var isViewingChangelog by remember { mutableStateOf(false) }

    // Direct Share state
    var sharedIncomingFiles by remember { mutableStateOf<List<SelectedFile>>(emptyList()) }
    var showSharedToolChooser by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val intent = activity?.intent
        if (intent != null) {
            val action = intent.action
            val type = intent.type ?: "*/*"
            val uris = mutableListOf<Uri>()

            if (action == Intent.ACTION_SEND) {
                val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                } ?: intent.data
                if (uri != null) uris.add(uri)
            } else if (action == Intent.ACTION_SEND_MULTIPLE) {
                val list = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                }
                if (list != null) uris.addAll(list)
            }

            if (uris.isNotEmpty()) {
                val files = uris.map { uri ->
                    val name = storageManager.getFileName(uri)
                    val size = storageManager.getFileSize(uri)
                    val mime = context.contentResolver.getType(uri) ?: type
                    SelectedFile(uri, name, size, mime)
                }
                sharedIncomingFiles = files
                showSharedToolChooser = true
            }
        }
    }

    // In-App Update state
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var downloadedBytes by remember { mutableLongStateOf(0L) }
    var totalBytes by remember { mutableLongStateOf(0L) }

    // Silently check for update in background on launch
    LaunchedEffect(Unit) {
        val info = appUpdater.checkForUpdate()
        if (info != null && info.isAvailable) {
            updateInfo = info
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            storageManager.persistFolderPermission(uri)
            val folderName = uri.lastPathSegment ?: "Selected Folder"
            scope.launch {
                preferencesManager.setDefaultSaveFolder(uri.toString(), folderName)
            }
        }
    }

    CompositionLocalProvider(LocalHapticEnabled provides hapticFeedback) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (activeTool == null && !isViewingChangelog) {
                    FloatingBottomNavBar(
                        currentRoute = currentScreen.route,
                        onNavigate = { currentScreen = it },
                        isFloating = floatingNavBar
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                BackHandler(enabled = isViewingChangelog || activeTool != null || currentScreen != NavScreen.HOME) {
                    when {
                        isViewingChangelog -> isViewingChangelog = false
                        activeTool != null -> {
                            activeTool = null
                            sharedIncomingFiles = emptyList()
                        }
                        currentScreen != NavScreen.HOME -> currentScreen = NavScreen.HOME
                    }
                }

                when {
                    isViewingChangelog -> {
                        ChangelogScreen(
                            onBack = { isViewingChangelog = false }
                        )
                    }
                    activeTool != null -> {
                        ToolExecutionScreen(
                            tool = activeTool!!,
                            defaultSaveUri = defaultSaveUri,
                            namingPrefix = namingPrefix,
                            askBeforeReplace = askBeforeReplace,
                            defaultQuality = imageQuality,
                            defaultPdfCompression = pdfCompression,
                            defaultFormat = defaultImageFormat,
                            defaultPageSize = defaultPageSize,
                            defaultOrientation = defaultOrientation,
                            autoDeleteTemp = autoDeleteTemp,
                            floatingTopBar = floatingTopBar,
                            initialFiles = sharedIncomingFiles,
                            storageManager = storageManager,
                            historyRepository = historyRepository,
                            onBack = {
                                activeTool = null
                                sharedIncomingFiles = emptyList()
                            }
                        )
                    }
                    else -> {
                        when (currentScreen) {
                            NavScreen.HOME -> {
                                HomeScreen(
                                    folderName = defaultFolderName,
                                    favoriteToolIds = favoriteToolIds,
                                    recentToolIds = recentToolIds,
                                    recentHistory = historyItems,
                                    floatingTopBar = floatingTopBar,
                                    onPickFolder = { folderPickerLauncher.launch(null) },
                                    onOpenTool = { tool ->
                                        scope.launch { preferencesManager.recordRecentTool(tool.id) }
                                        activeTool = tool
                                    },
                                    onToggleFavorite = { tool ->
                                        scope.launch { preferencesManager.toggleFavoriteTool(tool.id) }
                                    },
                                    onOpenHistoryFile = { item ->
                                        // Handled in history
                                    },
                                    onShareHistoryFile = { item ->
                                        // Handled in history
                                    }
                                )
                            }
                            NavScreen.TOOLS -> {
                                ToolsScreen(
                                    favoriteToolIds = favoriteToolIds,
                                    floatingTopBar = floatingTopBar,
                                    onOpenTool = { tool ->
                                        scope.launch { preferencesManager.recordRecentTool(tool.id) }
                                        activeTool = tool
                                    },
                                    onToggleFavorite = { tool ->
                                        scope.launch { preferencesManager.toggleFavoriteTool(tool.id) }
                                    }
                                )
                            }
                            NavScreen.HISTORY -> {
                                HistoryScreen(
                                    historyItems = historyItems,
                                    historyRepository = historyRepository,
                                    floatingTopBar = floatingTopBar
                                )
                            }
                            NavScreen.SETTINGS -> {
                                SettingsScreen(
                                    folderName = defaultFolderName,
                                    namingPrefix = namingPrefix,
                                    filenameTemplate = filenameTemplate,
                                    askBeforeReplace = askBeforeReplace,
                                    autoDeleteTemp = autoDeleteTemp,
                                    keepScreenAwake = keepScreenAwake,
                                    themeMode = themeMode,
                                    accentColor = accentColor,
                                    customAccentHex = customAccentHex,
                                    uiDensity = uiDensity,
                                    floatingNavBar = floatingNavBar,
                                    floatingTopBar = floatingTopBar,
                                    reduceAnimations = reduceAnimations,
                                    hapticFeedback = hapticFeedback,
                                    imageQuality = imageQuality,
                                    pdfCompression = pdfCompression,
                                    defaultImageFormat = defaultImageFormat,
                                    defaultPageSize = defaultPageSize,
                                    defaultOrientation = defaultOrientation,
                                    appLockEnabled = appLockEnabled,
                                    preferencesManager = preferencesManager,
                                    storageManager = storageManager,
                                    historyRepository = historyRepository,
                                    onPickFolder = { folderPickerLauncher.launch(null) },
                                    onOpenChangelog = { isViewingChangelog = true },
                                    onCheckForUpdates = {
                                        scope.launch {
                                            Toast.makeText(context, "Checking GitHub for updates...", Toast.LENGTH_SHORT).show()
                                            val info = appUpdater.checkForUpdate()
                                            if (info != null && info.isAvailable) {
                                                updateInfo = info
                                            } else {
                                                Toast.makeText(context, "You are using the latest version (v${appUpdater.getAppVersionName()})", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // App Lock Screen Overlay
                if (appLockEnabled && !isUnlocked) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "FileFlow is Locked",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Authentication required to access documents",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    if (activity != null) {
                                        BiometricAuthManager.authenticate(
                                            activity = activity,
                                            onSuccess = { isUnlocked = true },
                                            onError = { }
                                        )
                                    }
                                },
                                shape = CircleShape
                            ) {
                                Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unlock with Biometrics")
                            }
                        }
                    }
                }
            }

            // In-App Update Dialog
            if (updateInfo != null) {
                UpdateDialog(
                    updateInfo = updateInfo!!,
                    isDownloading = isDownloadingUpdate,
                    downloadProgress = downloadProgress,
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes,
                    onStartDownload = {
                        scope.launch {
                            isDownloadingUpdate = true
                            val info = updateInfo ?: return@launch
                            val apkFile = appUpdater.downloadApk(info.apkDownloadUrl, info.apkFileName) { percent, downloaded, total ->
                                downloadProgress = percent
                                downloadedBytes = downloaded
                                totalBytes = total
                            }
                            isDownloadingUpdate = false
                            if (apkFile != null) {
                                updateInfo = null
                                appUpdater.installApk(apkFile)
                            } else {
                                Toast.makeText(context, "Failed to download update APK", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDismiss = { updateInfo = null }
                )
            }

            // Direct Share Tool Chooser Dialog
            if (showSharedToolChooser && sharedIncomingFiles.isNotEmpty()) {
                val firstFile = sharedIncomingFiles.first()
                val isPdf = firstFile.name.endsWith(".pdf", ignoreCase = true) || firstFile.mimeType.contains("pdf")
                val isImage = firstFile.mimeType.startsWith("image/") || firstFile.name.endsWith(".jpg", ignoreCase = true) || firstFile.name.endsWith(".png", ignoreCase = true) || firstFile.name.endsWith(".webp", ignoreCase = true)

                val applicableTools = when {
                    isPdf -> listOf(
                        ToolType.SECURITY_SCANNER,
                        ToolType.PDF_COMPRESSOR,
                        ToolType.PDF_TO_IMAGES,
                        ToolType.PDF_TO_DOCX,
                        ToolType.PDF_SPLIT,
                        ToolType.PDF_EXTRACT_TEXT,
                        ToolType.OCR_TEXT_EXTRACTOR,
                        ToolType.PDF_SIGN_STAMP,
                        ToolType.PDF_PASSWORD_REMOVER,
                        ToolType.PDF_PROTECT,
                        ToolType.PDF_ROTATE,
                        ToolType.PDF_WATERMARK,
                        ToolType.PDF_METADATA_EDITOR,
                        ToolType.PDF_MERGE
                    )
                    isImage -> listOf(
                        ToolType.SECURITY_SCANNER,
                        ToolType.QR_BARCODE_SCANNER,
                        ToolType.IMAGE_FORMAT_CONVERTER,
                        ToolType.IMAGE_EXIF_STRIPPER,
                        ToolType.IMAGE_RESIZER,
                        ToolType.IMAGE_PALETTE_EXTRACTOR,
                        ToolType.IMAGE_TO_PDF,
                        ToolType.IMAGE_COMPRESSOR,
                        ToolType.DOCUMENT_SCANNER,
                        ToolType.OCR_TEXT_EXTRACTOR
                    )
                    else -> ToolType.entries
                }

                AlertDialog(
                    onDismissRequest = {
                        showSharedToolChooser = false
                        sharedIncomingFiles = emptyList()
                    },
                    title = {
                        Text("Process with FileFlow (${sharedIncomingFiles.size} file${if (sharedIncomingFiles.size > 1) "s" else ""})")
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(applicableTools) { toolItem ->
                                Surface(
                                    shape = ToolCardShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeTool = toolItem
                                            showSharedToolChooser = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = getToolIcon(toolItem.iconName),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = toolItem.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = toolItem.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        OutlinedButton(onClick = {
                            showSharedToolChooser = false
                            sharedIncomingFiles = emptyList()
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
