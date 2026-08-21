package com.fileflow.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.fileflow.app.core.engine.security.SecurityScannerEngine
import com.fileflow.app.core.model.SecurityServiceType
import com.fileflow.app.core.datastore.PreferencesManager
import com.fileflow.app.core.history.HistoryRepository
import com.fileflow.app.core.model.AccentColorMode
import com.fileflow.app.core.model.CompressionLevel
import com.fileflow.app.core.model.ImageFormatOption
import com.fileflow.app.core.model.ImageQualityOption
import com.fileflow.app.core.model.OrientationOption
import com.fileflow.app.core.model.PageSizeOption
import com.fileflow.app.core.model.ThemeMode
import com.fileflow.app.core.model.UiDensity
import com.fileflow.app.core.saf.StorageManager
import com.fileflow.app.ui.components.FloatingTopAppBar
import com.fileflow.app.ui.components.rememberAppHaptics
import com.fileflow.app.ui.theme.ToolCardShape
import com.fileflow.app.ui.theme.getAccentColor
import com.fileflow.app.core.auth.BiometricAuthManager
import com.fileflow.app.core.saf.NamingTemplateEngine
import com.fileflow.app.ui.components.StorageDashboardCard
import androidx.fragment.app.FragmentActivity

@Composable
fun SettingsScreen(
    folderName: String?,
    namingPrefix: String,
    filenameTemplate: String,
    askBeforeReplace: Boolean,
    autoDeleteTemp: Boolean,
    keepScreenAwake: Boolean,
    themeMode: ThemeMode,
    accentColor: AccentColorMode,
    customAccentHex: String,
    uiDensity: UiDensity,
    floatingNavBar: Boolean,
    floatingTopBar: Boolean,
    reduceAnimations: Boolean,
    hapticFeedback: Boolean,
    imageQuality: ImageQualityOption,
    pdfCompression: CompressionLevel,
    defaultImageFormat: ImageFormatOption,
    defaultPageSize: PageSizeOption,
    defaultOrientation: OrientationOption,
    appLockEnabled: Boolean,
    preferencesManager: PreferencesManager,
    storageManager: StorageManager,
    historyRepository: HistoryRepository,
    onPickFolder: () -> Unit,
    onOpenChangelog: () -> Unit,
    onCheckForUpdates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberAppHaptics()

    var showNamingDialog by remember { mutableStateOf(false) }
    var tempPrefixText by remember { mutableStateOf(namingPrefix) }

    var showTemplateDialog by remember { mutableStateOf(false) }
    var tempTemplateText by remember { mutableStateOf(filenameTemplate) }

    var showCustomColorDialog by remember { mutableStateOf(false) }
    var tempCustomHex by remember { mutableStateOf(customAccentHex) }

    val uriHandler = LocalUriHandler.current
    val vtApiKey by preferencesManager.virusTotalApiKey.collectAsState(initial = "")
    val haApiKey by preferencesManager.hybridAnalysisApiKey.collectAsState(initial = "")

    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showSecurityKeysDialog by remember { mutableStateOf(false) }
    var tempVtKey by remember { mutableStateOf("") }
    var tempHaKey by remember { mutableStateOf("") }
    var isKeyTesting by remember { mutableStateOf(false) }
    var keyTestFeedback by remember { mutableStateOf<String?>(null) }
    var isVtKeyVisible by remember { mutableStateOf(false) }
    var showGuideInDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        FloatingTopAppBar(
            title = "Settings",
            subtitle = "Preferences & appearance",
            isFloating = floatingTopBar
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage & Analytics Card
            item {
                StorageDashboardCard(
                    storageManager = storageManager,
                    onCleanTemp = {
                        Toast.makeText(context, "Temporary cache cleared", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Core Settings Section
            item {
                SettingsSectionHeader("Core Settings", Icons.Rounded.Tune)
            }

            item {
                Surface(
                    shape = ToolCardShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SettingsClickableRow(
                            title = "Default Save Folder",
                            subtitle = if (folderName.isNullOrBlank()) "Tap to select save folder" else folderName,
                            icon = Icons.Rounded.FolderOpen,
                            onClick = onPickFolder
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "File Naming Prefix",
                            subtitle = namingPrefix,
                            icon = Icons.AutoMirrored.Filled.Article,
                            onClick = {
                                tempPrefixText = namingPrefix
                                showNamingDialog = true
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Filename Template",
                            subtitle = filenameTemplate,
                            icon = Icons.Rounded.EditNote,
                            onClick = {
                                tempTemplateText = filenameTemplate
                                showTemplateDialog = true
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "Ask Before Replacing Files",
                            subtitle = "Resolve filename collisions safely",
                            checked = askBeforeReplace,
                            onCheckedChange = { scope.launch { preferencesManager.setAskBeforeReplace(it) } }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "Auto-Delete Temporary Files",
                            subtitle = "Clean cache automatically after conversion",
                            checked = autoDeleteTemp,
                            onCheckedChange = { scope.launch { preferencesManager.setAutoDeleteTemp(it) } }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "Keep Screen Awake",
                            subtitle = "Prevent display sleep during long conversions",
                            checked = keepScreenAwake,
                            onCheckedChange = { scope.launch { preferencesManager.setKeepScreenAwake(it) } }
                        )
                    }
                }
            }

            // Appearance Section
            item {
                SettingsSectionHeader("Appearance", Icons.Rounded.Palette)
            }

            item {
                Surface(
                    shape = ToolCardShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Theme Mode", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ThemeMode.entries) { mode ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = {
                                        haptics.tap()
                                        scope.launch { preferencesManager.setThemeMode(mode) }
                                    },
                                    label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    shape = CircleShape
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        Text("Accent Color", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(AccentColorMode.entries) { accent ->
                                val color = getAccentColor(accent, customAccentHex)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable {
                                            haptics.tap()
                                            if (accent == AccentColorMode.CUSTOM) {
                                                showCustomColorDialog = true
                                            } else {
                                                scope.launch { preferencesManager.setAccentColor(accent) }
                                            }
                                        }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (accentColor == accent) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "Floating Navigation Bar",
                            subtitle = "Floating rounded bottom nav bar",
                            checked = floatingNavBar,
                            onCheckedChange = { scope.launch { preferencesManager.setFloatingNavBar(it) } }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "Floating Top Bar",
                            subtitle = "Floating rounded header bar",
                            checked = floatingTopBar,
                            onCheckedChange = { scope.launch { preferencesManager.setFloatingTopBar(it) } }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "Reduce Animations",
                            subtitle = "Minimize interface transitions",
                            checked = reduceAnimations,
                            onCheckedChange = { scope.launch { preferencesManager.setReduceAnimations(it) } }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "Haptic Feedback",
                            subtitle = "Tactile feedback on button presses",
                            checked = hapticFeedback,
                            onCheckedChange = {
                                if (it) haptics.performDirectHeavy()
                                scope.launch { preferencesManager.setHapticFeedback(it) }
                            }
                        )
                    }
                }
            }

            // Processing Section
            item {
                SettingsSectionHeader("Default Processing Settings", Icons.Rounded.SettingsSuggest)
            }

            item {
                Surface(
                    shape = ToolCardShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Default Image Quality", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ImageQualityOption.entries) { opt ->
                                FilterChip(
                                    selected = imageQuality == opt,
                                    onClick = {
                                        haptics.tap()
                                        scope.launch { preferencesManager.setImageQuality(opt) }
                                    },
                                    label = { Text(opt.title) },
                                    shape = CircleShape
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        Text("Default PDF Compression", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CompressionLevel.entries) { opt ->
                                FilterChip(
                                    selected = pdfCompression == opt,
                                    onClick = {
                                        haptics.tap()
                                        scope.launch { preferencesManager.setPdfCompression(opt) }
                                    },
                                    label = { Text(opt.title) },
                                    shape = CircleShape
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        Text("Default Page Size", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PageSizeOption.entries) { opt ->
                                FilterChip(
                                    selected = defaultPageSize == opt,
                                    onClick = {
                                        haptics.tap()
                                        scope.launch { preferencesManager.setDefaultPageSize(opt) }
                                    },
                                    label = { Text(opt.title) },
                                    shape = CircleShape
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        Text("Default Image Format", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ImageFormatOption.entries) { opt ->
                                FilterChip(
                                    selected = defaultImageFormat == opt,
                                    onClick = {
                                        haptics.tap()
                                        scope.launch { preferencesManager.setDefaultImageFormat(opt) }
                                    },
                                    label = { Text(opt.name) },
                                    shape = CircleShape
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        Text("Default Orientation", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(OrientationOption.entries) { opt ->
                                FilterChip(
                                    selected = defaultOrientation == opt,
                                    onClick = {
                                        haptics.tap()
                                        scope.launch { preferencesManager.setDefaultOrientation(opt) }
                                    },
                                    label = { Text(opt.title) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }

            // Privacy Section
            item {
                SettingsSectionHeader("Privacy & Security", Icons.Rounded.PrivacyTip)
            }

            item {
                Surface(
                    shape = ToolCardShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SettingsClickableRow(
                            title = "100% Offline Processing",
                            subtitle = "No accounts, no cloud uploads, zero telemetry",
                            icon = Icons.Rounded.Security,
                            onClick = { showPrivacyPolicyDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Clear Temporary Cache",
                            subtitle = "Delete cached processing scratch files",
                            icon = Icons.Rounded.CleaningServices,
                            onClick = {
                                storageManager.clearTempFiles()
                                Toast.makeText(context, "Temporary files deleted", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Clear Recent History",
                            subtitle = "Delete all history logs from device",
                            icon = Icons.Rounded.DeleteOutline,
                            onClick = { showClearHistoryDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsToggleRow(
                            title = "App Lock",
                            subtitle = "Protect FileFlow with fingerprint / device PIN",
                            checked = appLockEnabled,
                            onCheckedChange = { enable ->
                                val activity = context as? FragmentActivity
                                if (enable && activity != null) {
                                    if (BiometricAuthManager.isBiometricAvailable(activity)) {
                                        BiometricAuthManager.authenticate(
                                            activity = activity,
                                            title = "Enable App Lock",
                                            subtitle = "Verify your identity to secure FileFlow",
                                            onSuccess = {
                                                haptics.heavyTap()
                                                scope.launch { preferencesManager.setAppLock(true) }
                                                Toast.makeText(context, "App Lock enabled", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, "Authentication failed: $err", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(context, "Biometrics or device lock not set up on device", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    scope.launch { preferencesManager.setAppLock(false) }
                                    Toast.makeText(context, "App Lock disabled", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Security & VirusTotal API Keys",
                            subtitle = if (vtApiKey.isNotBlank()) "VirusTotal API key active" else "Configure free API keys for malware scanning",
                            icon = Icons.Rounded.VpnKey,
                            onClick = {
                                tempVtKey = vtApiKey
                                tempHaKey = haApiKey
                                keyTestFeedback = null
                                showSecurityKeysDialog = true
                            }
                        )
                    }
                }
            }

            // About Section
            item {
                SettingsSectionHeader("About FileFlow", Icons.Rounded.Info)
            }

            item {
                Surface(
                    shape = ToolCardShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SettingsClickableRow(
                            title = "Version",
                            subtitle = "v${try { context.packageManager.getPackageInfo(context.packageName, 0).versionName } catch (_: Exception) { "1.1.0" }} (Production Release)",
                            icon = Icons.Rounded.Info,
                            onClick = onOpenChangelog
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Check for Updates",
                            subtitle = "Check GitHub for the latest version",
                            icon = Icons.Rounded.SettingsSuggest,
                            onClick = onCheckForUpdates
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Changelog & Release Notes",
                            subtitle = "What's new in this version",
                            icon = Icons.AutoMirrored.Filled.List,
                            onClick = onOpenChangelog
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Open-Source Licenses",
                            subtitle = "PDFBox, Jetpack Compose, Coil, Material 3",
                            icon = Icons.AutoMirrored.Filled.Article,
                            onClick = { showLicensesDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "GitHub Repository",
                            subtitle = "Open source code on GitHub",
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zer0k7/FileFlow"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Report a Bug / Feature Request",
                            subtitle = "Submit feedback or issue report",
                            icon = Icons.Rounded.BugReport,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zer0k7/FileFlow/issues"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialogs
    if (showNamingDialog) {
        AlertDialog(
            onDismissRequest = { showNamingDialog = false },
            title = { Text("File Naming Prefix") },
            text = {
                OutlinedTextField(
                    value = tempPrefixText,
                    onValueChange = { tempPrefixText = it },
                    label = { Text("Prefix (e.g., FileFlow)") },
                    shape = CircleShape,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    haptics.tap()
                    scope.launch { preferencesManager.setFileNamingPrefix(tempPrefixText) }
                    showNamingDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    haptics.tap()
                    showNamingDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = { Text("Filename Template") },
            text = {
                Column {
                    Text(
                        text = "Customize how output files are automatically named. Tap tags to insert.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempTemplateText,
                        onValueChange = { tempTemplateText = it },
                        label = { Text("Template Pattern") },
                        shape = CircleShape,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Available Tags:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("{PREFIX}", "{TOOL}", "{DATE}", "{TIME}", "{ORIGINAL}")) { tag ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    haptics.tap()
                                    tempTemplateText = if (tempTemplateText.isBlank()) tag else "${tempTemplateText}_$tag"
                                },
                                label = { Text(tag, fontSize = 11.sp) },
                                shape = CircleShape
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val previewSample = NamingTemplateEngine.generateName(
                        template = tempTemplateText,
                        prefix = namingPrefix,
                        toolName = "Sample",
                        originalName = "Document",
                        extension = "pdf"
                    )
                    Text(
                        text = "Preview: $previewSample",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptics.tap()
                    scope.launch { preferencesManager.setFilenameTemplate(tempTemplateText) }
                    showTemplateDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    haptics.tap()
                    showTemplateDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCustomColorDialog) {
        AlertDialog(
            onDismissRequest = { showCustomColorDialog = false },
            title = { Text("Custom Accent Hex") },
            text = {
                OutlinedTextField(
                    value = tempCustomHex,
                    onValueChange = { tempCustomHex = it },
                    label = { Text("Hex Color (e.g., #0284C7)") },
                    shape = CircleShape,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    haptics.tap()
                    scope.launch {
                        preferencesManager.setCustomAccentHex(tempCustomHex)
                        preferencesManager.setAccentColor(AccentColorMode.CUSTOM)
                    }
                    showCustomColorDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    haptics.tap()
                    showCustomColorDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy & Guarantee") },
            text = {
                Text(
                    "FileFlow is built with strict privacy-by-design:\n\n" +
                            "• Zero Cloud Processing: All document, PDF, and image operations are executed entirely offline on your device.\n\n" +
                            "• Zero Tracking: No analytics, advertising SDKs, crash trackers, or telemetry.\n\n" +
                            "• No Password Storage: Passwords used for unlocking PDFs are held in memory only for the operation and are never stored or logged.\n\n" +
                            "• Clean Sandbox: Temporary scratch files are automatically deleted after processing."
                )
            },
            confirmButton = {
                Button(onClick = {
                    haptics.tap()
                    showPrivacyPolicyDialog = false
                }) {
                    Text("Close")
                }
            }
        )
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Licenses") },
            text = {
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    item {
                        Text("• Apache PDFBox Android (Apache-2.0)\n• AndroidX & Jetpack Compose (Apache-2.0)\n• Kotlin Coroutines (Apache-2.0)\n• Android DataStore (Apache-2.0)\n• Coil (Apache-2.0)\n\nFileFlow is open source software distributed under the Apache 2.0 License.")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptics.tap()
                    showLicensesDialog = false
                }) {
                    Text("Done")
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History?") },
            text = { Text("Are you sure you want to clear your local processing history?") },
            confirmButton = {
                Button(onClick = {
                    haptics.heavyTap()
                    scope.launch { historyRepository.clearHistory() }
                    showClearHistoryDialog = false
                    Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    haptics.tap()
                    showClearHistoryDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSecurityKeysDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityKeysDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Security API Keys")
                }
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text(
                            text = "VirusTotal provides 70+ antivirus engines for scanning APKs, PDFs, and files. Free accounts include 500 scans/day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("VirusTotal API Key", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            FilledTonalButton(
                                onClick = {
                                    haptics.tap()
                                    showGuideInDialog = !showGuideInDialog
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 24.dp)
                            ) {
                                Text(if (showGuideInDialog) "Hide Guide" else "Setup Guide", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (showGuideInDialog) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("1. Sign up free at virustotal.com", style = MaterialTheme.typography.labelSmall)
                                    Text("2. Click profile avatar (top-right) & choose 'API key'", style = MaterialTheme.typography.labelSmall)
                                    Text("3. Copy your 64-char Personal API key & paste below", style = MaterialTheme.typography.labelSmall)
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
                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open VirusTotal Sign Up", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = tempVtKey,
                            onValueChange = {
                                tempVtKey = it
                                keyTestFeedback = null
                            },
                            placeholder = { Text("Paste 64-char VirusTotal Key...") },
                            visualTransformation = if (isVtKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isVtKeyVisible = !isVtKeyVisible }) {
                                    Icon(
                                        imageVector = if (isVtKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    haptics.tap()
                                    isKeyTesting = true
                                    keyTestFeedback = null
                                    scope.launch {
                                        try {
                                            val engine = SecurityScannerEngine(context, storageManager)
                                            val msg = engine.testApiKey(SecurityServiceType.VIRUSTOTAL, tempVtKey)
                                            preferencesManager.setVirusTotalApiKey(tempVtKey)
                                            keyTestFeedback = msg
                                        } catch (e: Exception) {
                                            keyTestFeedback = "Error: ${e.localizedMessage}"
                                        } finally {
                                            isKeyTesting = false
                                        }
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = tempVtKey.isNotBlank() && !isKeyTesting
                            ) {
                                if (isKeyTesting) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Test & Save VirusTotal Key")
                                }
                            }
                        }

                        if (keyTestFeedback != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = keyTestFeedback!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (keyTestFeedback!!.startsWith("Error")) MaterialTheme.colorScheme.error else Color(0xFF16A34A)
                            )
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        Text("Hybrid Analysis Key (Optional)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = tempHaKey,
                            onValueChange = { tempHaKey = it },
                            placeholder = { Text("Paste Hybrid Analysis Key...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "• MalwareBazaar is open and does not require an API key.\n• API keys are stored locally on your device only.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptics.tap()
                    scope.launch {
                        preferencesManager.setVirusTotalApiKey(tempVtKey)
                        preferencesManager.setHybridAnalysisApiKey(tempHaKey)
                    }
                    showSecurityKeysDialog = false
                    Toast.makeText(context, "API keys saved", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Save & Close")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    haptics.tap()
                    showSecurityKeysDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val haptics = rememberAppHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptics.tap()
                onClick()
            }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val haptics = rememberAppHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                haptics.tap()
                onCheckedChange(it)
            }
        )
    }
}
