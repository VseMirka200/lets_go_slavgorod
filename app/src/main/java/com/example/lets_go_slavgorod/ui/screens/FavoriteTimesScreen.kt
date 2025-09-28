package com.example.lets_go_slavgorod.ui.screens

import android.annotation.SuppressLint
import timber.log.Timber
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lets_go_slavgorod.R
import com.example.lets_go_slavgorod.data.model.BusSchedule
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.ui.components.ScheduleCard
import com.example.lets_go_slavgorod.ui.navigation.Screen
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel

@SuppressLint("LogNotTimber")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteTimesScreen(
    viewModel: BusViewModel,
    navController: NavController? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val favoriteTimesList by viewModel.favoriteTimes.collectAsState()
    val isLoading = false

    val groupedByRoute = remember(favoriteTimesList) {
        favoriteTimesList.groupBy { favoriteTime ->
            // Нормализуем данные для группировки, чтобы избежать дублирования
            val routeNumber = favoriteTime.routeNumber.trim()
            val routeName = favoriteTime.routeName.trim()
            
            // Создаем уникальный ключ для группировки
            val normalizedRouteName = if (routeName.isNotBlank()) {
                if (routeName.contains("№$routeNumber") || routeName.contains("$routeNumber")) {
                    routeName
                } else {
                    "$routeName №$routeNumber"
                }
            } else {
                "Маршрут №$routeNumber"
            }
            
            routeNumber to normalizedRouteName
        }
    }

    // Убираем группировку по точкам отправления - показываем только времена
    val nestedGroupedFavoriteTimes = remember(groupedByRoute) {
        groupedByRoute.mapValues { (_, timesInRoute) ->
            // Группируем все времена в одну группу для каждого маршрута
            mapOf("Все времена" to timesInRoute)
        }
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
                )
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 0.dp)
        ) {
            when {
                isLoading -> FLoadingState()
                favoriteTimesList.isEmpty() -> FEmptyState()
                else -> FavoriteNestedGroupedList(
                    nestedGroupedTimes = nestedGroupedFavoriteTimes,
                    onToggleFavoriteActiveState = { favoriteTime: FavoriteTime, isActive: Boolean ->
                        viewModel.updateFavoriteActiveState(favoriteTime, isActive)
                    },
                    navController = navController
                )
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

@Composable
fun FavoriteNestedGroupedList(
    nestedGroupedTimes: Map<Pair<String, String>, Map<String, List<FavoriteTime>>>,
    onToggleFavoriteActiveState: (favoriteTime: FavoriteTime, isActive: Boolean) -> Unit,
    navController: NavController? = null
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    
    // Оптимизация: кэшируем вычисления
    val routeKeys = remember(nestedGroupedTimes.keys) {
        nestedGroupedTimes.keys.map { (routeNumber, routeName) -> "$routeNumber-$routeName" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        nestedGroupedTimes.forEach { (routeInfoPair, timesByDeparturePoint) ->
            val (routeNumber, routeName) = routeInfoPair
            val routeKey = remember(routeNumber, routeName) { "$routeNumber-$routeName" }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = routeName, // routeName уже нормализован при группировке
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = { 
                                navController?.navigate("route_notifications/${routeNumber}")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Настройки уведомлений",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Показываем все времена для маршрута без группировки по точкам отправления
                    timesByDeparturePoint.forEach { (_, timesInDepartureGroup) ->
                        timesInDepartureGroup.forEach { favoriteTime ->
                            val scheduleDisplay = BusSchedule(
                                id = favoriteTime.id,
                                routeId = favoriteTime.routeId ?: "",
                                departureTime = favoriteTime.departureTime,
                                dayOfWeek = favoriteTime.dayOfWeek,
                                stopName = favoriteTime.stopName,
                                departurePoint = favoriteTime.departurePoint
                            )
                            
                            ScheduleCard(
                                schedule = scheduleDisplay,
                                isFavorite = favoriteTime.isActive,
                                onFavoriteClick = {
                                    onToggleFavoriteActiveState(favoriteTime, !favoriteTime.isActive)
                                },
                                routeNumber = null, // Скрываем номер маршрута, так как он уже в заголовке
                                routeName = null,   // Скрываем название маршрута, так как оно уже в заголовке
                                hideRouteInfo = true, // Скрываем всю информацию о маршруте
                                isNextUpcoming = false, // Обычный стиль для избранных
                                allSchedules = emptyList(), // Пустой список для обычного стиля
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableDeparturePointSection(
    departurePoint: String,
    schedulesInGroup: List<FavoriteTime>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleFavoriteActiveState: (favoriteTime: FavoriteTime, isActive: Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleExpand
                )
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = departurePoint,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.animateContentSize()
            ) {
                schedulesInGroup.forEach { favoriteTime ->
                    val scheduleDisplay = BusSchedule(
                        id = favoriteTime.id,
                        routeId = favoriteTime.routeId ?: "",
                        departureTime = favoriteTime.departureTime,
                        dayOfWeek = favoriteTime.dayOfWeek,
                        stopName = favoriteTime.stopName,
                        departurePoint = favoriteTime.departurePoint
                    )
                    
                    ScheduleCard(
                        schedule = scheduleDisplay,
                        isFavorite = favoriteTime.isActive,
                        onFavoriteClick = {
                            onToggleFavoriteActiveState(favoriteTime, !favoriteTime.isActive)
                        },
                        routeNumber = favoriteTime.routeNumber,
                        routeName = favoriteTime.routeName,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}