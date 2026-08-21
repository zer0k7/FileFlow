package com.fileflow.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fileflow.app.core.model.AccentColorMode
import com.fileflow.app.core.model.CompressionLevel
import com.fileflow.app.core.model.ImageFormatOption
import com.fileflow.app.core.model.ImageQualityOption
import com.fileflow.app.core.model.OrientationOption
import com.fileflow.app.core.model.PageSizeOption
import com.fileflow.app.core.model.ThemeMode
import com.fileflow.app.core.model.UiDensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fileflow_settings")

class PreferencesManager(private val context: Context) {

    private val dataStore = context.dataStore

    object Keys {
        val DEFAULT_SAVE_FOLDER_URI = stringPreferencesKey("default_save_folder_uri")
        val DEFAULT_SAVE_FOLDER_NAME = stringPreferencesKey("default_save_folder_name")
        val FILE_NAMING_PREFIX = stringPreferencesKey("file_naming_prefix")
        val FILENAME_TEMPLATE = stringPreferencesKey("filename_template")
        val ASK_BEFORE_REPLACE = booleanPreferencesKey("ask_before_replace")
        val AUTO_DELETE_TEMP = booleanPreferencesKey("auto_delete_temp")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")

        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val CUSTOM_ACCENT_HEX = stringPreferencesKey("custom_accent_hex")
        val UI_DENSITY = stringPreferencesKey("ui_density")
        val FLOATING_NAV_BAR = booleanPreferencesKey("floating_nav_bar")
        val FLOATING_TOP_BAR = booleanPreferencesKey("floating_top_bar")
        val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")

        val IMAGE_QUALITY = stringPreferencesKey("image_quality")
        val PDF_COMPRESSION = stringPreferencesKey("pdf_compression")
        val DEFAULT_IMAGE_FORMAT = stringPreferencesKey("default_image_format")
        val DEFAULT_PAGE_SIZE = stringPreferencesKey("default_page_size")
        val DEFAULT_ORIENTATION = stringPreferencesKey("default_orientation")

        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")

        val FAVORITE_TOOLS = stringPreferencesKey("favorite_tools")
        val RECENT_TOOLS = stringPreferencesKey("recent_tools")
        val ACKNOWLEDGED_VERSION = intPreferencesKey("acknowledged_version")

        val VIRUSTOTAL_API_KEY = stringPreferencesKey("virustotal_api_key")
        val HYBRID_ANALYSIS_API_KEY = stringPreferencesKey("hybrid_analysis_api_key")
        val SELECTED_SECURITY_SERVICE = stringPreferencesKey("selected_security_service")
    }

    val defaultSaveFolderUri: Flow<String?> = dataStore.data.map { it[Keys.DEFAULT_SAVE_FOLDER_URI] }
    val defaultSaveFolderName: Flow<String?> = dataStore.data.map { it[Keys.DEFAULT_SAVE_FOLDER_NAME] }
    val fileNamingPrefix: Flow<String> = dataStore.data.map { it[Keys.FILE_NAMING_PREFIX] ?: "FileFlow" }
    val filenameTemplate: Flow<String> = dataStore.data.map { it[Keys.FILENAME_TEMPLATE] ?: "{PREFIX}_{DATE}_{TIME}" }
    val askBeforeReplace: Flow<Boolean> = dataStore.data.map { it[Keys.ASK_BEFORE_REPLACE] ?: true }
    val autoDeleteTemp: Flow<Boolean> = dataStore.data.map { it[Keys.AUTO_DELETE_TEMP] ?: true }
    val keepScreenAwake: Flow<Boolean> = dataStore.data.map { it[Keys.KEEP_SCREEN_AWAKE] ?: false }

    val themeMode: Flow<ThemeMode> = dataStore.data.map {
        try {
            ThemeMode.valueOf(it[Keys.THEME_MODE] ?: ThemeMode.LIGHT.name)
        } catch (_: Exception) {
            ThemeMode.LIGHT
        }
    }

    val accentColor: Flow<AccentColorMode> = dataStore.data.map {
        try {
            AccentColorMode.valueOf(it[Keys.ACCENT_COLOR] ?: AccentColorMode.BLUE.name)
        } catch (_: Exception) {
            AccentColorMode.BLUE
        }
    }

    val customAccentHex: Flow<String> = dataStore.data.map { it[Keys.CUSTOM_ACCENT_HEX] ?: "#0284C7" }

