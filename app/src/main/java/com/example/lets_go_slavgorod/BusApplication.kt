package com.example.lets_go_slavgorod

import android.app.Application
import android.util.Log
import com.example.lets_go_slavgorod.BuildConfig
import com.example.lets_go_slavgorod.data.local.UpdatePreferences
import com.example.lets_go_slavgorod.notifications.NotificationHelper
import com.example.lets_go_slavgorod.updates.UpdateManager
import com.example.lets_go_slavgorod.utils.Constants
import com.example.lets_go_slavgorod.utils.createBusRoute
import com.example.lets_go_slavgorod.utils.logd
import com.example.lets_go_slavgorod.utils.loge
import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Главный класс приложения "Lets Go Slavgorod"
 * 
 * Основные функции:
 * - Инициализация Timber для логирования
 * - Создание каналов уведомлений
 * - Автоматическая проверка обновлений при запуске
 * - Управление жизненным циклом корутин
 * 
 * @author VseMirka200
 * @version 1.1
 * @since 1.0
 */
class BusApplication : Application() {
    
    /** Область видимости корутин для фоновых задач приложения */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Вызывается при завершении работы приложения
     * Отменяет все активные корутины
     */
    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
    
    /**
     * Инициализация приложения при запуске
     * 
     * Выполняет:
     * - Настройку системы логирования (синхронно)
     * - Создание каналов уведомлений (синхронно)
     * - Восстановление запланированных уведомлений (асинхронно)
     * - Автоматическую проверку обновлений (асинхронно)
     */
    override fun onCreate() {
        super.onCreate()
        
        // Инициализируем Timber для логирования (быстро)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // В релизной сборке логируем только критические ошибки
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    if (priority >= android.util.Log.ERROR) {
                        loge("Release", message, t)
                    }
                }
            })
        }
        
        logd("Application onCreate() called")
        
        // Создаем каналы для уведомлений (быстро)
        NotificationHelper.createNotificationChannel(this)
        
        // Запускаем тяжелые операции в фоне (не блокируют UI)
        applicationScope.launch {
            // Восстанавливаем запланированные уведомления
            rescheduleAlarmsOnStartup()
            
            // Запускаем автоматическую проверку обновлений
            startAutomaticUpdateCheck()
        }
    }
    
    /**
     * Восстанавливает запланированные уведомления при запуске приложения
     * 
     * Это необходимо для случаев:
     * - После перезагрузки устройства
     * - После обновления приложения
     * - После очистки данных приложения
     */
    private suspend fun rescheduleAlarmsOnStartup() {
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
    
    /**
     * Получает объект маршрута по его идентификатору
     * 
     * @param routeId идентификатор маршрута
     * @return объект BusRoute или null при ошибке
     * 
     * Логика:
     * 1. Пытается найти маршрут в репозитории
     * 2. Если не найден, создает fallback объект с базовой информацией
     * 3. Возвращает null только при критической ошибке
     */
    private fun getRouteById(routeId: String): com.example.lets_go_slavgorod.data.model.BusRoute? {
        return try {
            val repository = com.example.lets_go_slavgorod.data.repository.BusRouteRepository()
            repository.getRouteById(routeId) ?: run {
                // Создаем fallback объект для неизвестных маршрутов
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
     * 
     * Проверяет:
     * - Включена ли автоматическая проверка в настройках
     * - Доступность интернета
     * - Наличие новых версий на GitHub
     * 
     * При обнаружении обновления:
     * - Сохраняет информацию в UpdatePreferences
     * - Показывает уведомление пользователю
     */
    private suspend fun startAutomaticUpdateCheck() {
        try {
            logd("Starting automatic update check")
            
            // Проверяем, включена ли автоматическая проверка обновлений
            val updatePreferences = UpdatePreferences(this@BusApplication)
            val autoUpdateEnabled = updatePreferences.autoUpdateCheckEnabled.firstOrNull() ?: true
            
            if (!autoUpdateEnabled) {
                logd("Automatic update check is disabled by user")
                return
            }
            
            // Ждем 5 секунд после запуска приложения, чтобы не блокировать UI
            delay(5000)
            
            val updateManager = UpdateManager(this@BusApplication)
            val result = updateManager.checkForUpdatesWithResult()
            
            when {
                result.success && result.update != null -> {
                    logd("Automatic update check found new version: ${result.update.versionName}")
                    
                    // Валидируем данные обновления перед сохранением
                    if (result.update.versionName.isNotBlank() && result.update.downloadUrl.isNotBlank()) {
                        updatePreferences.setAvailableUpdate(
                            version = result.update.versionName,
                            url = result.update.downloadUrl,
                            notes = result.update.releaseNotes
                        )
                        
                        // Показываем уведомление о доступном обновлении
                        try {
                            NotificationHelper.showUpdateNotification(
                                context = this@BusApplication,
                                versionName = result.update.versionName,
                                releaseNotes = result.update.releaseNotes
                            )
                            logd("Update notification shown for version ${result.update.versionName}")
                        } catch (e: Exception) {
                            loge("Error showing update notification", e)
                        }
                    } else {
                        loge("Invalid update data received: version='${result.update.versionName}', url='${result.update.downloadUrl}'")
                    }
                }
                result.success -> {
                    logd("Automatic update check: no updates available")
                    // Очищаем информацию о доступном обновлении, если его больше нет
                    updatePreferences.clearAvailableUpdate()
                }
                else -> {
                    loge("Automatic update check failed: ${result.error}")
                    // Не очищаем кэш при ошибке, чтобы не потерять данные
                }
            }
            
            // Обновляем время последней проверки
            updatePreferences.setLastUpdateCheckTime(System.currentTimeMillis())
            
        } catch (e: Exception) {
            loge("Error during automatic update check", e)
            // В случае ошибки не прерываем работу приложения
        }
    }
}