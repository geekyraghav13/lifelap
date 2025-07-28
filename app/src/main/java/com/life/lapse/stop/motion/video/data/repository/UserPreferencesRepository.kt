package com.life.lapse.stop.motion.video.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define enums for type-safe settings
enum class AppTheme { LIGHT, DARK, SYSTEM_DEFAULT }
enum class ExportQuality { P720, P1080, P4K }

// Create the DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    // Define keys for each setting
    private val appThemeKey = stringPreferencesKey("app_theme")
    private val exportQualityKey = stringPreferencesKey("export_quality")

    // Create a Flow to read the saved AppTheme
    val appTheme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        AppTheme.valueOf(
            preferences[appThemeKey] ?: AppTheme.SYSTEM_DEFAULT.name
        )
    }

    // Create a Flow to read the saved ExportQuality
    val exportQuality: Flow<ExportQuality> = context.dataStore.data.map { preferences ->
        ExportQuality.valueOf(
            preferences[exportQualityKey] ?: ExportQuality.P1080.name
        )
    }

    // Function to update the AppTheme
    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[appThemeKey] = theme.name
        }
    }

    // Function to update the ExportQuality
    suspend fun updateExportQuality(quality: ExportQuality) {
        context.dataStore.edit { preferences ->
            preferences[exportQualityKey] = quality.name
        }
    }
}