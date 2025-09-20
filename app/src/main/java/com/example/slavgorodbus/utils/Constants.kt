package com.example.slavgorodbus.utils

/**
 * Константы приложения для оптимизации производительности
 * и избежания магических чисел в коде.
 */
object Constants {
    
    // Анимации
    const val ANIMATION_DURATION_SHORT = 100
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500
    
    // Размеры UI
    const val CARD_ELEVATION = 2
    const val CARD_CORNER_RADIUS = 12
    const val ROUTE_NUMBER_BOX_SIZE = 52
    const val ROUTE_NUMBER_BOX_CORNER_RADIUS = 16
    
    // Отступы
    const val PADDING_SMALL = 8
    const val PADDING_MEDIUM = 16
    const val PADDING_LARGE = 24
    
    // Цвета по умолчанию
    const val DEFAULT_ROUTE_COLOR = "#FF6200EE"
    const val DEFAULT_ROUTE_COLOR_ALT = "#FF1976D2"
    const val COLOR_ALPHA = 0.9f
    
    // Уведомления
    const val NOTIFICATION_LEAD_TIME_MINUTES = 5
    const val ALARM_REQUEST_CODE_PREFIX = "fav_alarm_"
    
    // База данных
    const val DATABASE_NAME = "bus_app_database"
    const val DATABASE_VERSION = 3
    
    // Поиск
    const val SEARCH_DEBOUNCE_DELAY = 300L
    
    // Кэширование
    const val CACHE_SIZE = 50
    const val CACHE_EXPIRE_TIME_HOURS = 24
}
