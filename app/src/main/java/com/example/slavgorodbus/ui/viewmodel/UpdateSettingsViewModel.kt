package com.example.slavgorodbus.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slavgorodbus.data.local.UpdatePreferences
import com.example.slavgorodbus.updates.UpdateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UpdateSettingsViewModel(private val context: Context) : ViewModel() {
    
    private val updatePreferences = UpdatePreferences(context)
    
    val autoUpdateCheckEnabled: Flow<Boolean> = updatePreferences.autoUpdateCheckEnabled
    val lastUpdateCheckTime: Flow<Long> = updatePreferences.lastUpdateCheckTime
    val availableUpdateVersion: Flow<String?> = updatePreferences.availableUpdateVersion
    val availableUpdateUrl: Flow<String?> = updatePreferences.availableUpdateUrl
    val availableUpdateNotes: Flow<String?> = updatePreferences.availableUpdateNotes
    
    // Преобразуем boolean в UpdateMode
    val currentUpdateMode: Flow<UpdateMode> = autoUpdateCheckEnabled.map { enabled ->
        if (enabled) UpdateMode.AUTOMATIC else UpdateMode.MANUAL
    }
    
    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates.asStateFlow()
    
    private val _updateCheckError = MutableStateFlow<String?>(null)
    val updateCheckError: StateFlow<String?> = _updateCheckError.asStateFlow()
    
    private val _updateCheckStatus = MutableStateFlow<String?>(null)
    val updateCheckStatus: StateFlow<String?> = _updateCheckStatus.asStateFlow()
    
    fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updatePreferences.setAutoUpdateCheckEnabled(enabled)
        }
    }
    
    fun setUpdateMode(mode: UpdateMode) {
        viewModelScope.launch {
            when (mode) {
                UpdateMode.AUTOMATIC -> updatePreferences.setAutoUpdateCheckEnabled(true)
                UpdateMode.MANUAL -> updatePreferences.setAutoUpdateCheckEnabled(false)
                UpdateMode.DISABLED -> updatePreferences.setAutoUpdateCheckEnabled(false)
            }
        }
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingUpdates.value = true
            _updateCheckError.value = null
            _updateCheckStatus.value = null
            
            try {
                val updateManager = UpdateManager(context)
                val result = updateManager.checkForUpdatesWithResult()
                
                if (result.success) {
                    if (result.update != null) {
                        // Сохраняем информацию о доступном обновлении
                        updatePreferences.setAvailableUpdate(
                            version = result.update.versionName,
                            url = result.update.downloadUrl,
                            notes = result.update.releaseNotes
                        )
                        _updateCheckStatus.value = "Доступна новая версия ${result.update.versionName}"
                    } else {
                        // Очищаем информацию о доступном обновлении
                        updatePreferences.clearAvailableUpdate()
                        _updateCheckStatus.value = "У вас установлена последняя версия"
                    }
                    
                    // Обновляем время последней проверки
                    updatePreferences.setLastUpdateCheckTime(System.currentTimeMillis())
                } else {
                    _updateCheckError.value = result.error ?: "Ошибка при проверке обновлений"
                }
            } catch (e: Exception) {
                _updateCheckError.value = "Ошибка: ${e.message}"
            } finally {
                _isCheckingUpdates.value = false
            }
        }
    }
    
    fun clearUpdateCheckError() {
        _updateCheckError.value = null
    }
    
    fun clearUpdateCheckStatus() {
        _updateCheckStatus.value = null
    }
    
    fun clearAvailableUpdate() {
        viewModelScope.launch {
            updatePreferences.clearAvailableUpdate()
        }
    }
}