    val uiDensity: Flow<UiDensity> = dataStore.data.map {
        try {
            UiDensity.valueOf(it[Keys.UI_DENSITY] ?: UiDensity.COMFORTABLE.name)
        } catch (_: Exception) {
            UiDensity.COMFORTABLE
        }
    }

    val floatingNavBar: Flow<Boolean> = dataStore.data.map { it[Keys.FLOATING_NAV_BAR] ?: true }
    val floatingTopBar: Flow<Boolean> = dataStore.data.map { it[Keys.FLOATING_TOP_BAR] ?: true }
    val reduceAnimations: Flow<Boolean> = dataStore.data.map { it[Keys.REDUCE_ANIMATIONS] ?: false }
    val hapticFeedback: Flow<Boolean> = dataStore.data.map { it[Keys.HAPTIC_FEEDBACK] ?: true }

    val imageQuality: Flow<ImageQualityOption> = dataStore.data.map {
        try {
            ImageQualityOption.valueOf(it[Keys.IMAGE_QUALITY] ?: ImageQualityOption.HIGH.name)
        } catch (_: Exception) {
            ImageQualityOption.HIGH
        }
    }

    val pdfCompression: Flow<CompressionLevel> = dataStore.data.map {
        try {
            CompressionLevel.valueOf(it[Keys.PDF_COMPRESSION] ?: CompressionLevel.RECOMMENDED.name)
        } catch (_: Exception) {
            CompressionLevel.RECOMMENDED
        }
    }

    val defaultImageFormat: Flow<ImageFormatOption> = dataStore.data.map {
        try {
            ImageFormatOption.valueOf(it[Keys.DEFAULT_IMAGE_FORMAT] ?: ImageFormatOption.JPG.name)
        } catch (_: Exception) {
            ImageFormatOption.JPG
        }
    }

    val defaultPageSize: Flow<PageSizeOption> = dataStore.data.map {
        try {
            PageSizeOption.valueOf(it[Keys.DEFAULT_PAGE_SIZE] ?: PageSizeOption.A4.name)
        } catch (_: Exception) {
            PageSizeOption.A4
        }
    }

    val defaultOrientation: Flow<OrientationOption> = dataStore.data.map {
        try {
            OrientationOption.valueOf(it[Keys.DEFAULT_ORIENTATION] ?: OrientationOption.AUTO.name)
        } catch (_: Exception) {
            OrientationOption.AUTO
        }
    }

