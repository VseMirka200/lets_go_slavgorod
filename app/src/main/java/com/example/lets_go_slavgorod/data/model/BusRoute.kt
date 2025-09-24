package com.example.lets_go_slavgorod.data.model

import com.example.lets_go_slavgorod.utils.ValidationUtils
import com.example.lets_go_slavgorod.utils.loge

data class BusRoute(
    val id: String,
    val routeNumber: String,
    val name: String,
    val description: String,
    val isActive: Boolean = true,
    val isFavorite: Boolean = false,
    val color: String = "#1976D2",
    val pricePrimary: String? = null,
    val priceSecondary: String? = null,
    val directionDetails: String? = null,
    val travelTime: String?,
    val paymentMethods: String?
) {
    
    /**
     * Проверяет валидность данных маршрута
     */
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
    
    /**
     * Создает безопасную копию с валидированными данными
     */
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