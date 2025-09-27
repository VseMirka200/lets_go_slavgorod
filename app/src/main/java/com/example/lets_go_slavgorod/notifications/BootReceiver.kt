package com.example.lets_go_slavgorod.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.model.FavoriteTime

/**
 * Получатель уведомлений о загрузке системы
 * 
 * Основные функции:
 * - Восстановление всех активных уведомлений после перезагрузки устройства
 * - Обработка событий BOOT_COMPLETED и обновления приложения
 * - Логирование событий для отладки
 * 
 * @author VseMirka200
 * @version 1.0
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot event received: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "System boot completed or app updated, restoring notifications...")
                
                // Восстанавливаем уведомления в фоновом потоке
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        restoreAllNotifications(context)
                        Log.d(TAG, "All notifications restored successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error restoring notifications", e)
                    }
                }
            }
            else -> {
                Log.w(TAG, "Unknown boot event: ${intent.action}")
            }
        }
    }
    
    /**
     * Восстанавливает все активные уведомления из базы данных
     * 
     * @param context контекст приложения
     */
    private suspend fun restoreAllNotifications(context: Context) {
        try {
            Log.d(TAG, "Starting notification restoration process...")
            
            // Получаем базу данных
            val database = AppDatabase.getDatabase(context.applicationContext)
            val favoriteTimeDao = database.favoriteTimeDao()
            
            // Получаем все активные избранные времена
            val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
            
            val activeFavoriteTimes = favoriteTimeEntities
                .filter { it.isActive }
                .map { entity ->
                    FavoriteTime(
                        id = entity.id,
                        routeId = entity.routeId,
                        routeNumber = "", // Будет получен из репозитория маршрутов
                        routeName = "",   // Будет получен из репозитория маршрутов
                        stopName = entity.stopName,
                        departureTime = entity.departureTime,
                        dayOfWeek = entity.dayOfWeek,
                        departurePoint = entity.departurePoint,
                        isActive = entity.isActive
                    )
                }
            
            Log.d(TAG, "Found ${activeFavoriteTimes.size} active favorite times to restore")
            
            // Восстанавливаем уведомления для каждого активного избранного времени
            activeFavoriteTimes.forEach { favoriteTime ->
                try {
                    AlarmScheduler.checkAndUpdateNotifications(context.applicationContext, favoriteTime)
                    Log.d(TAG, "Notification restored for favorite time: ${favoriteTime.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring notification for ${favoriteTime.id}", e)
                }
            }
            
            Log.d(TAG, "Notification restoration completed. Restored ${activeFavoriteTimes.size} notifications.")
            
        } catch (e: Exception) {
            Log.e(TAG, "Critical error during notification restoration", e)
        }
    }
}
