package com.example.lets_go_slavgorod.ui.viewmodel

// Android системные импорты
import android.app.Application
import android.util.Log

// ViewModel импорты
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

// Модели данных
import com.example.lets_go_slavgorod.data.local.entity.FavoriteTimeEntity
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.data.model.BusSchedule
import com.example.lets_go_slavgorod.data.model.FavoriteTime

// Локальная база данных
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.repository.BusRouteRepository

// Уведомления
import com.example.lets_go_slavgorod.notifications.AlarmScheduler

// Утилиты
import com.example.lets_go_slavgorod.utils.loge
import com.example.lets_go_slavgorod.utils.toFavoriteTime

// Coroutines импорты
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

/**
 * Состояние UI для экрана с маршрутами
 * 
 * @param routes список доступных маршрутов
 * @param isLoading флаг загрузки данных
 * @param error сообщение об ошибке (если есть)
 * @param isAddingFavorite флаг добавления в избранное
 * @param isRemovingFavorite флаг удаления из избранного
 */
data class BusUiState(
    val routes: List<BusRoute> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingFavorite: Boolean = false,
    val isRemovingFavorite: Boolean = false
)

/**
 * ViewModel для управления данными маршрутов и избранными временами
 * 
 * Основные функции:
 * - Загрузка и поиск маршрутов
 * - Управление избранными временами отправления
 * - Планирование уведомлений для избранных времен
 * - Интеграция с базой данных и системой уведомлений
 */
class BusViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BusUiState())
    val uiState: StateFlow<BusUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val favoriteTimeDao = AppDatabase.getDatabase(application).favoriteTimeDao()
    private val routeRepository = BusRouteRepository(application)

    val favoriteTimes: StateFlow<List<FavoriteTime>> =
        favoriteTimeDao.getAllFavoriteTimes()
            .map { entities ->
                entities.map { entity ->
                    entity.toFavoriteTime(routeRepository)
                }
            }
            .catch { exception ->
                loge("Error collecting favorite times", exception)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        loadInitialRoutes()
    }

    private fun loadInitialRoutes() {
        val routes = routeRepository.getAllRoutes()
        Log.d("BusViewModel", "Loading routes: ${routes.size} routes found")
        routes.forEach { route ->
            Log.d("BusViewModel", "Route: ${route.id} - ${route.name}")
        }
        _uiState.update { currentState ->
            currentState.copy(
                routes = routes,
                isLoading = false,
                error = null
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        val routesToDisplay = routeRepository.searchRoutes(query)
        _uiState.update { currentState ->
            currentState.copy(
                routes = routesToDisplay,
            )
        }
    }

    fun getRouteById(routeId: String?): BusRoute? {
        return routeRepository.getRouteById(routeId)
    }

    fun addFavoriteTime(schedule: BusSchedule) {
        viewModelScope.launch {
            try {
                _uiState.update { currentState ->
                    currentState.copy(isAddingFavorite = true, error = null)
                }
                
                // Валидация данных перед добавлением
                val sanitizedSchedule = schedule.sanitized()
                if (!sanitizedSchedule.isValid()) {
                    Log.e("BusViewModel", "Invalid schedule data, cannot add to favorites: ${schedule.id}")
                    _uiState.update { currentState ->
                        currentState.copy(
                            isAddingFavorite = false,
                            error = "Некорректные данные расписания"
                        )
                    }
                    return@launch
                }
                
                val favoriteTimeEntity = FavoriteTimeEntity(
                    id = sanitizedSchedule.id,
                    routeId = sanitizedSchedule.routeId,
                    departureTime = sanitizedSchedule.departureTime,
                    stopName = sanitizedSchedule.stopName,
                    departurePoint = sanitizedSchedule.departurePoint,
                    dayOfWeek = sanitizedSchedule.dayOfWeek,
                    isActive = true
                )
                favoriteTimeDao.addFavoriteTime(favoriteTimeEntity)

                val route = getRouteById(sanitizedSchedule.routeId)
                val favoriteForScheduler = FavoriteTime(
                    id = sanitizedSchedule.id,
                    routeId = sanitizedSchedule.routeId,
                    routeNumber = route?.routeNumber ?: "N/A",
                    routeName = route?.name ?: "Маршрут",
                    stopName = sanitizedSchedule.stopName,
                    departureTime = sanitizedSchedule.departureTime,
                    dayOfWeek = sanitizedSchedule.dayOfWeek,
                    departurePoint = sanitizedSchedule.departurePoint,
                    isActive = true
                )
                
                AlarmScheduler.checkAndUpdateNotifications(getApplication(), favoriteForScheduler)
                Log.d("BusViewModel", "Successfully added favorite time: ${sanitizedSchedule.id}")
                
                _uiState.update { currentState ->
                    currentState.copy(isAddingFavorite = false, error = null)
                }
                
            } catch (e: Exception) {
                Log.e("BusViewModel", "Error adding favorite time: ${schedule.id}", e)
                _uiState.update { currentState ->
                    currentState.copy(
                        isAddingFavorite = false,
                        error = "Ошибка при добавлении в избранное: ${e.message}"
                    )
                }
            }
        }
    }

    fun removeFavoriteTime(scheduleId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { currentState ->
                    currentState.copy(isRemovingFavorite = true, error = null)
                }
                
                favoriteTimeDao.removeFavoriteTime(scheduleId)
                AlarmScheduler.cancelAlarm(getApplication(), scheduleId)
                
                Log.d("BusViewModel", "Successfully removed favorite time: $scheduleId")
                
                _uiState.update { currentState ->
                    currentState.copy(isRemovingFavorite = false, error = null)
                }
                
            } catch (e: Exception) {
                Log.e("BusViewModel", "Error removing favorite time: $scheduleId", e)
                _uiState.update { currentState ->
                    currentState.copy(
                        isRemovingFavorite = false,
                        error = "Ошибка при удалении из избранного: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateFavoriteActiveState(favoriteTime: FavoriteTime, newActiveState: Boolean) {
        viewModelScope.launch {
            val entityInDb = favoriteTimeDao.getFavoriteTimeById(favoriteTime.id).firstOrNull()
            if (entityInDb == null) {
                Log.e("BusViewModel", "FavoriteTime with id ${favoriteTime.id} not found for update/removal.")
                return@launch
            }

            if (!newActiveState) {
                favoriteTimeDao.removeFavoriteTime(favoriteTime.id)
                Log.d("BusViewModel", "FavoriteTime ${favoriteTime.id} removed due to newActiveState=false.")
                try {
                    AlarmScheduler.cancelAlarm(getApplication(), favoriteTime.id)
                } catch (e: Exception) {
                    Log.e("BusViewModel", "Error cancelling alarm for favorite: ${favoriteTime.id}", e)
                }
            } else {
                if (!entityInDb.isActive) {
                    favoriteTimeDao.updateFavoriteTime(entityInDb.copy(isActive = true))
                    Log.d("BusViewModel", "FavoriteTime ${favoriteTime.id} activated.")
                    val updatedFavoriteForScheduler = favoriteTime.copy(isActive = true)
                    try {
                        AlarmScheduler.checkAndUpdateNotifications(getApplication(), updatedFavoriteForScheduler)
                    } catch (e: Exception) {
                        Log.e("BusViewModel", "Error rescheduling alarm for favorite: ${favoriteTime.id}", e)
                    }
                } else {
                    Log.d("BusViewModel", "FavoriteTime ${favoriteTime.id} is already active.")
                }
            }
        }
    }
    

}