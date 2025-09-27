package com.example.lets_go_slavgorod.utils

/**
 * Константы приложения для оптимизации производительности
 * и избежания магических чисел в коде.
 */
object Constants {
    
    // Анимации (в миллисекундах)
    const val ANIMATION_DURATION_SHORT = 100  // Быстрые анимации (кнопки, переключатели)
    const val ANIMATION_DURATION_MEDIUM = 300 // Стандартные анимации (переходы между экранами)
    const val ANIMATION_DURATION_LONG = 500  // Долгие анимации (сложные переходы)
    
    // Размеры UI (в dp)
    const val CARD_ELEVATION = 2                    // Тень карточек
    const val CARD_CORNER_RADIUS = 12              // Скругление углов карточек
    const val ROUTE_NUMBER_BOX_SIZE = 52           // Размер блока с номером маршрута
    const val ROUTE_NUMBER_BOX_CORNER_RADIUS = 16  // Скругление углов блока номера маршрута
    
    // Отступы (в dp)
    const val PADDING_SMALL = 8   // Малые отступы (между элементами)
    const val PADDING_MEDIUM = 16 // Средние отступы (между секциями)
    const val PADDING_LARGE = 24  // Большие отступы (от краев экрана)
    
    // Цвета по умолчанию (в формате ARGB)
    const val DEFAULT_ROUTE_COLOR = "#FF6200EE"    // Основной цвет маршрутов (фиолетовый)
    const val DEFAULT_ROUTE_COLOR_ALT = "#FF1976D2" // Альтернативный цвет маршрутов (синий)
    const val COLOR_ALPHA = 0.9f                   // Прозрачность цветов (90%)
    
    // Уведомления
    const val NOTIFICATION_LEAD_TIME_MINUTES = 5  // За сколько минут до отправления показывать уведомление
    const val ALARM_REQUEST_CODE_PREFIX = "fav_alarm_" // Префикс для кодов будильников избранных маршрутов
    
    // База данных
    const val DATABASE_NAME = "bus_app_database"  // Имя файла базы данных
    const val DATABASE_VERSION = 4               // Версия схемы базы данных
    
    // Поиск и производительность
    const val SEARCH_DEBOUNCE_DELAY = 300L       // Задержка перед поиском (мс)
    const val MAX_SEARCH_RESULTS = 100           // Максимальное количество результатов поиска
    const val SWIPE_THRESHOLD_PERCENT = 0.2f    // Порог свайпа (20% ширины экрана)
    const val MIN_SWIPE_DISTANCE = 120f         // Минимальное расстояние свайпа (пиксели)
    
    // Кэширование
    const val CACHE_SIZE = 50                    // Максимальный размер кэша (элементов)
    const val CACHE_EXPIRE_TIME_HOURS = 24      // Время жизни кэша (часы)
    
    // Логирование (уровни важности)
    const val LOG_LEVEL_DEBUG = 0  // Отладочная информация
    const val LOG_LEVEL_INFO = 1   // Информационные сообщения
    const val LOG_LEVEL_WARN = 2   // Предупреждения
    const val LOG_LEVEL_ERROR = 3  // Ошибки
}
