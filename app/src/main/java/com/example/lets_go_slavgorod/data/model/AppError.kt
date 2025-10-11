package com.example.lets_go_slavgorod.data.model

/**
 * Sealed class для типобезопасной обработки ошибок приложения
 * 
 * Представляет все возможные типы ошибок с дополнительной информацией.
 * Заменяет использование голых Exception и String для ошибок.
 * 
 * Преимущества:
 * - Type-safe обработка ошибок
 * - Exhaustive when statements
 * - Централизованная логика ошибок
 * - Легкое добавление новых типов ошибок
 * 
 * @author VseMirka200
 * @version 1.0
 * @since 2.0
 */
sealed class AppError {
    
    /**
     * Ошибки сети
     */
    sealed class Network : AppError() {
        /**
         * Нет подключения к интернету
         */
        object NoConnection : Network()
        
        /**
         * Таймаут запроса
         */
        object Timeout : Network()
        
        /**
         * Ошибка HTTP запроса
         * @param code код ответа HTTP
         * @param message сообщение об ошибке
         */
        data class HttpError(val code: Int, val message: String) : Network()
        
        /**
         * Общая ошибка сети
         * @param message сообщение об ошибке
         * @param cause исходное исключение
         */
        data class Generic(val message: String, val cause: Throwable? = null) : Network()
    }
    
    /**
     * Ошибки базы данных
     */
    sealed class Database : AppError() {
        /**
         * Ошибка чтения из базы данных
         * @param message сообщение об ошибке
         */
        data class ReadError(val message: String) : Database()
        
        /**
         * Ошибка записи в базу данных
         * @param message сообщение об ошибке
         */
        data class WriteError(val message: String) : Database()
        
        /**
         * Запись не найдена
         * @param id идентификатор записи
         */
        data class NotFound(val id: String) : Database()
        
        /**
         * Общая ошибка базы данных
         * @param message сообщение об ошибке
         * @param cause исходное исключение
         */
        data class Generic(val message: String, val cause: Throwable? = null) : Database()
    }
    
    /**
     * Ошибки валидации данных
     */
    sealed class Validation : AppError() {
        /**
         * Некорректный формат данных
         * @param field поле с ошибкой
         * @param message сообщение об ошибке
         */
        data class InvalidFormat(val field: String, val message: String) : Validation()
        
        /**
         * Отсутствует обязательное поле
         * @param field имя поля
         */
        data class MissingField(val field: String) : Validation()
        
        /**
         * Значение вне допустимого диапазона
         * @param field имя поля
         * @param value текущее значение
         */
        data class OutOfRange(val field: String, val value: String) : Validation()
    }
    
    /**
     * Ошибки разрешений
     */
    sealed class Permission : AppError() {
        /**
         * Отсутствует разрешение
         * @param permission имя разрешения
         */
        data class Denied(val permission: String) : Permission()
        
        /**
         * Пользователь не предоставил разрешение
         * @param permission имя разрешения
         */
        data class NotGranted(val permission: String) : Permission()
    }
    
    /**
     * Системные ошибки
     */
    sealed class System : AppError() {
        /**
         * Недостаточно памяти
         */
        object OutOfMemory : System()
        
        /**
         * Недостаточно места на диске
         */
        object OutOfStorage : System()
        
        /**
         * Общая системная ошибка
         * @param message сообщение об ошибке
         * @param cause исходное исключение
         */
        data class Generic(val message: String, val cause: Throwable? = null) : System()
    }
    
    /**
     * Неизвестная ошибка
     * @param message сообщение об ошибке
     * @param cause исходное исключение
     */
    data class Unknown(val message: String, val cause: Throwable? = null) : AppError()
}

/**
 * Расширение для получения пользовательского сообщения об ошибке (без ресурсов)
 * 
 * Используется когда нет доступа к Context (например, в тестах).
 * Для UI лучше использовать getUserMessage(context).
 */
