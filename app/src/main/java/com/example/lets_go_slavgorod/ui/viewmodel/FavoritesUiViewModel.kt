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
 * ViewModel для управления UI состоянием экрана избранного
 * 
 * Функциональность:
 * - Управление состоянием сворачивания групп избранного
 * - Фильтрация и сортировка избранного
 * - Управление анимациями
 * - Обработка ошибок UI
 */
class FavoritesUiViewModel : ViewModel() {

    // Состояние сворачивания групп избранного
    private val _groupStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val groupStates: StateFlow<Map<String, Boolean>> = _groupStates.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Состояние ошибок
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Фильтр по маршрутам
    private val _routeFilter = MutableStateFlow<String?>(null)
    val routeFilter: StateFlow<String?> = _routeFilter.asStateFlow()

    // Сортировка
    private val _sortOrder = MutableStateFlow(FavoritesSortOrder.TIME_ASC)
    val sortOrder: StateFlow<FavoritesSortOrder> = _sortOrder.asStateFlow()

    // Состояние анимаций
    private val _animationsEnabled = MutableStateFlow(true)
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    /**
     * Переключает состояние группы избранного
     */
    fun toggleGroup(groupKey: String) {
        viewModelScope.launch {
            try {
                val currentStates = _groupStates.value.toMutableMap()
                val currentState = currentStates[groupKey] ?: true // По умолчанию развернута
                currentStates[groupKey] = !currentState
                _groupStates.value = currentStates
                
                logd("FavoritesUiViewModel: Toggled group '$groupKey' to ${!currentState}")
            } catch (e: Exception) {
                loge("FavoritesUiViewModel: Error toggling group", e)
                _errorMessage.value = "Ошибка при изменении группы"
            }
        }
    }

    /**
     * Устанавливает состояние группы
     */
    fun setGroupState(groupKey: String, isExpanded: Boolean) {
        viewModelScope.launch {
            try {
                val currentStates = _groupStates.value.toMutableMap()
                currentStates[groupKey] = isExpanded
                _groupStates.value = currentStates
                
                logd("FavoritesUiViewModel: Set group '$groupKey' to $isExpanded")
            } catch (e: Exception) {
                loge("FavoritesUiViewModel: Error setting group state", e)
            }
        }
    }

    /**
     * Получает состояние группы
     */
    fun isGroupExpanded(groupKey: String): Boolean {
        return _groupStates.value[groupKey] ?: true
    }

    /**
     * Устанавливает фильтр по маршруту
     */
    fun setRouteFilter(routeNumber: String?) {
        _routeFilter.value = routeNumber
        logd("FavoritesUiViewModel: Route filter set to '$routeNumber'")
    }

    /**
     * Устанавливает порядок сортировки
     */
    fun setSortOrder(order: FavoritesSortOrder) {
        _sortOrder.value = order
        logd("FavoritesUiViewModel: Sort order set to $order")
    }

    /**
     * Устанавливает состояние загрузки
     */
    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
        logd("FavoritesUiViewModel: Loading state set to $isLoading")
    }

    /**
     * Устанавливает сообщение об ошибке
     */
    fun setError(message: String?) {
        _errorMessage.value = message
        if (message != null) {
            loge("FavoritesUiViewModel: Error set: $message")
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
        logd("FavoritesUiViewModel: Animations toggled to ${_animationsEnabled.value}")
    }

    /**
     * Сбрасывает все состояния
     */
    fun reset() {
        _groupStates.value = emptyMap()
        _isLoading.value = false
        _errorMessage.value = null
        _routeFilter.value = null
        _sortOrder.value = FavoritesSortOrder.TIME_ASC
        _animationsEnabled.value = true
        logd("FavoritesUiViewModel: All states reset")
    }
}

/**
 * Порядок сортировки избранного
 */
enum class FavoritesSortOrder {
    TIME_ASC,      // По времени (по возрастанию)
    TIME_DESC,     // По времени (по убыванию)
    ROUTE_ASC,     // По маршруту (по возрастанию)
    ROUTE_DESC     // По маршруту (по убыванию)
}
