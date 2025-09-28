package com.example.lets_go_slavgorod

// Android системные импорты

// Compose импорты

// ViewModel импорты
import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import timber.log.Timber
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.notifications.AlarmScheduler
import com.example.lets_go_slavgorod.ui.animations.NavigationAnimations
import com.example.lets_go_slavgorod.ui.components.UpdateDialogManager
import com.example.lets_go_slavgorod.ui.components.DisclaimerDialog
import com.example.lets_go_slavgorod.ui.navigation.BottomNavigation
import com.example.lets_go_slavgorod.ui.navigation.Screen
import com.example.lets_go_slavgorod.utils.DisclaimerManager
import com.example.lets_go_slavgorod.ui.screens.AboutScreen
import com.example.lets_go_slavgorod.ui.screens.FavoriteTimesScreen
import com.example.lets_go_slavgorod.ui.screens.HomeScreen
import com.example.lets_go_slavgorod.ui.screens.RouteNotificationSettingsScreen
import com.example.lets_go_slavgorod.ui.screens.ScheduleScreen
import com.example.lets_go_slavgorod.ui.screens.SettingsScreen
import com.example.lets_go_slavgorod.ui.screens.WebViewScreen
import com.example.lets_go_slavgorod.ui.theme.lets_go_slavgorodTheme
import com.example.lets_go_slavgorod.ui.viewmodel.AppTheme
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.NotificationSettingsViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.ThemeViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.ThemeViewModelFactory
import com.example.lets_go_slavgorod.ui.viewmodel.UpdateSettingsViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Главная активность приложения "Поехали! Славгород"
 * 
 * Оптимизированная активность для максимальной производительности:
 * - Быстрая инициализация без блокировок
 * - Оптимизированное управление жизненным циклом
 * - Эффективная обработка разрешений
 * - Плавная навигация между экранами
 * 
 * Основные функции:
 * - Управление разрешениями для уведомлений
 * - Инициализация темы приложения
 * - Настройка навигации между экранами
 * - Обработка точных будильников для уведомлений
 * 
 * Оптимизации производительности:
 * - Асинхронная инициализация тяжелых компонентов
 * - Кэширование ViewModels
 * - Минимизация перекомпозиций
 * - Оптимизированная обработка жизненного цикла
 * 
 * @author VseMirka200
 * @version 1.2
 * @since 1.0
 */
class MainActivity : ComponentActivity() {

    // =====================================================================================
    //                              VIEWMODELS И СОСТОЯНИЕ
    // =====================================================================================
    
