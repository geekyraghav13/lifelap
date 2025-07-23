package com.life.lapse.stop.motion.video

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.life.lapse.stop.motion.video.ui.AppNavigation
import com.life.lapse.stop.motion.video.ui.theme.LifeLapseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This function helps in drawing the UI behind the system bars (status bar, navigation bar)
        enableEdgeToEdge()
        setContent {
            // We apply our custom theme, which sets up all the colors and typography
            LifeLapseTheme {
                // AppNavigation is the composable that controls all screen transitions
                AppNavigation()
            }
        }
    }
}
