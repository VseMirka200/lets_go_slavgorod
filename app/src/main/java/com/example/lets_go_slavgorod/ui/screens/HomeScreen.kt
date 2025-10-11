package com.example.lets_go_slavgorod.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lets_go_slavgorod.R
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.ui.components.SearchBar
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.DisplaySettingsViewModel
import com.example.lets_go_slavgorod.utils.ConditionalLogging
import com.example.lets_go_slavgorod.ui.viewmodel.RouteDisplayMode
import com.example.lets_go_slavgorod.ui.components.BusRouteCard
import timber.log.Timber
import androidx.compose.runtime.LaunchedEffect

/**
 * Компонент состояния загрузки данных
 * 
 * Отображает индикатор загрузки с центрированием на экране
 * и информативным текстом для пользователя.
 * 
 * Показывается во время:
 * - Первоначальной загрузки маршрутов
 * - Обновления данных
 * - Любых асинхронных операций
 */
@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Загрузка маршрутов...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Компонент состояния ошибки при загрузке данных
 * 
 * Отображается при возникновении ошибок при загрузке маршрутов.
 * Показывает пользователю понятное сообщение об ошибке с иконкой.
 * 
 * Используется для:
 * - Ошибок сети
 * - Ошибок базы данных
 * - Неожиданных исключений
 * 
 * @param errorMessage сообщение об ошибке для отображения пользователю
 */
@Composable
fun ErrorState(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = stringResource(R.string.error_icon_description),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(id = R.string.error_loading_routes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage.ifEmpty { stringResource(id = R.string.unknown_error) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Состояние пустого списка маршрутов
 * 
 * Отображается когда нет доступных маршрутов или поиск не дал результатов.
 * Предоставляет пользователю понятную информацию о состоянии.
 * 
 * @param searchQuery текущий поисковый запрос пользователя
 */
@Composable
fun EmptyState(searchQuery: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Большая иконка с фоном
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (searchQuery.isNotEmpty()) Icons.Filled.SearchOff else Icons.Default.DirectionsBus,
                    contentDescription = stringResource(R.string.empty_state_icon_description),
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
            
            // Заголовок
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "Ничего не найдено"
                } else {
                    "Маршруты не найдены"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            // Описание
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "По запросу \"$searchQuery\" не найдено маршрутов.\nПопробуйте изменить поисковый запрос."
                } else {
                    "В данный момент маршруты недоступны.\nПотяните вниз для обновления."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Иконка-подсказка
            if (searchQuery.isEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Потяните экран вниз для обновления",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * Сетка маршрутов с максимальной оптимизацией производительности
 * 
 * Высокопроизводительная сетка с агрессивными оптимизациями:
 * - LazyVerticalGrid с оптимизированным кэшированием
 * - Предварительная загрузка элементов
 * - Минимизация перекомпозиций
 * - Оптимизированная навигация без задержек
 * 
 * @param routes список маршрутов для отображения
 * @param navController контроллер навигации для перехода к деталям маршрута
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesListState(
    routes: List<BusRoute>,
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val displaySettingsViewModel: DisplaySettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DisplaySettingsViewModel(context) as T
            }
        }
    )
    val displayMode by displaySettingsViewModel.displayMode.collectAsState(initial = RouteDisplayMode.GRID)
    val gridColumns by displaySettingsViewModel.gridColumns.collectAsState(initial = 2)
    
    when (displayMode) {
        RouteDisplayMode.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns), // Настраиваемое количество колонок
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Равномерные расстояния
                verticalArrangement = Arrangement.spacedBy(12.dp), // Равномерные расстояния
                // Агрессивные оптимизации производительности
                userScrollEnabled = true
            ) {
                items(
                    items = routes,
                    key = { route -> route.id },
                    contentType = { BusRoute::class }
                ) { route ->
                    // Оптимизированная карточка маршрута с минимальными перекомпозициями
                    BusRouteCard(
                        route = route,
                        isGridMode = true,
                        gridColumns = gridColumns,
                        onClick = {
                            // Быстрая навигация без задержек
                            try {
                                ConditionalLogging.debug("Navigation") { "Route clicked: ${route.id} - ${route.name}" }
                                navController.navigate("schedule/${route.id}") {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                }
                            } catch (e: Exception) {
                                ConditionalLogging.error("Navigation", e) { "Navigation error for route: ${route.id}" }
                            }
                        }
                    )
                }
            }
        }
        RouteDisplayMode.LIST -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = true,
                state = rememberLazyListState()
            ) {
                items(
                    items = routes,
                    key = { route -> route.id },
                    contentType = { BusRoute::class }
                ) { route ->
                    BusRouteCard(
                        route = route,
                        isGridMode = false,
                        onClick = {
                            try {
                                Timber.d("Route clicked: ${route.id} - ${route.name}")
                                navController.navigate("schedule/${route.id}") {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Navigation error for route: ${route.id}")
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Главный экран приложения с маршрутами автобусов
 * 
 * Основные функции:
 * - Отображение списка доступных маршрутов
 * - Поиск по маршрутам в реальном времени
 * - Навигация к деталям конкретного маршрута
 * - Обработка состояний загрузки, ошибок и пустого списка
 * 
 * Оптимизации:
 * - Использует LazyColumn для эффективного отображения списков
 * - Кэширует состояние для быстрого отклика
 * - Минимизирует перекомпозиции
 * 
 * @param navController контроллер навигации для переходов между экранами
 * @param viewModel ViewModel для управления данными маршрутов
 * @param modifier модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    Timber.d("HomeScreen is being displayed")
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Показываем Snackbar при ошибке
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = "Закрыть"
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name_actual),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    // Убрана кнопка обновления маршрутов
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(0)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = { /* Действие при поиске, если нужно */ },
            )

            when {
                uiState.isLoading -> LoadingState()
                uiState.error != null -> ErrorState(errorMessage = uiState.error!!)
                uiState.routes.isEmpty() -> EmptyState(searchQuery = searchQuery)
                else -> RoutesListState(
                    routes = uiState.routes,
                    navController = navController
                )
            }
        }
    }
}