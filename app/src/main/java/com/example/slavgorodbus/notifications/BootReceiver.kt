package com.example.slavgorodbus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.slavgorodbus.data.local.AppDatabase
import com.example.slavgorodbus.data.model.FavoriteTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("BootReceiver", "BootReceiver triggered with action: ${intent?.action}")
        
        if (context == null) {
            Log.e("BootReceiver", "Context is null, cannot reschedule alarms")
            return
        }
        
        val action = intent?.action
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.i("BootReceiver", "System boot or app update detected, rescheduling alarms")
                rescheduleAllAlarms(context)
            }
            else -> {
                Log.w("BootReceiver", "Unknown action: $action")
            }
        }
    }
    
    private fun rescheduleAllAlarms(context: Context) {
        coroutineScope.launch {
            try {
                Log.d("BootReceiver", "Starting alarm rescheduling process")
                
                // Create notification channel first
                NotificationHelper.createNotificationChannel(context)
                
                // Get database instance
                val database = AppDatabase.getDatabase(context)
                val favoriteTimeDao = database.favoriteTimeDao()
                
                // Get all active favorite times
                val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
                
                Log.d("BootReceiver", "Found ${favoriteTimeEntities.size} favorite times in database")
                
                // Reschedule alarms for active favorite times
                var rescheduledCount = 0
                favoriteTimeEntities
                    .filter { it.isActive }
                    .forEach { entity ->
                        try {
                            val route = getRouteById(context, entity.routeId)
                            val favoriteTime = FavoriteTime(
                                id = entity.id,
                                routeId = entity.routeId,
                                routeNumber = route?.routeNumber ?: "N/A",
                                routeName = route?.name ?: "Неизвестный маршрут",
                                stopName = entity.stopName,
                                departureTime = entity.departureTime,
                                dayOfWeek = entity.dayOfWeek,
                                departurePoint = entity.departurePoint,
                                isActive = entity.isActive
                            )
                            
                            AlarmScheduler.scheduleAlarm(context, favoriteTime)
                            rescheduledCount++
                            Log.d("BootReceiver", "Rescheduled alarm for favorite time: ${entity.id}")
                        } catch (e: Exception) {
                            Log.e("BootReceiver", "Error rescheduling alarm for favorite time: ${entity.id}", e)
                        }
                    }
                
                Log.i("BootReceiver", "Successfully rescheduled $rescheduledCount out of ${favoriteTimeEntities.size} favorite times")
                
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error during alarm rescheduling", e)
            }
        }
    }
    
    private suspend fun getRouteById(context: Context, routeId: String): com.example.slavgorodbus.data.model.BusRoute? {
        // This is a simplified version - in a real app, you might want to store routes in database too
        // For now, we'll return a basic route object
        return try {
            com.example.slavgorodbus.data.model.BusRoute(
                id = routeId,
                routeNumber = routeId,
                name = "Автобус №$routeId",
                description = "Маршрут",
                travelTime = "~40 минут",
                pricePrimary = "38₽ город / 55₽ межгород",
                paymentMethods = "Нал. / Безнал.",
                color = "#FF6200EE"
            )
        } catch (e: Exception) {
            Log.e("BootReceiver", "Error creating route object for ID: $routeId", e)
            null
        }
    }
}
