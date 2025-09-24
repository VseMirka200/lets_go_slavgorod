package com.example.lets_go_slavgorod.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    private var coroutineScope: CoroutineScope? = null
    
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
                coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                coroutineScope?.launch {
                    rescheduleAllAlarms(context)
                }
            }
            else -> {
                Log.w("BootReceiver", "Unknown action: $action")
            }
        }
    }
    
    private suspend fun rescheduleAllAlarms(context: Context) {
        try {
            Log.d("BootReceiver", "Starting alarm rescheduling process")
            
            NotificationHelper.createNotificationChannel(context)
            
            val database = AppDatabase.getDatabase(context)
            val favoriteTimeDao = database.favoriteTimeDao()
            
            val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
            
            Log.d("BootReceiver", "Found ${favoriteTimeEntities.size} favorite times in database")
            
            var rescheduledCount = 0
            favoriteTimeEntities
                .filter { it.isActive }
                .forEach { entity: FavoriteTimeEntity ->
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
        } finally {
            coroutineScope?.cancel()
        }
    }
    
    private suspend fun getRouteById(context: Context, routeId: String): com.example.lets_go_slavgorod.data.model.BusRoute? {
        return try {
            com.example.lets_go_slavgorod.data.model.BusRoute(
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
