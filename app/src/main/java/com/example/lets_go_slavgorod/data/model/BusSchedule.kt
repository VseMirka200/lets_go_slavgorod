package com.example.lets_go_slavgorod.data.model

import com.example.lets_go_slavgorod.utils.ValidationUtils
import com.example.lets_go_slavgorod.utils.loge

data class BusSchedule(
    val id: String,
    val routeId: String,
    val stopName: String,
    val departureTime: String,
    val dayOfWeek: Int,
    val isWeekend: Boolean = false,
    val notes: String? = null,
    val departurePoint: String
) {
    
    /**
     * Проверяет валидность данных расписания
     */
    fun isValid(): Boolean {
        return try {
            val validations = listOf(
                ValidationUtils.isValidRouteId(id) to "Invalid schedule ID: '$id'",
                ValidationUtils.isValidRouteId(routeId) to "Invalid route ID: '$routeId'",
                ValidationUtils.isValidStopName(stopName) to "Invalid stop name: '$stopName'",
                ValidationUtils.isValidTime(departureTime) to "Invalid departure time: '$departureTime'",
                ValidationUtils.isValidDayOfWeek(dayOfWeek) to "Invalid day of week: $dayOfWeek",
                ValidationUtils.isValidStopName(departurePoint) to "Invalid departure point: '$departurePoint'"
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
            loge("Error validating BusSchedule", e)
            false
        }
    }
    
    /**
     * Создает безопасную копию с валидированными данными
     */
    fun sanitized(): BusSchedule {
        return copy(
            id = ValidationUtils.sanitizeString(id),
            routeId = ValidationUtils.sanitizeString(routeId),
            stopName = ValidationUtils.sanitizeString(stopName),
            departureTime = ValidationUtils.sanitizeString(departureTime),
            departurePoint = ValidationUtils.sanitizeString(departurePoint),
            notes = notes?.let { ValidationUtils.sanitizeString(it) }
        )
    }
}