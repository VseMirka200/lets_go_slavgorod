package com.example.lets_go_slavgorod.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber

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
    
    companion object;

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Boot event received: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Timber.d("System boot completed or app updated, restoring notifications...")
                
                // Восстанавливаем уведомления в фоновом потоке
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        restoreAllNotifications(context)
                        Timber.d("All notifications restored successfully")
                    } catch (e: Exception) {
                        Timber.e(e, "Error restoring notifications")
                    }
                }
            }
            else -> {
                Timber.w("Unknown boot event: ${intent.action}")
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
            Timber.d("Starting notification restoration process...")
            
            // Получаем базу данных
            val database = AppDatabase.getDatabase(context.applicationContext)
            val favoriteTimeDao = database.favoriteTimeDao()
            
            // Получаем все активные избранные времена
            val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
            
            val activeFavoriteTimes = favoriteTimeEntities
                .filter { it.isActive }
                .map { entity: FavoriteTimeEntity ->
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
            
            Timber.d("Found ${activeFavoriteTimes.size} active favorite times to restore")
            
            // Восстанавливаем уведомления для каждого активного избранного времени
            activeFavoriteTimes.forEach { favoriteTime ->
                try {
                    AlarmScheduler.checkAndUpdateNotifications(context.applicationContext, favoriteTime)
                    Timber.d("Notification restored for favorite time: ${favoriteTime.id}")
                } catch (e: Exception) {
                    Timber.e(e, "Error restoring notification for ${favoriteTime.id}")
                }
            }
            
            Timber.d("Notification restoration completed. Restored ${activeFavoriteTimes.size} notifications.")
            
        } catch (e: Exception) {
            Timber.e(e, "Critical error during notification restoration")
        }
    }
}
