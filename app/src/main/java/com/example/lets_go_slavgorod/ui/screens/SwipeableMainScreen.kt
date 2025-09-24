package com.example.lets_go_slavgorod.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lets_go_slavgorod.ui.components.SwipeableContainer
import com.example.lets_go_slavgorod.ui.navigation.Screen
import com.example.lets_go_slavgorod.ui.navigation.bottomNavItems
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.ThemeViewModel

/**
 * Главный экран с поддержкой свайп-навигации между основными разделами
 * 
 * Функциональность:
 * - Объединяет четыре основных экрана: Главная, Избранное, Настройки, О программе
 * - Поддерживает горизонтальные свайпы для переключения между экранами
 * - Синхронизируется с нижней навигацией
 * - Сохраняет состояние экранов при переключении
 * - Адаптивные пороги свайпа для лучшего UX
 * 
 * Порядок экранов:
 * 0 - Главная (список маршрутов)
 * 1 - Избранное (сохраненные маршруты)
 * 2 - Настройки (конфигурация приложения)
 * 3 - О программе (информация и поддержка)
 * 
 * @param navController контроллер навигации для управления переходами
 * @param busViewModel ViewModel для управления данными автобусов
 * @param themeViewModel ViewModel для управления темой приложения
 * @param modifier модификатор для настройки внешнего вида
 * @param forceSettingsIndex принудительно показать экран настроек
 */
@Composable
fun SwipeableMainScreen(
    navController: NavController,
    busViewModel: BusViewModel,
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier,
    forceSettingsIndex: Boolean = false
) {
    // Получаем текущий маршрут из навигации
    val currentRoute by navController.currentBackStackEntryAsState()
    val currentScreenRoute = currentRoute?.destination?.route
    
    // Вычисляем текущий индекс на основе маршрута
    val currentIndex = when {
        forceSettingsIndex -> 2 // Принудительно показываем настройки
        currentScreenRoute == Screen.About.route -> 3 // О программе
        currentScreenRoute == Screen.Settings.route -> 2 // Настройки
        currentScreenRoute == Screen.FavoriteTimes.route -> 1 // Избранное
        currentScreenRoute == Screen.Home.route -> 0 // Главная
        else -> 0 // По умолчанию главная
    }
    
    Log.d("SwipeableMainScreen", "Current index: $currentIndex, forceSettingsIndex: $forceSettingsIndex, currentScreenRoute: $currentScreenRoute")

    SwipeableContainer(
        currentIndex = currentIndex,
        // Обработка свайпа влево (переход к следующему экрану)
        onSwipeToNext = {
            if (currentIndex < bottomNavItems.size - 1) {
                val nextScreen = bottomNavItems[currentIndex + 1]
                navController.navigate(nextScreen.route) {
                    // Сохраняем состояние при переходах
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        // Обработка свайпа вправо (переход к предыдущему экрану)
        onSwipeToPrevious = {
            if (currentIndex > 0) {
                val previousScreen = bottomNavItems[currentIndex - 1]
                navController.navigate(previousScreen.route) {
                    // Сохраняем состояние при переходах
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { index ->
        // Отображение соответствующего экрана в зависимости от индекса
        when (index) {
            0 -> HomeScreen(
                navController = navController,
                viewModel = busViewModel
            )
            1 -> FavoriteTimesScreen(
                viewModel = busViewModel
            )
            2 -> {
                SettingsScreen(
                    themeViewModel = themeViewModel
                )
            }
            3 -> {
                AboutScreen(
                    onBackClick = {
                        // Возвращаемся на главную при нажатии "Назад" в свайп-режиме
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
