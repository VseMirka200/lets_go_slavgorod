package com.example.lets_go_slavgorod.utils

import com.example.lets_go_slavgorod.data.model.BusSchedule
import java.util.*

/**
 * Утилиты для работы с расписанием автобусов
 * 
 * Основные функции:
 * - Генерация статических расписаний для всех маршрутов
 * - Создание расписаний для маршрута №102 (Славгород — Яровое)
 * - Создание расписаний для маршрута №1 (Вокзал — Совхоз)
 * - Поддержка различных выходов (1, 2, 3 выход)
 * 
 * @author VseMirka200
 * @version 1.0
 */
object ScheduleUtils {
    
    /**
     * Генерирует стандартные расписания для всех маршрутов
     * 
     * @param routeId ID маршрута
     * @return список расписаний для маршрута
     */
    fun generateSchedules(routeId: String): List<BusSchedule> {
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        
        return when (routeId) {
            "102" -> generateRoute102Schedules(currentDayOfWeek)
            "1" -> generateRoute1Schedules(currentDayOfWeek)
            else -> emptyList()
        }
    }
    
    /**
     * Генерирует расписания для маршрута №102 (Славгород — Яровое)
     */
    private fun generateRoute102Schedules(dayOfWeek: Int): List<BusSchedule> {
        return listOf(
            // Отправление из Славгорода (Рынок)
            BusSchedule("102_slav_1", "102", "Славгород (Рынок)", "06:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_2", "102", "Славгород (Рынок)", "06:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_3", "102", "Славгород (Рынок)", "07:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_4", "102", "Славгород (Рынок)", "07:40", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_5", "102", "Славгород (Рынок)", "08:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_6", "102", "Славгород (Рынок)", "08:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_7", "102", "Славгород (Рынок)", "09:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_8", "102", "Славгород (Рынок)", "09:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_9", "102", "Славгород (Рынок)", "10:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_10", "102", "Славгород (Рынок)", "10:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_11", "102", "Славгород (Рынок)", "11:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_12", "102", "Славгород (Рынок)", "11:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_13", "102", "Славгород (Рынок)", "12:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_14", "102", "Славгород (Рынок)", "12:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_15", "102", "Славгород (Рынок)", "13:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_16", "102", "Славгород (Рынок)", "13:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_17", "102", "Славгород (Рынок)", "14:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_18", "102", "Славгород (Рынок)", "14:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_19", "102", "Славгород (Рынок)", "15:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_20", "102", "Славгород (Рынок)", "15:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_21", "102", "Славгород (Рынок)", "16:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_22", "102", "Славгород (Рынок)", "16:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_23", "102", "Славгород (Рынок)", "17:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_24", "102", "Славгород (Рынок)", "17:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_25", "102", "Славгород (Рынок)", "18:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_26", "102", "Славгород (Рынок)", "18:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_27", "102", "Славгород (Рынок)", "19:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_28", "102", "Славгород (Рынок)", "19:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_29", "102", "Славгород (Рынок)", "20:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_30", "102", "Славгород (Рынок)", "20:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_31", "102", "Славгород (Рынок)", "21:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_32", "102", "Славгород (Рынок)", "21:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_33", "102", "Славгород (Рынок)", "22:00", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_34", "102", "Славгород (Рынок)", "22:30", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            BusSchedule("102_slav_35", "102", "Славгород (Рынок)", "00:02", dayOfWeek, notes = null, departurePoint = "Славгород (Рынок)"),
            
            // Отправление из Ярового (МЧС-128)
            BusSchedule("102_yar_1", "102", "Яровое (МЧС-128)", "06:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_2", "102", "Яровое (МЧС-128)", "06:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_3", "102", "Яровое (МЧС-128)", "07:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_4", "102", "Яровое (МЧС-128)", "07:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_5", "102", "Яровое (МЧС-128)", "08:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_6", "102", "Яровое (МЧС-128)", "08:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_7", "102", "Яровое (МЧС-128)", "09:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_8", "102", "Яровое (МЧС-128)", "09:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_9", "102", "Яровое (МЧС-128)", "10:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_10", "102", "Яровое (МЧС-128)", "10:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_11", "102", "Яровое (МЧС-128)", "11:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_12", "102", "Яровое (МЧС-128)", "11:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_13", "102", "Яровое (МЧС-128)", "12:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_14", "102", "Яровое (МЧС-128)", "12:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_15", "102", "Яровое (МЧС-128)", "13:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_16", "102", "Яровое (МЧС-128)", "13:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_17", "102", "Яровое (МЧС-128)", "14:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_18", "102", "Яровое (МЧС-128)", "14:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_19", "102", "Яровое (МЧС-128)", "15:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_20", "102", "Яровое (МЧС-128)", "15:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_21", "102", "Яровое (МЧС-128)", "16:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_22", "102", "Яровое (МЧС-128)", "16:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_23", "102", "Яровое (МЧС-128)", "17:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_24", "102", "Яровое (МЧС-128)", "17:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_25", "102", "Яровое (МЧС-128)", "18:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_26", "102", "Яровое (МЧС-128)", "18:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_27", "102", "Яровое (МЧС-128)", "19:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_28", "102", "Яровое (МЧС-128)", "19:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_29", "102", "Яровое (МЧС-128)", "20:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_30", "102", "Яровое (МЧС-128)", "20:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_31", "102", "Яровое (МЧС-128)", "21:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_32", "102", "Яровое (МЧС-128)", "21:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_33", "102", "Яровое (МЧС-128)", "22:15", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_34", "102", "Яровое (МЧС-128)", "22:45", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)"),
            BusSchedule("102_yar_35", "102", "Яровое (МЧС-128)", "23:10", dayOfWeek, notes = null, departurePoint = "Яровое (МЧС-128)")
        )
    }
    
    /**
     * Генерирует расписания для маршрута №1 (Вокзал — Совхоз)
     */
    private fun generateRoute1Schedules(dayOfWeek: Int): List<BusSchedule> {
        return listOf(
            // Отправление из Вокзала
            BusSchedule("1_vokzal_1", "1", "Вокзала", "07:00", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_2", "1", "Вокзала", "07:48", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_3", "1", "Вокзала", "08:36", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_4", "1", "Вокзала", "09:24", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_5", "1", "Вокзала", "10:12", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_6", "1", "Вокзала", "11:00", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_7", "1", "Вокзала", "11:48", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_8", "1", "Вокзала", "12:36", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_9", "1", "Вокзала", "13:24", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_10", "1", "Вокзала", "14:12", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_11", "1", "Вокзала", "15:00", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_12", "1", "Вокзала", "15:48", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_13", "1", "Вокзала", "16:36", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_14", "1", "Вокзала", "17:24", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_15", "1", "Вокзала", "18:12", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_16", "1", "Вокзала", "19:00", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_17", "1", "Вокзала", "19:48", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_18", "1", "Вокзала", "20:36", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_19", "1", "Вокзала", "21:24", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            BusSchedule("1_vokzal_20", "1", "Вокзала", "22:12", dayOfWeek, notes = "1 выход", departurePoint = "Вокзала"),
            
            // Отправление из Совхоза
            BusSchedule("1_sovhoz_1", "1", "Совхоза", "07:15", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_2", "1", "Совхоза", "08:03", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3", "1", "Совхоза", "08:51", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_4", "1", "Совхоза", "09:39", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_5", "1", "Совхоза", "10:27", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_6", "1", "Совхоза", "11:15", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_7", "1", "Совхоза", "12:03", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_8", "1", "Совхоза", "12:51", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_9", "1", "Совхоза", "13:39", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_10", "1", "Совхоза", "14:27", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_11", "1", "Совхоза", "15:15", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_12", "1", "Совхоза", "16:03", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_13", "1", "Совхоза", "16:51", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_14", "1", "Совхоза", "17:39", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_15", "1", "Совхоза", "18:27", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_16", "1", "Совхоза", "19:15", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_17", "1", "Совхоза", "20:03", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_18", "1", "Совхоза", "20:51", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_19", "1", "Совхоза", "21:39", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_20", "1", "Совхоза", "22:27", dayOfWeek, notes = "2 выход", departurePoint = "Совхоза"),
            
            // 3 выход (Совхоз → Вокзал)
            BusSchedule("1_sovhoz_3_1", "1", "Совхоза", "06:30", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_2", "1", "Совхоза", "07:18", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_3", "1", "Совхоза", "08:06", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_4", "1", "Совхоза", "08:54", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_5", "1", "Совхоза", "09:42", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_6", "1", "Совхоза", "10:30", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_7", "1", "Совхоза", "11:18", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_8", "1", "Совхоза", "12:06", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_9", "1", "Совхоза", "12:54", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_10", "1", "Совхоза", "13:42", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_11", "1", "Совхоза", "14:30", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_12", "1", "Совхоза", "15:18", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_13", "1", "Совхоза", "16:06", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_14", "1", "Совхоза", "16:54", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_15", "1", "Совхоза", "17:42", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_16", "1", "Совхоза", "18:30", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_17", "1", "Совхоза", "19:18", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_18", "1", "Совхоза", "20:06", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_19", "1", "Совхоза", "20:54", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_20", "1", "Совхоза", "21:42", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_21", "1", "Совхоза", "22:30", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза"),
            BusSchedule("1_sovhoz_3_22", "1", "Совхоза", "23:18", dayOfWeek, notes = "3 выход", departurePoint = "Совхоза")
        )
    }
}
