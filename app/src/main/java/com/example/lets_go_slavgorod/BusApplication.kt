package com.example.lets_go_slavgorod

import android.app.Application
import android.util.Log
import com.example.lets_go_slavgorod.data.local.UpdatePreferences
import com.example.lets_go_slavgorod.notifications.NotificationHelper
import com.example.lets_go_slavgorod.updates.UpdateManager
import com.example.lets_go_slavgorod.utils.Constants
import com.example.lets_go_slavgorod.utils.createBusRoute
import com.example.lets_go_slavgorod.utils.logd
import com.example.lets_go_slavgorod.utils.loge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
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
        logd("Application onCreate() called")
        
        NotificationHelper.createNotificationChannel(this)
        
        rescheduleAlarmsOnStartup()
        
        // Запускаем автоматическую проверку обновлений
        startAutomaticUpdateCheck()
    }
    
    private fun rescheduleAlarmsOnStartup() {
        applicationScope.launch {
            try {
                logd("Starting alarm rescheduling on app startup")
                
                val database = com.example.lets_go_slavgorod.data.local.AppDatabase.getDatabase(this@BusApplication)
                val favoriteTimeDao = database.favoriteTimeDao()
                
                val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
                
                logd("Found ${favoriteTimeEntities.size} favorite times in database")
                
                var rescheduledCount = 0
                favoriteTimeEntities
                    .filter { it.isActive }
                    .forEach { entity: com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity ->
                        try {
                            val route = getRouteById(entity.routeId)
                            val favoriteTime = com.example.lets_go_slavgorod.data.model.FavoriteTime(
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
                            
                            com.example.lets_go_slavgorod.notifications.AlarmScheduler.scheduleAlarm(this@BusApplication, favoriteTime)
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
    
    private fun getRouteById(routeId: String): com.example.lets_go_slavgorod.data.model.BusRoute? {
        return try {
            // Используем репозиторий для получения маршрута
            val repository = com.example.lets_go_slavgorod.data.repository.BusRouteRepository()
            repository.getRouteById(routeId) ?: run {
                // Fallback для неизвестных маршрутов
                createBusRoute(
                    id = routeId,
                    routeNumber = routeId,
                    name = "Автобус №$routeId",
                    description = "Маршрут",
                    travelTime = "~40 минут",
                    pricePrimary = "38₽ город / 55₽ межгород",
                    paymentMethods = "Нал. / Безнал.",
                    color = Constants.DEFAULT_ROUTE_COLOR
                )
            }
        } catch (e: Exception) {
            loge("Error creating route object for ID: $routeId", e)
            null
        }
    }
    
    /**
     * Запускает автоматическую проверку обновлений в фоне
     */
    private fun startAutomaticUpdateCheck() {
        applicationScope.launch {
            try {
                logd("Starting automatic update check")
                
                // Проверяем, включена ли автоматическая проверка обновлений
                val updatePreferences = UpdatePreferences(this@BusApplication)
                val autoUpdateEnabled = updatePreferences.autoUpdateCheckEnabled.firstOrNull() ?: true
                
                if (!autoUpdateEnabled) {
                    logd("Automatic update check is disabled by user")
                    return@launch
                }
                
                // Ждем 5 секунд после запуска приложения, чтобы не блокировать UI
                delay(5000)
                
                val updateManager = UpdateManager(this@BusApplication)
                val result = updateManager.checkForUpdatesWithResult()
                
                if (result.success && result.update != null) {
                    logd("Automatic update check found new version: ${result.update.versionName}")
                    
                    // Сохраняем информацию о доступном обновлении
                    updatePreferences.setAvailableUpdate(
                        version = result.update.versionName,
                        url = result.update.downloadUrl,
                        notes = result.update.releaseNotes
                    )
                } else if (result.success) {
                    logd("Automatic update check: no updates available")
                    // Очищаем информацию о доступном обновлении, если его больше нет
                    updatePreferences.clearAvailableUpdate()
                } else {
                    loge("Automatic update check failed: ${result.error}")
                }
                
                // Обновляем время последней проверки
                updatePreferences.setLastUpdateCheckTime(System.currentTimeMillis())
                
            } catch (e: Exception) {
                loge("Error during automatic update check", e)
            }
        }
    }
}