fun AppError.getUserMessage(): String {
    return when (this) {
        // Network errors
        is AppError.Network.NoConnection -> "Нет подключения к интернету"
        is AppError.Network.Timeout -> "Превышено время ожидания"
        is AppError.Network.HttpError -> "Ошибка сервера: ${this.message}"
        is AppError.Network.Generic -> this.message
        
        // Database errors
        is AppError.Database.ReadError -> "Ошибка чтения данных: ${this.message}"
        is AppError.Database.WriteError -> "Ошибка сохранения данных: ${this.message}"
        is AppError.Database.NotFound -> "Запись не найдена"
        is AppError.Database.Generic -> "Ошибка базы данных: ${this.message}"
        
        // Validation errors
        is AppError.Validation.InvalidFormat -> "Неверный формат: ${this.field}"
        is AppError.Validation.MissingField -> "Отсутствует обязательное поле: ${this.field}"
        is AppError.Validation.OutOfRange -> "Значение вне диапазона: ${this.field}"
        
        // Permission errors
        is AppError.Permission.Denied -> "Отсутствует разрешение: ${this.permission}"
        is AppError.Permission.NotGranted -> "Требуется разрешение: ${this.permission}"
        
        // System errors
        is AppError.System.OutOfMemory -> "Недостаточно памяти"
        is AppError.System.OutOfStorage -> "Недостаточно места на диске"
        is AppError.System.Generic -> "Системная ошибка: ${this.message}"
        
        // Unknown error
        is AppError.Unknown -> this.message.ifEmpty { "Неизвестная ошибка" }
    }
}

/**
 * Расширение для получения локализованного сообщения об ошибке из ресурсов
 * 
 * @param context контекст для доступа к строковым ресурсам
 * @return локализованное сообщение об ошибке
 */
fun AppError.getUserMessage(context: android.content.Context): String {
    return when (this) {
        // Network errors
        is AppError.Network.NoConnection -> context.getString(com.example.lets_go_slavgorod.R.string.error_network_no_connection)
        is AppError.Network.Timeout -> context.getString(com.example.lets_go_slavgorod.R.string.error_network_timeout)
        is AppError.Network.HttpError -> context.getString(com.example.lets_go_slavgorod.R.string.error_network_generic, this.message)
        is AppError.Network.Generic -> context.getString(com.example.lets_go_slavgorod.R.string.error_network_generic, this.message)
        
        // Database errors
        is AppError.Database.ReadError -> context.getString(com.example.lets_go_slavgorod.R.string.error_database_read, this.message)
        is AppError.Database.WriteError -> context.getString(com.example.lets_go_slavgorod.R.string.error_database_write, this.message)
        is AppError.Database.NotFound -> context.getString(com.example.lets_go_slavgorod.R.string.error_database_not_found)
        is AppError.Database.Generic -> context.getString(com.example.lets_go_slavgorod.R.string.error_database_generic, this.message)
        
        // Validation errors
        is AppError.Validation.InvalidFormat -> context.getString(com.example.lets_go_slavgorod.R.string.error_validation_invalid_format, this.field)
        is AppError.Validation.MissingField -> context.getString(com.example.lets_go_slavgorod.R.string.error_validation_missing_field, this.field)
        is AppError.Validation.OutOfRange -> context.getString(com.example.lets_go_slavgorod.R.string.error_validation_out_of_range, this.field)
        
        // Permission errors
        is AppError.Permission.Denied -> context.getString(com.example.lets_go_slavgorod.R.string.error_permission_denied, this.permission)
        is AppError.Permission.NotGranted -> context.getString(com.example.lets_go_slavgorod.R.string.error_permission_not_granted, this.permission)
        
        // System errors
        is AppError.System.OutOfMemory -> context.getString(com.example.lets_go_slavgorod.R.string.error_system_out_of_memory)
        is AppError.System.OutOfStorage -> context.getString(com.example.lets_go_slavgorod.R.string.error_system_out_of_storage)
        is AppError.System.Generic -> context.getString(com.example.lets_go_slavgorod.R.string.error_system_generic, this.message)
        
        // Unknown error
        is AppError.Unknown -> this.message.ifEmpty { context.getString(com.example.lets_go_slavgorod.R.string.error_unknown) }
    }
}

/**
 * Расширение для логирования ошибки
 */
fun AppError.getLogMessage(): String {
    return when (this) {
        is AppError.Network.Generic -> "Network error: $message, cause: ${cause?.message}"
        is AppError.Database.Generic -> "Database error: $message, cause: ${cause?.message}"
        is AppError.System.Generic -> "System error: $message, cause: ${cause?.message}"
        is AppError.Unknown -> "Unknown error: $message, cause: ${cause?.message}"
        else -> getUserMessage()
    }
}

