package com.life.lapse.stop.motion.video.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

// NEW: A data class to wrap each frame with a unique ID.
// This is the key to fixing the crash.
data class Frame(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri
)

// Represents the state of the project being editedaz
data class ProjectUiState(
    val frames: List<Frame> = emptyList(), // Now a list of Frame objects
    val selectedFrame: Frame? = null,
    val selectedSpeed: Float = 1.0f,
    val isPlaying: Boolean = false,
    val currentFrameIndex: Int = 0,
    // FIX: Corrected the typo from "exportResulat" to "exportResult"
    val exportResult: String? = null
)

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var playbackJob: Job? = null

    fun onExportClicked(context: Context) {
        if (_uiState.value.frames.isEmpty()) {
            _uiState.update { it.copy(exportResult = "Error: No frames to export.") }
            return
        }
        _uiState.update { it.copy(exportResult = "Export feature is coming soon!") }
    }

    fun clearExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }

    fun addFrame(uri: Uri) {
        val newFrame = Frame(uri = uri)
        _uiState.update { currentState ->
            currentState.copy(
                frames = currentState.frames + newFrame,
                selectedFrame = newFrame
            )
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

    fun onFrameSelected(frame: Frame) {
        _uiState.update { it.copy(selectedFrame = frame) }
    }

    fun onDeleteFrameClicked() {
        val selectedFrame = _uiState.value.selectedFrame ?: return
        _uiState.update { currentState ->
            val currentFrames = currentState.frames
            val newFrames = currentFrames.toMutableList().apply { remove(selectedFrame) }
            val deletedIndex = currentFrames.indexOf(selectedFrame)
            val newSelectedFrame = if (newFrames.isEmpty()) {
                null
            } else {
                newFrames.getOrNull(deletedIndex.coerceAtMost(newFrames.size - 1))
            }

            currentState.copy(
                frames = newFrames,
                selectedFrame = newSelectedFrame
            )
        }
    }

    fun onDuplicateFrameClicked() {
        val selectedFrame = _uiState.value.selectedFrame ?: return
        _uiState.update { currentState ->
            val currentFrames = currentState.frames
            val selectedIndex = currentFrames.indexOf(selectedFrame)
            if (selectedIndex == -1) return@update currentState

            // Create a new Frame object with a new unique ID but the same image Uri
            val newFrame = Frame(uri = selectedFrame.uri)

            val newFrames = currentFrames.toMutableList().apply {
                add(selectedIndex + 1, newFrame)
            }
            currentState.copy(
                frames = newFrames,
                selectedFrame = newFrame // Select the newly created duplicate
            )
        }
    }

    fun onMoveFrame(from: Int, to: Int) {
        _uiState.update { currentState ->
            val reorderedFrames = currentState.frames.toMutableList().apply {
                add(to, removeAt(from))
            }
            currentState.copy(frames = reorderedFrames)
        }
    }

    fun onSeekToFrame(frameIndex: Int) {
        pausePlayback()
        _uiState.update { it.copy(currentFrameIndex = frameIndex) }
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

    fun clearProject() {
        pausePlayback()
        _uiState.value = ProjectUiState()
    }
}
