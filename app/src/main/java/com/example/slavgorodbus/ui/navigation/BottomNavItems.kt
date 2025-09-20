package com.example.slavgorodbus.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings

/**
 * Данные для элемента нижней навигации
 */
data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String
)

/**
 * Список элементов нижней навигации
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        icon = Icons.Default.Home,
        title = "Главная"
    ),
    BottomNavItem(
        route = Screen.FavoriteTimes.route,
        icon = Icons.Default.Favorite,
        title = "Избранное"
    ),
    BottomNavItem(
        route = Screen.Settings.route,
        icon = Icons.Default.Settings,
        title = "Настройки"
    )
)
