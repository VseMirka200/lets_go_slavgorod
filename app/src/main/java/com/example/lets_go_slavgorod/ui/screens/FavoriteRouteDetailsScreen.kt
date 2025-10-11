package com.example.lets_go_slavgorod.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lets_go_slavgorod.data.model.BusSchedule
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.ui.components.ScheduleCard
import com.example.lets_go_slavgorod.ui.components.StickyDepartureHeader
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel

/**
 * Экран деталей избранных времен для конкретного маршрута
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoriteRouteDetailsScreen(
    routeId: String,
    viewModel: BusViewModel,
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    val favoriteTimesList by viewModel.favoriteTimes.collectAsState()
    
    // Фильтруем избранные времена для этого маршрута
    val routeFavorites = remember(favoriteTimesList, routeId) {
        favoriteTimesList.filter { it.routeId == routeId }
    }
    
    // Получаем информацию о маршруте из первого избранного
    val routeInfo = routeFavorites.firstOrNull()
    val routeNumber = routeInfo?.routeNumber ?: ""
    val routeName = routeInfo?.routeName ?: "Маршрут"
    
    // Группируем по пунктам отправления с улучшенными названиями
    val groupedByDeparturePoint = remember(routeFavorites) {
        routeFavorites.groupBy { favoriteTime ->
            // Улучшаем название пункта отправления
            val departurePoint = favoriteTime.departurePoint.trim()
            when {
                departurePoint.equals("вокзал", ignoreCase = true) -> "Вокзал (отправление)"
                departurePoint.equals("совхоз", ignoreCase = true) -> "Совхоз (отправление)"
                departurePoint.startsWith("Рынок", ignoreCase = true) -> departurePoint
                departurePoint.startsWith("МСЧ", ignoreCase = true) -> departurePoint
                else -> "$departurePoint (отправление)"
            }
        }
    }
    
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = routeName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            navController?.navigate("route_notifications/$routeNumber")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Настройки уведомлений",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(0)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedByDeparturePoint.forEach { (departurePoint, timesInDepartureGroup) ->
                val departureKey = "$routeId-$departurePoint"
                val isExpanded = expandedStates[departureKey] ?: true
                
                // Sticky header для пункта отправления
                stickyHeader(key = "header_$departureKey") {
                    StickyDepartureHeader(
                        title = departurePoint,
                        itemsCount = timesInDepartureGroup.size,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedStates[departureKey] = !isExpanded
                        }
                    )
                }
                
                // Содержимое секции (только если развернуто)
                if (isExpanded) {
                    timesInDepartureGroup.forEach { favoriteTime ->
                        item(key = "time_${favoriteTime.id}") {
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
                                    viewModel.updateFavoriteActiveState(favoriteTime, !favoriteTime.isActive)
                                },
                                routeNumber = null,
                                routeName = null,
                                hideRouteInfo = true,
                                isNextUpcoming = false,
                                allSchedules = emptyList(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

