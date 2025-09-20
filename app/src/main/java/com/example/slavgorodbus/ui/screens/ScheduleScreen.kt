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
const val STOP_VOKZAL = "Вокзал"
const val STOP_SOVHOZ = "Совхоз"

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

    val schedulesVokzal = remember(allSchedulesForRoute) {
        allSchedulesForRoute
            .filter { it.departurePoint == STOP_VOKZAL }
            .sortedBy { it.departureTime }
    }

    val schedulesSovhoz = remember(allSchedulesForRoute) {
        allSchedulesForRoute
            .filter { it.departurePoint == STOP_SOVHOZ }
            .sortedBy { it.departureTime }
    }

    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var nextUpcomingSlavgorodId by remember { mutableStateOf<String?>(null) }
    var nextUpcomingYarovoeId by remember { mutableStateOf<String?>(null) }
    var nextUpcomingVokzalId by remember { mutableStateOf<String?>(null) }
    var nextUpcomingSovhozId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit, schedulesSlavgorod, schedulesYarovoe, schedulesVokzal, schedulesSovhoz) {
        while (true) {
            currentTime = Calendar.getInstance()
            val now = currentTime
            nextUpcomingSlavgorodId = schedulesSlavgorod
                .firstOrNull { parseTimeSimple(it.departureTime).timeInMillis > now.timeInMillis }?.id
            nextUpcomingYarovoeId = schedulesYarovoe
                .firstOrNull { parseTimeSimple(it.departureTime).timeInMillis > now.timeInMillis }?.id
            nextUpcomingVokzalId = schedulesVokzal
                .firstOrNull { parseTimeSimple(it.departureTime).timeInMillis > now.timeInMillis }?.id
            nextUpcomingSovhozId = schedulesSovhoz
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
                schedulesVokzal = schedulesVokzal,
                schedulesSovhoz = schedulesSovhoz,
                nextUpcomingSlavgorodId = nextUpcomingSlavgorodId,
                nextUpcomingYarovoeId = nextUpcomingYarovoeId,
                nextUpcomingVokzalId = nextUpcomingVokzalId,
                nextUpcomingSovhozId = nextUpcomingSovhozId,
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
    schedulesVokzal: List<BusSchedule>,
    schedulesSovhoz: List<BusSchedule>,
    nextUpcomingSlavgorodId: String?,
    nextUpcomingYarovoeId: String?,
    nextUpcomingVokzalId: String?,
    nextUpcomingSovhozId: String?,
    viewModel: BusViewModel
) {
    var isSlavgorodSectionExpanded by remember { mutableStateOf(true) }
    var isYarovoeSectionExpanded by remember { mutableStateOf(true) }
    var isVokzalSectionExpanded by remember { mutableStateOf(true) }
    var isSovhozSectionExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RouteDetailsSummaryCard(route = route)
        }

        // Секции для маршрута №102 (Славгород — Яровое)
        if (route.id == "102") {
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
        }

        // Секции для маршрута №1 (Вокзал — Совхоз)
        if (route.id == "1") {
            item {
                ExpandableScheduleSection(
                    title = "Отправление из $STOP_VOKZAL",
                    schedules = schedulesVokzal,
                    nextUpcomingScheduleId = nextUpcomingVokzalId,
                    isExpanded = isVokzalSectionExpanded,
                    onToggleExpand = { isVokzalSectionExpanded = !isVokzalSectionExpanded },
                    viewModel = viewModel,
                    route = route,
                    departurePointForCheck = STOP_VOKZAL
                )
            }

            item {
                ExpandableScheduleSection(
                    title = "Отправление из $STOP_SOVHOZ",
                    schedules = schedulesSovhoz,
                    nextUpcomingScheduleId = nextUpcomingSovhozId,
                    isExpanded = isSovhozSectionExpanded,
                    onToggleExpand = { isSovhozSectionExpanded = !isSovhozSectionExpanded },
                    viewModel = viewModel,
                    route = route,
                    departurePointForCheck = STOP_SOVHOZ
                )
            }
        }

        if (schedulesSlavgorod.isEmpty() && schedulesYarovoe.isEmpty() && 
            schedulesVokzal.isEmpty() && schedulesSovhoz.isEmpty() &&
            (route.id == "102" || route.id == "1") ) {
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
    return route.id == "102" || route.id == "1"
}

private fun generateSampleSchedules(routeId: String): List<BusSchedule> {
    val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    
    return when (routeId) {
        "1" -> listOf(
            // Расписание маршрута №1 - 1 выход (Вокзал → Совхоз)
            BusSchedule("1_vokzal_1", "1", STOP_VOKZAL, "07:00", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_2", "1", STOP_VOKZAL, "07:48", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_3", "1", STOP_VOKZAL, "08:36", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_4", "1", STOP_VOKZAL, "09:24", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_5", "1", STOP_VOKZAL, "10:12", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_6", "1", STOP_VOKZAL, "11:00", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_7", "1", STOP_VOKZAL, "11:48", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_8", "1", STOP_VOKZAL, "12:36", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_9", "1", STOP_VOKZAL, "13:24", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_10", "1", STOP_VOKZAL, "14:12", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_11", "1", STOP_VOKZAL, "15:00", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_12", "1", STOP_VOKZAL, "15:48", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_13", "1", STOP_VOKZAL, "16:36", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_14", "1", STOP_VOKZAL, "17:24", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_15", "1", STOP_VOKZAL, "18:12", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_16", "1", STOP_VOKZAL, "19:00", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_VOKZAL),

            // Расписание маршрута №1 - 2 выход (Вокзал → Совхоз)
            BusSchedule("1_vokzal_17", "1", STOP_VOKZAL, "07:15", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_18", "1", STOP_VOKZAL, "08:03", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_19", "1", STOP_VOKZAL, "08:51", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_20", "1", STOP_VOKZAL, "09:39", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_21", "1", STOP_VOKZAL, "10:27", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_22", "1", STOP_VOKZAL, "11:15", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_23", "1", STOP_VOKZAL, "12:03", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_24", "1", STOP_VOKZAL, "12:51", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_25", "1", STOP_VOKZAL, "13:39", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_26", "1", STOP_VOKZAL, "14:27", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_27", "1", STOP_VOKZAL, "15:15", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_28", "1", STOP_VOKZAL, "16:03", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_29", "1", STOP_VOKZAL, "16:51", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_30", "1", STOP_VOKZAL, "17:39", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_VOKZAL),

            // Расписание маршрута №1 - 3 выход (Вокзал → Совхоз)
            BusSchedule("1_vokzal_31", "1", STOP_VOKZAL, "07:30", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_32", "1", STOP_VOKZAL, "08:18", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_33", "1", STOP_VOKZAL, "09:06", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_34", "1", STOP_VOKZAL, "09:54", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_35", "1", STOP_VOKZAL, "10:42", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_36", "1", STOP_VOKZAL, "11:30", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_37", "1", STOP_VOKZAL, "12:18", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_38", "1", STOP_VOKZAL, "13:06", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_39", "1", STOP_VOKZAL, "13:54", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_40", "1", STOP_VOKZAL, "14:42", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_41", "1", STOP_VOKZAL, "15:30", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),
            BusSchedule("1_vokzal_42", "1", STOP_VOKZAL, "16:18", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_VOKZAL),

            // Расписание маршрута №1 - 1 выход (Совхоз → Вокзал)
            BusSchedule("1_sovhoz_1", "1", STOP_SOVHOZ, "07:24", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_2", "1", STOP_SOVHOZ, "08:12", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_3", "1", STOP_SOVHOZ, "09:00", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_4", "1", STOP_SOVHOZ, "09:48", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_5", "1", STOP_SOVHOZ, "10:36", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_6", "1", STOP_SOVHOZ, "11:24", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_7", "1", STOP_SOVHOZ, "12:12", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_8", "1", STOP_SOVHOZ, "13:48", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_9", "1", STOP_SOVHOZ, "14:36", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_10", "1", STOP_SOVHOZ, "15:24", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_11", "1", STOP_SOVHOZ, "16:12", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_12", "1", STOP_SOVHOZ, "17:00", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_13", "1", STOP_SOVHOZ, "17:48", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_14", "1", STOP_SOVHOZ, "18:36", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_15", "1", STOP_SOVHOZ, "19:24", currentDayOfWeek, notes = "1 выход", departurePoint = STOP_SOVHOZ),

            // Расписание маршрута №1 - 2 выход (Совхоз → Вокзал)
            BusSchedule("1_sovhoz_16", "1", STOP_SOVHOZ, "07:39", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_17", "1", STOP_SOVHOZ, "08:27", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_18", "1", STOP_SOVHOZ, "09:15", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_19", "1", STOP_SOVHOZ, "10:03", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_20", "1", STOP_SOVHOZ, "10:51", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_21", "1", STOP_SOVHOZ, "11:39", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_22", "1", STOP_SOVHOZ, "12:27", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_23", "1", STOP_SOVHOZ, "13:15", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_24", "1", STOP_SOVHOZ, "14:51", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_25", "1", STOP_SOVHOZ, "15:39", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_26", "1", STOP_SOVHOZ, "16:27", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_27", "1", STOP_SOVHOZ, "17:15", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_28", "1", STOP_SOVHOZ, "18:03", currentDayOfWeek, notes = "2 выход", departurePoint = STOP_SOVHOZ),

            // Расписание маршрута №1 - 3 выход (Совхоз → Вокзал)
            BusSchedule("1_sovhoz_29", "1", STOP_SOVHOZ, "07:54", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_30", "1", STOP_SOVHOZ, "08:42", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_31", "1", STOP_SOVHOZ, "09:30", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_32", "1", STOP_SOVHOZ, "10:18", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_33", "1", STOP_SOVHOZ, "11:06", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_34", "1", STOP_SOVHOZ, "12:42", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_35", "1", STOP_SOVHOZ, "13:30", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_36", "1", STOP_SOVHOZ, "14:18", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_37", "1", STOP_SOVHOZ, "15:06", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_38", "1", STOP_SOVHOZ, "15:54", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ),
            BusSchedule("1_sovhoz_39", "1", STOP_SOVHOZ, "16:42", currentDayOfWeek, notes = "3 выход", departurePoint = STOP_SOVHOZ)
        )
        "102" -> listOf(
            // Расписание отправлении Славгород (Рынок)
            BusSchedule("102_slav_1", "102", STOP_SLAVGORD_RYNOK, "06:25", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_2", "102", STOP_SLAVGORD_RYNOK, "06:45", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_3", "102", STOP_SLAVGORD_RYNOK, "07:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_4", "102", STOP_SLAVGORD_RYNOK, "07:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_6", "102", STOP_SLAVGORD_RYNOK, "07:40", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_7", "102", STOP_SLAVGORD_RYNOK, "08:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_8", "102", STOP_SLAVGORD_RYNOK, "08:25", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_9", "102", STOP_SLAVGORD_RYNOK, "08:40", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_10", "102", STOP_SLAVGORD_RYNOK, "09:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_11", "102", STOP_SLAVGORD_RYNOK, "09:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_12", "102", STOP_SLAVGORD_RYNOK, "09:35", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_13", "102", STOP_SLAVGORD_RYNOK, "10:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_14", "102", STOP_SLAVGORD_RYNOK, "10:25", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_15", "102", STOP_SLAVGORD_RYNOK, "10:50", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_16", "102", STOP_SLAVGORD_RYNOK, "11:10", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_17", "102", STOP_SLAVGORD_RYNOK, "11:35", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_18", "102", STOP_SLAVGORD_RYNOK, "12:05", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_19", "102", STOP_SLAVGORD_RYNOK, "12:30", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_20", "102", STOP_SLAVGORD_RYNOK, "12:55", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_21", "102", STOP_SLAVGORD_RYNOK, "13:15", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_22", "102", STOP_SLAVGORD_RYNOK, "13:35", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_23", "102", STOP_SLAVGORD_RYNOK, "14:05", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_24", "102", STOP_SLAVGORD_RYNOK, "14:30", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_25", "102", STOP_SLAVGORD_RYNOK, "14:55", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_26", "102", STOP_SLAVGORD_RYNOK, "15:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_27", "102", STOP_SLAVGORD_RYNOK, "15:45", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_28", "102", STOP_SLAVGORD_RYNOK, "16:10", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_29", "102", STOP_SLAVGORD_RYNOK, "16:35", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_30", "102", STOP_SLAVGORD_RYNOK, "17:05", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_31", "102", STOP_SLAVGORD_RYNOK, "17:25", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_32", "102", STOP_SLAVGORD_RYNOK, "17:50", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_33", "102", STOP_SLAVGORD_RYNOK, "18:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_34", "102", STOP_SLAVGORD_RYNOK, "18:50", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_35", "102", STOP_SLAVGORD_RYNOK, "19:20", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_36", "102", STOP_SLAVGORD_RYNOK, "20:00", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),
            BusSchedule("102_slav_37", "102", STOP_SLAVGORD_RYNOK, "20:30", currentDayOfWeek, notes = null, departurePoint = STOP_SLAVGORD_RYNOK),


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
