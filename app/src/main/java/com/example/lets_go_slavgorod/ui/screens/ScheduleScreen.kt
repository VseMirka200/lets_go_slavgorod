@file:Suppress("DEPRECATION")

package com.example.lets_go_slavgorod.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.model.BusSchedule
import com.example.lets_go_slavgorod.ui.components.SettingsSwipeableContainer
import com.example.lets_go_slavgorod.ui.components.schedule.ScheduleHeader
import com.example.lets_go_slavgorod.ui.components.schedule.RouteDetailsSummaryCard
import com.example.lets_go_slavgorod.ui.components.schedule.ScheduleList
import com.example.lets_go_slavgorod.ui.navigation.Screen
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel
import com.example.lets_go_slavgorod.utils.ScheduleUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val STOP_SLAVGORD_RYNOK = "Рынок (Славгород)"
const val STOP_YAROVOE_MCHS = "МСЧ-128 (Яровое)"
const val STOP_VOKZAL = "вокзал"
const val STOP_SOVHOZ = "совхоз"

/**
 * Экран расписания маршрута с детальной информацией
 * 
 * Функциональность:
 * - Отображение расписания для выбранного маршрута
 * - Разделение по точкам отправления
 * - Возможность сворачивания секций
 * - Интеграция с избранными временами
 * - Подсветка ближайшего рейса
 * - Навигация между экранами
 */
@Composable
fun ScheduleScreen(
    route: BusRoute?,
    onBackClick: () -> Unit,
    viewModel: BusViewModel,
    navController: androidx.navigation.NavController? = null
) {
    val allSchedulesForRoute = remember(route) {
        if (route != null) {
            val schedules = ScheduleUtils.generateSchedules(route.id)
            Log.d("ScheduleScreen", "Generated ${schedules.size} schedules for route ${route.id}")
            schedules
        } else {
            emptyList()
        }
    }

    val schedulesSlavgorod = remember(allSchedulesForRoute) {
        val filtered = allSchedulesForRoute
            .filter { it.departurePoint == STOP_SLAVGORD_RYNOK }
            .sortedBy { it.departureTime }
        Log.d("ScheduleScreen", "Slavgorod schedules: ${filtered.size}")
        filtered
    }

    val schedulesYarovoe = remember(allSchedulesForRoute) {
        val filtered = allSchedulesForRoute
            .filter { it.departurePoint == STOP_YAROVOE_MCHS }
            .sortedBy { it.departureTime }
        Log.d("ScheduleScreen", "Yarovoe schedules: ${filtered.size}")
        filtered
    }

    val schedulesVokzal = remember(allSchedulesForRoute) {
        val filtered = allSchedulesForRoute
            .filter { it.departurePoint == STOP_VOKZAL }
            .sortedBy { it.departureTime }
        Log.d("ScheduleScreen", "Vokzal schedules: ${filtered.size}")
        filtered
    }

    val schedulesSovhoz = remember(allSchedulesForRoute) {
        val filtered = allSchedulesForRoute
            .filter { it.departurePoint == STOP_SOVHOZ }
            .sortedBy { it.departureTime }
        Log.d("ScheduleScreen", "Sovhoz schedules: ${filtered.size}")
        filtered
    }

    // Определяем ближайшие рейсы для каждой точки отправления
    val nextUpcomingSlavgorodId = remember(schedulesSlavgorod) {
        getNextUpcomingScheduleId(schedulesSlavgorod)
    }

    val nextUpcomingYarovoeId = remember(schedulesYarovoe) {
        getNextUpcomingScheduleId(schedulesYarovoe)
    }

    val nextUpcomingVokzalId = remember(schedulesVokzal) {
        getNextUpcomingScheduleId(schedulesVokzal)
    }

    val nextUpcomingSovhozId = remember(schedulesSovhoz) {
        getNextUpcomingScheduleId(schedulesSovhoz)
    }

    SettingsSwipeableContainer(
        onSwipeToNext = {
            Log.d("ScheduleScreen", "Swipe left detected - navigating to Favorites")
            navController?.navigate(Screen.FavoriteTimes.route) {
                popUpTo(Screen.Home.route) { inclusive = false }
                launchSingleTop = true
                restoreState = true
            }
        },
        onSwipeToPrevious = {
            Log.d("ScheduleScreen", "Swipe right detected - navigating to Settings")
            navController?.navigate(Screen.Settings.route) {
                popUpTo(Screen.Home.route) { inclusive = false }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                ScheduleHeader(
                    route = route,
                    onBackClick = onBackClick
                )
            },
            contentWindowInsets = WindowInsets(0)
        ) { paddingValues ->
            if (route == null) {
                NoRouteSelectedMessage(Modifier
                    .padding(paddingValues)
                    .fillMaxSize())
            } else {
                Column(modifier = Modifier.padding(paddingValues)) {
                    RouteDetailsSummaryCard(
                        route = route,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 15.dp)
                    )
                    ScheduleList(
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
    }
}

/**
 * Определяет ID ближайшего рейса из списка расписаний
 */
private fun getNextUpcomingScheduleId(schedules: List<BusSchedule>): String? {
    if (schedules.isEmpty()) return null
    
    val currentTime = Calendar.getInstance()
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Находим все рейсы, которые еще не прошли сегодня
    val upcomingToday = schedules.filter { schedule ->
        try {
            val departureTime = timeFormat.parse(schedule.departureTime)
            if (departureTime != null) {
                val scheduleCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, departureTime.hours)
                    set(Calendar.MINUTE, departureTime.minutes)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                scheduleCalendar.after(currentTime)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ScheduleScreen", "Error parsing time: ${schedule.departureTime}", e)
            false
        }
    }
    
    // Если есть рейсы сегодня, возвращаем ближайший
    if (upcomingToday.isNotEmpty()) {
        Log.d("ScheduleScreen", "Found ${upcomingToday.size} upcoming departures today. Next: ${upcomingToday.first().departureTime}")
        return upcomingToday.first().id
    }
    
    // Если рейсов сегодня больше нет, возвращаем первый рейс завтра
    val firstTomorrow = schedules.firstOrNull()
    Log.d("ScheduleScreen", "No departures today. First tomorrow: ${firstTomorrow?.departureTime}")
    return firstTomorrow?.id
}

/**
 * Сообщение об отсутствии выбранного маршрута
 */
@Composable
private fun NoRouteSelectedMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Пожалуйста, выберите маршрут для просмотра расписания и деталей.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
        }
    }
}