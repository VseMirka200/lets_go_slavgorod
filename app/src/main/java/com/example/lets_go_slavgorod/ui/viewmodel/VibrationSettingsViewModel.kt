package com.example.lets_go_slavgorod.ui.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lets_go_slavgorod.data.local.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для управления настройками вибрации уведомлений
 */
class VibrationSettingsViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
    }
    
    /**
     * Текущее состояние настройки вибрации
     */
    val vibrationEnabled: StateFlow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[VIBRATION_ENABLED_KEY] ?: true  // По умолчанию включено
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    /**
     * Устанавливает состояние вибрации
     */
    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                context.dataStore.edit { preferences ->
                    preferences[VIBRATION_ENABLED_KEY] = enabled
                }
                Timber.d("Vibration settings updated: enabled=$enabled")
            } catch (e: Exception) {
                Timber.e(e, "Error saving vibration settings")
            }
        }
    }
}

