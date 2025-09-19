package com.example.slavgorodbus.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.slavgorodbus.data.model.BusRoute
import com.example.slavgorodbus.data.model.BusSchedule
import com.example.slavgorodbus.ui.components.ScheduleCard
import com.example.slavgorodbus.ui.viewmodel.BusViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.runtime.collectAsState

const val STOP_SLAVGORD_RYNOK = "Славгород (Рынок)"
const val STOP_YAROVOE_MCHS = "Яровое (МЧС-128)"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    route: BusRoute?,
    onBackClick: () -> Unit,
    viewModel: BusViewModel
) {
    val allSchedulesForRoute = remember(route) {
        if (route != null) {
            generateSampleSchedules(route.id)
        } else {
            emptyList()
        }
    }

    val schedulesSlavgorod = remember(allSchedulesForRoute) {
        allSchedulesForRoute
            .filter { it.departurePoint == STOP_SLAVGORD_RYNOK }
            .sortedBy { it.departureTime }
    }

    val schedulesYarovoe = remember(allSchedulesForRoute) {
        allSchedulesForRoute
            .filter { it.departurePoint == STOP_YAROVOE_MCHS }
            .sortedBy { it.departureTime }
    }

    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var nextUpcomingSlavgorodId by remember { mutableStateOf<String?>(null) }
    var nextUpcomingYarovoeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit, schedulesSlavgorod, schedulesYarovoe) {
        while (true) {
            currentTime = Calendar.getInstance()
            val now = currentTime
            nextUpcomingSlavgorodId = schedulesSlavgorod
                .firstOrNull { parseTimeSimple(it.departureTime).timeInMillis > now.timeInMillis }?.id
            nextUpcomingYarovoeId = schedulesYarovoe
                .firstOrNull { parseTimeSimple(it.departureTime).timeInMillis > now.timeInMillis }?.id
            delay(30000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = route?.name ?: "Расписание",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (route == null) {
            NoRouteSelectedMessage(Modifier
                .padding(paddingValues)
                .fillMaxSize())
        } else {
            ScheduleListContent(
                modifier = Modifier.padding(paddingValues),
                route = route,
                schedulesSlavgorod = schedulesSlavgorod,
                schedulesYarovoe = schedulesYarovoe,
                nextUpcomingSlavgorodId = nextUpcomingSlavgorodId,
                nextUpcomingYarovoeId = nextUpcomingYarovoeId,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ScheduleListContent(
    modifier: Modifier = Modifier,
    route: BusRoute,
    schedulesSlavgorod: List<BusSchedule>,
    schedulesYarovoe: List<BusSchedule>,
    nextUpcomingSlavgorodId: String?,
    nextUpcomingYarovoeId: String?,
    viewModel: BusViewModel
) {
    var isSlavgorodSectionExpanded by remember { mutableStateOf(true) }
    var isYarovoeSectionExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RouteDetailsSummaryCard(route = route)
        }

        item {
            ExpandableScheduleSection(
                title = "Отправление из $STOP_SLAVGORD_RYNOK",
                schedules = schedulesSlavgorod,
                nextUpcomingScheduleId = nextUpcomingSlavgorodId,
                isExpanded = isSlavgorodSectionExpanded,
                onToggleExpand = { isSlavgorodSectionExpanded = !isSlavgorodSectionExpanded },
                viewModel = viewModel,
                route = route,
                departurePointForCheck = STOP_SLAVGORD_RYNOK
            )
        }

        item {
            ExpandableScheduleSection(
                title = "Отправление из $STOP_YAROVOE_MCHS",
                schedules = schedulesYarovoe,
                nextUpcomingScheduleId = nextUpcomingYarovoeId,
                isExpanded = isYarovoeSectionExpanded,
                onToggleExpand = { isYarovoeSectionExpanded = !isYarovoeSectionExpanded },
                viewModel = viewModel,
                route = route,
                departurePointForCheck = STOP_YAROVOE_MCHS
            )
        }

        if (schedulesSlavgorod.isEmpty() && schedulesYarovoe.isEmpty() &&
            (route.id == "102") ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Для маршрута '${route.name}' расписание отсутствует.",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ExpandableScheduleSection(
    modifier: Modifier = Modifier,
    title: String,
    schedules: List<BusSchedule>,
    nextUpcomingScheduleId: String?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    viewModel: BusViewModel,
    route: BusRoute,
    departurePointForCheck: String
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleExpand
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
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
                Column {
                    if (schedules.isNotEmpty()) {
                        schedules.forEach { schedule ->
                            if (schedules.first() != schedule) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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

@Composable
private fun NoRouteSelectedMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Маршрут не выбран",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Пожалуйста, выберите маршрут для просмотра расписания и деталей.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun NoScheduleMessage(departurePoint: String, modifier: Modifier = Modifier) {
    Text(
        "Для '$departurePoint' расписание на сегодня отсутствует или закончилось.",
        modifier = modifier
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun RouteDetailsSummaryCard(route: BusRoute) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            route.travelTime?.let { DetailRow("Время в пути:", it) }
            route.pricePrimary?.let { DetailRow("Стоимость:", it) }
            route.paymentMethods?.let { DetailRow("Способы оплаты:", it, allowMultiLineValue = false) }
            if (route.travelTime != null || route.pricePrimary != null || route.paymentMethods != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "Примечание: Указано время отправления от начальных/конечных остановок маршрута.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, allowMultiLineValue: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = if (allowMultiLineValue) Alignment.Top else Alignment.CenterVertically
    ) {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (allowMultiLineValue) Int.MAX_VALUE else 1,
            overflow = if (allowMultiLineValue) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun parseTimeSimple(timeString: String): Calendar {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = sdf.parse(timeString)
        val calendar = Calendar.getInstance()
        if (date != null) {
            val parsedCalendar = Calendar.getInstance()
            parsedCalendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, parsedCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, parsedCalendar.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }
        calendar
    } catch (_: Exception) {
        Calendar.getInstance()
    }
}

private fun shouldShowNoScheduleMessage(route: BusRoute): Boolean {
    return route.id == "102"
}

private fun generateSampleSchedules(routeId: String): List<BusSchedule> {
    // Используем текущий день недели для всех рейсов
    val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    
    return when (routeId) {
        "102" -> listOf(
            // Расписание отправление Славгород (Рынок)
            BusSchedule("102_slav_1", "102", STOP_SLAVGORD_RYNOK, "06:25", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_2", "102", STOP_SLAVGORD_RYNOK, "06:45", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_3", "102", STOP_SLAVGORD_RYNOK, "07:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_4", "102", STOP_SLAVGORD_RYNOK, "07:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_6", "102", STOP_SLAVGORD_RYNOK, "07:40", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_7", "102", STOP_SLAVGORD_RYNOK, "08:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_7a", "102", STOP_SLAVGORD_RYNOK, "08:25", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_8", "102", STOP_SLAVGORD_RYNOK, "08:40", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_9", "102", STOP_SLAVGORD_RYNOK, "09:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_10", "102", STOP_SLAVGORD_RYNOK, "09:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_11", "102", STOP_SLAVGORD_RYNOK, "09:35", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_12", "102", STOP_SLAVGORD_RYNOK, "10:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_13", "102", STOP_SLAVGORD_RYNOK, "10:25", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_14", "102", STOP_SLAVGORD_RYNOK, "10:50", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_15", "102", STOP_SLAVGORD_RYNOK, "11:10", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_16", "102", STOP_SLAVGORD_RYNOK, "11:35", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_17", "102", STOP_SLAVGORD_RYNOK, "12:05", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_18", "102", STOP_SLAVGORD_RYNOK, "12:30", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_19", "102", STOP_SLAVGORD_RYNOK, "12:55", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_20", "102", STOP_SLAVGORD_RYNOK, "13:15", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_21", "102", STOP_SLAVGORD_RYNOK, "13:35", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_22", "102", STOP_SLAVGORD_RYNOK, "14:05", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_23", "102", STOP_SLAVGORD_RYNOK, "14:30", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_24", "102", STOP_SLAVGORD_RYNOK, "14:55", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_25", "102", STOP_SLAVGORD_RYNOK, "15:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_26", "102", STOP_SLAVGORD_RYNOK, "15:45", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_27", "102", STOP_SLAVGORD_RYNOK, "16:10", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_28", "102", STOP_SLAVGORD_RYNOK, "16:35", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_29", "102", STOP_SLAVGORD_RYNOK, "17:05", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_30", "102", STOP_SLAVGORD_RYNOK, "17:25", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_31", "102", STOP_SLAVGORD_RYNOK, "17:50", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_32", "102", STOP_SLAVGORD_RYNOK, "18:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_33", "102", STOP_SLAVGORD_RYNOK, "18:50", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_34", "102", STOP_SLAVGORD_RYNOK, "19:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_35", "102", STOP_SLAVGORD_RYNOK, "20:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_36", "102", STOP_SLAVGORD_RYNOK, "20:30", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),


            // Расписание отправлении Яровое (МСЧ-128)
            BusSchedule("102_yar_1", "102", STOP_YAROVOE_MCHS, "07:00", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_2", "102", STOP_YAROVOE_MCHS, "07:20", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_3", "102", STOP_YAROVOE_MCHS, "07:35", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_4", "102", STOP_YAROVOE_MCHS, "07:55", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_5", "102", STOP_YAROVOE_MCHS, "08:20", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_6", "102", STOP_YAROVOE_MCHS, "08:40", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_7", "102", STOP_YAROVOE_MCHS, "09:00", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_8", "102", STOP_YAROVOE_MCHS, "09:20", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_9", "102", STOP_YAROVOE_MCHS, "09:40", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_10", "102", STOP_YAROVOE_MCHS, "10:10", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_11", "102", STOP_YAROVOE_MCHS, "10:25", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_12", "102", STOP_YAROVOE_MCHS, "10:30", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_13", "102", STOP_YAROVOE_MCHS, "11:10", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_14", "102", STOP_YAROVOE_MCHS, "11:30", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_15", "102", STOP_YAROVOE_MCHS, "11:55", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_16", "102", STOP_YAROVOE_MCHS, "12:20", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_17", "102", STOP_YAROVOE_MCHS, "12:40", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_18", "102", STOP_YAROVOE_MCHS, "13:05", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_19", "102", STOP_YAROVOE_MCHS, "13:30", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_20", "102", STOP_YAROVOE_MCHS, "13:55", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_21", "102", STOP_YAROVOE_MCHS, "14:15", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_22", "102", STOP_YAROVOE_MCHS, "14:45", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_23", "102", STOP_YAROVOE_MCHS, "15:10", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_24", "102", STOP_YAROVOE_MCHS, "15:30", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_25", "102", STOP_YAROVOE_MCHS, "15:55", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_26", "102", STOP_YAROVOE_MCHS, "16:20", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_27", "102", STOP_YAROVOE_MCHS, "16:45", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_28", "102", STOP_YAROVOE_MCHS, "17:10", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_29", "102", STOP_YAROVOE_MCHS, "17:40", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_30", "102", STOP_YAROVOE_MCHS, "18:10", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_31", "102", STOP_YAROVOE_MCHS, "18:35", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_32", "102", STOP_YAROVOE_MCHS, "19:00", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_33", "102", STOP_YAROVOE_MCHS, "19:25", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_34", "102", STOP_YAROVOE_MCHS, "20:00", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_35", "102", STOP_YAROVOE_MCHS, "20:30", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS),
            BusSchedule("102_yar_36", "102", STOP_YAROVOE_MCHS, "21:00", currentDayOfWeek, notes = null, departurePoint = STOP_YAROVOE_MCHS)
        )
        else -> emptyList()
    }
}