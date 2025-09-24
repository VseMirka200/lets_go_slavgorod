package com.example.lets_go_slavgorod.utils

import android.util.Log
import java.util.Calendar

/**
 * Утилиты для валидации данных в приложении
 */
object ValidationUtils {
    
    private const val TAG = "ValidationUtils"
    
    /**
     * Валидирует время в формате HH:MM
     */
    fun isValidTime(time: String): Boolean {
        if (time.isBlank()) {
            Log.w(TAG, "Time is blank")
            return false
        }
        
        val timeParts = time.split(":")
        if (timeParts.size != 2) {
            Log.w(TAG, "Invalid time format: '$time' - expected HH:MM")
            return false
        }
        
        return try {
            val hour = timeParts[0]?.trim()?.toInt() ?: return false
            val minute = timeParts[1]?.trim()?.toInt() ?: return false
            
            val isValid = hour in 0..23 && minute in 0..59
            if (!isValid) {
                Log.w(TAG, "Invalid time values: hour=$hour, minute=$minute")
            }
            isValid
        } catch (e: NumberFormatException) {
            Log.w(TAG, "Invalid number format in time: '$time'", e)
            false
        }
    }
    
    /**
     * Валидирует день недели
     */
    fun isValidDayOfWeek(dayOfWeek: Int): Boolean {
        val isValid = dayOfWeek in Calendar.SUNDAY..Calendar.SATURDAY
        if (!isValid) {
            Log.w(TAG, "Invalid day of week: $dayOfWeek - expected ${Calendar.SUNDAY}-${Calendar.SATURDAY}")
        }
        return isValid
    }
    
    /**
     * Валидирует ID маршрута
     */
    fun isValidRouteId(routeId: String?): Boolean {
        if (routeId.isNullOrBlank()) {
            Log.w(TAG, "Route ID is null or blank")
            return false
        }
        
        val isValid = routeId?.trim()?.isNotBlank() ?: false
        if (!isValid) {
            Log.w(TAG, "Route ID is empty after trimming: '$routeId'")
        }
        return isValid
    }
    
    /**
     * Валидирует название остановки
     */
    fun isValidStopName(stopName: String?): Boolean {
        if (stopName.isNullOrBlank()) {
            Log.w(TAG, "Stop name is null or blank")
            return false
        }
        
        val trimmed = stopName?.trim() ?: ""
        val isValid = trimmed.isNotBlank() && trimmed.length >= 2
        if (!isValid) {
            Log.w(TAG, "Stop name is too short or empty: '$stopName'")
        }
        return isValid
    }
    
    /**
     * Валидирует название маршрута
     */
    fun isValidRouteName(routeName: String?): Boolean {
        if (routeName.isNullOrBlank()) {
            Log.w(TAG, "Route name is null or blank")
            return false
        }
        
        val trimmed = routeName?.trim() ?: ""
        val isValid = trimmed.isNotBlank() && trimmed.length >= 3
        if (!isValid) {
            Log.w(TAG, "Route name is too short or empty: '$routeName'")
        }
        return isValid
    }
    
    /**
     * Валидирует номер маршрута
     */
    fun isValidRouteNumber(routeNumber: String?): Boolean {
        if (routeNumber.isNullOrBlank()) {
            Log.w(TAG, "Route number is null or blank")
            return false
        }
        
        val trimmed = routeNumber?.trim() ?: ""
        val isValid = trimmed.isNotBlank() && trimmed.length >= 1
        if (!isValid) {
            Log.w(TAG, "Route number is empty: '$routeNumber'")
        }
        return isValid
    }
    
    /**
     * Валидирует URL
     */
    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) {
            Log.w(TAG, "URL is null or blank")
            return false
        }
        
        val trimmed = url?.trim() ?: ""
        val isValid = trimmed.isNotBlank() && 
                     (trimmed.startsWith("http://") || trimmed.startsWith("https://"))
        
        if (!isValid) {
            Log.w(TAG, "Invalid URL format: '$url'")
        }
        return isValid
    }
    
    /**
     * Валидирует версию приложения
     */
    fun isValidVersion(version: String?): Boolean {
        if (version.isNullOrBlank()) {
            Log.w(TAG, "Version is null or blank")
            return false
        }
        
        val trimmed = version?.trim() ?: ""
        val isValid = trimmed.isNotBlank() && 
                     trimmed.matches(Regex("^\\d+(\\.\\d+)*$"))
        
        if (!isValid) {
            Log.w(TAG, "Invalid version format: '$version' - expected format: X.Y.Z")
        }
        return isValid
    }
    
    /**
     * Очищает и нормализует строку
     */
    fun sanitizeString(input: String?): String {
        return input?.trim()?.takeIf { it.isNotBlank() } ?: ""
    }
    
    /**
     * Проверяет, является ли строка безопасной для отображения
     */
    fun isSafeForDisplay(text: String?): Boolean {
        if (text.isNullOrBlank()) return true
        
        // Проверяем на потенциально опасные символы
        val dangerousChars = setOf('<', '>', '&', '"', '\'', '\\', '/', '\n', '\r', '\t')
        return !text.any { it in dangerousChars }
    }
}