    /** ViewModel для управления темой приложения */
    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(this)
    }

    /** Launcher для запроса разрешения на уведомления */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Timber.d("Notification permission granted.")
            } else {
                Timber.w("Notification permission denied.")
            }
        }
    
    /** Состояние показа диалога с предупреждением */
    private var showDisclaimerDialog by mutableStateOf(false)

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
                Timber.d("Notification permission already granted.")
                checkExactAlarmPermission()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Timber.i("Showing rationale for notification permission. Launching permission request again.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                Timber.d("Requesting notification permission.")
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
            Timber.i("Can schedule exact alarms: $canScheduleExact")

            if (!canScheduleExact) {
                Timber.w("Exact alarm permission not granted. User needs to enable it in settings.")
                // You could show a dialog here to guide the user to settings
            }
        }
    }

    /**
     * Восстанавливает уведомления после перезапуска приложения
     * 
     * Вызывается при запуске приложения для восстановления всех активных уведомлений
     * в соответствии с текущими настройками пользователя
     */
    private suspend fun restoreNotifications() {
        try {
            Timber.d("Restoring notifications after app restart")
            
            val database = AppDatabase.getDatabase(this)
            val favoriteTimeDao = database.favoriteTimeDao()
            
            val favoriteTimeEntities = favoriteTimeDao.getAllFavoriteTimes().firstOrNull() ?: emptyList()
            
            val activeFavoriteTimes = favoriteTimeEntities
                .filter { entity -> entity.isActive }
                .map { entity ->
                    FavoriteTime(
                        id = entity.id,
                        routeId = entity.routeId,
                        routeNumber = "N/A",
                        routeName = "Маршрут",
                        stopName = entity.stopName,
                        departureTime = entity.departureTime,
                        dayOfWeek = entity.dayOfWeek,
                        departurePoint = entity.departurePoint,
                        isActive = entity.isActive
                    )
                }
            
            AlarmScheduler.updateAllAlarmsBasedOnSettings(this, activeFavoriteTimes)
            Timber.d("Restored ${activeFavoriteTimes.size} active notifications")
            
        } catch (e: Exception) {
            Timber.e(e, "Error restoring notifications")
        }
    }

    // =====================================================================================
    //                              ЖИЗНЕННЫЙ ЦИКЛ АКТИВНОСТИ
    // =====================================================================================
    
    /**
     * Инициализация активности с оптимизациями производительности
     * 
     * Оптимизированная последовательность инициализации:
     * 1. Критичные операции (синхронно)
     * 2. UI инициализация (быстро)
     * 3. Тяжелые операции (асинхронно)
     * 
     * Оптимизации:
     * - Минимизация блокирующих операций
     * - Асинхронная обработка разрешений
     * - Кэширование состояния
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // =====================================================================================
        //                              КРИТИЧЕСКИ ВАЖНЫЕ ОПЕРАЦИИ
        // =====================================================================================
        
        // Включаем Edge-to-Edge для современного дизайна
        enableEdgeToEdge()

        // =====================================================================================
        //                              ИНИЦИАЛИЗАЦИЯ UI
        // =====================================================================================
        
        // Быстрая инициализация UI
        setContent {
            BusScheduleApp(themeViewModel = themeViewModel)
        }
        
        // =====================================================================================
        //                              АСИНХРОННЫЕ ОПЕРАЦИИ
        // =====================================================================================
        
        // Проверяем, нужно ли показать диалог с предупреждением (быстро)
        if (DisclaimerManager.shouldShowDisclaimer(this)) {
            showDisclaimerDialog = true
        }
        
        // Запрашиваем разрешения асинхронно, чтобы не блокировать UI
        lifecycleScope.launch {
            askNotificationPermission()
            // Восстанавливаем уведомления после перезапуска приложения
            restoreNotifications()
        }
    }
    
    /**
     * Оптимизированная обработка паузы активности
     * 
     * Вызывается при сворачивании приложения для:
     * - Сохранения состояния
     * - Очистки временных ресурсов
     * - Оптимизации производительности
     */
    override fun onPause() {
        super.onPause()
        Timber.d("MainActivity onPause - optimizing for background")
        
        // Оптимизации для фонового режима
        // (здесь можно добавить дополнительные оптимизации)
    }
    
    /**
     * Оптимизированная обработка возобновления активности
     * 
     * Вызывается при возврате к приложению для:
     * - Восстановления состояния
     * - Обновления данных
     * - Оптимизации производительности
     */
    override fun onResume() {
        super.onResume()
        Timber.d("MainActivity onResume - optimizing for foreground")
        
        // Оптимизации для активного режима
        // (здесь можно добавить дополнительные оптимизации)
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
    var showDisclaimer by remember { mutableStateOf(false) }
    val busViewModel: BusViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BusViewModel(localContext.applicationContext as Application) as T
            }
        }
    )
    
    val notificationSettingsViewModel: NotificationSettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationSettingsViewModel(localContext.applicationContext as Application) as T
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
    
    // Проверяем, нужно ли показать диалог с предупреждением
    if (DisclaimerManager.shouldShowDisclaimer(localContext)) {
        showDisclaimer = true
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
                themeViewModel = themeViewModel,
                notificationSettingsViewModel = notificationSettingsViewModel
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
            
            // Диалог с предупреждением о неофициальном статусе приложения
            if (showDisclaimer) {
                DisclaimerDialog(
                    onDismiss = { showDisclaimer = false },
                    onAccept = {
                        DisclaimerManager.markDisclaimerAccepted(localContext)
                        showDisclaimer = false
                    },
                    onDontShowAgain = {
                        DisclaimerManager.markDisclaimerDontShowAgain(localContext)
                        showDisclaimer = false
                    }
                )
            }
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
    themeViewModel: ThemeViewModel,
    notificationSettingsViewModel: NotificationSettingsViewModel
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
            HomeScreen(
                navController = navController,
                viewModel = busViewModel
            )
        }

        composable(
            route = Screen.FavoriteTimes.route,
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft }
        ) {
            FavoriteTimesScreen(
                viewModel = busViewModel,
                navController = navController
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
            Timber.d("Navigating to schedule for routeId: $routeId")
            val route = busViewModel.getRouteById(routeId)
            Timber.d("Found route: ${route?.name} (${route?.id})")
            ScheduleScreen(
                route = route,
                onBackClick = { navController.popBackStack() },
                viewModel = busViewModel
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
            route = "route_notifications/{routeId}",
            arguments = listOf(
                navArgument("routeId") { type = NavType.StringType }
            ),
            enterTransition = { NavigationAnimations.slideInFromRight },
            exitTransition = { NavigationAnimations.slideOutToLeft }
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            val route = busViewModel.getRouteById(routeId)
            if (route != null) {
                RouteNotificationSettingsScreen(
                    route = route,
                    notificationSettingsViewModel = notificationSettingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
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