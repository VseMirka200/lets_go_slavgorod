package com.example.lets_go_slavgorod.utils

import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.repository.BusRouteRepository
import timber.log.Timber

// Логирование ошибок с тегом
fun loge(tag: String, message: String, throwable: Throwable? = null) {
    if (throwable != null) {
        Timber.tag(tag).e(throwable, message)
    } else {
        Timber.tag(tag).e(message)
    }
}

// Логирование ошибок
fun loge(message: String, throwable: Throwable? = null) {
    if (throwable != null) {
        Timber.e(throwable, message)
    } else {
        Timber.e(message)
    }
}

// Логирование отладочной информации
fun logd(message: String) {
    Timber.d(message)
}


// Преобразование Entity в модель данных
fun FavoriteTimeEntity.toFavoriteTime(routeRepository: Any? = null): FavoriteTime {
    // Используем сохраненные в Entity данные о маршруте
    var routeNumber = this.routeNumber
    var routeName = this.routeName
    
    // Если данные в Entity пустые, пытаемся получить их из репозитория
    if (routeNumber.isBlank() && routeRepository != null) {
        try {
            val routes = (routeRepository as BusRouteRepository).getAllRoutes()
            val route = routes.find { it.id == this.routeId }
            route?.let {
                routeNumber = it.routeNumber
                routeName = it.name
                Timber.d("Found route info from repository: number='$routeNumber', name='$routeName'")
            } ?: run {
                Timber.w("Route not found for routeId: ${this.routeId}")
            }
        } catch (e: Exception) {
            loge("Error getting route info", e)
        }
    }
    
    // Fallback: если routeNumber все еще пустой, используем routeId
    if (routeNumber.isBlank()) {
        routeNumber = this.routeId ?: "Неизвестный"
        Timber.w("Using routeId as fallback routeNumber: '$routeNumber'")
    }
    
    if (routeName.isBlank()) {
        routeName = "Маршрут"
    }
    
    return FavoriteTime(
        id = this.id,
        routeId = this.routeId,
        routeNumber = routeNumber,
        routeName = routeName,
        stopName = this.stopName,
        departureTime = this.departureTime,
        dayOfWeek = this.dayOfWeek,
        departurePoint = this.departurePoint,
        isActive = this.isActive
    )
}

// Создание объекта маршрута
fun createBusRoute(
    id: String,
    routeNumber: String,
    name: String,
    description: String = "",
    travelTime: String = "",
    pricePrimary: String = "",
    paymentMethods: String = "",
    color: String = "#FF5722"
): BusRoute {
    return BusRoute(
        id = id,
        routeNumber = routeNumber,
        name = name,
        description = description,
        travelTime = travelTime,
        pricePrimary = pricePrimary,
        paymentMethods = paymentMethods,
        color = color
    )
}

// Поиск маршрутов по запросу
fun List<BusRoute>.search(query: String): List<BusRoute> {
    if (query.isBlank()) return this
    
    val lowercaseQuery = query.lowercase()
    return this.filter { route ->
        route.name.lowercase().contains(lowercaseQuery) ||
        route.routeNumber.lowercase().contains(lowercaseQuery) ||
        route.description.lowercase().contains(lowercaseQuery)
    }
}
