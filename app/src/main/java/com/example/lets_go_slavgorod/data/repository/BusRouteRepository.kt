package com.example.lets_go_slavgorod.data.repository

import android.content.Context
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.utils.CacheUtils
import com.example.lets_go_slavgorod.utils.Constants
import com.example.lets_go_slavgorod.utils.NetworkUtils
import com.example.lets_go_slavgorod.utils.createBusRoute
import com.example.lets_go_slavgorod.utils.logd
import com.example.lets_go_slavgorod.utils.loge
import com.example.lets_go_slavgorod.utils.search
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Репозиторий для управления данными маршрутов автобусов.
 * Обеспечивает кэширование и единую точку доступа к данным.
 */
class BusRouteRepository(private val context: Context? = null) {
    
    private val _routes = MutableStateFlow<List<BusRoute>>(emptyList())
    val routes: Flow<List<BusRoute>> = _routes.asStateFlow()
    
    private val routesCache = mutableMapOf<String, BusRoute>()
    
    init {
        loadInitialRoutes()
    }
    
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
                    description = "Маршрут Славгород — Яровое",
                    travelTime = "~40 минут",
                    pricePrimary = "38₽ город / 55₽ межгород",
                    paymentMethods = "Нал. / Безнал.",
                    color = Constants.DEFAULT_ROUTE_COLOR
                ),
                createBusRoute(
                    id = "1",
                    routeNumber = "1",
                    name = "Автобус №1",
                    description = "Маршрут Вокзал — Совхоз",
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
    
    fun getRouteById(routeId: String?): BusRoute? {
        if (routeId == null) return null
        return routesCache[routeId]
    }
    
    fun searchRoutes(query: String): List<BusRoute> {
        return _routes.value.search(query)
    }
    
    fun getAllRoutes(): List<BusRoute> = _routes.value
    
    /**
     * Проверяет доступность сети
     */
    fun isNetworkAvailable(): Boolean {
        return context?.let { NetworkUtils.isNetworkAvailable(it) } ?: false
    }
    
    /**
     * Получает тип соединения
     */
    fun getConnectionType(): String {
        return context?.let { NetworkUtils.getConnectionType(it) } ?: "Unknown"
    }
    
    /**
     * Обновляет кэш маршрутов
     */
    suspend fun refreshCache() {
        if (context != null) {
            try {
                val currentRoutes = _routes.value
                CacheUtils.cacheRoutes(context, currentRoutes)
                logd("Routes cache refreshed with ${currentRoutes.size} routes")
            } catch (e: Exception) {
                loge("Error refreshing routes cache", e)
            }
        }
    }
    
    /**
     * Очищает кэш маршрутов
     */
    fun clearCache() {
        if (context != null) {
            try {
                CacheUtils.clearRoutesCache(context)
                logd("Routes cache cleared")
            } catch (e: Exception) {
                loge("Error clearing routes cache", e)
            }
        }
    }
}
