package com.example.lets_go_slavgorod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.model.BusSchedule
import com.example.lets_go_slavgorod.utils.ScheduleUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Состояние UI для экрана расписания
 * 
 * @param isLoading флаг загрузки данных
 * @param schedulesSlavgorod расписания из Славгорода
 * @param schedulesYarovoe расписания из Ярового
 * @param schedulesVokzal расписания от Вокзала
 * @param error сообщение об ошибке
 */
data class ScheduleUiState(
    val isLoading: Boolean = false,
    val schedulesSlavgorod: List<BusSchedule> = emptyList(),
    val schedulesYarovoe: List<BusSchedule> = emptyList(),
    val schedulesVokzal: List<BusSchedule> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel для управления расписанием маршрута
 * 
 * Оптимизирует генерацию и фильтрацию расписаний:
 * - Асинхронная генерация в фоновом потоке
 * - Кэширование результатов
 * - Управление состоянием загрузки
 * - Обработка ошибок
 * 
 * @author VseMirka200
 * @version 1.0
 * @since 2.0
 */
class ScheduleViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScheduleUiState(isLoading = true))
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()
    
    // Константы для остановок
    companion object {
        private const val STOP_SLAVGORD_RYNOK = "Рынок (Славгород)"
        private const val STOP_YAROVOE_MCHS = "МЧС (Яровое)"
        private const val STOP_YAROVOE_ZORI = "Ст. Зори (Яровое)"
        private const val STOP_VOKZAL = "Вокзал (Славгород)"
    }
    
    /**
     * Загружает расписание для маршрута
     * 
     * Выполняется асинхронно в фоновом потоке для избежания блокировки UI
     * 
     * @param route маршрут для которого нужно загрузить расписание
     */
    fun loadSchedule(route: BusRoute) {
        viewModelScope.launch {
            try {
                _uiState.value = ScheduleUiState(isLoading = true)
                
                Timber.d("Starting schedule generation for route ${route.id}")
                val startTime = System.currentTimeMillis()
                
                // Генерируем расписание в фоновом потоке
                val allSchedules = withContext(Dispatchers.Default) {
                    ScheduleUtils.generateSchedules(route.id)
                }
                
                val loadTime = System.currentTimeMillis() - startTime
                Timber.d("Generated ${allSchedules.size} schedules for route ${route.id} in ${loadTime}ms")
                
                // Фильтруем по точкам отправления в фоновом потоке
                val (slavgorod, yarovoe, vokzal) = withContext(Dispatchers.Default) {
                    val slavgorod = allSchedules
                        .filter { schedule -> schedule.departurePoint == STOP_SLAVGORD_RYNOK }
                        .sortedBy { schedule -> schedule.departureTime }
                    
                    val yarovoe = allSchedules
                        .filter { schedule ->
                            when (route.id) {
                                "102B" -> schedule.departurePoint == STOP_YAROVOE_ZORI
                                else -> schedule.departurePoint == STOP_YAROVOE_MCHS
                            }
                        }
                        .sortedBy { schedule -> schedule.departureTime }
                    
                    val vokzal = allSchedules
                        .filter { schedule -> schedule.departurePoint == STOP_VOKZAL }
                        .sortedBy { schedule -> schedule.departureTime }
                    
                    Triple(slavgorod, yarovoe, vokzal)
                }
                
                Timber.d("Filtered schedules: Slavgorod=${slavgorod.size}, Yarovoe=${yarovoe.size}, Vokzal=${vokzal.size}")
                
                _uiState.value = ScheduleUiState(
                    isLoading = false,
                    schedulesSlavgorod = slavgorod,
                    schedulesYarovoe = yarovoe,
                    schedulesVokzal = vokzal,
                    error = null
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Error loading schedule for route ${route.id}")
                _uiState.value = ScheduleUiState(
                    isLoading = false,
                    error = "Ошибка загрузки расписания: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Очищает состояние
     */
    fun clear() {
        _uiState.value = ScheduleUiState(isLoading = true)
    }
}

