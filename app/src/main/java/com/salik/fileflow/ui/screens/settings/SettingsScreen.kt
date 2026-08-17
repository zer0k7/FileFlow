package com.salik.fileflow.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salik.fileflow.core.datastore.PreferencesManager
import com.salik.fileflow.core.history.HistoryRepository
import com.salik.fileflow.core.model.AccentColorMode
import com.salik.fileflow.core.model.CompressionLevel
import com.salik.fileflow.core.model.ImageFormatOption
import com.salik.fileflow.core.model.ImageQualityOption
import com.salik.fileflow.core.model.OrientationOption
import com.salik.fileflow.core.model.PageSizeOption
import com.salik.fileflow.core.model.ThemeMode
import com.salik.fileflow.core.model.UiDensity
import com.salik.fileflow.core.saf.StorageManager
import com.salik.fileflow.ui.components.FloatingTopAppBar
import com.salik.fileflow.ui.theme.ToolCardShape
import com.salik.fileflow.ui.theme.getAccentColor
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    folderName: String?,
    namingPrefix: String,
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showNamingDialog by remember { mutableStateOf(false) }
    var tempPrefixText by remember { mutableStateOf(namingPrefix) }

    var showCustomColorDialog by remember { mutableStateOf(false) }
    var tempCustomHex by remember { mutableStateOf(customAccentHex) }

    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        FloatingTopAppBar(
            title = "Settings",
            subtitle = "Preferences & appearance"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                            icon = Icons.AutoMirrored.rounded.Article,
                            onClick = {
                                tempPrefixText = namingPrefix
                                showNamingDialog = true
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
                                    onClick = { scope.launch { preferencesManager.setThemeMode(mode) } },
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
                            onCheckedChange = { scope.launch { preferencesManager.setHapticFeedback(it) } }
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
                                    onClick = { scope.launch { preferencesManager.setImageQuality(opt) } },
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
                                    onClick = { scope.launch { preferencesManager.setPdfCompression(opt) } },
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
                                    onClick = { scope.launch { preferencesManager.setDefaultPageSize(opt) } },
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
                            subtitle = "Protect FileFlow with device PIN/Biometrics",
                            checked = appLockEnabled,
                            onCheckedChange = { scope.launch { preferencesManager.setAppLock(it) } }
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
                            subtitle = "1.0.0 (Production Release)",
                            icon = Icons.Rounded.Info,
                            onClick = onOpenChangelog
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Changelog & Release Notes",
                            subtitle = "What's new in this version",
                            icon = Icons.AutoMirrored.rounded.List,
                            onClick = onOpenChangelog
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "Open-Source Licenses",
                            subtitle = "PDFBox, Jetpack Compose, Coil, Material 3",
                            icon = Icons.AutoMirrored.rounded.Article,
                            onClick = { showLicensesDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsClickableRow(
                            title = "GitHub Repository",
                            subtitle = "Open source code on GitHub",
                            icon = Icons.AutoMirrored.rounded.HelpOutline,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
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
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
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
                    scope.launch { preferencesManager.setFileNamingPrefix(tempPrefixText) }
                    showNamingDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNamingDialog = false }) {
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
                OutlinedButton(onClick = { showCustomColorDialog = false }) {
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
                Button(onClick = { showPrivacyPolicyDialog = false }) {
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
                Button(onClick = { showLicensesDialog = false }) {
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
                    scope.launch { historyRepository.clearHistory() }
                    showClearHistoryDialog = false
                    Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertCenter,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertCenter
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
            imageVector = Icons.AutoMirrored.rounded.KeyboardArrowRight,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertCenter,
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
            onCheckedChange = onCheckedChange
        )
    }
}
