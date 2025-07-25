package com.life.lapse.stop.motion.video.ui.editor

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.life.lapse.stop.motion.video.data.model.Project
import com.life.lapse.stop.motion.video.data.repository.ProjectRepository
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
    val project: Project = Project(),
    val frames: List<Frame> = emptyList(),
    val selectedFrame: Frame? = null,
    val isPlaying: Boolean = false,
    val currentFrameIndex: Int = 0,
    val exportResult: String? = null,
    val isFullScreen: Boolean = false,
    val showFullScreenControls: Boolean = true
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val projectRepository = ProjectRepository(application)
    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState = _uiState.asStateFlow()
    private var playbackJob: Job? = null
    private var hideControlsJob: Job? = null

    fun loadProject(projectId: String?) {
        if (projectId == null) {
            val newProject = Project()
            _uiState.value = ProjectUiState(project = newProject)
            projectRepository.saveProject(newProject)
            return
        }
        viewModelScope.launch {
            val project = projectRepository.loadProject(projectId) ?: Project()
            val frames = project.frameUris.map { Frame(uri = it.toUri()) }
            _uiState.value = ProjectUiState(
                project = project,
                frames = frames,
                selectedFrame = frames.firstOrNull()
            )
        }
    }

    private fun saveProject() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val updatedProject = currentState.project.copy(
                frameUris = currentState.frames.map { it.uri.toString() },
                dateModified = System.currentTimeMillis()
            )
            _uiState.update { it.copy(project = updatedProject) }
            projectRepository.saveProject(updatedProject)
        }
    }

    fun addFrame(uri: Uri) {
        val newFrame = Frame(uri = uri)
        _uiState.update { currentState ->
            val newFrames = currentState.frames + newFrame
            currentState.copy(frames = newFrames, selectedFrame = newFrame)
        }
        saveProject()
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
            val newSelectedFrame = if (newFrames.isEmpty()) null else newFrames.getOrNull(deletedIndex.coerceAtMost(newFrames.size - 1))
            currentState.copy(frames = newFrames, selectedFrame = newSelectedFrame)
        }
        saveProject()
    }

    fun onDuplicateFrameClicked() {
        val selectedFrame = _uiState.value.selectedFrame ?: return
        _uiState.update { currentState ->
            val currentFrames = currentState.frames
            val selectedIndex = currentFrames.indexOf(selectedFrame)
            if (selectedIndex == -1) return@update currentState
            val newFrame = Frame(uri = selectedFrame.uri)
            val newFrames = currentFrames.toMutableList().apply { add(selectedIndex + 1, newFrame) }
            currentState.copy(frames = newFrames, selectedFrame = newFrame)
        }
        saveProject()
    }

    fun onMoveFrame(from: Int, to: Int) {
        _uiState.update { currentState ->
            val reorderedFrames = currentState.frames.toMutableList().apply { add(to, removeAt(from)) }
            currentState.copy(frames = reorderedFrames)
        }
        saveProject()
    }

    fun onToggleFullScreen() {
        val isEnteringFullScreen = !_uiState.value.isFullScreen
        _uiState.update { it.copy(isFullScreen = isEnteringFullScreen, showFullScreenControls = true) }
        if (isEnteringFullScreen) {
            scheduleHideControls()
        } else {
            hideControlsJob?.cancel()
        }
    }

    fun onFullScreenPlayerTap() {
        if (!_uiState.value.isFullScreen) return
        val currentlyVisible = _uiState.value.showFullScreenControls
        _uiState.update { it.copy(showFullScreenControls = !currentlyVisible) }
        if (!currentlyVisible) {
            scheduleHideControls()
        } else {
            hideControlsJob?.cancel()
        }
    }

    private fun scheduleHideControls() {
        hideControlsJob?.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(showFullScreenControls = false) }
        }
    }

    fun onExportClicked(context: Context) {
        _uiState.update { it.copy(exportResult = "Export feature is coming soon!") }
    }
    fun clearExportResult() { _uiState.update { it.copy(exportResult = null) } }
    fun onSpeedSelected(speed: Float) {
        _uiState.update { it.copy(project = it.project.copy(speed = speed)) }
        saveProject()
    }
    fun onTogglePlayback() { if (_uiState.value.isPlaying) pausePlayback() else startPlayback() }
    fun onSeekToFrame(frameIndex: Int) {
        pausePlayback()
        _uiState.update { it.copy(currentFrameIndex = frameIndex) }
    }
    private fun startPlayback() {
        if (_uiState.value.frames.isEmpty()) return
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            while (isActive) {
                val frameDuration = (1000 / (_uiState.value.project.speed)).toLong()
                delay(frameDuration)
                _uiState.update { cs -> cs.copy(currentFrameIndex = (cs.currentFrameIndex + 1) % cs.frames.size) }
            }
        }
    }
    private fun pausePlayback() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }
}
