package com.example.slavgorodbus.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Навигационные элементы для нижней панели навигации
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Главная",
        icon = Icons.Default.Home
    )
    
    object FavoriteTimes : Screen(
        route = "favorite_times",
        title = "Избранное",
        icon = Icons.Default.Favorite
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Настройки",
        icon = Icons.Default.Settings
    )
    
    object RouteDetails : Screen(
        route = "route_details/{routeId}",
        title = "Детали маршрута",
        icon = Icons.Default.Home
    ) {
        fun createRoute(routeId: Int) = "route_details/$routeId"
    }
    
    object Schedule : Screen(
        route = "schedule",
        title = "Расписание",
        icon = Icons.Default.Home
    )
    
    object About : Screen(
        route = "about",
        title = "О приложении",
        icon = Icons.Default.Home
    )
}

/**
 * Список основных экранов для нижней навигации
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.FavoriteTimes,
    Screen.Settings
)
