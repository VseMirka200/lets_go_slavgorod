package com.example.lets_go_slavgorod.utils

import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.repository.BusRouteRepository
import timber.log.Timber

/**
 * Расширения и утилитарные функции для проекта
 * 
 * Содержит вспомогательные функции для:
 * - Логирования (упрощенный доступ к Timber)
 * - Преобразования данных (Entity -> Model)
 * - Создания объектов (фабричные методы)
 * - Поиска и фильтрации данных
 * 
 * Все функции включают обработку ошибок и валидацию входных данных.
 * 
 * @author VseMirka200
 * @version 2.0
 * @since 1.0
 */

/**
 * Логирование ошибок с тегом
 * 
 * Упрощенный способ логирования ошибок с возможностью указания тега
 * для лучшей категоризации в логах.
 * 
 * @param tag тег для категоризации логов
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
 * Логирование ошибок без тега
 * 
 * Упрощенный способ логирования ошибок для общего использования.
 * Timber автоматически обрабатывает null значения throwable.
 * 
 * @param message сообщение об ошибке
 * @param throwable исключение (опционально)
 */
fun loge(message: String, throwable: Throwable? = null) {
    Timber.e(throwable, message)
}

/**
 * Логирование отладочной информации
 * 
 * Упрощенный способ логирования отладочных сообщений.
 * 
 * @param message отладочное сообщение
 */
fun logd(message: String) {
    Timber.d(message)
}


// Преобразование Entity в модель данных
fun FavoriteTimeEntity.toFavoriteTime(routeRepository: Any? = null): FavoriteTime {
    // Валидация входных данных
    if (this.id.isBlank()) {
        loge("FavoriteTimeEntity has blank ID")
        throw IllegalArgumentException("FavoriteTimeEntity ID cannot be blank")
    }
    
    if (this.routeId.isBlank()) {
        loge("FavoriteTimeEntity has blank routeId for ID: ${this.id}")
        throw IllegalArgumentException("FavoriteTimeEntity routeId cannot be blank")
    }
    
    // Используем сохраненные в Entity данные о маршруте
    var routeNumber = this.routeNumber
    var routeName = this.routeName
    
    // Если данные в Entity пустые, пытаемся получить их из репозитория
    if (routeNumber.isBlank() && routeRepository != null) {
        try {
            val repository = routeRepository as? BusRouteRepository
            if (repository != null) {
                val routes = repository.getAllRoutes()
                val route = routes.find { it.id == this.routeId }
                route?.let {
                    routeNumber = it.routeNumber
                    routeName = it.name
                    Timber.d("Found route info from repository: number='$routeNumber', name='$routeName'")
                } ?: run {
                    Timber.w("Route not found for routeId: ${this.routeId}")
                }
            } else {
                Timber.w("Invalid repository type provided: ${routeRepository.javaClass.simpleName}")
            }
        } catch (e: Exception) {
            loge("Error getting route info for routeId: ${this.routeId}", e)
        }
    }
    
    // Fallback: если routeNumber все еще пустой, используем routeId
    if (routeNumber.isBlank()) {
        routeNumber = this.routeId.takeIf { it.isNotBlank() } ?: "Неизвестный"
        Timber.w("Using routeId as fallback routeNumber: '$routeNumber'")
    }
    
    if (routeName.isBlank()) {
        routeName = "Маршрут ${this.routeId}"
    }
    
    // Валидация времени добавления
    val addedDate = if (this.addedDate <= 0L) {
        Timber.w("Invalid addedDate for ID: ${this.id}, using current time")
        System.currentTimeMillis()
    } else {
        this.addedDate
    }
    
    return FavoriteTime(
        id = this.id,
        routeId = this.routeId,
        routeNumber = routeNumber,
        routeName = routeName,
        stopName = this.stopName.takeIf { it.isNotBlank() } ?: "Неизвестная остановка",
        departureTime = this.departureTime.takeIf { it.isNotBlank() } ?: "00:00",
        dayOfWeek = this.dayOfWeek.takeIf { it in 1..7 } ?: 1,
        departurePoint = this.departurePoint.takeIf { it.isNotBlank() } ?: "Неизвестный пункт",
        addedDate = addedDate,
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
): BusRoute? {
    // Валидация входных параметров
    if (id.isBlank()) {
        loge("createBusRoute: ID cannot be blank")
        return null
    }
    
    if (routeNumber.isBlank()) {
        loge("createBusRoute: routeNumber cannot be blank for ID: $id")
        return null
    }
    
    if (name.isBlank()) {
        loge("createBusRoute: name cannot be blank for ID: $id")
        return null
    }
    
    // Валидация цвета
    val validatedColor = if (ValidationUtils.isValidColor(color)) {
        color
    } else {
        loge("createBusRoute: Invalid color format '$color' for ID: $id, using default")
        "#FF5722"
    }
    
    try {
        return BusRoute(
            id = id.trim(),
            routeNumber = routeNumber.trim(),
            name = name.trim(),
            description = description.trim(),
            travelTime = travelTime.trim().takeIf { it.isNotBlank() },
            pricePrimary = pricePrimary.trim().takeIf { it.isNotBlank() },
            paymentMethods = paymentMethods.trim().takeIf { it.isNotBlank() },
            color = validatedColor
        )
    } catch (e: Exception) {
        loge("createBusRoute: Error creating BusRoute for ID: $id", e)
        return null
    }
}

// Поиск маршрутов по запросу
fun List<BusRoute>.search(query: String): List<BusRoute> {
    if (query.isBlank()) return this
    
    // Валидация входных данных
    if (this.isEmpty()) {
        Timber.d("search: Empty route list provided")
        return emptyList()
    }
    
    val lowercaseQuery = query.lowercase().trim()
    
    try {
        return this.filter { route ->
            // Проверяем, что маршрут валиден
            if (!route.isValid()) {
                Timber.w("search: Invalid route found: ${route.id}")
                return@filter false
            }
            
            // Поиск по различным полям
            val matchesName = route.name.lowercase().contains(lowercaseQuery)
            val matchesNumber = route.routeNumber.lowercase().contains(lowercaseQuery)
            val matchesDescription = route.description.lowercase().contains(lowercaseQuery)
            val matchesStop = route.directionDetails?.lowercase()?.contains(lowercaseQuery) == true
            
            matchesName || matchesNumber || matchesDescription || matchesStop
        }.sortedWith(compareBy<BusRoute> { route ->
            // Приоритет: точное совпадение номера > совпадение в начале > остальное
            when {
                route.routeNumber.lowercase() == lowercaseQuery -> 0
                route.routeNumber.lowercase().startsWith(lowercaseQuery) -> 1
                route.name.lowercase().startsWith(lowercaseQuery) -> 2
                else -> 3
            }
        })
    } catch (e: Exception) {
        loge("search: Error during search for query: '$query'", e)
        return emptyList()
    }
}
