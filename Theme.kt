package com.focusflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B8DEF),
    secondary = Color(0xFF9B6BFF),
    background = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7EA6FF),
    secondary = Color(0xFFB093FF),
    background = Color(0xFF121318),
    surface = Color(0xFF1C1E26)
)

private val AmoledColors = darkColorScheme(
    primary = Color(0xFF7EA6FF),
    secondary = Color(0xFFB093FF),
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A)
)

enum class AppThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

@Composable
fun FocusFlowTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
    }

    val colors = when {
        themeMode == AppThemeMode.AMOLED -> AmoledColors
        useDark -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
