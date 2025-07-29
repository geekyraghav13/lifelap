package com.life.lapse.stop.motion.video.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Pink_Primary,
    background = Dark_Background,
    surface = Dark_Surface,
    onPrimary = Color.White,
    onBackground = Text_White,
    onSurface = Text_White,
    onSurfaceVariant = Text_Gray
)

// ✅ ADDED: Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = Pink_Primary,
    background = Light_Background,
    surface = Light_Surface,
    onPrimary = Color.White,
    onBackground = Text_Black,
    onSurface = Text_Black,
    onSurfaceVariant = Text_DarkGray
)

@Composable
fun LifeLapseTheme(
    // ✅ MODIFIED: Added darkTheme parameter
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // ✅ MODIFIED: Select color scheme based on the parameter
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // ✅ MODIFIED: Set status bar color and icon appearance dynamically
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}