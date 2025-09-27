package com.example.lets_go_slavgorod.utils

import android.util.Log
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.data.model.BusRoute
import timber.log.Timber

/**
 * Расширения для работы с логами и преобразованиями данных
 * 
 * Основные функции:
 * - Удобное логирование ошибок
 * - Преобразование Entity в модель данных
 * 
 * @author VseMirka200
 * @version 1.0
 */

/**
 * Логирует ошибку с тегом и сообщением (Timber)
 * 
 * @param tag тег для логирования
 * @param message сообщение об ошибке
 * @param throwable исключение (опционально)
 */
fun loge(tag: String, message: String, throwable: Throwable? = null) {
    if (throwable != null) {
        Timber.tag(tag).e(throwable, message)
    } else {
        Timber.tag(tag).e(message)
    }
}

/**
 * Логирует ошибку с сообщением (Timber, перегрузка для удобства)
 * 
 * @param message сообщение об ошибке
 * @param throwable исключение (опционально)
 */
fun loge(message: String, throwable: Throwable? = null) {
    if (throwable != null) {
        Timber.e(throwable, message)
    } else {
        Timber.e(message)
    }
}

/**
 * Логирует отладочную информацию (Timber)
 * 
 * @param message сообщение для отладки
 */
fun logd(message: String) {
    Timber.d(message)
}

/**
 * Логирует информационное сообщение (Timber)
 * 
 * @param message информационное сообщение
 */
fun logi(message: String) {
    Timber.i(message)
}

/**
 * Логирует предупреждение (Timber)
 * 
 * @param message сообщение-предупреждение
 */
fun logw(message: String) {
    Timber.w(message)
}

/**
 * Преобразует FavoriteTimeEntity в FavoriteTime
 * 
 * @param entity сущность из базы данных
 * @param routeRepository репозиторий маршрутов (не используется, но требуется для совместимости)
 * @return модель данных FavoriteTime
 */
fun FavoriteTimeEntity.toFavoriteTime(routeRepository: Any? = null): FavoriteTime {
    // Получаем данные о маршруте из репозитория
    var routeNumber = ""
    var routeName = ""
    
    if (routeRepository != null) {
        try {
            val routes = (routeRepository as com.example.lets_go_slavgorod.data.repository.BusRouteRepository).getAllRoutes()
            val route = routes.find { it.id == this.routeId }
            route?.let {
                routeNumber = it.id
                routeName = it.name
            }
        } catch (e: Exception) {
            loge("Error getting route info", e)
        }
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

/**
 * Создает объект BusRoute с заданными параметрами
 * 
 * @param id уникальный идентификатор маршрута
 * @param routeNumber номер маршрута
 * @param name название маршрута
 * @param description описание маршрута
 * @param travelTime время в пути
 * @param pricePrimary стоимость проезда
 * @param paymentMethods способы оплаты
 * @param color цвет маршрута
 * @return объект BusRoute
 */
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

/**
 * Расширение для поиска маршрутов по запросу
 * 
 * @param query поисковый запрос
 * @return отфильтрованный список маршрутов
 */
fun List<BusRoute>.search(query: String): List<BusRoute> {
    if (query.isBlank()) return this
    
    val lowercaseQuery = query.lowercase()
    return this.filter { route ->
        route.name.lowercase().contains(lowercaseQuery) ||
        route.routeNumber.lowercase().contains(lowercaseQuery) ||
        route.description.lowercase().contains(lowercaseQuery)
    }
}
