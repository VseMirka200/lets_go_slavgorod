package com.example.lets_go_slavgorod.ui.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.local.dataStore
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.notifications.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar

/**
 * ViewModel для управления тихим режимом уведомлений
 */
class QuietModeViewModel(private val context: Context) : ViewModel() {
    
    private val _quietMode = MutableStateFlow(QuietMode.ENABLED)
    val quietMode: StateFlow<QuietMode> = _quietMode.asStateFlow()
    
    private val _quietUntilTime = MutableStateFlow<Long?>(null)
    val quietUntilTime: StateFlow<Long?> = _quietUntilTime.asStateFlow()
    
    private val _customDays = MutableStateFlow(0)
    val customDays: StateFlow<Int> = _customDays.asStateFlow()
    
    companion object {
        private val QUIET_MODE_KEY = stringPreferencesKey("quiet_mode")
        private val QUIET_UNTIL_KEY = longPreferencesKey("quiet_until_time")
        private val CUSTOM_DAYS_KEY = stringPreferencesKey("custom_days_count")
    }
    
    init {
        loadQuietMode()
    }
    
    // Загрузка настроек тихого режима
    private fun loadQuietMode() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                val modeString = preferences[QUIET_MODE_KEY] ?: QuietMode.ENABLED.name
                val untilTime = preferences[QUIET_UNTIL_KEY]
                val customDaysStr = preferences[CUSTOM_DAYS_KEY]
                
                try {
                    val mode = QuietMode.valueOf(modeString)
                    
                    // Проверяем, не истек ли срок отключения
                    if (untilTime != null && untilTime < System.currentTimeMillis()) {
                        // Время истекло - включаем уведомления
                        setQuietMode(QuietMode.ENABLED)
                    } else {
                        _quietMode.value = mode
                        _quietUntilTime.value = untilTime
                        _customDays.value = customDaysStr?.toIntOrNull() ?: 0
                    }
                } catch (e: IllegalArgumentException) {
                    Timber.w("Invalid quiet mode: $modeString, resetting to ENABLED")
                    _quietMode.value = QuietMode.ENABLED
                }
            }
        }
    }
    
    // Установка режима уведомлений
    fun setQuietMode(mode: QuietMode, customDays: Int = 0) {
        viewModelScope.launch {
            try {
                val untilTime = if (mode == QuietMode.CUSTOM_DAYS && customDays > 0) {
                    calculateCustomDaysUntilTime(customDays)
                } else {
                    null
                }
                
                context.dataStore.edit { preferences ->
                    preferences[QUIET_MODE_KEY] = mode.name
                    if (untilTime != null) {
                        preferences[QUIET_UNTIL_KEY] = untilTime
                        preferences[CUSTOM_DAYS_KEY] = customDays.toString()
                    } else {
                        preferences.remove(QUIET_UNTIL_KEY)
                        preferences.remove(CUSTOM_DAYS_KEY)
                    }
                }
                
                _quietMode.value = mode
                _quietUntilTime.value = untilTime
                _customDays.value = customDays
                
                Timber.d("Notifications mode set to: $mode, days: $customDays, until: $untilTime")
                
                // Обновляем все уведомления в соответствии с новым режимом
                updateAllAlarms()
            } catch (e: Exception) {
                Timber.e(e, "Error setting quiet mode")
            }
        }
    }
    
    // Обновление всех уведомлений при изменении тихого режима
    private fun updateAllAlarms() {
        viewModelScope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
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
                
                AlarmScheduler.updateAllAlarmsBasedOnSettings(context, activeFavoriteTimes)
                Timber.d("Updated all alarms based on quiet mode change")
            } catch (e: Exception) {
                Timber.e(e, "Error updating alarms after quiet mode change")
            }
        }
    }
    
    // Вычисление времени окончания для пользовательского количества дней
    private fun calculateCustomDaysUntilTime(days: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    // Проверка, активны ли уведомления
    fun isQuietModeActive(): Boolean {
        val mode = _quietMode.value
        val untilTime = _quietUntilTime.value
        
        return when {
            mode == QuietMode.ENABLED -> false
            mode == QuietMode.DISABLED -> true
            mode == QuietMode.CUSTOM_DAYS && untilTime != null -> {
                val isActive = System.currentTimeMillis() < untilTime
                if (!isActive) {
                    // Время истекло - включаем уведомления
                    setQuietMode(QuietMode.ENABLED)
                }
                isActive
            }
            else -> false
        }
    }
}
