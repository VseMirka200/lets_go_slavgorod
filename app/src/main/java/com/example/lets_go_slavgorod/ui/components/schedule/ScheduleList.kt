package com.example.lets_go_slavgorod.ui.components.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.model.BusSchedule
import com.example.lets_go_slavgorod.ui.components.ScheduleCard
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel

/**
 * Список расписаний с возможностью сворачивания секций
 * 
 * Функциональность:
 * - Отображение расписаний по секциям (отправления из разных точек)
 * - Возможность сворачивания/разворачивания секций
 * - Интеграция с избранными временами
 * - Подсветка ближайшего рейса
 */
@Composable
fun ScheduleList(
    route: BusRoute,
    schedulesSlavgorod: List<BusSchedule>,
    schedulesYarovoe: List<BusSchedule>,
    schedulesVokzal: List<BusSchedule>,
    schedulesSovhoz: List<BusSchedule>,
    nextUpcomingSlavgorodId: String?,
    nextUpcomingYarovoeId: String?,
    nextUpcomingVokzalId: String?,
    nextUpcomingSovhozId: String?,
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    var isSlavgorodSectionExpanded by remember { mutableStateOf(true) }
    var isYarovoeSectionExpanded by remember { mutableStateOf(true) }
    var isVokzalSectionExpanded by remember { mutableStateOf(true) }
    var isSovhozSectionExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        // Оптимизация производительности
        userScrollEnabled = true
    ) {
        // Секции для маршрута №102 (Славгород — Яровое)
        if (route.id == "102") {
            // Славгород (Рынок)
            if (schedulesSlavgorod.isNotEmpty()) {
                item {
                    ExpandableScheduleSection(
                        title = "Отправление из Рынок (Славгород)",
                        schedules = schedulesSlavgorod,
                        nextUpcomingScheduleId = nextUpcomingSlavgorodId,
                        isExpanded = isSlavgorodSectionExpanded,
                        onToggleExpand = { isSlavgorodSectionExpanded = !isSlavgorodSectionExpanded },
                        viewModel = viewModel,
                        route = route,
                        departurePointForCheck = "Рынок (Славгород)"
                    )
                }
            }

            // Яровое (МЧС-128)
            if (schedulesYarovoe.isNotEmpty()) {
                item {
                    ExpandableScheduleSection(
                        title = "Отправление из МСЧ-128 (Яровое)",
                        schedules = schedulesYarovoe,
                        nextUpcomingScheduleId = nextUpcomingYarovoeId,
                        isExpanded = isYarovoeSectionExpanded,
                        onToggleExpand = { isYarovoeSectionExpanded = !isYarovoeSectionExpanded },
                        viewModel = viewModel,
                        route = route,
                        departurePointForCheck = "МСЧ-128 (Яровое)"
                    )
                }
            }
        }

        // Секции для маршрута №1 (Вокзал — Совхоз)
        if (route.id == "1") {
            // Вокзал
            if (schedulesVokzal.isNotEmpty()) {
                item {
                    ExpandableScheduleSection(
                        title = "Отправление из вокзала",
                        schedules = schedulesVokzal,
                        nextUpcomingScheduleId = nextUpcomingVokzalId,
                        isExpanded = isVokzalSectionExpanded,
                        onToggleExpand = { isVokzalSectionExpanded = !isVokzalSectionExpanded },
                        viewModel = viewModel,
                        route = route,
                        departurePointForCheck = "вокзал"
                    )
                }
            }

            // Совхоз
            if (schedulesSovhoz.isNotEmpty()) {
                item {
                    ExpandableScheduleSection(
                        title = "Отправление из совхоза",
                        schedules = schedulesSovhoz,
                        nextUpcomingScheduleId = nextUpcomingSovhozId,
                        isExpanded = isSovhozSectionExpanded,
                        onToggleExpand = { isSovhozSectionExpanded = !isSovhozSectionExpanded },
                        viewModel = viewModel,
                        route = route,
                        departurePointForCheck = "совхоз"
                    )
                }
            }
        }

        // Сообщение об отсутствии расписания
        if (shouldShowNoScheduleMessage(route)) {
            item {
                NoScheduleMessage(
                    departurePoint = "выбранного маршрута",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * Секция расписания с возможностью сворачивания
 */
@Composable
private fun ExpandableScheduleSection(
    title: String,
    schedules: List<BusSchedule>,
    nextUpcomingScheduleId: String?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    viewModel: BusViewModel,
    route: BusRoute,
    departurePointForCheck: String,
    modifier: Modifier = Modifier
) {
    val favoriteTimesList by viewModel.favoriteTimes.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(bottom = if (isExpanded && schedules.isNotEmpty()) 8.dp else 0.dp)
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
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Показываем ближайший рейс в свернутом виде
                if (!isExpanded && nextUpcomingScheduleId != null) {
                    val nextSchedule = schedules.find { it.id == nextUpcomingScheduleId }
                    if (nextSchedule != null) {
                        ScheduleCard(
                            schedule = nextSchedule,
                            isFavorite = favoriteTimesList.any { it.id == nextSchedule.id && it.isActive },
                            onFavoriteClick = {
                                val isCurrentlyFavorite = favoriteTimesList.any { it.id == nextSchedule.id && it.isActive }
                                if (isCurrentlyFavorite) {
                                    viewModel.removeFavoriteTime(nextSchedule.id)
                                } else {
                                    viewModel.addFavoriteTime(nextSchedule)
                                }
                            },
                            isNextUpcoming = true,
                            allSchedules = schedules,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    if (schedules.isNotEmpty()) {
                        schedules.forEachIndexed { index, schedule ->
                            if (index > 0) {
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                            }

                            val isCurrentlyFavorite = remember(favoriteTimesList, schedule.id) {
                                favoriteTimesList.any { it.id == schedule.id && it.isActive }
                            }

                            ScheduleCard(
                                schedule = schedule,
                                isFavorite = isCurrentlyFavorite,
                                onFavoriteClick = {
                                    if (isCurrentlyFavorite) {
                                        viewModel.removeFavoriteTime(schedule.id)
                                    } else {
                                        viewModel.addFavoriteTime(schedule)
                                    }
                                },
                                isNextUpcoming = schedule.id == nextUpcomingScheduleId,
                                allSchedules = schedules,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                            )
                        }
                    } else if (shouldShowNoScheduleMessage(route)) {
                        NoScheduleMessage(
                            departurePoint = departurePointForCheck,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Сообщение об отсутствии расписания
 */
@Composable
private fun NoScheduleMessage(
    departurePoint: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Для $departurePoint расписание отсутствует.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        )
    }
}

/**
 * Проверяет, нужно ли показать сообщение об отсутствии расписания
 */
private fun shouldShowNoScheduleMessage(route: BusRoute): Boolean {
    return when (route.id) {
        "102" -> false // Маршрут 102 всегда имеет расписание
        "1" -> false  // Маршрут 1 всегда имеет расписание
        else -> true
    }
}
