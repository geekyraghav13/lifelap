package com.life.lapse.stop.motion.video

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.life.lapse.stop.motion.video.data.repository.AppTheme
import com.life.lapse.stop.motion.video.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferencesRepository = UserPreferencesRepository(application)

    // This flow will provide the currently saved theme to the UI
    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM_DEFAULT
        )
}