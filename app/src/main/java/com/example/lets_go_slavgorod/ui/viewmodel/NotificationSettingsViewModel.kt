package com.example.lets_go_slavgorod.ui.viewmodel

import android.app.Application
import timber.log.Timber
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.lets_go_slavgorod.data.local.dataStore
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.notifications.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/**
 * Режимы уведомлений о времени отправления автобусов
 * 
 * - WEEKDAYS: уведомления только в будни (понедельник-пятница)
 * - ALL_DAYS: уведомления каждый день
 * - SELECTED_DAYS: уведомления в выбранные пользователем дни недели
 * - DISABLED: уведомления отключены
 */
enum class NotificationMode {
    WEEKDAYS,
    ALL_DAYS,
    SELECTED_DAYS,
    DISABLED
}

/**
 * ViewModel для управления настройками уведомлений
 * 
 * Основные функции:
 * - Управление режимами уведомлений (все дни/будни/выбранные дни/отключено)
 * - Сохранение выбранных дней недели для уведомлений
 * - Обновление всех активных уведомлений при изменении настроек
 * - Интеграция с AlarmScheduler для планирования уведомлений
 */
class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        val NOTIFICATION_MODE_KEY = stringPreferencesKey("notification_mode")
        val SELECTED_DAYS_KEY = stringSetPreferencesKey("selected_notification_days")
        val ROUTE_NOTIFICATION_MODE_KEY = stringPreferencesKey("route_notification_mode_")
        val ROUTE_SELECTED_DAYS_KEY = stringSetPreferencesKey("route_selected_days_")
    }

    val currentNotificationMode: StateFlow<NotificationMode> =
        application.dataStore.data
            .map { preferences ->
                val modeName = preferences[NOTIFICATION_MODE_KEY] ?: NotificationMode.ALL_DAYS.name
                try {
                    NotificationMode.valueOf(modeName)
                } catch (_: IllegalArgumentException) {
                    Timber.w("Invalid notification mode in DataStore: $modeName, defaulting to ALL_DAYS")
                    NotificationMode.ALL_DAYS
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = NotificationMode.ALL_DAYS
            )

    val selectedNotificationDays: StateFlow<Set<DayOfWeek>> =
        application.dataStore.data
            .map { preferences ->
                val dayNames = preferences[SELECTED_DAYS_KEY] ?: emptySet()
                dayNames.mapNotNull { dayName ->
                    try {
                        DayOfWeek.valueOf(dayName)
                    } catch (_: IllegalArgumentException) {
                        Timber.w("Invalid day name in DataStore: $dayName")
                        null
                    }
                }.toSet()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptySet()
            )

    /**
     * Получает настройки уведомлений для конкретного маршрута
     */
    fun getRouteNotificationMode(routeId: String): StateFlow<NotificationMode> =
        getApplication<Application>().dataStore.data
            .map { preferences ->
                val modeName = preferences[stringPreferencesKey("${ROUTE_NOTIFICATION_MODE_KEY.name}$routeId")]
                    ?: currentNotificationMode.value.name
                try {
                    NotificationMode.valueOf(modeName)
                } catch (_: IllegalArgumentException) {
                    currentNotificationMode.value
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = currentNotificationMode.value
            )

    /**
     * Получает выбранные дни для конкретного маршрута
     */
    fun getRouteSelectedDays(routeId: String): StateFlow<Set<DayOfWeek>> =
        getApplication<Application>().dataStore.data
            .map { preferences ->
                val dayNames = preferences[stringSetPreferencesKey("${ROUTE_SELECTED_DAYS_KEY.name}$routeId")]
                    ?: selectedNotificationDays.value.map { it.name }.toSet()
                dayNames.mapNotNull { dayName ->
                    try {
                        DayOfWeek.valueOf(dayName)
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                }.toSet()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = selectedNotificationDays.value
            )

    /**
     * Устанавливает режим уведомлений для конкретного маршрута
     */
    fun setRouteNotificationMode(routeId: String, mode: NotificationMode) {
        viewModelScope.launch {
            try {
                getApplication<Application>().dataStore.edit { settings ->
                    settings[stringPreferencesKey("${ROUTE_NOTIFICATION_MODE_KEY.name}$routeId")] = mode.name
                    if (mode != NotificationMode.SELECTED_DAYS) {
                        settings.remove(stringSetPreferencesKey("${ROUTE_SELECTED_DAYS_KEY.name}$routeId"))
                    }
                }
                Timber.d("Route $routeId notification mode set to: ${mode.name}")
                updateAllActiveAlarms()
            } catch (e: Exception) {
                Timber.e(e, "Failed to save route notification mode")
            }
        }
    }

    /**
     * Устанавливает выбранные дни для конкретного маршрута
     */
    fun setRouteSelectedDays(routeId: String, days: Set<DayOfWeek>) {
        viewModelScope.launch {
            try {
                val dayNames = days.map { it.name }.toSet()
                getApplication<Application>().dataStore.edit { settings ->
                    settings[stringSetPreferencesKey("${ROUTE_SELECTED_DAYS_KEY.name}$routeId")] = dayNames
                }
                Timber.d("Route $routeId selected days saved: $dayNames")
                updateAllActiveAlarms()
            } catch (e: Exception) {
                Timber.e(e, "Failed to save route selected days")
            }
        }
    }

    /**
     * Обновляет все активные уведомления в соответствии с текущими настройками
     */
    private fun updateAllActiveAlarms() {
        viewModelScope.launch {
            try {
                Timber.d("Updating all active alarms based on notification settings")
                
                val database = AppDatabase.getDatabase(getApplication())
                val favoriteTimeDao = database.favoriteTimeDao()
                
                val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
                
                val activeFavoriteTimes = favoriteTimeEntities
                    .filter { it.isActive }
                    .map { entity: FavoriteTimeEntity ->
                        FavoriteTime(
                            id = entity.id,
                            routeId = entity.routeId,
                            routeNumber = "N/A",
                            routeName = "Маршрут",
                            stopName = entity.stopName,
                            departureTime = entity.departureTime,
                            dayOfWeek = entity.dayOfWeek,
                            departurePoint = entity.departurePoint,
                            isActive = entity.isActive
                        )
                    }
                
                AlarmScheduler.updateAllAlarmsBasedOnSettings(getApplication(), activeFavoriteTimes)
                Timber.d("Updated ${activeFavoriteTimes.size} active alarms")
                
            } catch (e: Exception) {
                Timber.e(e, "Error updating active alarms")
            }
        }
    }

}