package com.mr10.vello.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.mr10.vello.data.local.LocalSettingsManager

private val Palette0 = lightColorScheme(
    primary = VelloPrimaryContainer,
    secondary = VelloSecondary,
    tertiary = VelloTertiary,
    background = VelloBackground,
    surface = VelloSurface,
    onPrimary = Color.White,
    onSurface = VelloOnSurface
)

private val Palette1 = lightColorScheme(
    primary = Color(0xFF1976D2), // Blue
    secondary = Color(0xFF2196F3),
    tertiary = Color(0xFF03A9F4),
    background = Color(0xFFF0F4F8),
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

private val Palette2 = lightColorScheme(
    primary = Color(0xFF7B1FA2), // Purple
    secondary = Color(0xFF9C27B0),
    tertiary = Color(0xFFBA68C8),
    background = Color(0xFFF3E5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

private val Palette3 = lightColorScheme(
    primary = Color(0xFFC2185B), // Rose
    secondary = Color(0xFFE91E63),
    tertiary = Color(0xFFF06292),
    background = Color(0xFFFCE4EC),
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

private val Palette4 = lightColorScheme(
    primary = Color(0xFFE65100), // Orange
    secondary = Color(0xFFFB8C00),
    tertiary = Color(0xFFFFB74D),
    background = Color(0xFFFFF3E0),
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

private val Palette5 = lightColorScheme(
    primary = Color(0xFF00796B), // Teal
    secondary = Color(0xFF009688),
    tertiary = Color(0xFF4DB6AC),
    background = Color(0xFFE0F2F1),
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

private val Palette6 = lightColorScheme(
    primary = Color(0xFF388E3C), // Forest
    secondary = Color(0xFF4CAF50),
    tertiary = Color(0xFF81C784),
    background = Color(0xFFE8F5E9),
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

private val Palette7 = lightColorScheme(
    primary = Color(0xFF455A64), // Slate
    secondary = Color(0xFF607D8B),
    tertiary = Color(0xFF90A4AE),
    background = Color(0xFFECEFF1),
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

val Palettes = listOf(Palette0, Palette1, Palette2, Palette3, Palette4, Palette5, Palette6, Palette7)

@Composable
fun VelloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { LocalSettingsManager.getInstance(context) }
    val themeIndex by settingsManager.themeIndex.collectAsState()
    val fontSizeName by settingsManager.fontSize.collectAsState()

    val colorScheme = Palettes.getOrElse(themeIndex) { Palette0 }
    
    val fontSizeMultiplier = when (fontSizeName) {
        "Small" -> 0.85f
        "Large" -> 1.2f
        else -> 1.0f
    }

    val scaledTypography = androidx.compose.material3.Typography(
        bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * fontSizeMultiplier),
        bodyMedium = Typography.bodyMedium.copy(fontSize = Typography.bodyMedium.fontSize * fontSizeMultiplier),
        bodySmall = Typography.bodySmall.copy(fontSize = Typography.bodySmall.fontSize * fontSizeMultiplier),
        titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * fontSizeMultiplier),
        titleMedium = Typography.titleMedium.copy(fontSize = Typography.titleMedium.fontSize * fontSizeMultiplier),
        titleSmall = Typography.titleSmall.copy(fontSize = Typography.titleSmall.fontSize * fontSizeMultiplier),
        labelLarge = Typography.labelLarge.copy(fontSize = Typography.labelLarge.fontSize * fontSizeMultiplier),
        labelMedium = Typography.labelMedium.copy(fontSize = Typography.labelMedium.fontSize * fontSizeMultiplier),
        labelSmall = Typography.labelSmall.copy(fontSize = Typography.labelSmall.fontSize * fontSizeMultiplier),
        headlineLarge = Typography.headlineLarge.copy(fontSize = Typography.headlineLarge.fontSize * fontSizeMultiplier),
        headlineMedium = Typography.headlineMedium.copy(fontSize = Typography.headlineMedium.fontSize * fontSizeMultiplier),
        headlineSmall = Typography.headlineSmall.copy(fontSize = Typography.headlineSmall.fontSize * fontSizeMultiplier)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
