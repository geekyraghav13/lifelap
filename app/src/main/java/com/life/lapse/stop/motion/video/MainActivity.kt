package com.life.lapse.stop.motion.video

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.life.lapse.stop.motion.video.data.repository.AppTheme
import com.life.lapse.stop.motion.video.ui.AppNavigation
import com.life.lapse.stop.motion.video.ui.theme.LifeLapseTheme

class MainActivity : ComponentActivity() {
    // ✅ ADDED: Instantiate the MainViewModel
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ✅ ADDED: Collect the theme state from the ViewModel
            val currentTheme by viewModel.appTheme.collectAsState()

            // ✅ MODIFIED: Determine if dark theme should be used
            val useDarkTheme = when (currentTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
            }

            // ✅ MODIFIED: Pass the dynamic theme choice to the LifeLapseTheme
            LifeLapseTheme(darkTheme = useDarkTheme) {
                AppNavigation()
            }
        }
    }
}