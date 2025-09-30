package com.example.lets_go_slavgorod.data.model

import com.example.lets_go_slavgorod.utils.ValidationUtils
import com.example.lets_go_slavgorod.utils.loge

/**
 * Модель автобусного маршрута
 */
data class BusRoute(
    val id: String,                        // Уникальный ID маршрута
    val routeNumber: String,               // Номер маршрута (например, "1", "2А")
    val name: String,                      // Название маршрута
    val description: String,               // Описание маршрута
    val isActive: Boolean = true,          // Активен ли маршрут
    val isFavorite: Boolean = false,       // Добавлен ли в избранное
    val color: String = "#1976D2",         // Цвет маршрута в интерфейсе
    val pricePrimary: String? = null,      // Основная цена
    val priceSecondary: String? = null,    // Дополнительная цена
    val directionDetails: String? = null,  // Детали направления
    val travelTime: String?,               // Время в пути
    val paymentMethods: String?            // Способы оплаты
) {
    
    // Валидация данных маршрута
    fun isValid(): Boolean {
        return try {
            val validations = listOf(
                ValidationUtils.isValidRouteId(id) to "Invalid route ID: '$id'",
                ValidationUtils.isValidRouteNumber(routeNumber) to "Invalid route number: '$routeNumber'",
                ValidationUtils.isValidRouteName(name) to "Invalid route name: '$name'",
                ValidationUtils.isValidStopName(description) to "Invalid description: '$description'"
            )
            
            val failedValidations = validations.filter { !it.first }
            if (failedValidations.isNotEmpty()) {
                failedValidations.forEach { (_, message) ->
                    loge(message)
                }
                return false
            }
            
            true
        } catch (e: Exception) {
            loge("Error validating BusRoute", e)
            false
        }
    }
    
    // Очистка и санитизация данных
    fun sanitized(): BusRoute {
        return copy(
            id = ValidationUtils.sanitizeString(id),
            routeNumber = ValidationUtils.sanitizeString(routeNumber),
            name = ValidationUtils.sanitizeString(name),
            description = ValidationUtils.sanitizeString(description),
            pricePrimary = pricePrimary?.let { ValidationUtils.sanitizeString(it) },
            priceSecondary = priceSecondary?.let { ValidationUtils.sanitizeString(it) },
            directionDetails = directionDetails?.let { ValidationUtils.sanitizeString(it) },
            travelTime = travelTime?.let { ValidationUtils.sanitizeString(it) },
            paymentMethods = paymentMethods?.let { ValidationUtils.sanitizeString(it) }
        )
    }
}