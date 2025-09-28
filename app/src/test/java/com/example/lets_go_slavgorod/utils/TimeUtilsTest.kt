package com.example.lets_go_slavgorod.utils

import com.example.lets_go_slavgorod.data.model.BusSchedule
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Тесты для утилит работы с временем
 */
class TimeUtilsTest {

    @Test
    fun `getTimeUntilDeparture should return correct minutes for future time`() {
        // Given
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val departureTime = "10:30"

        // When
        val result = TimeUtils.getTimeUntilDeparture(departureTime, currentTime)

        // Then
        assertEquals(30, result)
    }

    @Test
    fun `getTimeUntilDeparture should return null for past time`() {
        // Given
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val departureTime = "10:00"

        // When
        val result = TimeUtils.getTimeUntilDeparture(departureTime, currentTime)

        // Then
        assertNull(result)
    }

    @Test
    fun `getTimeUntilDeparture should handle same time correctly`() {
        // Given
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val departureTime = "10:00"

        // When
        val result = TimeUtils.getTimeUntilDeparture(departureTime, currentTime)

        // Then
        assertEquals(0, result)
    }

    @Test
    fun `formatTimeUntilDeparture should format minutes correctly`() {
        // Test cases for different time formats
        assertEquals("Сейчас", TimeUtils.formatTimeUntilDeparture(0))
        assertEquals("Через 1 минуту", TimeUtils.formatTimeUntilDeparture(1))
        assertEquals("Через 5 мин", TimeUtils.formatTimeUntilDeparture(5))
        assertEquals("Через 30 мин", TimeUtils.formatTimeUntilDeparture(30))
        assertEquals("Через 1 ч", TimeUtils.formatTimeUntilDeparture(60))
        assertEquals("Через 1 ч 30 мин", TimeUtils.formatTimeUntilDeparture(90))
        assertEquals("Через 2 ч", TimeUtils.formatTimeUntilDeparture(120))
        assertEquals("Через 2 ч 15 мин", TimeUtils.formatTimeUntilDeparture(135))
    }

