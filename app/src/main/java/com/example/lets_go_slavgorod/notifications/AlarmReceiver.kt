package com.example.lets_go_slavgorod.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.repository.BusRouteRepository
import com.example.lets_go_slavgorod.utils.toFavoriteTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Получатель уведомлений о срабатывании будильника
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Alarm received: ${intent.action}")
        
        // Получаем данные из Intent
        val favoriteId = intent.getStringExtra("FAVORITE_ID")
        val routeInfo = intent.getStringExtra("ROUTE_INFO")
        val departureTimeInfo = intent.getStringExtra("DEPARTURE_TIME_INFO")
        val destinationInfo = intent.getStringExtra("DESTINATION_INFO")
        val departurePointInfo = intent.getStringExtra("DEPARTURE_POINT_INFO")
        
        Timber.d("Alarm data - favoriteId: $favoriteId, routeInfo: $routeInfo")
        
        // Отправляем уведомление
        if (favoriteId != null) {
            val safeRouteInfo = if (routeInfo.isNullOrBlank()) "Автобус" else routeInfo
            val safeDepartureTimeInfo = if (departureTimeInfo.isNullOrBlank()) "Время отправления" else departureTimeInfo
            val safeDeparturePointInfo = if (departurePointInfo.isNullOrBlank()) "Пункт отправления" else departurePointInfo
            
            Timber.d("Sending notification with: routeInfo='$safeRouteInfo', departureTime='$safeDepartureTimeInfo'")
            
            NotificationHelper.showDepartureNotification(
                context = context,
                favoriteTimeId = favoriteId,
                routeInfo = safeRouteInfo,
                departureTimeInfo = safeDepartureTimeInfo,
                destinationInfo = destinationInfo ?: "",
                departurePointInfo = safeDeparturePointInfo
            )
            
            // ВАЖНО: Перепланируем уведомление на следующий день
            rescheduleNotification(context, favoriteId)
        } else {
            Timber.w("No favoriteId found in alarm intent")
        }
    }
    
    // Перепланирование уведомления на следующий день
    private fun rescheduleNotification(context: Context, favoriteId: String) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val favoriteTimeDao = database.favoriteTimeDao()
                val repository = BusRouteRepository()
                
                // Получаем данные из БД
                val favoriteEntity = favoriteTimeDao.getAllFavoriteTimes().firstOrNull()
                    ?.find { it.id == favoriteId }
                
                if (favoriteEntity != null && favoriteEntity.isActive) {
                    val favoriteTime = favoriteEntity.toFavoriteTime(repository)
                    
                    // Планируем следующее уведомление
                    AlarmScheduler.scheduleAlarm(context, favoriteTime)
                    Timber.d("Rescheduled notification for next occurrence: $favoriteId")
                } else {
                    Timber.w("FavoriteTime not found or inactive, not rescheduling: $favoriteId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error rescheduling notification for $favoriteId")
            }
        }
    }
}
