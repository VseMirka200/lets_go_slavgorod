@file:Suppress("DEPRECATION")

package com.example.lets_go_slavgorod.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.model.BusSchedule
import com.example.lets_go_slavgorod.ui.components.schedule.RouteDetailsSummaryCard
import com.example.lets_go_slavgorod.ui.components.schedule.ScheduleHeader
import com.example.lets_go_slavgorod.ui.components.schedule.ScheduleList
import com.example.lets_go_slavgorod.ui.viewmodel.BusViewModel
import com.example.lets_go_slavgorod.utils.ScheduleUtils
import com.example.lets_go_slavgorod.utils.ConditionalLogging
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar

const val STOP_SLAVGORD_RYNOK = "Рынок (Славгород)"
const val STOP_YAROVOE_MCHS = "МСЧ-128 (Яровое)"
const val STOP_YAROVOE_ZORI = "Ст. Зори (Яровое)"
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
    viewModel: BusViewModel
) {
    // Состояние загрузки и данных
    var isLoading by remember(route) { mutableStateOf(true) }
    var schedulesSlavgorod by remember { mutableStateOf<List<BusSchedule>>(emptyList()) }
    var schedulesYarovoe by remember { mutableStateOf<List<BusSchedule>>(emptyList()) }
    var schedulesVokzal by remember { mutableStateOf<List<BusSchedule>>(emptyList()) }
    var schedulesSovhoz by remember { mutableStateOf<List<BusSchedule>>(emptyList()) }
    var nextUpcomingSlavgorodId by remember { mutableStateOf<String?>(null) }
    var nextUpcomingYarovoeId by remember { mutableStateOf<String?>(null) }
    var nextUpcomingVokzalId by remember { mutableStateOf<String?>(null) }
    var nextUpcomingSovhozId by remember { mutableStateOf<String?>(null) }
    
    // Динамическая загрузка данных
    LaunchedEffect(route) {
        if (route != null) {
            isLoading = true
            ConditionalLogging.debug("Schedule") { "Starting schedule generation for route ${route.id}" }
            
            val startTime = System.currentTimeMillis()
            
            // Генерируем расписание в фоне
            val allSchedules = ScheduleUtils.generateSchedules(route.id)
            ConditionalLogging.debug("Schedule") { "Generated ${allSchedules.size} schedules for route ${route.id}" }
            if (route.id == "102B") {
                ConditionalLogging.debug("Schedule") { "102B schedules: ${allSchedules.map { "${it.departurePoint} - ${it.departureTime}" }}" }
            }
            
            // Фильтруем по точкам отправления
            schedulesSlavgorod = allSchedules
                .filter { it.departurePoint == STOP_SLAVGORD_RYNOK }
                .sortedBy { it.departureTime }
            ConditionalLogging.debug("Schedule") { "Slavgorod schedules: ${schedulesSlavgorod.size}" }
            
            schedulesYarovoe = allSchedules
                .filter { 
                    if (route.id == "102B") {
                        it.departurePoint == STOP_YAROVOE_ZORI
                    } else {
                        it.departurePoint == STOP_YAROVOE_MCHS
                    }
                }
                .sortedBy { it.departureTime }
            ConditionalLogging.debug("Schedule") { "Yarovoe schedules: ${schedulesYarovoe.size}" }
            if (route.id == "102B") {
                ConditionalLogging.debug("Schedule") { "102B Yarovoe schedules: ${schedulesYarovoe.map { "${it.departureTime}" }}" }
            }
            
            schedulesVokzal = allSchedules
                .filter { it.departurePoint == STOP_VOKZAL }
                .sortedBy { it.departureTime }
            ConditionalLogging.debug("Schedule") { "Vokzal schedules: ${schedulesVokzal.size}" }
            
            schedulesSovhoz = allSchedules
                .filter { it.departurePoint == STOP_SOVHOZ }
                .sortedBy { it.departureTime }
            ConditionalLogging.debug("Schedule") { "Sovhoz schedules: ${schedulesSovhoz.size}" }
            
            // Определяем ближайшие рейсы
            nextUpcomingSlavgorodId = getNextUpcomingScheduleId(schedulesSlavgorod)
            nextUpcomingYarovoeId = getNextUpcomingScheduleId(schedulesYarovoe)
            nextUpcomingVokzalId = getNextUpcomingScheduleId(schedulesVokzal)
            nextUpcomingSovhozId = getNextUpcomingScheduleId(schedulesSovhoz)
            
            val elapsedTime = System.currentTimeMillis() - startTime
            ConditionalLogging.debug("Schedule") { "Schedule data fully loaded in ${elapsedTime}ms" }
            
            // Гарантируем показ анимации минимум 1 секунду
            if (elapsedTime < 1000) {
                delay(1000 - elapsedTime)
            }
            
            isLoading = false
        } else {
            isLoading = false
        }
    }

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
            } else if (isLoading) {
                // Анимация загрузки расписания
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
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
                            text = "Загрузка расписания...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.padding(paddingValues)) {
                    RouteDetailsSummaryCard(
                        route = route,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)
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

/**
 * Определяет ID ближайшего рейса из списка расписаний
 */
private fun getNextUpcomingScheduleId(schedules: List<BusSchedule>): String? {
    if (schedules.isEmpty()) return null
    
    val currentTime = Calendar.getInstance()
    val timeFormat = SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    
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
            Timber.e(e, "Error parsing time: ${schedule.departureTime}")
            false
        }
    }
    
    // Если есть рейсы сегодня, возвращаем ближайший
    if (upcomingToday.isNotEmpty()) {
        ConditionalLogging.debug("Schedule") { "Found ${upcomingToday.size} upcoming departures today. Next: ${upcomingToday.first().departureTime}" }
        return upcomingToday.first().id
    }
    
    // Если рейсов сегодня больше нет, возвращаем первый рейс завтра
    val firstTomorrow = schedules.firstOrNull()
    ConditionalLogging.debug("Schedule") { "No departures today. First tomorrow: ${firstTomorrow?.departureTime}" }
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