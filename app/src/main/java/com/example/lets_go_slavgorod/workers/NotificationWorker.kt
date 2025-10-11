package com.example.lets_go_slavgorod.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.notifications.NotificationHelper
import com.example.lets_go_slavgorod.utils.TimeUtils
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Worker для отправки уведомлений через WorkManager
 * 
 * Более надежный способ планирования уведомлений по сравнению с AlarmManager:
 * - Гарантированное выполнение даже при Doze режиме
 * - Автоматическая обработка перезагрузок
 * - Умное управление батареей
 * - Поддержка повторных попыток при ошибках
 * 
 * @author VseMirka200
 * @version 1.0
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val KEY_SCHEDULE_ID = "schedule_id"
        const val KEY_ROUTE_NUMBER = "route_number"
        const val KEY_ROUTE_NAME = "route_name"
        const val KEY_DEPARTURE_TIME = "departure_time"
        const val KEY_STOP_NAME = "stop_name"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val scheduleId = inputData.getString(KEY_SCHEDULE_ID) ?: return Result.failure()
            val routeNumber = inputData.getString(KEY_ROUTE_NUMBER) ?: "N/A"
            val routeName = inputData.getString(KEY_ROUTE_NAME) ?: "Автобус"
            val departureTime = inputData.getString(KEY_DEPARTURE_TIME) ?: return Result.failure()
            val stopName = inputData.getString(KEY_STOP_NAME) ?: "Остановка"
            
            Timber.d("NotificationWorker: Processing notification for schedule $scheduleId")
            
            // Проверяем, что рейс еще актуален
            val minutesUntilDeparture = TimeUtils.getTimeUntilDeparture(departureTime)
            if (minutesUntilDeparture == null || minutesUntilDeparture < 0) {
                Timber.d("NotificationWorker: Schedule $scheduleId has already departed")
                return Result.success()
            }
            
            // Проверяем, что уведомление еще в избранном и активно
            val database = AppDatabase.getDatabase(applicationContext)
            val favoriteEntity = database.favoriteTimeDao()
                .getFavoriteTimeById(scheduleId)
                .first()
            
            if (favoriteEntity == null) {
                Timber.d("NotificationWorker: Schedule $scheduleId not found in favorites")
                return Result.success()
            }
            
            if (!favoriteEntity.isActive) {
                Timber.d("NotificationWorker: Schedule $scheduleId is inactive")
                return Result.success()
            }
            
            // Отправляем уведомление через NotificationHelper
            NotificationHelper.showDepartureNotification(
                context = applicationContext,
                favoriteTimeId = scheduleId,
                routeInfo = "Маршрут $routeNumber: $routeName",
                departureTimeInfo = departureTime,
                destinationInfo = stopName,
                departurePointInfo = stopName,
                enableVibration = true
            )
            
            Timber.d("NotificationWorker: Successfully sent notification for schedule $scheduleId")
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "NotificationWorker: Error processing notification")
            // Повторная попытка при ошибке
            Result.retry()
        }
    }
}

/**
 * Вспомогательные функции для создания Data для Worker
 */
fun createNotificationWorkData(
    scheduleId: String,
    routeNumber: String,
    routeName: String,
    departureTime: String,
    stopName: String
): Data {
    return Data.Builder()
        .putString(NotificationWorker.KEY_SCHEDULE_ID, scheduleId)
        .putString(NotificationWorker.KEY_ROUTE_NUMBER, routeNumber)
        .putString(NotificationWorker.KEY_ROUTE_NAME, routeName)
        .putString(NotificationWorker.KEY_DEPARTURE_TIME, departureTime)
        .putString(NotificationWorker.KEY_STOP_NAME, stopName)
        .build()
}

