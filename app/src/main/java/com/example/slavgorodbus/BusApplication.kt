package com.example.slavgorodbus

import android.app.Application
import android.util.Log
import com.example.slavgorodbus.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BusApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d("BusApplication", "Application onCreate() called")
        
        NotificationHelper.createNotificationChannel(this)
        
        rescheduleAlarmsOnStartup()
    }
    
    private fun rescheduleAlarmsOnStartup() {
        applicationScope.launch {
            try {
                Log.d("BusApplication", "Starting alarm rescheduling on app startup")
                
                val database = com.example.slavgorodbus.data.local.AppDatabase.getDatabase(this@BusApplication)
                val favoriteTimeDao = database.favoriteTimeDao()
                
                val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
                
                Log.d("BusApplication", "Found ${favoriteTimeEntities.size} favorite times in database")
                
                var rescheduledCount = 0
                favoriteTimeEntities
                    .filter { it.isActive }
                    .forEach { entity ->
                        try {
                            val route = getRouteById(entity.routeId)
                            val favoriteTime = com.example.slavgorodbus.data.model.FavoriteTime(
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
                            
                            com.example.slavgorodbus.notifications.AlarmScheduler.scheduleAlarm(this@BusApplication, favoriteTime)
                            rescheduledCount++
                            Log.d("BusApplication", "Rescheduled alarm for favorite time: ${entity.id}")
                        } catch (e: Exception) {
                            Log.e("BusApplication", "Error rescheduling alarm for favorite time: ${entity.id}", e)
                        }
                    }
                
                Log.i("BusApplication", "Successfully rescheduled $rescheduledCount out of ${favoriteTimeEntities.size} favorite times on startup")
                
            } catch (e: Exception) {
                Log.e("BusApplication", "Error during alarm rescheduling on startup", e)
            }
        }
    }
    
    private fun getRouteById(routeId: String): com.example.slavgorodbus.data.model.BusRoute? {
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
            Log.e("BusApplication", "Error creating route object for ID: $routeId", e)
            null
        }
    }
}