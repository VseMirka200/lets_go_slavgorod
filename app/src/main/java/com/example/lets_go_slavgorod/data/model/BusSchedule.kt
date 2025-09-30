package com.example.lets_go_slavgorod.data.model

import com.example.lets_go_slavgorod.utils.ValidationUtils
import com.example.lets_go_slavgorod.utils.loge

/**
 * Модель расписания автобуса
 */
data class BusSchedule(
    val id: String,                     // Уникальный ID расписания
    val routeId: String,                // ID маршрута
    val stopName: String,               // Название остановки
    val departureTime: String,          // Время отправления (HH:mm)
    val dayOfWeek: Int,                 // День недели (1-7)
    val isWeekend: Boolean = false,     // Выходной день
    val notes: String? = null,          // Дополнительные заметки
    val departurePoint: String          // Пункт отправления
) {
    
    // Валидация данных расписания
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
    
    // Очистка и санитизация данных
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