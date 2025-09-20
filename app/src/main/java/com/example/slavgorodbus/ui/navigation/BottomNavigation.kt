package com.example.slavgorodbus.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Нижняя панель навигации для основных экранов приложения
 * @param navController контроллер навигации для управления переходами
 */
@Composable
fun BottomNavigation(navController: NavController) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val currentScreenRoute = currentRoute?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = currentScreenRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        // Очищаем стек навигации при переходе к основным экранам
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}