    val appLockEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.APP_LOCK_ENABLED] ?: false }
    val appLockPin: Flow<String?> = dataStore.data.map { it[Keys.APP_LOCK_PIN] }

    val favoriteTools: Flow<Set<String>> = dataStore.data.map {
        val raw = it[Keys.FAVORITE_TOOLS] ?: ""
        if (raw.isBlank()) setOf("image_to_pdf", "pdf_compressor", "document_scanner")
        else raw.split(",").filter { s -> s.isNotBlank() }.toSet()
    }

    val recentTools: Flow<List<String>> = dataStore.data.map {
        val raw = it[Keys.RECENT_TOOLS] ?: ""
        if (raw.isBlank()) emptyList()
        else raw.split(",").filter { s -> s.isNotBlank() }
    }

    val acknowledgedVersion: Flow<Int> = dataStore.data.map { it[Keys.ACKNOWLEDGED_VERSION] ?: 0 }

    suspend fun setDefaultSaveFolder(uri: String, name: String) {
        dataStore.edit {
            it[Keys.DEFAULT_SAVE_FOLDER_URI] = uri
            it[Keys.DEFAULT_SAVE_FOLDER_NAME] = name
        }
    }

    suspend fun clearDefaultSaveFolder() {
        dataStore.edit {
            it.remove(Keys.DEFAULT_SAVE_FOLDER_URI)
            it.remove(Keys.DEFAULT_SAVE_FOLDER_NAME)
        }
    }

    suspend fun setFileNamingPrefix(prefix: String) {
        dataStore.edit { it[Keys.FILE_NAMING_PREFIX] = prefix }
    }

    suspend fun setFilenameTemplate(template: String) {
        dataStore.edit { it[Keys.FILENAME_TEMPLATE] = template }
    }

    suspend fun setAskBeforeReplace(enabled: Boolean) {
        dataStore.edit { it[Keys.ASK_BEFORE_REPLACE] = enabled }
    }

    suspend fun setAutoDeleteTemp(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_DELETE_TEMP] = enabled }
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        dataStore.edit { it[Keys.KEEP_SCREEN_AWAKE] = enabled }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColor(accent: AccentColorMode) {
        dataStore.edit { it[Keys.ACCENT_COLOR] = accent.name }
    }

    suspend fun setCustomAccentHex(hex: String) {
        dataStore.edit { it[Keys.CUSTOM_ACCENT_HEX] = hex }
    }

    suspend fun setUiDensity(density: UiDensity) {
        dataStore.edit { it[Keys.UI_DENSITY] = density.name }
    }

    suspend fun setFloatingNavBar(enabled: Boolean) {
        dataStore.edit { it[Keys.FLOATING_NAV_BAR] = enabled }
    }

    suspend fun setFloatingTopBar(enabled: Boolean) {
        dataStore.edit { it[Keys.FLOATING_TOP_BAR] = enabled }
    }

    suspend fun setReduceAnimations(enabled: Boolean) {
        dataStore.edit { it[Keys.REDUCE_ANIMATIONS] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setImageQuality(quality: ImageQualityOption) {
        dataStore.edit { it[Keys.IMAGE_QUALITY] = quality.name }
    }

    suspend fun setPdfCompression(level: CompressionLevel) {
        dataStore.edit { it[Keys.PDF_COMPRESSION] = level.name }
    }

    suspend fun setDefaultImageFormat(format: ImageFormatOption) {
        dataStore.edit { it[Keys.DEFAULT_IMAGE_FORMAT] = format.name }
    }

    suspend fun setDefaultPageSize(size: PageSizeOption) {
        dataStore.edit { it[Keys.DEFAULT_PAGE_SIZE] = size.name }
    }

    suspend fun setDefaultOrientation(orientation: OrientationOption) {
        dataStore.edit { it[Keys.DEFAULT_ORIENTATION] = orientation.name }
    }

    suspend fun setAppLock(enabled: Boolean, pin: String? = null) {
        dataStore.edit {
            it[Keys.APP_LOCK_ENABLED] = enabled
            if (pin != null) it[Keys.APP_LOCK_PIN] = pin
            else if (!enabled) it.remove(Keys.APP_LOCK_PIN)
        }
    }

    suspend fun toggleFavoriteTool(toolId: String) {
        dataStore.edit {
            val raw = it[Keys.FAVORITE_TOOLS] ?: "image_to_pdf,pdf_compressor,document_scanner"
            val current = raw.split(",").filter { s -> s.isNotBlank() }.toMutableSet()
            if (current.contains(toolId)) {
                current.remove(toolId)
            } else {
                current.add(toolId)
            }
            it[Keys.FAVORITE_TOOLS] = current.joinToString(",")
        }
    }

    suspend fun recordRecentTool(toolId: String) {
        dataStore.edit {
            val raw = it[Keys.RECENT_TOOLS] ?: ""
            val list = raw.split(",").filter { s -> s.isNotBlank() && s != toolId }.toMutableList()
            list.add(0, toolId)
            it[Keys.RECENT_TOOLS] = list.take(8).joinToString(",")
        }
    }

    val virusTotalApiKey: Flow<String> = dataStore.data.map { it[Keys.VIRUSTOTAL_API_KEY] ?: "" }
    val hybridAnalysisApiKey: Flow<String> = dataStore.data.map { it[Keys.HYBRID_ANALYSIS_API_KEY] ?: "" }
    val selectedSecurityService: Flow<String> = dataStore.data.map { it[Keys.SELECTED_SECURITY_SERVICE] ?: "VIRUSTOTAL" }

    suspend fun setAcknowledgedVersion(versionCode: Int) {
        dataStore.edit { it[Keys.ACKNOWLEDGED_VERSION] = versionCode }
    }

    suspend fun setVirusTotalApiKey(apiKey: String) {
        dataStore.edit { it[Keys.VIRUSTOTAL_API_KEY] = apiKey.trim() }
    }

    suspend fun setHybridAnalysisApiKey(apiKey: String) {
        dataStore.edit { it[Keys.HYBRID_ANALYSIS_API_KEY] = apiKey.trim() }
    }

    suspend fun setSelectedSecurityService(service: String) {
        dataStore.edit { it[Keys.SELECTED_SECURITY_SERVICE] = service }
    }
}
