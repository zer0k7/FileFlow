package com.fileflow.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.fileflow.app.core.model.AccentColorMode
import com.fileflow.app.core.model.ThemeMode

val PrimaryBlue = Color(0xFF0284C7)
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkCard = Color(0xFF1E293B)

val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0B0F19)
val AmoledSurfaceVariant = Color(0xFF161F30)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)

fun getAccentColor(mode: AccentColorMode, customHex: String): Color {
    return when (mode) {
        AccentColorMode.SYSTEM -> PrimaryBlue
        AccentColorMode.BLUE -> Color(0xFF0284C7)
        AccentColorMode.INDIGO -> Color(0xFF4F46E5)
        AccentColorMode.PURPLE -> Color(0xFF9333EA)
        AccentColorMode.VIOLET -> Color(0xFF7C3AED)
        AccentColorMode.PINK -> Color(0xFFDB2777)
        AccentColorMode.RED -> Color(0xFFDC2626)
        AccentColorMode.ORANGE -> Color(0xFFEA580C)
        AccentColorMode.AMBER -> Color(0xFFD97706)
        AccentColorMode.GREEN -> Color(0xFF16A34A)
        AccentColorMode.TEAL -> Color(0xFF0D9488)
        AccentColorMode.CYAN -> Color(0xFF0891B2)
        AccentColorMode.LIME -> Color(0xFF65A30D)
        AccentColorMode.CUSTOM -> {
            try {
                val hex = customHex.removePrefix("#")
                Color(android.graphics.Color.parseColor("#$hex"))
            } catch (_: Exception) {
                PrimaryBlue
            }
        }
    }
}

fun generateCustomColorScheme(
    accent: Color,
    themeMode: ThemeMode,
    isSystemDark: Boolean
): ColorScheme {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val isAmoled = themeMode == ThemeMode.AMOLED

    return if (isDark) {
        val bg = if (isAmoled) AmoledBackground else DarkBackground
        val surf = if (isAmoled) AmoledSurface else DarkSurface
        val surfVar = if (isAmoled) AmoledSurfaceVariant else DarkSurfaceVariant
        val container = if (isAmoled) Color(0xFF0F172A) else Color(0xFF1E293B)
        val containerHigh = if (isAmoled) Color(0xFF162032) else Color(0xFF253347)

        darkColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = accent.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = accent.copy(alpha = 0.8f),
            onSecondary = Color.White,
            background = bg,
            onBackground = Color(0xFFF1F5F9),
            surface = surf,
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = surfVar,
            onSurfaceVariant = Color(0xFF94A3B8),
            surfaceContainer = container,
            surfaceContainerLow = if (isAmoled) Color(0xFF070A10) else Color(0xFF162032),
            surfaceContainerHigh = containerHigh,
            surfaceContainerHighest = if (isAmoled) Color(0xFF1E293B) else Color(0xFF334155),
            surfaceDim = if (isAmoled) AmoledBackground else Color(0xFF0F172A),
            surfaceBright = if (isAmoled) Color(0xFF1E293B) else Color(0xFF334155),
            outline = Color(0xFF475569),
            outlineVariant = Color(0xFF334155)
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = accent.copy(alpha = 0.12f),
            onPrimaryContainer = accent,
            secondary = accent.copy(alpha = 0.85f),
            onSecondary = Color.White,
            background = LightBackground,
            onBackground = Color(0xFF0F172A),
            surface = LightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = Color(0xFF64748B),
            surfaceContainer = Color(0xFFF8FAFC),
            surfaceContainerLow = Color(0xFFFFFFFF),
            surfaceContainerHigh = Color(0xFFF1F5F9),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            surfaceDim = Color(0xFFE2E8F0),
            surfaceBright = Color(0xFFFFFFFF),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0)
        )
    }
}
