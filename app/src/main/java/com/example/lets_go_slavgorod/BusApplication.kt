package com.example.lets_go_slavgorod

import android.app.Application
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import timber.log.Timber
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.data.local.UpdatePreferences
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.data.repository.BusRouteRepository
import com.example.lets_go_slavgorod.notifications.AlarmScheduler
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

/**
 * Главный класс приложения "Поехали! Славгород"
 * 
 * Оптимизированное приложение для просмотра расписания автобусов в городе Славгороде.
 * Обеспечивает быстрый запуск, эффективную работу и удобный пользовательский интерфейс.
 * 
 * Основные функции:
 * - Быстрая инициализация с минимальной задержкой запуска
 * - Оптимизированная система логирования (Timber)
 * - Автоматическое создание каналов уведомлений
 * - Фоновая проверка обновлений приложения
 * - Восстановление запланированных уведомлений после перезагрузки
 * - Управление жизненным циклом корутин для асинхронных задач
 * 
 * Оптимизации производительности:
 * - Асинхронная инициализация тяжелых компонентов
 * - Кэширование данных для быстрого доступа
 * - Минимизация блокирующих операций в главном потоке
 * - Эффективное управление памятью
 * 
 * @author VseMirka200
 * @version 1.2
 * @since 1.0
 */
class BusApplication : MultiDexApplication() {
    
    // =====================================================================================
    //                              ОБЛАСТЬ ВИДИМОСТИ КОРУТИН
    // =====================================================================================
    
    /**
     * Область видимости корутин для фоновых задач приложения
     * 
     * Использует SupervisorJob для независимого выполнения задач:
     * - Ошибка в одной корутине не влияет на другие
     * - Dispatchers.IO для операций ввода-вывода
     * - Автоматическая отмена при завершении приложения
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // =====================================================================================
    //                              ЖИЗНЕННЫЙ ЦИКЛ ПРИЛОЖЕНИЯ
    // =====================================================================================
    
    /**
     * Вызывается при завершении работы приложения
     * 
     * Отменяет все активные корутины для корректного завершения работы:
     * - Предотвращает утечки памяти
     * - Останавливает фоновые задачи
     * - Освобождает ресурсы
     */
    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
    
    /**
     * Инициализация приложения при запуске
     * 
     * Оптимизированная последовательность инициализации для быстрого запуска:
     * 1. Критически важные компоненты (синхронно)
     * 2. Тяжелые операции (асинхронно в фоне)
     * 
     * Приоритеты инициализации:
     * - MultiDex: поддержка больших приложений
     * - Timber: система логирования
     * - NotificationHelper: каналы уведомлений
     * - Фоновые задачи: восстановление уведомлений, проверка обновлений
     */
    override fun onCreate() {
        super.onCreate()
        
        // =====================================================================================
        //                              КРИТИЧЕСКИ ВАЖНЫЕ КОМПОНЕНТЫ
        // =====================================================================================
        
        // Инициализируем MultiDex для поддержки core library desugaring
        MultiDex.install(this)
        
        // Инициализируем Timber для логирования (быстро, синхронно)
        initializeLogging()
        
        // Создаем каналы для уведомлений (быстро, синхронно)
        NotificationHelper.createNotificationChannel(this)
        
        // =====================================================================================
        //                              ФОНОВЫЕ ЗАДАЧИ
        // =====================================================================================
        
        // Запускаем тяжелые операции в фоне (не блокируют UI)
        applicationScope.launch {
            // Восстанавливаем запланированные уведомления
            rescheduleAlarmsOnStartup()
            
            // Запускаем автоматическую проверку обновлений
            startAutomaticUpdateCheck()
        }
    }
    
    // =====================================================================================
    //                              СИСТЕМА ЛОГИРОВАНИЯ
    // =====================================================================================
    
    /**
     * Инициализация системы логирования
     * 
     * Оптимизированная настройка логирования для максимальной производительности:
     * - Debug: полное логирование для разработки
     * - Release: только критические ошибки для продакшена
     * 
     * Преимущества:
     * - Минимальное влияние на производительность в релизе
     * - Подробная отладочная информация в debug режиме
     * - Автоматическая фильтрация по приоритету
     */
    private fun initializeLogging() {
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
    }
    
    // =====================================================================================
    //                              ВОССТАНОВЛЕНИЕ УВЕДОМЛЕНИЙ
    // =====================================================================================
    
    /**
     * Восстанавливает запланированные уведомления при запуске приложения
     * 
     * Критически важная функция для обеспечения непрерывности работы уведомлений.
     * Необходима для случаев:
     * - После перезагрузки устройства (система сбрасывает все AlarmManager)
     * - После обновления приложения (новые компоненты требуют переинициализации)
     * - После очистки данных приложения (восстановление из базы данных)
     * 
     * Алгоритм восстановления:
     * 1. Получение всех активных избранных времен из базы данных
     * 2. Валидация данных маршрутов
     * 3. Планирование уведомлений через AlarmScheduler
     * 4. Логирование результатов для отладки
     */
    private suspend fun rescheduleAlarmsOnStartup() {
        try {
            logd("Starting alarm rescheduling on app startup")
            
            val database = AppDatabase.getDatabase(this@BusApplication)
            val favoriteTimeDao = database.favoriteTimeDao()
            
            val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
            
            logd("Found ${favoriteTimeEntities.size} favorite times in database")
            
            var rescheduledCount = 0
            favoriteTimeEntities
                .filter { it.isActive }
                .forEach { entity: FavoriteTimeEntity ->
                    try {
                        val route = getRouteById(entity.routeId)
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
                        
                        AlarmScheduler.scheduleAlarm(this@BusApplication, favoriteTime)
                        rescheduledCount++
                        Timber.d("Rescheduled alarm for favorite time: ${entity.id}")
                    } catch (e: Exception) {
                        Timber.e(e, "Error rescheduling alarm for favorite time: ${entity.id}")
                    }
                }
            
            Timber.i("Successfully rescheduled $rescheduledCount out of ${favoriteTimeEntities.size} favorite times on startup")
            
        } catch (e: Exception) {
            Timber.e(e, "Error during alarm rescheduling on startup")
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
    private fun getRouteById(routeId: String): BusRoute? {
        return try {
            val repository = BusRouteRepository()
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