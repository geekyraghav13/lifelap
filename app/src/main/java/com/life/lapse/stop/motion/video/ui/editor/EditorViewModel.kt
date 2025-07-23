package com.life.lapse.stop.motion.video.ui.editor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Represents the state of the Editor UI
data class EditorUiState(
    val frames: List<String> = emptyList(), // Will hold image URIs
    val selectedSpeed: Float = 1.0f
)

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load mock data for now so we can see the UI
        loadMockFrames()
    }

    private fun loadMockFrames() {
        _uiState.update {
            it.copy(
                frames = listOf("frame1", "frame2", "frame3", "frame4") // Simple placeholders
            )
        }
    }

    fun onSpeedSelected(speed: Float) {
        _uiState.update { it.copy(selectedSpeed = speed) }
    }

    fun onDeleteFrameClicked() {
        // TODO: Implement delete logic
    }

    fun onDuplicateFrameClicked() {
        // TODO: Implement duplicate logic
    }

    fun onAddFrameClicked() {
        // TODO: Implement logic to go back to camera to add a frame
    }
}
