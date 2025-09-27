package com.example.lets_go_slavgorod.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Утилиты для работы с временем и обратным отсчетом
 * 
 * Основные функции:
 * - Вычисление времени до отправления автобуса
 * - Форматирование времени в читаемый вид
 * - Определение ближайшего рейса
 * - Работа с секундами для точного отсчета
 * 
 * @author VseMirka200
 * @version 1.0
 */
object TimeUtils {
    
    private const val TAG = "TimeUtils"
    
    /**
     * Форматирует время в читаемый вид
     */
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    /**
     * Вычисляет время до ближайшего рейса
     * 
     * @param departureTime время отправления в формате HH:mm
     * @param currentTime текущее время (по умолчанию - сейчас)
     * @return время до отправления в минутах, или null если рейс уже ушел
     */
    fun getTimeUntilDeparture(departureTime: String, currentTime: Calendar = Calendar.getInstance()): Int? {
        return try {
            Log.d(TAG, "Calculating time until departure: $departureTime")
            val departureCalendar = parseTime(departureTime)
            val current = currentTime.clone() as Calendar
            
            // Устанавливаем секунды и миллисекунды в 0 для точного сравнения
            current.set(Calendar.SECOND, 0)
            current.set(Calendar.MILLISECOND, 0)
            
            Log.d(TAG, "Current time: ${current.get(Calendar.HOUR_OF_DAY)}:${current.get(Calendar.MINUTE)}")
            Log.d(TAG, "Departure time: ${departureCalendar.get(Calendar.HOUR_OF_DAY)}:${departureCalendar.get(Calendar.MINUTE)}")
            
            // Если время отправления уже прошло сегодня, считаем его на завтра
            if (departureCalendar.before(current)) {
                Log.d(TAG, "Departure time is in the past, adding one day")
                departureCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val diffInMillis = departureCalendar.timeInMillis - current.timeInMillis
            val diffInMinutes = (diffInMillis / (1000 * 60)).toInt()
            
            Log.d(TAG, "Time difference in minutes: $diffInMinutes")
            
            if (diffInMinutes >= 0) {
                diffInMinutes
            } else {
                Log.d(TAG, "Departure time is in the past, returning null")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating time until departure", e)
            null
        }
    }
    
    /**
     * Вычисляет время до ближайшего рейса с секундами
     * 
     * @param departureTime время отправления в формате HH:mm
     * @param currentTime текущее время (по умолчанию - сейчас)
     * @return Pair<минуты, секунды> до отправления, или null если рейс уже ушел
     */
    fun getTimeUntilDepartureWithSeconds(departureTime: String, currentTime: Calendar = Calendar.getInstance()): Pair<Int, Int>? {
        return try {
            val departureCalendar = parseTime(departureTime)
            val current = currentTime.clone() as Calendar
            
            // Если время отправления уже прошло сегодня, считаем его на завтра
            if (departureCalendar.before(current)) {
                departureCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val diffInMillis = departureCalendar.timeInMillis - current.timeInMillis
            
            if (diffInMillis >= 0) {
                val totalSeconds = (diffInMillis / 1000).toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                Pair(minutes, seconds)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating time until departure with seconds", e)
            null
        }
    }
    
    /**
     * Парсит время из строки в Calendar
     * 
     * @param timeString время в формате HH:mm
     * @return Calendar с установленным временем
     */
    fun parseTime(timeString: String): Calendar {
        return try {
            val time = timeFormat.parse(timeString)
            val calendar = Calendar.getInstance()
            if (time != null) {
                calendar.time = time
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                // Устанавливаем дату на сегодня
                val today = Calendar.getInstance()
                calendar.set(Calendar.YEAR, today.get(Calendar.YEAR))
                calendar.set(Calendar.MONTH, today.get(Calendar.MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
            }
            calendar
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time: $timeString", e)
            Calendar.getInstance()
        }
    }
    
    /**
     * Форматирует время до отправления в читаемый вид
     * 
     * @param minutes количество минут до отправления
     * @return отформатированная строка
     */
    fun formatTimeUntilDeparture(minutes: Int): String {
        return when {
            minutes < 1 -> "Сейчас"
            minutes == 1 -> "Через 1 минуту"
            minutes < 60 -> "Через $minutes мин"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                when {
                    remainingMinutes == 0 -> "Через $hours ч"
                    hours == 1 -> "Через 1 ч $remainingMinutes мин"
                    else -> "Через $hours ч $remainingMinutes мин"
                }
            }
        }
    }
    
    /**
     * Форматирует время до отправления с точным временем
     * 
     * @param minutes количество минут до отправления
     * @param departureTime время отправления
     * @return отформатированная строка с точным временем
     */
    fun formatTimeUntilDepartureWithExactTime(minutes: Int, departureTime: String): String {
        return when {
            minutes < 1 -> "Сейчас"
            minutes == 1 -> "Через 1 минуту"
            minutes < 60 -> "Через $minutes мин"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                when {
                    remainingMinutes == 0 -> "Через $hours ч"
                    hours == 1 -> "Через 1 ч $remainingMinutes мин"
                    else -> "Через $hours ч $remainingMinutes мин"
                }
            }
        }
    }
    
    /**
     * Форматирует время до отправления с секундами для ближайших рейсов
     * 
     * @param minutes количество минут до отправления
     * @param seconds количество секунд до отправления
     * @param departureTime время отправления
     * @return отформатированная строка с секундами
     */
    fun formatTimeUntilDepartureWithSeconds(minutes: Int, seconds: Int, departureTime: String): String {
        return when {
            minutes < 1 -> {
                if (seconds <= 0) "Сейчас"
                else "Через $seconds сек"
            }
            minutes == 1 -> "Через 1 мин $seconds сек"
            minutes < 60 -> "Через $minutes мин $seconds сек"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                when {
                    remainingMinutes == 0 -> "Через $hours ч"
                    hours == 1 -> "Через 1 ч $remainingMinutes мин"
                    else -> "Через $hours ч $remainingMinutes мин"
                }
            }
        }
    }
    
    /**
     * Получает ближайший рейс из списка расписаний
     * 
     * @param schedules список расписаний
     * @param currentTime текущее время
     * @return ближайший рейс или null
     */
    fun getNextDeparture(schedules: List<com.example.lets_go_slavgorod.data.model.BusSchedule>, currentTime: Calendar = Calendar.getInstance()): com.example.lets_go_slavgorod.data.model.BusSchedule? {
        if (schedules.isEmpty()) return null
        
        // Находим ближайший рейс среди всех расписаний
        val nextDeparture = schedules.minByOrNull { schedule ->
            val timeUntilDeparture = getTimeUntilDeparture(schedule.departureTime, currentTime)
            if (timeUntilDeparture != null) {
                timeUntilDeparture
            } else {
                // Если рейс уже прошел сегодня, считаем его на завтра
                val departureCalendar = parseTime(schedule.departureTime)
                val current = currentTime.clone() as Calendar
                current.set(Calendar.SECOND, 0)
                current.set(Calendar.MILLISECOND, 0)
                
                // Добавляем день к времени отправления
                departureCalendar.add(Calendar.DAY_OF_MONTH, 1)
                
                val diffInMillis = departureCalendar.timeInMillis - current.timeInMillis
                val diffInMinutes = (diffInMillis / (1000 * 60)).toInt()
                
                if (diffInMinutes >= 0) {
                    diffInMinutes
                } else {
                    Int.MAX_VALUE
                }
            }
        }
        return nextDeparture
    }
    
    /**
     * Проверяет, является ли рейс ближайшим
     * 
     * @param schedule расписание для проверки
     * @param allSchedules все расписания
     * @param currentTime текущее время
     * @return true если это ближайший рейс
     */
    fun isNextDeparture(
        schedule: com.example.lets_go_slavgorod.data.model.BusSchedule,
        allSchedules: List<com.example.lets_go_slavgorod.data.model.BusSchedule>,
        currentTime: Calendar = Calendar.getInstance()
    ): Boolean {
        val nextDeparture = getNextDeparture(allSchedules, currentTime)
        return nextDeparture?.id == schedule.id
    }
    
    /**
     * Получает время до ближайшего рейса в читаемом формате
     * 
     * @param schedule расписание
     * @param currentTime текущее время
     * @return отформатированное время до отправления или null
     */
    fun getFormattedTimeUntilDeparture(
        schedule: com.example.lets_go_slavgorod.data.model.BusSchedule,
        currentTime: Calendar = Calendar.getInstance()
    ): String? {
        val minutes = getTimeUntilDeparture(schedule.departureTime, currentTime)
        return minutes?.let { formatTimeUntilDeparture(it) }
    }
}
