package com.example.lets_go_slavgorod.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lets_go_slavgorod.R
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.ui.components.BusRouteCard
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.DisplaySettingsViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.RouteDisplayMode

@SuppressLint("LogNotTimber")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteTimesScreen(
    viewModel: BusViewModel,
    navController: NavController? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val favoriteTimesList by viewModel.favoriteTimes.collectAsState()
    val isLoading = false
    
    // Получаем настройки отображения
    val displaySettingsViewModel: DisplaySettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DisplaySettingsViewModel(context) as T
            }
        }
    )
    val displayMode by displaySettingsViewModel.displayMode.collectAsState(initial = RouteDisplayMode.GRID)
    
    // Группируем избранные времена по маршрутам и сортируем по дате добавления
    val favoriteRoutes = remember(favoriteTimesList) {
        favoriteTimesList
            .groupBy { it.routeId }
            .map { (routeId, times) ->
                // Берем самое последнее добавленное время для определения даты
                val latestTime = times.maxByOrNull { it.addedDate } ?: times.first()
                // Получаем цвет для маршрута (используем те же цвета, что в BusRouteRepository)
                val routeColor = when (routeId) {
                    "102" -> "#FF6200EE"  // Фиолетовый
                    "1" -> "#FF1976D2"    // Синий
                    else -> "#1976D2"     // По умолчанию синий
                }
                // Создаем виртуальный BusRoute для отображения
                BusRoute(
                    id = routeId,
                    routeNumber = latestTime.routeNumber,
                    name = latestTime.routeName,
                    description = "${times.size} избранных времен",
                    travelTime = null,
                    paymentMethods = null,
                    color = routeColor
                ) to latestTime.addedDate
            }
            .sortedByDescending { it.second } // Сортируем по дате добавления (новые сначала)
            .map { it.first } // Убираем временную метку, оставляем только BusRoute
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.favorite_times_screen_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
        when {
            isLoading -> FLoadingState()
            favoriteTimesList.isEmpty() -> FEmptyState()
            else -> {
                // Отображаем карточки маршрутов в зависимости от режима
                val isGridMode = displayMode == RouteDisplayMode.GRID
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (isGridMode) {
                    // Режим сетки
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = favoriteRoutes,
                            key = { route -> route.id }
                        ) { route ->
                            BusRouteCard(
                                route = route,
                                onClick = { 
                                    navController?.navigate("favorite_route_details/${route.id}")
                                },
                                isGridMode = true,
                                showNotificationButton = false
                            )
                        }
                    }
                } else {
                    // Режим списка
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        userScrollEnabled = true
                    ) {
                        items(
                            items = favoriteRoutes,
                            key = { route -> route.id }
                        ) { route ->
                            BusRouteCard(
                                route = route,
                                onClick = { 
                                    navController?.navigate("favorite_route_details/${route.id}")
                                },
                                isGridMode = false,
                                showNotificationButton = true,
                                onNotificationClick = {
                                    navController?.navigate("route_notifications/${route.routeNumber}")
                                }
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
fun FLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun FEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = stringResource(R.string.empty_state_icon_description_favorites),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(id = R.string.no_favorite_times_title_updated),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(id = R.string.no_favorite_times_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}