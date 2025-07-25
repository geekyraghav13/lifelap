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

data class Frame(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri
)

data class ProjectUiState(
    val frames: List<Frame> = emptyList(),
    val selectedFrame: Frame? = null,
    val selectedSpeed: Float = 1.0f,
    val isPlaying: Boolean = false,
    val currentFrameIndex: Int = 0,
    val exportResult: String? = null,
    val isFullScreen: Boolean = false,
    // NEW: State to control visibility of player controls in fullscreen
    val showFullScreenControls: Boolean = true
)

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var playbackJob: Job? = null
    private var hideControlsJob: Job? = null // NEW: Job to manage auto-hiding

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
            val newFrame = Frame(uri = selectedFrame.uri)
            val newFrames = currentFrames.toMutableList().apply {
                add(selectedIndex + 1, newFrame)
            }
            currentState.copy(
                frames = newFrames,
                selectedFrame = newFrame
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

    fun onToggleFullScreen() {
        val isEnteringFullScreen = !_uiState.value.isFullScreen
        _uiState.update { it.copy(isFullScreen = isEnteringFullScreen, showFullScreenControls = true) }
        if (isEnteringFullScreen) {
            // When entering fullscreen, start the timer to hide controls
            scheduleHideControls()
        } else {
            // When exiting, cancel any pending hide operations
            hideControlsJob?.cancel()
        }
    }

    // NEW: When tapping the screen in fullscreen, show controls and reset the timer
    fun onFullScreenPlayerTap() {
        if (!_uiState.value.isFullScreen) return

        if (_uiState.value.showFullScreenControls) {
            // If controls are already showing, do nothing
            return
        }

        _uiState.update { it.copy(showFullScreenControls = true) }
        scheduleHideControls()
    }

    private fun scheduleHideControls() {
        hideControlsJob?.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(3000) // Wait for 3 seconds
            _uiState.update { it.copy(showFullScreenControls = false) }
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

    fun clearProject() {
        pausePlayback()
        _uiState.value = ProjectUiState()
    }
}
