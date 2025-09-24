package com.example.slavgorodbus.ui.navigation

/**
 * Sealed class для определения экранов навигации
 * 
 * Содержит все основные экраны приложения:
 * - Home: главный экран с маршрутами
 * - FavoriteTimes: экран избранных маршрутов
 * - Settings: экран настроек приложения
 * - About: экран информации о программе
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FavoriteTimes : Screen("favorite_times")
    object Settings : Screen("settings")
    object About : Screen("about")
}
