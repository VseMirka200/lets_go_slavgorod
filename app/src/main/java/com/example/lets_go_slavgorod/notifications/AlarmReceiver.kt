package com.example.lets_go_slavgorod.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

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
    
    companion object;

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Alarm received: ${intent.action}")
        
        // Получаем данные из Intent
        val favoriteId = intent.getStringExtra("FAVORITE_ID")
        val routeInfo = intent.getStringExtra("ROUTE_INFO")
        val departureTimeInfo = intent.getStringExtra("DEPARTURE_TIME_INFO")
        val destinationInfo = intent.getStringExtra("DESTINATION_INFO")
        val departurePointInfo = intent.getStringExtra("DEPARTURE_POINT_INFO")
        
        Timber.d("Alarm data - favoriteId: $favoriteId, routeInfo: $routeInfo")
        
        // Отправляем уведомление с улучшенной обработкой данных
        if (favoriteId != null) {
            // Улучшенная обработка routeInfo
            val safeRouteInfo = if (routeInfo.isNullOrBlank()) {
                "Автобус"
            } else {
                routeInfo
            }
            
            val safeDepartureTimeInfo = if (departureTimeInfo.isNullOrBlank()) {
                "Время отправления"
            } else {
                departureTimeInfo
            }
            
            val safeDeparturePointInfo = if (departurePointInfo.isNullOrBlank()) {
                "Пункт отправления"
            } else {
                departurePointInfo
            }
            
            Timber.d("Sending notification with: routeInfo='$safeRouteInfo', departureTime='$safeDepartureTimeInfo'")
            
            NotificationHelper.showDepartureNotification(
                context = context,
                favoriteTimeId = favoriteId,
                routeInfo = safeRouteInfo,
                departureTimeInfo = safeDepartureTimeInfo,
                destinationInfo = destinationInfo ?: "",
                departurePointInfo = safeDeparturePointInfo
            )
        } else {
            Timber.w("No favoriteId found in alarm intent")
        }
    }
}
