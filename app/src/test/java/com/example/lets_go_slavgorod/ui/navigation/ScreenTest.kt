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

}
