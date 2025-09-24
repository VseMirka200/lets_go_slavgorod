package com.example.lets_go_slavgorod.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lets_go_slavgorod.R
import com.example.lets_go_slavgorod.data.model.FavoriteTime
import com.example.lets_go_slavgorod.ui.components.ScheduleCard
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteTimesScreen(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteTimesList by viewModel.favoriteTimes.collectAsState()
    val isLoading = false

    val groupedByRoute = remember(favoriteTimesList) {
        favoriteTimesList.groupBy {
            it.routeNumber to it.routeName
        }
    }

    val nestedGroupedFavoriteTimes = remember(groupedByRoute) {
        groupedByRoute.mapValues { (_, timesInRoute) ->
            timesInRoute.groupBy { it.departurePoint }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> FLoadingState()
                favoriteTimesList.isEmpty() -> FEmptyState()
                else -> FavoriteNestedGroupedList(
                    nestedGroupedTimes = nestedGroupedFavoriteTimes,
                    onToggleFavoriteActiveState = { favoriteTime, isActive ->
                        viewModel.updateFavoriteActiveState(favoriteTime, isActive)
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteNestedGroupedList(
    modifier: Modifier = Modifier,
    nestedGroupedTimes: Map<Pair<String, String>, Map<String, List<FavoriteTime>>>,
    onToggleFavoriteActiveState: (favoriteTime: FavoriteTime, isActive: Boolean) -> Unit
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        nestedGroupedTimes.forEach { (routeInfoPair, timesByDeparturePoint) ->
            val (routeNumber, routeName) = routeInfoPair
            val routeKey = "$routeNumber-$routeName"

            item(key = "route_card_$routeKey") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = routeName.trim(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        timesByDeparturePoint.forEach { (departurePoint, timesInDepartureGroup) ->
                            val departureGroupKey = "${routeKey}_${departurePoint}"
                            if (!expandedStates.containsKey(departureGroupKey)) {
                                expandedStates[departureGroupKey] = true
                            }
                            val isExpanded = expandedStates[departureGroupKey] ?: true

                            ExpandableDeparturePointSection(
                                departurePoint = departurePoint,
                                schedulesInGroup = timesInDepartureGroup,
                                isExpanded = isExpanded,
                                onToggleExpand = { expandedStates[departureGroupKey] = !isExpanded },
                                onToggleFavoriteActiveState = onToggleFavoriteActiveState
                            )
                            if (timesByDeparturePoint.keys.last() != departurePoint && timesByDeparturePoint.size > 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableDeparturePointSection(
    modifier: Modifier = Modifier,
    departurePoint: String,
    schedulesInGroup: List<FavoriteTime>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleFavoriteActiveState: (favoriteTime: FavoriteTime, isActive: Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleExpand
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Отправление из $departurePoint",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(bottom = if (schedulesInGroup.isNotEmpty()) 8.dp else 0.dp)) {
                schedulesInGroup.forEachIndexed { index, favoriteTime ->
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp))
                    }
                    val scheduleDisplay = com.example.lets_go_slavgorod.data.model.BusSchedule(
                        id = favoriteTime.id,
                        routeId = favoriteTime.routeId,
                        stopName = favoriteTime.stopName,
                        departureTime = favoriteTime.departureTime,
                        dayOfWeek = favoriteTime.dayOfWeek,
                        departurePoint = favoriteTime.departurePoint,
                        notes = null
                    )
                    ScheduleCard(
                        schedule = scheduleDisplay,
                        isFavorite = favoriteTime.isActive,
                        onFavoriteClick = {
                            onToggleFavoriteActiveState(favoriteTime, !favoriteTime.isActive)
                        },
                        routeNumber = null,
                        routeName = null,
                        isNextUpcoming = false,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
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