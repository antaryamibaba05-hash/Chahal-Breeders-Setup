package com.highflyerpro.tracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "high_flyer_preferences")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class LandingMode { CONFIRMATION, QUICK_ONE_TAP }

class PreferencesRepository(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_LANDING_MODE = stringPreferencesKey("landing_mode")
        val KEY_IS_INITIALIZED = booleanPreferencesKey("is_initialized")
        val KEY_BACKUP_FOLDER_URI = stringPreferencesKey("backup_folder_uri")
        val KEY_LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
        val KEY_AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val KEY_BACKUP_RETENTION = stringPreferencesKey("backup_retention") // KEEP_FOREVER, KEEP_30, KEEP_60, KEEP_90
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_CAMERA_ENABLED = booleanPreferencesKey("camera_enabled")
        val KEY_PHOTOS_GRANTED = booleanPreferencesKey("photos_granted")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val landingModeFlow: Flow<LandingMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[KEY_LANDING_MODE] ?: LandingMode.CONFIRMATION.name
        try {
            LandingMode.valueOf(modeStr)
        } catch (e: Exception) {
            LandingMode.CONFIRMATION
        }
    }

    val isInitializedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_INITIALIZED] ?: false
    }

    val backupFolderUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_BACKUP_FOLDER_URI]
    }

    val lastBackupTimestampFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_BACKUP_TIMESTAMP] ?: 0L
    }

    val autoBackupEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_BACKUP_ENABLED] ?: true
    }

    val backupRetentionFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_BACKUP_RETENTION] ?: "KEEP_FOREVER"
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS_ENABLED] ?: false
    }

    val cameraEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_CAMERA_ENABLED] ?: false
    }

    val photosGrantedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_PHOTOS_GRANTED] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setLandingMode(mode: LandingMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANDING_MODE] = mode.name
        }
    }

    suspend fun setInitialized(initialized: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_INITIALIZED] = initialized
        }
    }

    suspend fun setBackupFolderUri(uriString: String?) {
        context.dataStore.edit { preferences ->
            if (uriString != null) {
                preferences[KEY_BACKUP_FOLDER_URI] = uriString
            } else {
                preferences.remove(KEY_BACKUP_FOLDER_URI)
            }
        }
    }

    suspend fun setLastBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_BACKUP_TIMESTAMP] = timestamp
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setBackupRetention(retention: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BACKUP_RETENTION] = retention
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setCameraEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CAMERA_ENABLED] = enabled
        }
    }

    suspend fun setPhotosGranted(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PHOTOS_GRANTED] = granted
        }
    }
}
