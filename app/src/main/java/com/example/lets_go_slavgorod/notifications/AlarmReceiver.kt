package com.example.lets_go_slavgorod.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Получатель уведомлений о срабатывании будильника
 * 
 * Основные функции:
 * - Обработка срабатывания будильника для уведомлений
 * - Отправка уведомлений пользователю
 * - Логирование событий для отладки
 * 
 * @author VseMirka200
 * @version 1.0
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received: ${intent.action}")
        
        // Получаем данные из Intent
        val favoriteId = intent.getStringExtra("FAVORITE_ID")
        val routeInfo = intent.getStringExtra("ROUTE_INFO")
        val departureTimeInfo = intent.getStringExtra("DEPARTURE_TIME_INFO")
        val destinationInfo = intent.getStringExtra("DESTINATION_INFO")
        val departurePointInfo = intent.getStringExtra("DEPARTURE_POINT_INFO")
        
        Log.d(TAG, "Alarm data - favoriteId: $favoriteId, routeInfo: $routeInfo")
        
        // Отправляем уведомление
        if (favoriteId != null) {
            NotificationHelper.showDepartureNotification(
                context = context,
                favoriteTimeId = favoriteId,
                routeInfo = routeInfo ?: "Маршрут",
                departureTimeInfo = departureTimeInfo ?: "Время",
                destinationInfo = destinationInfo ?: "Направление",
                departurePointInfo = departurePointInfo ?: "Отправление"
            )
        } else {
            Log.w(TAG, "No favoriteId found in alarm intent")
        }
    }
}
