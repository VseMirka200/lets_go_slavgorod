package com.example.lets_go_slavgorod.utils

import timber.log.Timber
import java.util.Calendar

/**
 * Утилиты для валидации данных в приложении
 */
object ValidationUtils {

    /**
     * Валидирует время в формате HH:MM
     */
    fun isValidTime(time: String): Boolean {
        if (time.isBlank()) {
            Timber.w("Time is blank")
            return false
        }
        
        val timeParts = time.split(":")
        if (timeParts.size != 2) {
            Timber.w("Invalid time format: '$time' - expected HH:MM")
            return false
        }
        
        return try {
            val hour = timeParts[0].trim().toInt()
            val minute = timeParts[1].trim().toInt()
            
            val isValid = hour in 0..23 && minute in 0..59
            if (!isValid) {
                Timber.w("Invalid time values: hour=$hour, minute=$minute")
            }
            isValid
        } catch (e: NumberFormatException) {
            Timber.w(e, "Invalid number format in time: '$time'")
            false
        }
    }
    
    /**
     * Валидирует день недели
     */
    fun isValidDayOfWeek(dayOfWeek: Int): Boolean {
        val isValid = dayOfWeek in Calendar.SUNDAY..Calendar.SATURDAY
        if (!isValid) {
            Timber.w("Invalid day of week: $dayOfWeek - expected ${Calendar.SUNDAY}-${Calendar.SATURDAY}")
        }
        return isValid
    }
    
    /**
     * Валидирует ID маршрута
     */
    fun isValidRouteId(routeId: String?): Boolean {
        if (routeId.isNullOrBlank()) {
            Timber.w("Route ID is null or blank")
            return false
        }
        
        val isValid = routeId.trim().isNotBlank()
        if (!isValid) {
            Timber.w("Route ID is empty after trimming: '$routeId'")
        }
        return isValid
    }
    
    /**
     * Валидирует название остановки
     */
    fun isValidStopName(stopName: String?): Boolean {
        if (stopName.isNullOrBlank()) {
            Timber.w("Stop name is null or blank")
            return false
        }
        
        val trimmed = stopName.trim()
        val isValid = trimmed.isNotBlank() && trimmed.length >= 2
        if (!isValid) {
            Timber.w("Stop name is too short or empty: '$stopName'")
        }
        return isValid
    }
    
    /**
     * Валидирует название маршрута
     */
    fun isValidRouteName(routeName: String?): Boolean {
        if (routeName.isNullOrBlank()) {
            Timber.w("Route name is null or blank")
            return false
        }
        
        val trimmed = routeName.trim()
        val isValid = trimmed.isNotBlank() && trimmed.length >= 3
        if (!isValid) {
            Timber.w("Route name is too short or empty: '$routeName'")
        }
        return isValid
    }
    
    /**
     * Валидирует номер маршрута
     */
    fun isValidRouteNumber(routeNumber: String?): Boolean {
        if (routeNumber.isNullOrBlank()) {
            Timber.w("Route number is null or blank")
            return false
        }
        
        val trimmed = routeNumber.trim()
        val isValid = trimmed.isNotBlank() && trimmed.isNotEmpty()
        if (!isValid) {
            Timber.w("Route number is empty: '$routeNumber'")
        }
        return isValid
    }

    /**
     * Очищает и нормализует строку
     */
    fun sanitizeString(input: String?): String {
        return input?.trim()?.takeIf { it.isNotBlank() } ?: ""
    }

}
