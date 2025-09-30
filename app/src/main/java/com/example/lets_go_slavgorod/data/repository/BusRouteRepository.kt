package com.example.lets_go_slavgorod.data.repository

import android.content.Context
import timber.log.Timber
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.utils.CacheUtils
import com.example.lets_go_slavgorod.utils.Constants
import com.example.lets_go_slavgorod.utils.createBusRoute
import com.example.lets_go_slavgorod.utils.logd
import com.example.lets_go_slavgorod.utils.loge
import com.example.lets_go_slavgorod.utils.search
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Репозиторий для управления маршрутами автобусов
 */
class BusRouteRepository(private val context: Context? = null) {
    
    // Потоки данных и кэширование
    private val _routes = MutableStateFlow<List<BusRoute>>(emptyList())
    private val routesCache = mutableMapOf<String, BusRoute>()
    
    init {
        Timber.d("Repository initializing...")
        loadInitialRoutes()
        Timber.d("Repository initialization completed. Routes count: ${_routes.value.size}")
    }
    
    /**
     * Загружает начальные маршруты с оптимизацией
     * 
     * Логика загрузки:
     * 1. Попытка загрузки из кэша (если доступен)
     * 2. Создание базовых маршрутов
     * 3. Валидация данных
     * 4. Кэширование валидных маршрутов
     * 5. Сохранение в кэш (если есть контекст)
     */
    private fun loadInitialRoutes() {
        try {
            // Сначала пытаемся загрузить из кэша
            if (context != null && CacheUtils.hasValidCache(context)) {
                logd("Loading routes from cache")
                // В реальном приложении здесь был бы асинхронный вызов
                // CacheUtils.loadCachedRoutes(context)
            }
            
            // Загружаем базовые маршруты
            val sampleRoutes = listOfNotNull(
                createBusRoute(
                    id = "102",
                    routeNumber = "102",
                    name = "Автобус №102",
                    description = "Рынок (Славгород) — МСЧ-128 (Яровое)",
                    travelTime = "~35 минут",
                    pricePrimary = "38₽ город / 55₽ межгород",
                    paymentMethods = "Нал. / Безнал.",
                    color = Constants.DEFAULT_ROUTE_COLOR
                ),
                createBusRoute(
                    id = "1",
                    routeNumber = "1",
                    name = "Автобус №1",
                    description = "Маршрут вокзал — совхоз",
                    travelTime = "~24 минуты",
                    pricePrimary = "38₽ город",
                    paymentMethods = "Только нал.",
                    color = Constants.DEFAULT_ROUTE_COLOR_ALT
                )
            )
            
            // Валидируем и кэшируем маршруты
            val validRoutes = sampleRoutes.filter { route ->
                val isValid = route.isValid()
                if (!isValid) {
                    loge("Invalid route found: ${route.id}")
                }
                isValid
            }
            
            // Кэшируем валидные маршруты для быстрого доступа
            validRoutes.forEach { route ->
                routesCache[route.id] = route
            }
            
            _routes.value = validRoutes
            Timber.d("Routes set to _routes: ${validRoutes.size} routes")
            validRoutes.forEach { route ->
                Timber.d("Route in _routes: ${route.id} - ${route.name}")
            }
            
            // Сохраняем в кэш, если есть контекст
            if (context != null) {
                // В реальном приложении здесь был бы асинхронный вызов
                // CacheUtils.cacheRoutes(context, validRoutes)
            }
            
            logd("Loaded ${validRoutes.size} valid routes")
            
        } catch (e: Exception) {
            loge("Error loading initial routes", e)
            _routes.value = emptyList()
        }
    }
    
    /**
     * Получает маршрут по идентификатору
     * 
     * Оптимизация: использует локальный кэш для быстрого доступа
     * 
     * @param routeId идентификатор маршрута
     * @return объект BusRoute или null если не найден
     */
    fun getRouteById(routeId: String?): BusRoute? {
        if (routeId == null) return null
        return routesCache[routeId]
    }
    
    /**
     * Выполняет поиск маршрутов по запросу
     * 
     * @param query поисковый запрос
     * @return список найденных маршрутов
     */
    fun searchRoutes(query: String): List<BusRoute> {
        return _routes.value.search(query)
    }
    
    /**
     * Получает все доступные маршруты
     * 
     * @return список всех маршрутов
     */
    fun getAllRoutes(): List<BusRoute> = _routes.value

}
