package com.example.lets_go_slavgorod

// Android системные импорты
import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log

// Compose импорты
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// ViewModel импорты
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lets_go_slavgorod.ui.animations.NavigationAnimations
import com.example.lets_go_slavgorod.ui.navigation.BottomNavigation
import com.example.lets_go_slavgorod.ui.navigation.Screen
import com.example.lets_go_slavgorod.ui.screens.AboutScreen
import com.example.lets_go_slavgorod.ui.screens.HomeScreen
import com.example.lets_go_slavgorod.ui.screens.ScheduleScreen
import com.example.lets_go_slavgorod.ui.screens.SettingsScreen
import com.example.lets_go_slavgorod.ui.screens.SwipeableMainScreen
import com.example.lets_go_slavgorod.ui.screens.WebViewScreen
import com.example.lets_go_slavgorod.ui.theme.lets_go_slavgorodTheme
import com.example.lets_go_slavgorod.ui.viewmodel.AppTheme
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.ThemeViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.ThemeViewModelFactory
import com.example.lets_go_slavgorod.ui.viewmodel.UpdateSettingsViewModel
import com.example.lets_go_slavgorod.ui.components.UpdateDialogManager

/**
 * Главная активность приложения "Поехали! Славгород"
 * 
 * Основные функции:
 * - Управление разрешениями для уведомлений
 * - Инициализация темы приложения
 * - Настройка навигации между экранами
 * - Обработка точных будильников для уведомлений
 */
class MainActivity : ComponentActivity() {

    // ViewModel для управления темой приложения
    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(this)
    }

    // Launcher для запроса разрешения на уведомления
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted.")
            } else {
                Log.w("MainActivity", "Notification permission denied.")
            }
        }

    /**
     * Запрашивает разрешение на отправку уведомлений
     * 
     * Проверяет версию Android и соответствующие разрешения:
     * - Android 13+: POST_NOTIFICATIONS
     * - Старые версии: точные будильники
     */
    private fun askNotificationPermission() {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                // For older versions, check exact alarm permission
                checkExactAlarmPermission()
            }
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("MainActivity", "Notification permission already granted.")
                checkExactAlarmPermission()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Log.i("MainActivity", "Showing rationale for notification permission. Launching permission request again.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                Log.d("MainActivity", "Requesting notification permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Проверяет разрешение на точные будильники
     * 
     * Необходимо для корректной работы уведомлений о времени отправления автобусов
     */
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as? AlarmManager
            val canScheduleExact = alarmManager?.canScheduleExactAlarms() ?: false
            Log.i("MainActivity", "Can schedule exact alarms: $canScheduleExact")

            if (!canScheduleExact) {
                Log.w("MainActivity", "Exact alarm permission not granted. User needs to enable it in settings.")
                // You could show a dialog here to guide the user to settings
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        askNotificationPermission()

        setContent {
            BusScheduleApp(themeViewModel = themeViewModel)
        }
    }
}

/**
 * Основной Composable компонент приложения
 * 
 * Настраивает:
 * - Тему приложения (светлая/темная/системная)
 * - Навигацию между экранами
 * - Нижнюю панель навигации
 * 
 * @param themeViewModel ViewModel для управления темой
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScheduleApp(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val localContext = LocalContext.current
    val busViewModel: BusViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BusViewModel(localContext.applicationContext as Application) as T
            }
        }
    )
    
    // ViewModel для управления обновлениями
    val updateSettingsViewModel: UpdateSettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UpdateSettingsViewModel(localContext) as T
            }
        }
    )
    
    val currentAppTheme by themeViewModel.currentTheme.collectAsState()
    val useDarkTheme = when (currentAppTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    // Данные о доступном обновлении
    val availableUpdateVersion by updateSettingsViewModel.availableUpdateVersion.collectAsState(initial = null)
    val availableUpdateUrl by updateSettingsViewModel.availableUpdateUrl.collectAsState(initial = null)
    val availableUpdateNotes by updateSettingsViewModel.availableUpdateNotes.collectAsState(initial = null)

    lets_go_slavgorodTheme(darkTheme = useDarkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigation(navController = navController)
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                busViewModel = busViewModel,
                themeViewModel = themeViewModel
            )
            
            // Глобальный диалог обновления
            UpdateDialogManager(
                availableUpdateVersion = availableUpdateVersion,
                availableUpdateUrl = availableUpdateUrl,
                availableUpdateNotes = availableUpdateNotes,
                onDownloadUpdate = { url ->
                    // Открываем ссылку в WebView внутри приложения
                    val route = Screen.WebView.createRoute(url, "Скачать обновление")
                    navController.navigate(route)
                },
                onClearAvailableUpdate = {
                    updateSettingsViewModel.clearAvailableUpdate()
                }
            )
        }
    }
}

/**
 * Навигационный хост приложения
 * 
 * Определяет маршруты между экранами:
 * - Главная: список маршрутов
 * - Избранное: сохраненные маршруты
 * - Настройки: конфигурация приложения
 * - О программе: информация и поддержка
 * - Детали маршрута: расписание конкретного маршрута
 * 
 * @param navController контроллер навигации
 * @param modifier модификатор для настройки внешнего вида
 * @param busViewModel ViewModel для работы с данными маршрутов
 * @param themeViewModel ViewModel для управления темой
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    busViewModel: BusViewModel,
    themeViewModel: ThemeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft }
        ) {
            SwipeableMainScreen(
                navController = navController,
                busViewModel = busViewModel,
                themeViewModel = themeViewModel
            )
        }

        composable(
            route = Screen.FavoriteTimes.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft }
        ) {
            SwipeableMainScreen(
                navController = navController,
                busViewModel = busViewModel,
                themeViewModel = themeViewModel
            )
        }

        composable(
            route = "schedule/{routeId}",
            arguments = listOf(
                navArgument("routeId") { type = NavType.StringType }
            ),
            enterTransition = { NavigationAnimations.slideInFromBottomSchedule },
            exitTransition = { NavigationAnimations.slideOutToBottomSchedule }
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            val route = busViewModel.getRouteById(routeId)
            ScheduleScreen(
                route = route,
                onBackClick = { navController.popBackStack() },
                viewModel = busViewModel,
                navController = navController
            )
        }


        composable(
            route = Screen.Settings.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft }
        ) {
            SettingsScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }

        composable(
            route = Screen.About.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft }
        ) {
            AboutScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "webview/{url}/{title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            ),
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft }
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url")?.decodeUrl() ?: ""
            val title = backStackEntry.arguments?.getString("title")?.decodeUrl() ?: "Веб-страница"
            
            // Определяем, нужен ли полноэкранный режим для страниц поддержки и платежей
            val isFullScreen = url.contains("cloudtips.ru") || 
                              url.contains("pay.") || 
                              url.contains("donate") || 
                              url.contains("support") ||
                              url.contains("payment")
            
            WebViewScreen(
                navController = navController,
                url = url,
                title = title,
                isFullScreen = isFullScreen
            )
        }
        
    }
}

/**
 * Декодирует URL из навигации
 */
private fun String.decodeUrl(): String {
    return this.replace("%2F", "/")
        .replace("%3A", ":")
        .replace("%3F", "?")
        .replace("%26", "&")
        .replace("%3D", "=")
        .replace("%23", "#")
        .replace("%20", " ")
}