package com.life.lapse.stop.motion.video.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.life.lapse.stop.motion.video.data.model.Project
import com.life.lapse.stop.motion.video.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val projectRepository = ProjectRepository(application)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    // State for the delete confirmation dialog
    private val _projectToDelete = MutableStateFlow<Project?>(null)
    val projectToDelete = _projectToDelete.asStateFlow()

    // ✅ ADDED: State for the rename dialog
    private val _projectToRename = MutableStateFlow<Project?>(null)
    val projectToRename = _projectToRename.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            val projects = projectRepository.loadProjects()
            _uiState.value = HomeUiState(projects = projects, isLoading = false)
        }
    }

    // --- Delete Logic ---
    fun onDeleteRequest(project: Project) {
        _projectToDelete.value = project
    }

    fun onDeleteDialogDismiss() {
        _projectToDelete.value = null
    }

    fun onDeleteConfirm() {
        viewModelScope.launch {
            projectToDelete.value?.let { project ->
                projectRepository.deleteProject(project.id)
                _projectToDelete.value = null
                loadProjects()
            }
        }
    }

    // ✅ ADDED: Functions to handle the rename logic
    fun onRenameRequest(project: Project) {
        _projectToRename.value = project
    }

    fun onRenameDialogDismiss() {
        _projectToRename.value = null
    }

    fun onRenameConfirm(newTitle: String) {
        viewModelScope.launch {
            projectToRename.value?.let { project ->
                val updatedProject = project.copy(title = newTitle)
                projectRepository.saveProject(updatedProject)
                _projectToRename.value = null
                loadProjects()
            }
        }
    }
}