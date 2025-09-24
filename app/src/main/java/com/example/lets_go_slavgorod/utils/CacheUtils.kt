package com.example.lets_go_slavgorod.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.lets_go_slavgorod.data.model.BusRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Утилиты для кэширования данных приложения
 */
object CacheUtils {
    
    private const val TAG = "CacheUtils"
    private const val PREFS_NAME = "app_cache"
    private const val KEY_ROUTES_CACHE = "routes_cache"
    private const val KEY_CACHE_TIMESTAMP = "cache_timestamp"
    private const val CACHE_EXPIRE_HOURS = 24L
    
    /**
     * Сохраняет маршруты в кэш
     */
    suspend fun cacheRoutes(context: Context, routes: List<BusRoute>) = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val routesJson = JSONArray()
            
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
            
            prefs.edit()
                .putString(KEY_ROUTES_CACHE, routesJson.toString())
                .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                .apply()
                
            Log.d(TAG, "Cached ${routes.size} routes successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching routes", e)
        }
    }
    
    /**
     * Загружает маршруты из кэша
     */
    suspend fun loadCachedRoutes(context: Context): List<BusRoute>? = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cacheTimestamp = prefs.getLong(KEY_CACHE_TIMESTAMP, 0L)
            val currentTime = System.currentTimeMillis()
            
            // Проверяем, не истек ли кэш
            if (currentTime - cacheTimestamp > TimeUnit.HOURS.toMillis(CACHE_EXPIRE_HOURS)) {
                Log.d(TAG, "Cache expired, clearing cached routes")
                clearRoutesCache(context)
                return@withContext null
            }
            
            val routesJsonString = prefs.getString(KEY_ROUTES_CACHE, null)
            if (routesJsonString.isNullOrBlank()) {
                Log.d(TAG, "No cached routes found")
                return@withContext null
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
                    isActive = routeJson.getBoolean("isActive"),
                    isFavorite = routeJson.getBoolean("isFavorite"),
                    color = routeJson.getString("color"),
                    pricePrimary = routeJson.getString("pricePrimary").takeIf { it.isNotBlank() },
                    priceSecondary = routeJson.getString("priceSecondary").takeIf { it.isNotBlank() },
                    directionDetails = routeJson.getString("directionDetails").takeIf { it.isNotBlank() },
                    travelTime = routeJson.getString("travelTime").takeIf { it.isNotBlank() },
                    paymentMethods = routeJson.getString("paymentMethods").takeIf { it.isNotBlank() }
                )
                routes.add(route)
            }
            
            Log.d(TAG, "Loaded ${routes.size} routes from cache")
            routes
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached routes", e)
            null
        }
    }
    
    /**
     * Проверяет, есть ли актуальный кэш
     */
    fun hasValidCache(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cacheTimestamp = prefs.getLong(KEY_CACHE_TIMESTAMP, 0L)
            val currentTime = System.currentTimeMillis()
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
     */
    fun clearRoutesCache(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(KEY_ROUTES_CACHE)
                .remove(KEY_CACHE_TIMESTAMP)
                .apply()
            Log.d(TAG, "Routes cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing routes cache", e)
        }
    }
    
    /**
     * Получает время последнего обновления кэша
     */
    fun getLastCacheTime(context: Context): Long {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getLong(KEY_CACHE_TIMESTAMP, 0L)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last cache time", e)
            0L
        }
    }
}
