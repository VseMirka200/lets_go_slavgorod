package com.example.lets_go_slavgorod.data.repository

import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.utils.Constants
import com.example.lets_go_slavgorod.utils.createBusRoute
import com.example.lets_go_slavgorod.utils.search
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Репозиторий для управления данными маршрутов автобусов.
 * Обеспечивает кэширование и единую точку доступа к данным.
 */
class BusRouteRepository {
    
    private val _routes = MutableStateFlow<List<BusRoute>>(emptyList())
    val routes: Flow<List<BusRoute>> = _routes.asStateFlow()
    
    private val routesCache = mutableMapOf<String, BusRoute>()
    
    init {
        loadInitialRoutes()
    }
    
    private fun loadInitialRoutes() {
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
        
        // Кэшируем маршруты для быстрого доступа
        sampleRoutes.forEach { route ->
            routesCache[route.id] = route
        }
        
        _routes.value = sampleRoutes
    }
    
    fun getRouteById(routeId: String?): BusRoute? {
        if (routeId == null) return null
        return routesCache[routeId]
    }
    
    fun searchRoutes(query: String): List<BusRoute> {
        return _routes.value.search(query)
    }
    
    fun getAllRoutes(): List<BusRoute> = _routes.value
}
