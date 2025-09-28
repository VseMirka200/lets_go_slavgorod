package com.example.lets_go_slavgorod.utils

import android.content.Context
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Утилиты для кэширования данных приложения
 */
object CacheUtils {

    private const val PREFS_NAME = "app_cache"           // Имя файла SharedPreferences для кэша
    private const val KEY_CACHE_TIMESTAMP = "cache_timestamp" // Ключ для времени создания кэша
    private const val CACHE_EXPIRE_HOURS = 24L           // Время жизни кэша (24 часа)

    /**
     * Проверяет, есть ли актуальный кэш
     * 
     * Проверяет, не истек ли срок действия кэша (24 часа)
     * @param context контекст приложения
     * @return true если кэш актуален, false если истек или отсутствует
     */
    fun hasValidCache(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cacheTimestamp = prefs.getLong(KEY_CACHE_TIMESTAMP, 0L)
            val currentTime = System.currentTimeMillis()
            // Проверяем, не истек ли срок действия кэша
            val isValid = currentTime - cacheTimestamp <= TimeUnit.HOURS.toMillis(CACHE_EXPIRE_HOURS)
            Timber.d("Cache validity check: $isValid")
            isValid
        } catch (e: Exception) {
            Timber.e(e, "Error checking cache validity")
            false
        }
    }

}
