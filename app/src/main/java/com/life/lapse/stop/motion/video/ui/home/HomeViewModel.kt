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

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            val projects = projectRepository.loadProjects()
            _uiState.value = HomeUiState(projects = projects, isLoading = false)
        }
    }
}
