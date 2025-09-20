package com.example.slavgorodbus.ui.navigation

/**
 * Sealed class для определения экранов навигации
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FavoriteTimes : Screen("favorite_times")
    object Settings : Screen("settings")
    object About : Screen("about")
}
