package com.fileflow.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.fileflow.app.ui.components.FloatingBottomNavBar
import com.fileflow.app.ui.components.NavScreen
import com.fileflow.app.ui.screens.history.HistoryScreen
import com.fileflow.app.ui.screens.home.HomeScreen
import com.fileflow.app.ui.screens.processing.ToolExecutionScreen
import com.fileflow.app.ui.screens.settings.ChangelogScreen
import com.fileflow.app.ui.screens.settings.SettingsScreen
import com.fileflow.app.ui.screens.tools.ToolsScreen
import com.fileflow.app.ui.theme.FileFlowTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var storageManager: StorageManager
    private lateinit var historyRepository: HistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(applicationContext)
        storageManager = StorageManager(applicationContext)
        historyRepository = HistoryRepository(applicationContext)

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
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
    val scope = rememberCoroutineScope()

    val defaultSaveUri by preferencesManager.defaultSaveFolderUri.collectAsState(initial = null)
    val defaultFolderName by preferencesManager.defaultSaveFolderName.collectAsState(initial = null)
    val namingPrefix by preferencesManager.fileNamingPrefix.collectAsState(initial = "FileFlow")
    val askBeforeReplace by preferencesManager.askBeforeReplace.collectAsState(initial = true)
    val autoDeleteTemp by preferencesManager.autoDeleteTemp.collectAsState(initial = true)
    val keepScreenAwake by preferencesManager.keepScreenAwake.collectAsState(initial = false)

    val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
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
    val favoriteToolIds by preferencesManager.favoriteTools.collectAsState(initial = setOf("image_to_pdf", "pdf_compressor", "document_scanner"))
    val recentToolIds by preferencesManager.recentTools.collectAsState(initial = emptyList())
    val historyItems by historyRepository.historyFlow.collectAsState(initial = emptyList())

    var currentScreen by remember { mutableStateOf(NavScreen.HOME) }
    var activeTool by remember { mutableStateOf<ToolType?>(null) }
    var isViewingChangelog by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (activeTool == null && !isViewingChangelog && floatingNavBar) {
                FloatingBottomNavBar(
                    currentRoute = currentScreen.route,
                    onNavigate = { currentScreen = it }
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
                    activeTool != null -> activeTool = null
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
                        storageManager = storageManager,
                        historyRepository = historyRepository,
                        onBack = { activeTool = null }
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
                                historyRepository = historyRepository
                            )
                        }
                        NavScreen.SETTINGS -> {
                            SettingsScreen(
                                folderName = defaultFolderName,
                                namingPrefix = namingPrefix,
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
                                onOpenChangelog = { isViewingChangelog = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
