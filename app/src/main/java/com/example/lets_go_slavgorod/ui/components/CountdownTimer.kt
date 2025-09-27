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
import androidx.compose.ui.text.style.TextOverflow
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
    
    // Анимация пульсации для ближайшего рейса
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    // Определяем, является ли этот рейс ближайшим
    val isNextDeparture = remember(schedule, allSchedules, currentTime) {
        allSchedules.isNotEmpty() && 
        TimeUtils.getNextDeparture(allSchedules, currentTime)?.id == schedule.id
    }
    
    // Обновляем время каждую секунду только для ближайшего рейса
    LaunchedEffect(isNextDeparture) {
        if (isNextDeparture) {
            while (true) {
                delay(1000)
                currentTime = Calendar.getInstance()
            }
        }
    }
    
    // Вычисляем время до отправления
    val timeWithSeconds = if (isNextDeparture) {
        TimeUtils.getTimeUntilDepartureWithSeconds(schedule.departureTime, currentTime)
    } else {
        null
    }
    
    val timeUntilDeparture = if (isNextDeparture) {
        timeWithSeconds?.first
    } else {
        TimeUtils.getTimeUntilDeparture(schedule.departureTime, currentTime)
    }
    
    val minutes = timeUntilDeparture ?: -1
    
    // Форматируем время для отображения
    val formattedTime = if (isNextDeparture && timeWithSeconds != null) {
        // Для ближайших рейсов показываем секунды
        TimeUtils.formatTimeUntilDepartureWithSeconds(
            timeWithSeconds.first, 
            timeWithSeconds.second, 
            schedule.departureTime
        )
    } else {
        // Для дальних рейсов показываем только минуты с точным временем
        TimeUtils.formatTimeUntilDepartureWithExactTime(minutes, schedule.departureTime)
    }
    
    Log.d("CountdownTimer", "Formatted time: $formattedTime, minutes: $minutes")
    
    if (isNextDeparture && showLabel) {
        // Для ближайшего рейса показываем только плашку
        Row(
            modifier = modifier.background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Ближайший рейс",
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = pulseAlpha),
                modifier = Modifier.size(16.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Ближайший рейс",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = pulseAlpha)
                )
                Text(
                    text = "через ${formattedTime.replace("Через ", "")}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = pulseAlpha)
                )
            }
        }
    } else {
        // Для обычных рейсов показываем только время отсчёта
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (minutes < 0) FontWeight.Normal else FontWeight.Medium
            ),
            color = if (minutes < 0) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = if (minutes >= 0) {
                Modifier.background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
            } else {
                Modifier
            }
        )
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
    val nextDeparture = TimeUtils.getNextDeparture(allSchedules)
    
    if (nextDeparture != null) {
        var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
        
        // Анимация пульсации
        val infiniteTransition = rememberInfiniteTransition(label = "headerPulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "headerPulseAlpha"
        )
        
        // Обновляем время каждую секунду
        LaunchedEffect(nextDeparture.id) {
            while (true) {
                delay(1000)
                currentTime = Calendar.getInstance()
            }
        }
        
        val timeWithSeconds = TimeUtils.getTimeUntilDepartureWithSeconds(nextDeparture.departureTime, currentTime)
        val minutes = timeWithSeconds?.first ?: 0
        
        val formattedTime = if (timeWithSeconds != null) {
            TimeUtils.formatTimeUntilDepartureWithSeconds(
                timeWithSeconds.first,
                timeWithSeconds.second,
                nextDeparture.departureTime
            )
        } else {
            "Сейчас"
        }
        
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Ближайший рейс",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Ближайший рейс",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
                    )
                }
                Text(
                    text = "${nextDeparture.departureTime} • ${nextDeparture.departurePoint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ).padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}