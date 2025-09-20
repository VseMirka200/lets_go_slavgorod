package com.example.slavgorodbus.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.updatePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_preferences")

object UpdatePreferencesKeys {
    val AUTO_UPDATE_CHECK_ENABLED = booleanPreferencesKey("auto_update_check_enabled")
    val LAST_UPDATE_CHECK_TIME = longPreferencesKey("last_update_check_time")
    val AVAILABLE_UPDATE_VERSION = stringPreferencesKey("available_update_version")
    val AVAILABLE_UPDATE_URL = stringPreferencesKey("available_update_url")
    val AVAILABLE_UPDATE_NOTES = stringPreferencesKey("available_update_notes")
    val UPDATE_NOTIFICATION_SHOWN = booleanPreferencesKey("update_notification_shown")
}

class UpdatePreferences(private val context: Context) {
    
    val autoUpdateCheckEnabled: Flow<Boolean> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.AUTO_UPDATE_CHECK_ENABLED] ?: true
        }
    
    val lastUpdateCheckTime: Flow<Long> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.LAST_UPDATE_CHECK_TIME] ?: 0L
        }
    
    val availableUpdateVersion: Flow<String?> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_VERSION]
        }
    
    val availableUpdateUrl: Flow<String?> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_URL]
        }
    
    val availableUpdateNotes: Flow<String?> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_NOTES]
        }
    
    val updateNotificationShown: Flow<Boolean> = context.updatePreferencesDataStore.data
        .map { preferences ->
            preferences[UpdatePreferencesKeys.UPDATE_NOTIFICATION_SHOWN] ?: false
        }
    
    suspend fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UpdatePreferencesKeys.AUTO_UPDATE_CHECK_ENABLED] = enabled
        }
    }
    
    suspend fun setLastUpdateCheckTime(time: Long) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UpdatePreferencesKeys.LAST_UPDATE_CHECK_TIME] = time
        }
    }
    
    suspend fun setAvailableUpdate(version: String, url: String, notes: String) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_VERSION] = version
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_URL] = url
            preferences[UpdatePreferencesKeys.AVAILABLE_UPDATE_NOTES] = notes
            preferences[UpdatePreferencesKeys.UPDATE_NOTIFICATION_SHOWN] = false
        }
    }
    
    suspend fun clearAvailableUpdate() {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences.remove(UpdatePreferencesKeys.AVAILABLE_UPDATE_VERSION)
            preferences.remove(UpdatePreferencesKeys.AVAILABLE_UPDATE_URL)
            preferences.remove(UpdatePreferencesKeys.AVAILABLE_UPDATE_NOTES)
            preferences.remove(UpdatePreferencesKeys.UPDATE_NOTIFICATION_SHOWN)
        }
    }
    
    suspend fun setUpdateNotificationShown(shown: Boolean) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UpdatePreferencesKeys.UPDATE_NOTIFICATION_SHOWN] = shown
        }
    }
}
