package com.example.lets_go_slavgorod.ui.navigation

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для навигационных экранов
 */
class ScreenTest {

    @Test
    fun `Home route should be correct`() {
        assertEquals("home", Screen.Home.route)
    }

    @Test
    fun `FavoriteTimes route should be correct`() {
        assertEquals("favorite_times", Screen.FavoriteTimes.route)
    }

    @Test
    fun `Settings route should be correct`() {
        assertEquals("settings", Screen.Settings.route)
    }

    @Test
    fun `About route should be correct`() {
        assertEquals("about", Screen.About.route)
    }

    @Test
    fun `WebView route should be correct`() {
        assertEquals("webview/{url}/{title}", Screen.WebView.route)
    }

    @Test
    fun `WebView createRoute should encode URL correctly`() {
        // Given
        val url = "https://example.com/path?param=value"
        val title = "Test Page"

        // When
        val route = Screen.WebView.createRoute(url, title)

        // Then
        assertTrue(route.contains("webview/"))
        assertTrue(route.contains("https%3A//example.com/path%3Fparam%3Dvalue"))
        assertTrue(route.contains("Test%20Page"))
    }

    @Test
    fun `WebView createRoute should handle special characters`() {
        // Given
        val url = "https://example.com/path with spaces"
        val title = "Page with & symbols"

        // When
        val route = Screen.WebView.createRoute(url, title)

        // Then
        assertTrue(route.contains("webview/"))
        assertTrue(route.contains("https%3A//example.com/path%20with%20spaces"))
        assertTrue(route.contains("Page%20with%20%26%20symbols"))
    }

    @Test
    fun `WebView createRoute should use default title when not provided`() {
        // Given
        val url = "https://example.com"

        // When
        val route = Screen.WebView.createRoute(url)

        // Then
        assertTrue(route.contains("webview/"))
        assertTrue(route.contains("https%3A//example.com"))
        assertTrue(route.contains("Веб-страница"))
    }
}
