package com.life.lapse.stop.motion.video.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

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

@Composable
fun LifeLapseTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // FIX: The deprecated line below has been removed.
            // window.statusBarColor = colorScheme.background.toArgb()

            // This line correctly sets the status bar icons to be light,
            // which is what we need for a dark background.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
