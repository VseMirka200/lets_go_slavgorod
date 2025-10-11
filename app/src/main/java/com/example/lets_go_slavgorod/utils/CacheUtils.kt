package com.example.lets_go_slavgorod.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.lets_go_slavgorod.data.model.BusRoute
import timber.log.Timber
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.json.JSONObject

/**
 * Утилиты для кэширования данных приложения
 */
object CacheUtils {

    private const val PREFS_NAME = "app_cache"           // Имя файла SharedPreferences для кэша
    private const val KEY_CACHE_TIMESTAMP = "cache_timestamp" // Ключ для времени создания кэша
    private const val KEY_ROUTES_CACHE = "routes_cache"   // Ключ для кэша маршрутов
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

    /**
     * Сохраняет маршруты в кэш
     * 
     * @param context контекст приложения
     * @param routes список маршрутов для кэширования
     */
    fun cacheRoutes(context: Context, routes: List<BusRoute>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val routesJson = JSONArray()
            
            routes.forEach { route ->
                val routeJson = JSONObject().apply {
                    put("id", route.id)
                    put("routeNumber", route.routeNumber)
                    put("name", route.name)
                    put("description", route.description)
                    put("travelTime", route.travelTime ?: "")
                    put("pricePrimary", route.pricePrimary ?: "")
                    put("priceSecondary", route.priceSecondary ?: "")
                    put("paymentMethods", route.paymentMethods ?: "")
                    put("directionDetails", route.directionDetails ?: "")
                    put("color", route.color)
                    put("isActive", route.isActive)
                    put("isFavorite", route.isFavorite)
                }
                routesJson.put(routeJson)
            }
            
            prefs.edit().apply {
                putString(KEY_ROUTES_CACHE, routesJson.toString())
                putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                apply()
            }
            
            Timber.d("Cached ${routes.size} routes successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error caching routes")
        }
    }

    /**
     * Загружает маршруты из кэша
     * 
     * @param context контекст приложения
     * @return список маршрутов из кэша или пустой список при ошибке
     */
    fun loadCachedRoutes(context: Context): List<BusRoute> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val routesJsonString = prefs.getString(KEY_ROUTES_CACHE, null)
            
            if (routesJsonString.isNullOrBlank()) {
                Timber.d("No cached routes found")
                return emptyList()
            }
            
            val routesJson = JSONArray(routesJsonString)
            val routes = mutableListOf<BusRoute>()
            
            for (i in 0 until routesJson.length()) {
                val routeJson = routesJson.getJSONObject(i)
                val route = BusRoute(
                    id = routeJson.getString("id"),
                    routeNumber = routeJson.getString("routeNumber"),
                    name = routeJson.getString("name"),
                    description = routeJson.getString("description"),
                    travelTime = routeJson.optString("travelTime").takeIf { it.isNotBlank() },
                    pricePrimary = routeJson.optString("pricePrimary").takeIf { it.isNotBlank() },
                    priceSecondary = routeJson.optString("priceSecondary").takeIf { it.isNotBlank() },
                    paymentMethods = routeJson.optString("paymentMethods").takeIf { it.isNotBlank() },
                    directionDetails = routeJson.optString("directionDetails").takeIf { it.isNotBlank() },
                    color = routeJson.getString("color"),
                    isActive = routeJson.optBoolean("isActive", true),
                    isFavorite = routeJson.optBoolean("isFavorite", false)
                )
                routes.add(route)
            }
            
            Timber.d("Loaded ${routes.size} routes from cache")
            routes
        } catch (e: Exception) {
            Timber.e(e, "Error loading cached routes")
            emptyList()
        }
    }

    /**
     * Очищает кэш
     * 
     * @param context контекст приложения
     */
    fun clearCache(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Timber.d("Cache cleared successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing cache")
        }
    }

    /**
     * Получает время последнего обновления кэша
     * 
     * @param context контекст приложения
     * @return timestamp последнего обновления или 0 если кэш отсутствует
     */
    fun getCacheTimestamp(context: Context): Long {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getLong(KEY_CACHE_TIMESTAMP, 0L)
        } catch (e: Exception) {
            Timber.e(e, "Error getting cache timestamp")
            0L
        }
    }

}
