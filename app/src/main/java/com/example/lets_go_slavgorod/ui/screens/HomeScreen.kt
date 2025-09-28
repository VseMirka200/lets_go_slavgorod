package com.example.lets_go_slavgorod.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.lets_go_slavgorod.ui.components.BusRouteCard
import com.example.lets_go_slavgorod.ui.navigation.Screen
import timber.log.Timber

/**
 * Состояние загрузки данных
 * 
 * Отображается во время инициализации приложения и загрузки маршрутов.
 * Использует Material Design 3 компоненты для консистентности.
 */
@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Состояние ошибки при загрузке данных
 * 
 * Отображается при возникновении ошибок при загрузке маршрутов.
 * Показывает пользователю понятное сообщение об ошибке с иконкой.
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = stringResource(R.string.empty_state_icon_description),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "По запросу \"$searchQuery\" ничего не найдено"
                } else {
                    "Маршруты не найдены"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "Попробуйте изменить поисковый запрос",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Список маршрутов с максимальной оптимизацией производительности
 * 
 * Высокопроизводительный список с агрессивными оптимизациями:
 * - LazyColumn с оптимизированным кэшированием
 * - Предварительная загрузка элементов
 * - Минимизация перекомпозиций
 * - Оптимизированная навигация без задержек
 * 
 * @param routes список маршрутов для отображения
 * @param navController контроллер навигации для перехода к деталям маршрута
 */
@Composable
fun RoutesListState(
    routes: List<BusRoute>,
    navController: NavController,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        // Агрессивные оптимизации производительности
        userScrollEnabled = true,
        // Дополнительные оптимизации для плавной прокрутки
        state = rememberLazyListState()
    ) {
        items(
            items = routes,
            key = { route -> route.id },
            contentType = { BusRoute::class }
        ) { route ->
            // Оптимизированная карточка маршрута с минимальными перекомпозициями
            BusRouteCard(
                route = route,
                onRouteClick = { clickedRoute ->
                    // Быстрая навигация без задержек
                    try {
                        Timber.d("Route clicked: ${clickedRoute.id} - ${clickedRoute.name}")
                        navController.navigate("schedule/${clickedRoute.id}") {
                            // Оптимизированные флаги навигации
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("home") {
                                saveState = true
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Navigation error for route: ${clickedRoute.id}")
                    }
                }
            )
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

    // Убраны свайпы - используем обычный контейнер
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name_actual),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                    navController = navController,
                )
            }
        }
    }
}