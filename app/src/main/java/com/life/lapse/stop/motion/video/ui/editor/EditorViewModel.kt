package com.life.lapse.stop.motion.video.ui.editor

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Represents the state of the project being edited
data class ProjectUiState(
    val frames: List<Uri> = emptyList(), // Now holds real image URIs
    val selectedFrameUri: Uri? = null,
    val selectedSpeed: Float = 1.0f
)

// This ViewModel will be shared between the Camera and Editor screens.
class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()

    fun addFrame(uri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(frames = currentState.frames + uri)
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

    // This function will be used to clear the project when we are done.
    fun clearProject() {
        _uiState.value = ProjectUiState()
    }
}