    @Test
    fun `getNextDeparture should return closest schedule`() {
        // Given
        val schedules = listOf(
            BusSchedule("1", "102", "Славгород (Рынок)", "08:00", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("2", "102", "Славгород (Рынок)", "10:30", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("3", "102", "Славгород (Рынок)", "13:00", 1, notes = null, departurePoint = "Славгород (Рынок)")
        )
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // When
        val result = TimeUtils.getNextDeparture(schedules, currentTime)

        // Then
        assertNotNull(result)
        assertEquals("2", result?.id) // 10:30 is the next departure
        assertEquals("10:30", result?.departureTime)
    }

    @Test
    fun `getNextDeparture should return null when no future schedules`() {
        // Given
        val schedules = listOf(
            BusSchedule("1", "102", "Славгород (Рынок)", "08:00", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("2", "102", "Славгород (Рынок)", "09:00", 1, departurePoint = "Славгород (Рынок)")
        )
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // When
        val result = TimeUtils.getNextDeparture(schedules, currentTime)

        // Then
        assertNull(result)
    }

    @Test
    fun `isNextDeparture should return true for next departure`() {
        // Given
        val schedules = listOf(
            BusSchedule("1", "102", "Славгород (Рынок)", "08:00", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("2", "102", "Славгород (Рынок)", "10:30", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("3", "102", "Славгород (Рынок)", "13:00", 1, notes = null, departurePoint = "Славгород (Рынок)")
        )
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetSchedule = schedules[1] // 10:30

        // When
        val result = TimeUtils.isNextDeparture(targetSchedule, schedules, currentTime)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isNextDeparture should return false for non-next departure`() {
        // Given
        val schedules = listOf(
            BusSchedule("1", "102", "Славгород (Рынок)", "08:00", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("2", "102", "Славгород (Рынок)", "10:30", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("3", "102", "Славгород (Рынок)", "13:00", 1, notes = null, departurePoint = "Славгород (Рынок)")
        )
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetSchedule = schedules[2] // 13:00

        // When
        val result = TimeUtils.isNextDeparture(targetSchedule, schedules, currentTime)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getFormattedTimeUntilDeparture should return formatted string`() {
        // Given
        val schedule = BusSchedule("1", "102", "Славгород (Рынок)", "10:30", 1, departurePoint = "Славгород (Рынок)")
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // When
        val result = TimeUtils.getFormattedTimeUntilDeparture(schedule, currentTime)

        // Then
        assertEquals("Через 30 мин", result)
    }

    @Test
    fun `getFormattedTimeUntilDeparture should return null for past time`() {
        // Given
        val schedule = BusSchedule("1", "102", "Славгород (Рынок)", "09:00", 1, departurePoint = "Славгород (Рынок)")
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // When
        val result = TimeUtils.getFormattedTimeUntilDeparture(schedule, currentTime)

        // Then
        assertNull(result)
    }

    @Test
    fun `getTimeUntilDepartureWithSeconds should return correct minutes and seconds`() {
        // Given
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 30)
            set(Calendar.MILLISECOND, 0)
        }
        val departureTime = "10:30"

        // When
        val result = TimeUtils.getTimeUntilDepartureWithSeconds(departureTime, currentTime)

        // Then
        assertNotNull(result)
        assertEquals(29, result?.first) // 29 минут
        assertEquals(30, result?.second) // 30 секунд
    }

    @Test
    fun `getTimeUntilDepartureWithSeconds should return null for past time`() {
        // Given
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val departureTime = "10:00"

        // When
        val result = TimeUtils.getTimeUntilDepartureWithSeconds(departureTime, currentTime)

        // Then
        assertNull(result)
    }

    @Test
    fun `formatTimeUntilDepartureWithExactTime should format with exact time`() {
        // Test cases for different time formats with exact time
        assertEquals("Сейчас (10-00)", TimeUtils.formatTimeUntilDepartureWithExactTime(0, "10:00"))
        assertEquals("Через 1 минуту (10-01)", TimeUtils.formatTimeUntilDepartureWithExactTime(1, "10:01"))
        assertEquals("Через 5 мин (10-05)", TimeUtils.formatTimeUntilDepartureWithExactTime(5, "10:05"))
        assertEquals("Через 30 мин (10-30)", TimeUtils.formatTimeUntilDepartureWithExactTime(30, "10:30"))
        assertEquals("Через 1 ч (11-00)", TimeUtils.formatTimeUntilDepartureWithExactTime(60, "11:00"))
        assertEquals("Через 1 ч 30 мин (11-30)", TimeUtils.formatTimeUntilDepartureWithExactTime(90, "11:30"))
        assertEquals("Через 2 ч (12-00)", TimeUtils.formatTimeUntilDepartureWithExactTime(120, "12:00"))
    }

    @Test
    fun `formatTimeUntilDepartureWithSeconds should format with seconds`() {
        // Test cases for different time formats with seconds
        assertEquals("Сейчас (10-00)", TimeUtils.formatTimeUntilDepartureWithSeconds(0, 0, "10:00"))
        assertEquals("Через 30 сек (10-00)", TimeUtils.formatTimeUntilDepartureWithSeconds(0, 30, "10:00"))
        assertEquals("Через 1 мин 30 сек (10-01)", TimeUtils.formatTimeUntilDepartureWithSeconds(1, 30, "10:01"))
        assertEquals("Через 5 мин 45 сек (10-05)", TimeUtils.formatTimeUntilDepartureWithSeconds(5, 45, "10:05"))
        assertEquals("Через 30 мин (10-30)", TimeUtils.formatTimeUntilDepartureWithSeconds(30, 0, "10:30"))
        assertEquals("Через 1 ч (11-00)", TimeUtils.formatTimeUntilDepartureWithSeconds(60, 0, "11:00"))
        assertEquals("Через 1 ч 30 мин (11-30)", TimeUtils.formatTimeUntilDepartureWithSeconds(90, 0, "11:30"))
        assertEquals("Через 2 ч (12-00)", TimeUtils.formatTimeUntilDepartureWithSeconds(120, 0, "12:00"))
    }

    @Test
    fun `getNextDeparture should find next day departure at 22-40`() {
        // Given - время 22:40, все рейсы уже прошли
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 40)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val schedules = listOf(
            BusSchedule("1", "102", "Славгород (Рынок)", "08:00", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("2", "102", "Славгород (Рынок)", "10:30", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("3", "102", "Славгород (Рынок)", "13:00", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("4", "102", "Славгород (Рынок)", "15:30", 1, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("5", "102", "Славгород (Рынок)", "18:00", 1, notes = null, departurePoint = "Славгород (Рынок)")
        )

        // When
        val result = TimeUtils.getNextDeparture(schedules, currentTime)

        // Then
        assertNotNull(result)
        assertEquals("08:00", result?.departureTime) // Первый рейс завтра
    }

    @Test
    fun `getTimeUntilDeparture should calculate time until next day`() {
        // Given - время 22:40, рейс на 08:00 завтра
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 40)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val departureTime = "08:00"

        // When
        val result = TimeUtils.getTimeUntilDeparture(departureTime, currentTime)

        // Then
        assertNotNull(result)
        // 22:40 до 08:00 завтра = 9 часов 20 минут = 560 минут
        assertEquals(560, result)
    }
}
