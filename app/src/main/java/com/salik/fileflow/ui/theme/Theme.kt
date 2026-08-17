package com.salik.fileflow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.salik.fileflow.core.model.AccentColorMode
import com.salik.fileflow.core.model.ThemeMode

@Composable
fun FileFlowTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColorMode: AccentColorMode = AccentColorMode.BLUE,
    customAccentHex: String = "#0284C7",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }

    val colorScheme = when {
        accentColorMode == AccentColorMode.SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) {
                val base = dynamicDarkColorScheme(context)
                if (themeMode == ThemeMode.AMOLED) {
                    base.copy(
                        background = AmoledBackground,
                        surface = AmoledSurface,
                        surfaceVariant = AmoledSurfaceVariant
                    )
                } else base
            } else {
                dynamicLightColorScheme(context)
            }
        }
        else -> {
            val accentColor = getAccentColor(accentColorMode, customAccentHex)
            generateCustomColorScheme(accentColor, themeMode, systemDark)
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
