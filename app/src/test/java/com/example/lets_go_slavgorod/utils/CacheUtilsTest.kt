package com.example.lets_go_slavgorod.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.lets_go_slavgorod.data.model.BusRoute
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import timber.log.Timber

/**
 * Unit тесты для CacheUtils
 * 
 * Тестируют функции кэширования данных.
 * Используют Mockito для мокирования Android компонентов.
 * 
 * @author VseMirka200
 * @version 1.0
 */
class CacheUtilsTest {

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        // Инициализация Timber для тестов (без вывода)
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                // Не выводим логи в тестах
            }
        })

        // Мокирование Android компонентов
        mockContext = mock(Context::class.java)
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putLong(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.clear()).thenReturn(mockEditor)
    }

    // ===== Тесты для hasValidCache =====
    
    @Test
    fun `hasValidCache returns true for fresh cache`() {
        val currentTime = System.currentTimeMillis()
        whenever(mockSharedPreferences.getLong("cache_timestamp", 0L))
            .thenReturn(currentTime)
        
        val result = CacheUtils.hasValidCache(mockContext)
        assertTrue(result)
    }

    @Test
    fun `hasValidCache returns false for expired cache`() {
        val oneDayAgo = System.currentTimeMillis() - (25 * 60 * 60 * 1000) // 25 часов назад
        whenever(mockSharedPreferences.getLong("cache_timestamp", 0L))
            .thenReturn(oneDayAgo)
        
        val result = CacheUtils.hasValidCache(mockContext)
        assertFalse(result)
    }

    @Test
    fun `hasValidCache returns false when no cache exists`() {
        whenever(mockSharedPreferences.getLong("cache_timestamp", 0L))
            .thenReturn(0L)
        
        val result = CacheUtils.hasValidCache(mockContext)
        assertFalse(result)
    }

    // ===== Тесты для cacheRoutes =====
    
    @Test
    fun `cacheRoutes stores routes in SharedPreferences`() {
        val routes = listOf(
            createTestRoute("1", "Маршрут 1"),
            createTestRoute("2", "Маршрут 2")
        )
        
        CacheUtils.cacheRoutes(mockContext, routes)
        
        verify(mockSharedPreferences).edit()
        verify(mockEditor).putString(eq("routes_cache"), any())
        verify(mockEditor).putLong(eq("cache_timestamp"), any())
        verify(mockEditor).apply()
    }

    @Test
    fun `cacheRoutes handles empty list`() {
        CacheUtils.cacheRoutes(mockContext, emptyList())
        
        verify(mockSharedPreferences).edit()
        verify(mockEditor).apply()
    }

    // ===== Тесты для loadCachedRoutes =====
    
    @Test
    fun `loadCachedRoutes returns empty list when cache is empty`() {
        whenever(mockSharedPreferences.getString("routes_cache", null))
            .thenReturn(null)
        
        val result = CacheUtils.loadCachedRoutes(mockContext)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `loadCachedRoutes handles invalid JSON gracefully`() {
        whenever(mockSharedPreferences.getString("routes_cache", null))
            .thenReturn("invalid json")
        
        val result = CacheUtils.loadCachedRoutes(mockContext)
        assertTrue(result.isEmpty())
    }

    // ===== Тесты для clearCache =====
    
    @Test
    fun `clearCache removes all cached data`() {
        CacheUtils.clearCache(mockContext)
        
        verify(mockSharedPreferences).edit()
        verify(mockEditor).clear()
        verify(mockEditor).apply()
    }

    // ===== Тесты для getCacheTimestamp =====
    
    @Test
    fun `getCacheTimestamp returns stored timestamp`() {
        val timestamp = System.currentTimeMillis()
        whenever(mockSharedPreferences.getLong("cache_timestamp", 0L))
            .thenReturn(timestamp)
        
        val result = CacheUtils.getCacheTimestamp(mockContext)
        assertEquals(timestamp, result)
    }

    @Test
    fun `getCacheTimestamp returns zero when no timestamp exists`() {
        whenever(mockSharedPreferences.getLong("cache_timestamp", 0L))
            .thenReturn(0L)
        
        val result = CacheUtils.getCacheTimestamp(mockContext)
        assertEquals(0L, result)
    }

    // ===== Вспомогательные функции =====
    
    private fun createTestRoute(id: String, name: String): BusRoute {
        return BusRoute(
            id = id,
            routeNumber = id,
            name = name,
            description = "Тестовое описание",
            travelTime = "20 минут",
            paymentMethods = "Нал./Безнал.",
            color = "#FF1976D2"
        )
    }
}

