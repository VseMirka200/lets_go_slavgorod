package com.example.lets_go_slavgorod.utils

import android.content.Context
import android.util.Log
import com.example.lets_go_slavgorod.data.model.BusRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

/**
 * Утилиты для кэширования данных приложения
 */
object CacheUtils {
    
    private const val TAG = "CacheUtils"
    private const val PREFS_NAME = "app_cache"           // Имя файла SharedPreferences для кэша
    private const val KEY_ROUTES_CACHE = "routes_cache"   // Ключ для кэша маршрутов
    private const val KEY_CACHE_TIMESTAMP = "cache_timestamp" // Ключ для времени создания кэша
    private const val CACHE_EXPIRE_HOURS = 24L           // Время жизни кэша (24 часа)
    
    /**
     * Сохраняет маршруты в кэш
     * 
     * Конвертирует список маршрутов в JSON и сохраняет в SharedPreferences
     * @param context контекст приложения
     * @param routes список маршрутов для кэширования
     */
    suspend fun cacheRoutes(context: Context, routes: List<BusRoute>) = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val routesJson = JSONArray()
            
            // Конвертируем каждый маршрут в JSON объект
            routes.forEach { route ->
                val routeJson = JSONObject().apply {
                    put("id", route.id)
                    put("routeNumber", route.routeNumber)
                    put("name", route.name)
                    put("description", route.description)
                    put("isActive", route.isActive)
                    put("isFavorite", route.isFavorite)
                    put("color", route.color)
                    put("pricePrimary", route.pricePrimary ?: "")
                    put("priceSecondary", route.priceSecondary ?: "")
                    put("directionDetails", route.directionDetails ?: "")
                    put("travelTime", route.travelTime ?: "")
                    put("paymentMethods", route.paymentMethods ?: "")
                }
                routesJson.put(routeJson)
            }
            
            // Сохраняем JSON и время создания кэша
            prefs.edit {
                putString(KEY_ROUTES_CACHE, routesJson.toString())
                    .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
            }
                
            Log.d(TAG, "Cached ${routes.size} routes successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching routes", e)
        }
    }

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
            Log.d(TAG, "Cache validity check: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Error checking cache validity", e)
            false
        }
    }
    
    /**
     * Очищает кэш маршрутов
     * 
     * Удаляет все данные кэша из SharedPreferences
     * @param context контекст приложения
     */
    fun clearRoutesCache(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(KEY_ROUTES_CACHE)      // Удаляем кэш маршрутов
                    .remove(KEY_CACHE_TIMESTAMP) // Удаляем время создания кэша
            }
            Log.d(TAG, "Routes cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing routes cache", e)
        }
    }

}
