package com.life.lapse.stop.motion.video.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppTheme { LIGHT, DARK, SYSTEM_DEFAULT }
enum class ExportQuality { P720, P1080, P4K }

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    private val appThemeKey = stringPreferencesKey("app_theme")
    private val exportQualityKey = stringPreferencesKey("export_quality")
    // ✅ ADDED: Key for the new onboarding setting
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    val appTheme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        AppTheme.valueOf(
            preferences[appThemeKey] ?: AppTheme.SYSTEM_DEFAULT.name
        )
    }

    val exportQuality: Flow<ExportQuality> = context.dataStore.data.map { preferences ->
        ExportQuality.valueOf(
            preferences[exportQualityKey] ?: ExportQuality.P1080.name
        )
    }

    // ✅ ADDED: Flow to read the onboarding status (defaults to false)
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[onboardingCompletedKey] ?: false
    }

    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[appThemeKey] = theme.name
        }
    }

    suspend fun updateExportQuality(quality: ExportQuality) {
        context.dataStore.edit { preferences ->
            preferences[exportQualityKey] = quality.name
        }
    }

    // ✅ ADDED: Function to update the onboarding status to true
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = completed
        }
    }
}