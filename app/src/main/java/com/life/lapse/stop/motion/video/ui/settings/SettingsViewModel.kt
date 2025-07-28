package com.life.lapse.stop.motion.video.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.life.lapse.stop.motion.video.data.repository.AppTheme
import com.life.lapse.stop.motion.video.data.repository.ExportQuality
import com.life.lapse.stop.motion.video.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// This data class holds all the state for our UI
data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val exportQuality: ExportQuality = ExportQuality.P1080,
    val isThemeDialogOpen: Boolean = false,
    val isQualityDialogOpen: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefsRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Observe changes from the repository and update the UI state
        userPrefsRepository.appTheme
            .onEach { theme -> _uiState.update { it.copy(appTheme = theme) } }
            .launchIn(viewModelScope)

        userPrefsRepository.exportQuality
            .onEach { quality -> _uiState.update { it.copy(exportQuality = quality) } }
            .launchIn(viewModelScope)
    }

    // --- Functions for the UI to call ---

    fun onThemeChanged(theme: AppTheme) {
        viewModelScope.launch {
            userPrefsRepository.updateAppTheme(theme)
            _uiState.update { it.copy(isThemeDialogOpen = false) }
        }
    }

    fun onQualityChanged(quality: ExportQuality) {
        viewModelScope.launch {
            userPrefsRepository.updateExportQuality(quality)
            _uiState.update { it.copy(isQualityDialogOpen = false) }
        }
    }

    fun showThemeDialog(show: Boolean) {
        _uiState.update { it.copy(isThemeDialogOpen = show) }
    }

    fun showQualityDialog(show: Boolean) {
        _uiState.update { it.copy(isQualityDialogOpen = show) }
    }
}