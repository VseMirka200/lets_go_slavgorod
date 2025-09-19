package com.example.slavgorodbus.ui.screens

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.slavgorodbus.ui.components.SwipeableContainer
import com.example.slavgorodbus.ui.navigation.Screen
import com.example.slavgorodbus.ui.navigation.bottomNavItems
import com.example.slavgorodbus.ui.viewmodel.BusViewModel
import com.example.slavgorodbus.ui.viewmodel.ThemeViewModel

/**
 * Главный экран с поддержкой свайп-навигации между основными разделами
 * 
 * Функциональность:
 * - Объединяет три основных экрана: Главная, Избранное, Настройки
 * - Поддерживает свайп-навигацию между экранами
 * - Синхронизируется с нижней навигацией
 * - Сохраняет состояние экранов при переключении
 * 
 * @param navController контроллер навигации для управления переходами
 * @param busViewModel ViewModel для управления данными автобусов
 * @param themeViewModel ViewModel для управления темой приложения
 * @param modifier модификатор для настройки внешнего вида
 */
@RequiresApi(Build.VERSION_CODES.O)
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
    
    // Находим текущий индекс экрана в списке основных экранов
    val currentIndex = remember(currentScreenRoute) {
        bottomNavItems.indexOfFirst { it.route == currentScreenRoute }.takeIf { it >= 0 } ?: 0
    }

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
                // Получаем Activity для передачи в SettingsScreen
                val activity = LocalContext.current as ComponentActivity
                SettingsScreen(
                    themeViewModel = themeViewModel,
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    activity = activity
                )
            }
        }
    }
}
