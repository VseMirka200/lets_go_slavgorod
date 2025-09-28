package com.example.lets_go_slavgorod.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.lets_go_slavgorod.data.local.dataStore
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.ui.viewmodel.NotificationMode
import com.example.lets_go_slavgorod.utils.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.Calendar

/**
 * Менеджер планирования уведомлений о времени отправления автобусов
 * 
 * Основные функции:
 * - Планирование уведомлений с учетом пользовательских настроек
 * - Отмена уведомлений при удалении избранного времени
 * - Обновление всех уведомлений при изменении настроек
 * - Поддержка различных режимов уведомлений (все дни/будни/выбранные дни/отключено)
 */
object AlarmScheduler {

    // Префикс для кодов запросов будильников
    private const val ALARM_REQUEST_CODE_PREFIX = Constants.ALARM_REQUEST_CODE_PREFIX
    // Время опережения уведомления (5 минут до отправления)
    private const val FIVE_MINUTES_IN_MILLIS = Constants.NOTIFICATION_LEAD_TIME_MINUTES * 60 * 1000L

    /**
     * Проверяет, должны ли отправляться уведомления в соответствии с настройками пользователя
     * 
     * Учитывает различные режимы уведомлений:
     * - DISABLED: уведомления отключены
     * - ALL_DAYS: уведомления каждый день
     * - WEEKDAYS: уведомления только в будни
     * - SELECTED_DAYS: уведомления в выбранные дни недели
     * 
     * @param context контекст приложения для доступа к настройкам
     * @param routeId ID маршрута для проверки индивидуальных настроек
     * @return true если уведомление должно быть отправлено
     */
    private fun shouldSendNotification(context: Context, routeId: String? = null): Boolean {
        return try {
            val preferences = runBlocking { context.dataStore.data.first() }
            
            // Проверяем индивидуальные настройки маршрута, если указан routeId
            val (notificationModeString, selectedDaysString) = if (routeId != null) {
                val routeMode = preferences[stringPreferencesKey("route_notification_mode_$routeId")]
                val routeDays = preferences[stringSetPreferencesKey("route_selected_days_$routeId")]
                if (routeMode != null) {
                    routeMode to routeDays
                } else {
                    // Используем глобальные настройки
                    preferences[stringPreferencesKey("notification_mode")] to preferences[stringSetPreferencesKey("selected_notification_days")]
                }
            } else {
                // Используем глобальные настройки
                preferences[stringPreferencesKey("notification_mode")] to preferences[stringSetPreferencesKey("selected_notification_days")]
            }
            
            val notificationMode = try {
                NotificationMode.valueOf(notificationModeString ?: NotificationMode.ALL_DAYS.name)
            } catch (_: IllegalArgumentException) {
                Timber.w("Invalid notification mode: $notificationModeString, defaulting to ALL_DAYS")
                NotificationMode.ALL_DAYS
            }

            when (notificationMode) {
                NotificationMode.DISABLED -> {
                    Timber.d("Notifications disabled by user settings")
                    false
                }
                NotificationMode.ALL_DAYS -> {
                    Timber.d("Notifications enabled for all days")
                    true
                }
                NotificationMode.WEEKDAYS -> {
                    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    val isWeekday = currentDay in Calendar.MONDAY..Calendar.FRIDAY
                    Timber.d("Weekdays only mode: current day $currentDay, is weekday: $isWeekday")
                    isWeekday
                }
                NotificationMode.SELECTED_DAYS -> {
                    val selectedDays = selectedDaysString?.mapNotNull { dayName ->
                        try {
                            DayOfWeek.valueOf(dayName)
                        } catch (_: IllegalArgumentException) {
                            Timber.w("Invalid day name in settings: $dayName")
                            null
                        }
                    }?.toSet() ?: emptySet()
                    
                    val currentDayOfWeek = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> DayOfWeek.SUNDAY
                        Calendar.MONDAY -> DayOfWeek.MONDAY
                        Calendar.TUESDAY -> DayOfWeek.TUESDAY
                        Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
                        Calendar.THURSDAY -> DayOfWeek.THURSDAY
                        Calendar.FRIDAY -> DayOfWeek.FRIDAY
                        Calendar.SATURDAY -> DayOfWeek.SATURDAY
                        else -> null
                    }
                    
                    val isSelectedDay = currentDayOfWeek != null && currentDayOfWeek in selectedDays
                    Timber.d("Selected days mode: selected days $selectedDays, current day $currentDayOfWeek, is selected: $isSelectedDay")
                    isSelectedDay
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking notification settings, defaulting to enabled")
            true
        }
    }

    /**
     * Планирует уведомление для избранного времени
     * 
     * Создает точный будильник за 5 минут до времени отправления автобуса.
     * Учитывает настройки пользователя и совместимость с версиями Android.
     * 
     * @param context контекст приложения
     * @param favoriteTime избранное время для планирования уведомления
     */
    @SuppressLint("TimberArgCount", "ThrowableNotAtBeginning", "BinaryOperationInTimber")
    fun scheduleAlarm(context: Context, favoriteTime: FavoriteTime) {
        if (!shouldSendNotification(context, favoriteTime.routeId)) {
            Timber.d("Notification skipped for ${favoriteTime.id} due to user settings")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Timber.e("AlarmManager is null. Cannot schedule alarm for ID ${favoriteTime.id}.")
            return
        }

        val calculatedDepartureTime = calculateNextDepartureTimeInMillis(context, favoriteTime)
        if (calculatedDepartureTime == -1L) {
            Timber.e("Failed to calculate a valid departure time for ${favoriteTime.id}. Not scheduling.")
            return
        }

        val triggerAtMillis = calculatedDepartureTime - FIVE_MINUTES_IN_MILLIS

        if (triggerAtMillis <= System.currentTimeMillis()) {
            Timber.w(
                "AlarmScheduler",
                "Alarm time for ${favoriteTime.id} (Route ${favoriteTime.routeNumber} at ${favoriteTime.departureTime}) " +
                        "is in the past or too soon (${formatMillis(triggerAtMillis)}). Not scheduling."
            )
            return
        }

        // Улучшенная обработка номера маршрута для уведомления
        val routeNumber = favoriteTime.routeNumber.trim()
        val routeInfoForNotification = if (routeNumber.isNotBlank()) {
            "Автобус №$routeNumber"
        } else {
            // Fallback: используем routeId или общее название
            val fallbackNumber = favoriteTime.routeId ?: "Неизвестный"
            "Маршрут $fallbackNumber"
        }
        val departureTimeInfoForNotification = "в ${favoriteTime.departureTime.trim()}"
        
        Timber.d("Route number for notification: '${favoriteTime.routeNumber}' -> '$routeInfoForNotification'")
        Timber.d("Full favoriteTime data: routeId='${favoriteTime.routeId}', routeNumber='${favoriteTime.routeNumber}', routeName='${favoriteTime.routeName}'")
        val destinationInfoForNotification = ""
        val departurePointStr = favoriteTime.departurePoint.trim()
        val departurePointInfoForNotification = if (departurePointStr.isNotBlank()) {
            "От: $departurePointStr"
        } else {
            ""
        }

        Timber.d(
            "AlarmScheduler",
            "%snull", "%snull", "Data for Intent: favoriteId='${favoriteTime.id}', " +
                    "routeInfo='$routeInfoForNotification', " +
                    "departureTimeInfo='$departureTimeInfoForNotification', "
        )

        val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            action = "com.example.lets_go_slavgorod.ALARM_TRIGGER_${favoriteTime.id}"
            putExtra("FAVORITE_ID", favoriteTime.id)
            putExtra("ROUTE_INFO", routeInfoForNotification)
            putExtra("DEPARTURE_TIME_INFO", departureTimeInfoForNotification)
            putExtra("DESTINATION_INFO", destinationInfoForNotification)
            putExtra("DEPARTURE_POINT_INFO", departurePointInfoForNotification)
        }

        val requestCode = (ALARM_REQUEST_CODE_PREFIX + favoriteTime.id).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        Timber.d("Attempting to schedule alarm for ID ${favoriteTime.id} at ${formatMillis(triggerAtMillis)} (requestCode: $requestCode, action: ${intent.action})")
        Timber.d("Current time: ${formatMillis(System.currentTimeMillis())}, Target departure: ${formatMillis(calculatedDepartureTime)}")

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val canScheduleExact = alarmManager.canScheduleExactAlarms()
                    Timber.i("Android S+ detected. Can schedule exact alarms for ID ${favoriteTime.id}? $canScheduleExact")
                    if (canScheduleExact) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                        Timber.d("Exact alarm scheduled successfully for ID ${favoriteTime.id} at ${formatMillis(triggerAtMillis)}")
                    } else {
                        Timber.w(
                            "AlarmScheduler",
                            "Exact alarms NOT PERMITTED for ID ${favoriteTime.id}. Scheduling inexact alarm (setWindow)." +
                                    " User may need to grant permission in settings: ${Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM}"
                        )
                        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 60_000L, pendingIntent)
                        Timber.d("Inexact (window) alarm scheduled for ID ${favoriteTime.id} around ${formatMillis(triggerAtMillis)}")
                    }
                }
                else -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Timber.d(
                        "AlarmScheduler",
                        "Alarm (setExactAndAllowWhileIdle) scheduled for ID ${favoriteTime.id} at ${
                            formatMillis(triggerAtMillis)
                        } on Android M-R"
                    )
                }
            }
        } catch (se: SecurityException) {
            Timber.e("SecurityException: Cannot schedule alarm for ID ${favoriteTime.id}. " +
                    "Check permissions (e.g., SCHEDULE_EXACT_ALARM, WAKE_LOCK).", se)
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule alarm for ID ${favoriteTime.id}")
        }
    }

    fun cancelAlarm(context: Context, favoriteTimeId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Timber.e("AlarmManager is null. Cannot cancel alarm for ID $favoriteTimeId.")
            return
        }

        val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            action = "com.example.lets_go_slavgorod.ALARM_TRIGGER_${favoriteTimeId}"
        }
        val requestCode = (ALARM_REQUEST_CODE_PREFIX + favoriteTimeId).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            try {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Timber.d("Alarm cancelled successfully for ID $favoriteTimeId (requestCode: $requestCode, action: ${intent.action})")
            } catch (e: Exception) {
                Timber.e(e, "Error cancelling alarm for ID $favoriteTimeId")
            }
        } else {
            Timber.w("No alarm found to cancel for ID $favoriteTimeId (PendingIntent was null). " +
                    "This is normal if it was already cancelled or never scheduled with this ID/action/requestCode.")
        }
    }

    @SuppressLint("TimberArgCount")
    private fun calculateNextDepartureTimeInMillis(context: Context, favoriteTime: FavoriteTime): Long {
        if (favoriteTime.departureTime.isBlank()) {
            Timber.e("Departure time is blank for ID ${favoriteTime.id}")
            return -1L
        }
        val timeParts = favoriteTime.departureTime.split(":")
        if (timeParts.size != 2) {
            Timber.e("Invalid departure time format: '${favoriteTime.departureTime}' for ID ${favoriteTime.id}")
            return -1L
        }

        val hour: Int
        val minute: Int
        try {
            hour = timeParts[0].trim().toInt()
            minute = timeParts[1].trim().toInt()
        } catch (nfe: NumberFormatException) {
            Timber.e(nfe, "Invalid number format in departure time parts: '${favoriteTime.departureTime}' for ID ${favoriteTime.id}")
            return -1L
        }

        if (hour !in 0..23 || minute !in 0..59) {
            Timber.e("Invalid time values: hour=$hour, minute=$minute for ID ${favoriteTime.id}")
            return -1L
        }

        val targetDayOfWeek = favoriteTime.dayOfWeek
        if (targetDayOfWeek !in Calendar.SUNDAY..Calendar.SATURDAY) {
            Timber.e("Invalid dayOfWeek: $targetDayOfWeek for ID ${favoriteTime.id}. Expected ${Calendar.SUNDAY}-${Calendar.SATURDAY}.")
            return -1L
        }

        val now = Calendar.getInstance()
        val nextDepartureBase = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Проверяем, нужно ли учитывать день недели или планировать на каждый день
        val shouldCheckDayOfWeek = try {
            val preferences = runBlocking { context.dataStore.data.first() }
            // Проверяем индивидуальные настройки маршрута
            val routeMode = preferences[stringPreferencesKey("route_notification_mode_${favoriteTime.routeId}")]
            val notificationModeString = routeMode ?: preferences[stringPreferencesKey("notification_mode")] 
                ?: NotificationMode.ALL_DAYS.name
            val notificationMode = NotificationMode.valueOf(notificationModeString)
            notificationMode != NotificationMode.ALL_DAYS
        } catch (e: Exception) {
            Timber.w(e, "Error checking notification mode, defaulting to day-specific scheduling")
            true
        }

        if (shouldCheckDayOfWeek) {
            // Планируем только на указанный день недели
            for (i in 0..7) {
                val candidateDeparture = (nextDepartureBase.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, i)
                }
                if (candidateDeparture.get(Calendar.DAY_OF_WEEK) == targetDayOfWeek && candidateDeparture.after(now)) {
                    Timber.d("Calculated next departure for ${favoriteTime.id} (${favoriteTime.departureTime}, targetDay $targetDayOfWeek): ${formatMillis(candidateDeparture.timeInMillis)} (found after $i day iterations)")
                    return candidateDeparture.timeInMillis
                }
            }
        } else {
            // Планируем на каждый день в указанное время
            val nextDeparture = (nextDepartureBase.clone() as Calendar).apply {
                if (!after(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            Timber.d("Calculated next departure for ${favoriteTime.id} (${favoriteTime.departureTime}, every day): ${formatMillis(nextDeparture.timeInMillis)}")
            return nextDeparture.timeInMillis
        }

        Timber.e("Could not find a suitable future departure day within a week for ${favoriteTime.id}")
        return -1L
    }

    /**
     * Обновляет все активные уведомления в соответствии с текущими настройками
     */
    fun updateAllAlarmsBasedOnSettings(context: Context, favoriteTimes: List<FavoriteTime>) {
        Timber.d("Updating all alarms based on current notification settings")
        
        favoriteTimes.forEach { favoriteTime ->
            try {
                cancelAlarm(context, favoriteTime.id)
                
                if (shouldSendNotification(context, favoriteTime.routeId)) {
                    scheduleAlarm(context, favoriteTime)
                    Timber.d("Rescheduled alarm for ${favoriteTime.id} based on settings")
                } else {
                    Timber.d("Alarm for ${favoriteTime.id} cancelled due to settings")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating alarm for ${favoriteTime.id}")
            }
        }
    }

    /**
     * Проверяет и обновляет уведомления при изменении настроек
     */
    fun checkAndUpdateNotifications(context: Context, favoriteTime: FavoriteTime) {
        if (shouldSendNotification(context, favoriteTime.routeId)) {
            scheduleAlarm(context, favoriteTime)
            Timber.d("Notification scheduled for ${favoriteTime.id}")
        } else {
            cancelAlarm(context, favoriteTime.id)
            Timber.d("Notification cancelled for ${favoriteTime.id} due to settings")
        }
    }

    private fun formatMillis(millis: Long): String {
        return try {
            if (millis <= 0) return "Invalid or Past Millis"
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(millis)
        } catch (e: Exception) {
            Timber.e(e, "Error formatting millis: $millis")
            "Error formatting timestamp"
        }
    }
}