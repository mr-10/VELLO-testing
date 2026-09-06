package com.mr10.vello.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppGreen,
    onPrimary = WhatsAppTextPrimaryDark,
    primaryContainer = WhatsAppHeaderDark,
    onPrimaryContainer = WhatsAppTextPrimaryDark,
    secondary = WhatsAppGreen,
    onSecondary = WhatsAppTextPrimaryDark,
    background = WhatsAppBackgroundDark,
    onBackground = WhatsAppTextPrimaryDark,
    surface = WhatsAppSurfaceDark,
    onSurface = WhatsAppTextPrimaryDark,
    surfaceVariant = WhatsAppDividerDark,
    onSurfaceVariant = WhatsAppTextSecondaryDark,
    outline = WhatsAppDividerDark,
    error = WhatsAppError
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppHeaderLight,
    onPrimary = WhatsAppSurfaceLight,
    primaryContainer = WhatsAppHeaderLight,
    onPrimaryContainer = WhatsAppSurfaceLight,
    secondary = WhatsAppGreen,
    onSecondary = WhatsAppSurfaceLight,
    background = WhatsAppSurfaceLight,
    onBackground = WhatsAppTextPrimaryLight,
    surface = WhatsAppSurfaceLight,
    onSurface = WhatsAppTextPrimaryLight,
    surfaceVariant = WhatsAppDividerLight,
    onSurfaceVariant = WhatsAppTextSecondaryLight,
    outline = WhatsAppDividerLight,
    error = WhatsAppError
)

@Composable
fun VelloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
