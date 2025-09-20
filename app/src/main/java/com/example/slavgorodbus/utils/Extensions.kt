package com.example.slavgorodbus.utils

import android.content.Context
import android.util.Log
import com.example.slavgorodbus.data.local.entity.FavoriteTimeEntity
import com.example.slavgorodbus.data.model.BusRoute
import com.example.slavgorodbus.data.model.FavoriteTime
import com.example.slavgorodbus.data.repository.BusRouteRepository

/**
 * Extension функции для улучшения читаемости и переиспользования кода
 */

/**
 * Конвертирует FavoriteTimeEntity в FavoriteTime с получением данных маршрута
 */
fun FavoriteTimeEntity.toFavoriteTime(routeRepository: BusRouteRepository): FavoriteTime {
    val route = routeRepository.getRouteById(this.routeId)
    return FavoriteTime(
        id = this.id,
        routeId = this.routeId,
        routeNumber = route?.routeNumber ?: "N/A",
        routeName = route?.name ?: "Неизвестный маршрут",
        stopName = this.stopName,
        departureTime = this.departureTime,
        dayOfWeek = this.dayOfWeek,
        departurePoint = this.departurePoint,
        isActive = this.isActive
    )
}

/**
 * Безопасное логирование с автоматическим тегом
 */
inline fun <reified T> T.logd(message: String) {
    Log.d(T::class.java.simpleName, message)
}

inline fun <reified T> T.loge(message: String, throwable: Throwable? = null) {
    Log.e(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logi(message: String) {
    Log.i(T::class.java.simpleName, message)
}

inline fun <reified T> T.logw(message: String) {
    Log.w(T::class.java.simpleName, message)
}

/**
 * Проверяет, является ли строка пустой или null
 */
fun String?.isNullOrBlank(): Boolean = this.isNullOrBlank()

/**
 * Extension для List<BusRoute> - поиск по ID
 */
fun List<BusRoute>.findById(id: String?): BusRoute? = 
    if (id == null) null else find { it.id == id }

/**
 * Extension для поиска маршрутов
 */
fun List<BusRoute>.search(query: String): List<BusRoute> {
    if (query.isBlank()) return this
    
    val lowercaseQuery = query.lowercase()
    return filter { route ->
        route.routeNumber.lowercase().contains(lowercaseQuery) ||
        route.name.lowercase().contains(lowercaseQuery) ||
        route.description.lowercase().contains(lowercaseQuery)
    }
}


/**
 * Extension для создания BusRoute с валидацией
 */
fun createBusRoute(
    id: String,
    routeNumber: String,
    name: String,
    description: String,
    travelTime: String,
    pricePrimary: String,
    paymentMethods: String,
    color: String = Constants.DEFAULT_ROUTE_COLOR
): BusRoute? = try {
    BusRoute(
        id = id,
        routeNumber = routeNumber,
        name = name,
        description = description,
        travelTime = travelTime,
        pricePrimary = pricePrimary,
        paymentMethods = paymentMethods,
        color = color
    )
} catch (e: Exception) {
    e.printStackTrace()
    null
}
