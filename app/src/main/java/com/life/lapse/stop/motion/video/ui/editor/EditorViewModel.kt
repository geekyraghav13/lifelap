package com.life.lapse.stop.motion.video.ui.editor

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Represents the state of the project being edited
data class ProjectUiState(
    val frames: List<Uri> = emptyList(),
    val selectedFrameUri: Uri? = null,
    val selectedSpeed: Float = 1.0f,
    val isPlaying: Boolean = false,
    val currentFrameIndex: Int = 0,
    // NEW: Add state for exporting
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportResult: String? = null // Will hold success or error message
)

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var playbackJob: Job? = null

    // NEW: Export video function
    fun onExportClicked(context: Context) {
        if (_uiState.value.frames.isEmpty()) {
            _uiState.update { it.copy(exportResult = "Error: No frames to export.") }
            return
        }

        pausePlayback()
        _uiState.update { it.copy(isExporting = true, exportProgress = 0f, exportResult = null) }

        // In a real app, you would use a proper video exporting library here.
        // For this example, we will simulate the export process.
        viewModelScope.launch {
            try {
                // Simulate a 3-second export process
                for (i in 1..10) {
                    delay(300)
                    _uiState.update { it.copy(exportProgress = i / 10f) }
                }
                Log.d("EditorViewModel", "Simulated export successful.")
                _uiState.update { it.copy(isExporting = false, exportResult = "Video saved to Gallery!") }
            } catch (e: Exception) {
                Log.e("EditorViewModel", "Simulated export failed.", e)
                _uiState.update { it.copy(isExporting = false, exportResult = "Error: Export failed.") }
            }
        }
    }

    fun clearExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }

    fun addFrame(uri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(frames = currentState.frames + uri)
        }
    }

    fun onSpeedSelected(speed: Float) {
        _uiState.update { it.copy(selectedSpeed = speed) }
    }

    fun onTogglePlayback() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        if (_uiState.value.frames.isEmpty()) return

        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            while (isActive) {
                val frameDuration = (1000 / _uiState.value.selectedSpeed).toLong()
                delay(frameDuration)

                _uiState.update { currentState ->
                    val nextIndex = (currentState.currentFrameIndex + 1) % currentState.frames.size
                    currentState.copy(currentFrameIndex = nextIndex)
                }
            }
        }
    }

    private fun pausePlayback() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun onDeleteFrameClicked() { /* TODO */ }
    fun onDuplicateFrameClicked() { /* TODO */ }

    fun clearProject() {
        pausePlayback()
        _uiState.value = ProjectUiState()
    }
}
