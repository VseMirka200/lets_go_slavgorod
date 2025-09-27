package com.example.lets_go_slavgorod.ui.components

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lets_go_slavgorod.ui.theme.BusBlue
import com.example.lets_go_slavgorod.ui.theme.TransportGreen
import com.example.lets_go_slavgorod.ui.theme.TransportOrange
import com.example.lets_go_slavgorod.data.model.BusSchedule
import com.example.lets_go_slavgorod.utils.TimeUtils
import kotlinx.coroutines.delay
import java.util.*

/**
 * Компонент обратного отсчета времени до отправления автобуса
 * 
 * Основные функции:
 * - Отображение времени до ближайшего рейса
 * - Анимация пульсации для привлечения внимания
 * - Поддержка различных состояний (ближайший рейс, обычный рейс)
 * - Автоматическое обновление каждую секунду
 * 
 * @param schedule расписание автобуса
 * @param allSchedules все расписания для определения ближайшего рейса
 * @param showLabel показывать ли лейбл "Ближайший рейс" в тексте
 * @param modifier модификатор для настройки внешнего вида
 * 
 * @author VseMirka200
 * @version 1.0
 */
@Composable
fun CountdownTimer(
    schedule: BusSchedule,
    allSchedules: List<BusSchedule>,
    showLabel: Boolean = false,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var timeUntilDeparture by remember { mutableStateOf<Int?>(null) }
    var timeWithSeconds by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isNextDeparture by remember { mutableStateOf(false) }
    
    // Анимация для пульсации ближайшего рейса
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Обновляем время каждую секунду для ближайших рейсов, каждую минуту для остальных
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            timeUntilDeparture = TimeUtils.getTimeUntilDeparture(schedule.departureTime, currentTime)
            timeWithSeconds = TimeUtils.getTimeUntilDepartureWithSeconds(schedule.departureTime, currentTime)
            isNextDeparture = TimeUtils.isNextDeparture(schedule, allSchedules, currentTime)
            
            // Для ближайших рейсов (менее 10 минут) обновляем каждую секунду
            val updateInterval = if (timeUntilDeparture != null && timeUntilDeparture!! < 10) 1000L else 60000L
            delay(updateInterval)
        }
    }
    
    // Обновляем время при изменении расписания
    LaunchedEffect(schedule.departureTime) {
        currentTime = Calendar.getInstance()
        timeUntilDeparture = TimeUtils.getTimeUntilDeparture(schedule.departureTime, currentTime)
        timeWithSeconds = TimeUtils.getTimeUntilDepartureWithSeconds(schedule.departureTime, currentTime)
        isNextDeparture = TimeUtils.isNextDeparture(schedule, allSchedules, currentTime)
        
    }
    
    // Отображаем компонент только если рейс еще не ушел
    Log.d("CountdownTimer", "Schedule: ${schedule.departureTime}, timeUntilDeparture: $timeUntilDeparture, isNextDeparture: $isNextDeparture")
    
    timeUntilDeparture?.let { minutes ->
        val formattedTime = if (timeWithSeconds != null && minutes < 10) {
            // Для ближайших рейсов показываем секунды
            TimeUtils.formatTimeUntilDepartureWithSeconds(
                timeWithSeconds!!.first, 
                timeWithSeconds!!.second, 
                schedule.departureTime
            )
        } else {
            // Для дальних рейсов показываем только минуты с точным временем
            TimeUtils.formatTimeUntilDepartureWithExactTime(minutes, schedule.departureTime)
        }
        
        Row(
            modifier = modifier
                .background(
                    color = if (isNextDeparture) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Иконка часов
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Время до отправления",
                tint = if (isNextDeparture) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp)
            )
            
            // Текст с временем (с лейблом "Ближайший рейс" если нужно)
            Text(
                text = if (showLabel && isNextDeparture) {
                    "Ближайший рейс: $formattedTime"
                } else {
                    formattedTime
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isNextDeparture) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = if (isNextDeparture) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            // Индикатор ближайшего рейса
            if (isNextDeparture) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

/**
 * Компонент для отображения времени до ближайшего рейса в заголовке
 * 
 * @param allSchedules все расписания
 * @param modifier модификатор для настройки внешнего вида
 */
@Composable
fun NextDepartureHeader(
    allSchedules: List<BusSchedule>,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var nextDeparture by remember { mutableStateOf<BusSchedule?>(null) }
    var timeUntilDeparture by remember { mutableStateOf<Int?>(null) }
    var timeWithSeconds by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    // Анимация для пульсации
    val infiniteTransition = rememberInfiniteTransition(label = "header_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Обновляем время каждую секунду для ближайших рейсов, каждую минуту для остальных
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            nextDeparture = TimeUtils.getNextDeparture(allSchedules, currentTime)
            timeUntilDeparture = nextDeparture?.let { 
                TimeUtils.getTimeUntilDeparture(it.departureTime, currentTime) 
            }
            timeWithSeconds = nextDeparture?.let { 
                TimeUtils.getTimeUntilDepartureWithSeconds(it.departureTime, currentTime) 
            }
            
            // Для ближайших рейсов (менее 10 минут) обновляем каждую секунду
            val updateInterval = if (timeUntilDeparture != null && timeUntilDeparture!! < 10) 1000L else 60000L
            delay(updateInterval)
        }
    }
    
    // Обновляем при изменении расписаний
    LaunchedEffect(allSchedules) {
        currentTime = Calendar.getInstance()
        nextDeparture = TimeUtils.getNextDeparture(allSchedules, currentTime)
        timeUntilDeparture = nextDeparture?.let { 
            TimeUtils.getTimeUntilDeparture(it.departureTime, currentTime) 
        }
        timeWithSeconds = nextDeparture?.let { 
            TimeUtils.getTimeUntilDepartureWithSeconds(it.departureTime, currentTime) 
        }
    }
    
    nextDeparture?.let { departure ->
        timeUntilDeparture?.let { minutes ->
            val formattedTime = if (timeWithSeconds != null && minutes < 10) {
                // Для ближайших рейсов показываем секунды
                TimeUtils.formatTimeUntilDepartureWithSeconds(
                    timeWithSeconds!!.first, 
                    timeWithSeconds!!.second, 
                    departure.departureTime
                )
            } else {
                // Для дальних рейсов показываем только минуты с точным временем
                TimeUtils.formatTimeUntilDepartureWithExactTime(minutes, departure.departureTime)
            }
            
            
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Ближайший рейс",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Ближайший рейс: $formattedTime",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Text(
                        text = "Отправление в ${departure.departureTime}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "От ${departure.departurePoint}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
