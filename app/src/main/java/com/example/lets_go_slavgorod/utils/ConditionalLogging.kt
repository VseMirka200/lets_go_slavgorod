package com.example.lets_go_slavgorod.utils

import com.example.lets_go_slavgorod.BuildConfig
import timber.log.Timber

/**
 * Утилиты для условного логирования
 * 
 * Предоставляет методы для логирования, которые автоматически отключаются
 * в релизной версии приложения для улучшения производительности.
 * 
 * @author VseMirka200
 * @version 1.0
 */
object ConditionalLogging {
    
    /**
     * Логирует отладочную информацию только в DEBUG версии
     */
    inline fun debug(tag: String = "", message: () -> String) {
        if (BuildConfig.DEBUG) {
            Timber.tag(tag).d(message())
        }
    }
    
    /**
     * Логирует информационные сообщения только в DEBUG версии
     */
    inline fun info(tag: String = "", message: () -> String) {
        if (BuildConfig.DEBUG) {
            Timber.tag(tag).i(message())
        }
    }
    
    /**
     * Логирует предупреждения (всегда)
     */
    inline fun warn(tag: String = "", message: () -> String) {
        Timber.tag(tag).w(message())
    }
    
    /**
     * Логирует ошибки (всегда)
     */
    inline fun error(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message())
        } else {
            Timber.tag(tag).e(message())
        }
    }
    
    /**
     * Логирует критические ошибки (всегда)
     */
    inline fun wtf(tag: String = "", message: () -> String) {
        Timber.tag(tag).wtf(message())
    }
}
