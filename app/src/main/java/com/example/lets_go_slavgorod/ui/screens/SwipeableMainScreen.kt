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
 * - Объединяет два основных экрана: Главная, Избранное
 * - Поддерживает горизонтальные свайпы для переключения между экранами
 * - Синхронизируется с нижней навигацией
 * - Сохраняет состояние экранов при переключении
 * - Адаптивные пороги свайпа для лучшего UX
 * 
 * Порядок экранов:
 * 0 - Маршруты (список маршрутов)
 * 1 - Избранное (сохраненные маршруты)
 * 
 * @param navController контроллер навигации для управления переходами
 * @param busViewModel ViewModel для управления данными автобусов
 * @param themeViewModel ViewModel для управления темой приложения
 * @param modifier модификатор для настройки внешнего вида
 */
@Composable
fun SwipeableMainScreen(
    navController: NavController,
    busViewModel: BusViewModel,
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    // Получаем текущий маршрут из навигации
    val currentRoute by navController.currentBackStackEntryAsState()
    val currentScreenRoute = currentRoute?.destination?.route
    
    // Вычисляем текущий индекс на основе маршрута (только Главная и Избранное)
    val currentIndex = when {
        currentScreenRoute == Screen.FavoriteTimes.route -> 1 // Избранное
        currentScreenRoute == Screen.Home.route -> 0 // Главная
        else -> 0 // По умолчанию главная
    }
    
    Log.d("SwipeableMainScreen", "Current index: $currentIndex, currentScreenRoute: $currentScreenRoute")

    SwipeableContainer(
        currentIndex = currentIndex,
        // Обработка свайпа влево (по порядку экранов)
        onSwipeToNext = {
            Log.d("SwipeableMainScreen", "Swipe left detected, currentIndex: $currentIndex")
            when (currentIndex) {
                0 -> {
                    // С маршрутов на избранное
                    Log.d("SwipeableMainScreen", "Navigating from Home to FavoriteTimes")
                    navController.navigate(Screen.FavoriteTimes.route) {
                        popUpTo(0) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                1 -> {
                    // С избранного на настройки
                    Log.d("SwipeableMainScreen", "Navigating from FavoriteTimes to Settings")
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(0) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                2 -> {
                    // С настроек на маршруты (циклично)
                    Log.d("SwipeableMainScreen", "Navigating from Settings to Home")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
        // Обработка свайпа вправо (в обратном порядке)
        onSwipeToPrevious = {
            Log.d("SwipeableMainScreen", "Swipe right detected, currentIndex: $currentIndex")
            when (currentIndex) {
                0 -> {
                    // С маршрутов на настройки (циклично)
                    Log.d("SwipeableMainScreen", "Navigating from Home to Settings")
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(0) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                1 -> {
                    // С избранного на маршруты
                    Log.d("SwipeableMainScreen", "Navigating from FavoriteTimes to Home")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                2 -> {
                    // С настроек на избранное
                    Log.d("SwipeableMainScreen", "Navigating from Settings to FavoriteTimes")
                    navController.navigate(Screen.FavoriteTimes.route) {
                        popUpTo(0) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
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
        }
    }
}
