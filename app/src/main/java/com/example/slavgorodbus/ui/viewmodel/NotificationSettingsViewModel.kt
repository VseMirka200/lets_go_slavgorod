package com.example.slavgorodbus.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.slavgorodbus.data.local.dataStore
import com.example.slavgorodbus.data.local.AppDatabase
import com.example.slavgorodbus.data.model.FavoriteTime
import com.example.slavgorodbus.notifications.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.DayOfWeek

enum class NotificationMode {
    WEEKDAYS,
    ALL_DAYS,
    SELECTED_DAYS,
    DISABLED
}

class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        val NOTIFICATION_MODE_KEY = stringPreferencesKey("notification_mode")
        val SELECTED_DAYS_KEY = stringSetPreferencesKey("selected_notification_days") // ++ Ключ для выбранных дней ++
        const val TAG = "NotificationSettingsVM"
    }

    val currentNotificationMode: StateFlow<NotificationMode> =
        application.dataStore.data
            .map { preferences ->
                val modeName = preferences[NOTIFICATION_MODE_KEY] ?: NotificationMode.ALL_DAYS.name
                try {
                    NotificationMode.valueOf(modeName)
                } catch (_: IllegalArgumentException) {
                    Log.w(TAG, "Invalid notification mode in DataStore: $modeName, defaulting to ALL_DAYS")
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
                        Log.w(TAG, "Invalid day name in DataStore: $dayName")
                        null
                    }
                }.toSet()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptySet()
            )

    fun setNotificationMode(mode: NotificationMode) {
        viewModelScope.launch {
            try {
                getApplication<Application>().dataStore.edit { settings ->
                    settings[NOTIFICATION_MODE_KEY] = mode.name
                    if (mode != NotificationMode.SELECTED_DAYS) {
                        settings.remove(SELECTED_DAYS_KEY)
                        Log.d(TAG, "Selected notification days cleared due to mode change to ${mode.name}.")
                    }
                }
                Log.d(TAG, "Notification mode set to: ${mode.name} and saved.")
                
                // Обновляем все активные уведомления
                updateAllActiveAlarms()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save notification mode or clear days", e)
            }
        }
    }

    fun setSelectedNotificationDays(days: Set<DayOfWeek>) {
        viewModelScope.launch {
            try {
                val dayNames = days.map { it.name }.toSet()
                getApplication<Application>().dataStore.edit { settings ->
                    settings[SELECTED_DAYS_KEY] = dayNames
                }
                Log.d(TAG, "Selected notification days saved: $dayNames")
                if (currentNotificationMode.value != NotificationMode.SELECTED_DAYS) {
                    setNotificationMode(NotificationMode.SELECTED_DAYS)
                } else {
                    // Обновляем все активные уведомления
                    updateAllActiveAlarms()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save selected notification days", e)
            }
        }
    }

    /**
     * Обновляет все активные уведомления в соответствии с текущими настройками
     */
    private fun updateAllActiveAlarms() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating all active alarms based on notification settings")
                
                val database = AppDatabase.getDatabase(getApplication())
                val favoriteTimeDao = database.favoriteTimeDao()
                
                val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
                
                val activeFavoriteTimes = favoriteTimeEntities
                    .filter { it.isActive }
                    .map { entity ->
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
                Log.d(TAG, "Updated ${activeFavoriteTimes.size} active alarms")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating active alarms", e)
            }
        }
    }

    /**
     * Обновляет уведомления для конкретного избранного времени
     */
    fun updateNotificationForFavoriteTime(favoriteTime: FavoriteTime) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating notification for favorite time: ${favoriteTime.id}")
                AlarmScheduler.checkAndUpdateNotifications(getApplication(), favoriteTime)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating notification for favorite time: ${favoriteTime.id}", e)
            }
        }
    }
}