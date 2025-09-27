package com.example.lets_go_slavgorod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.lets_go_slavgorod.utils.logd
import com.example.lets_go_slavgorod.utils.loge

/**
 * ViewModel для управления UI состоянием экрана расписания
 * 
 * Функциональность:
 * - Управление состоянием сворачивания секций
 * - Кэширование состояний для лучшей производительности
 * - Обработка ошибок UI
 * - Управление анимациями и переходами
 */
class ScheduleUiViewModel : ViewModel() {

    // Состояние сворачивания секций
    private val _sectionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val sectionStates: StateFlow<Map<String, Boolean>> = _sectionStates.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Состояние ошибок
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Состояние анимаций
    private val _animationsEnabled = MutableStateFlow(true)
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    /**
     * Переключает состояние секции (свернута/развернута)
     */
    fun toggleSection(sectionKey: String) {
        viewModelScope.launch {
            try {
                val currentStates = _sectionStates.value.toMutableMap()
                val currentState = currentStates[sectionKey] ?: true // По умолчанию развернута
                currentStates[sectionKey] = !currentState
                _sectionStates.value = currentStates
                
                logd("ScheduleUiViewModel: Toggled section '$sectionKey' to ${!currentState}")
            } catch (e: Exception) {
                loge("ScheduleUiViewModel: Error toggling section", e)
                _errorMessage.value = "Ошибка при изменении секции"
            }
        }
    }

    /**
     * Устанавливает состояние секции
     */
    fun setSectionState(sectionKey: String, isExpanded: Boolean) {
        viewModelScope.launch {
            try {
                val currentStates = _sectionStates.value.toMutableMap()
                currentStates[sectionKey] = isExpanded
                _sectionStates.value = currentStates
                
                logd("ScheduleUiViewModel: Set section '$sectionKey' to $isExpanded")
            } catch (e: Exception) {
                loge("ScheduleUiViewModel: Error setting section state", e)
            }
        }
    }

    /**
     * Получает состояние секции
     */
    fun isSectionExpanded(sectionKey: String): Boolean {
        return _sectionStates.value[sectionKey] ?: true
    }

    /**
     * Разворачивает все секции
     */
    fun expandAllSections(sectionKeys: List<String>) {
        viewModelScope.launch {
            try {
                val currentStates = _sectionStates.value.toMutableMap()
                sectionKeys.forEach { key ->
                    currentStates[key] = true
                }
                _sectionStates.value = currentStates
                
                logd("ScheduleUiViewModel: Expanded all sections")
            } catch (e: Exception) {
                loge("ScheduleUiViewModel: Error expanding all sections", e)
            }
        }
    }

    /**
     * Сворачивает все секции
     */
    fun collapseAllSections(sectionKeys: List<String>) {
        viewModelScope.launch {
            try {
                val currentStates = _sectionStates.value.toMutableMap()
                sectionKeys.forEach { key ->
                    currentStates[key] = false
                }
                _sectionStates.value = currentStates
                
                logd("ScheduleUiViewModel: Collapsed all sections")
            } catch (e: Exception) {
                loge("ScheduleUiViewModel: Error collapsing all sections", e)
            }
        }
    }

    /**
     * Устанавливает состояние загрузки
     */
    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
        logd("ScheduleUiViewModel: Loading state set to $isLoading")
    }

    /**
     * Устанавливает сообщение об ошибке
     */
    fun setError(message: String?) {
        _errorMessage.value = message
        if (message != null) {
            loge("ScheduleUiViewModel: Error set: $message")
        }
    }

    /**
     * Очищает сообщение об ошибке
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Переключает состояние анимаций
     */
    fun toggleAnimations() {
        _animationsEnabled.value = !_animationsEnabled.value
        logd("ScheduleUiViewModel: Animations toggled to ${_animationsEnabled.value}")
    }

    /**
     * Сбрасывает все состояния
     */
    fun reset() {
        _sectionStates.value = emptyMap()
        _isLoading.value = false
        _errorMessage.value = null
        _animationsEnabled.value = true
        logd("ScheduleUiViewModel: All states reset")
    }
}
