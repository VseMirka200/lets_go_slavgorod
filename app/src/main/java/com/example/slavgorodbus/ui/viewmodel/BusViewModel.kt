package com.example.slavgorodbus.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.slavgorodbus.data.local.entity.FavoriteTimeEntity
import com.example.slavgorodbus.data.model.BusRoute
import com.example.slavgorodbus.data.model.BusSchedule
import com.example.slavgorodbus.data.model.FavoriteTime
import com.example.slavgorodbus.data.local.AppDatabase
import com.example.slavgorodbus.notifications.AlarmScheduler
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

data class BusUiState(
    val routes: List<BusRoute> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class BusViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BusUiState())
    val uiState: StateFlow<BusUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val favoriteTimeDao = AppDatabase.getDatabase(application).favoriteTimeDao()

    val favoriteTimes: StateFlow<List<FavoriteTime>> =
        favoriteTimeDao.getAllFavoriteTimes()
            .map { entities ->
                entities.map { entity ->
                    val route = getRouteById(entity.routeId)
                    FavoriteTime(
                        id = entity.id,
                        routeId = entity.routeId,
                        routeNumber = route?.routeNumber ?: "N/A",
                        routeName = route?.name ?: "Неизвестный маршрут",
                        stopName = entity.stopName,
                        departureTime = entity.departureTime,
                        dayOfWeek = entity.dayOfWeek,
                        departurePoint = entity.departurePoint,
                        isActive = entity.isActive
                    )
                }
            }
            .catch { exception ->
                Log.e("BusViewModel", "Error collecting favorite times", exception)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _allRoutes = MutableStateFlow<List<BusRoute>>(emptyList())

    init {
        val sampleRoutes = listOf(
            BusRoute(
                id = "102",
                routeNumber = "102",
                name = "Автобус №102",
                description = "Маршрут Славгород — Яровое",
                travelTime = "~40 минут",
                pricePrimary = "38₽ город / 55₽ межгород",
                paymentMethods = "Нал. / Безнал.",
                color = "#FF6200EE"
            )
        )
        _allRoutes.value = sampleRoutes
        loadInitialRoutes()
    }

    private fun loadInitialRoutes() {
        _uiState.update { currentState ->
            currentState.copy(
                routes = _allRoutes.value,
                isLoading = false,
                error = null
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        val routesToDisplay = if (query.isBlank()) {
            _allRoutes.value
        } else {
            val lowercaseQuery = query.lowercase()
            _allRoutes.value.filter {
                it.routeNumber.lowercase().contains(lowercaseQuery) ||
                        it.name.lowercase().contains(lowercaseQuery)
            }
        }
        _uiState.update { currentState ->
            currentState.copy(
                routes = routesToDisplay,
            )
        }
    }

    fun getRouteById(routeId: String?): BusRoute? {
        if (routeId == null) return null
        return _allRoutes.value.find { it.id == routeId }
    }

    fun addFavoriteTime(schedule: BusSchedule) {
        viewModelScope.launch {
            val favoriteTimeEntity = FavoriteTimeEntity(
                id = schedule.id,
                routeId = schedule.routeId,
                departureTime = schedule.departureTime,
                stopName = schedule.stopName,
                departurePoint = schedule.departurePoint,
                dayOfWeek = schedule.dayOfWeek,
                isActive = true
            )
            favoriteTimeDao.addFavoriteTime(favoriteTimeEntity)

            val route = getRouteById(schedule.routeId)
            val favoriteForScheduler = FavoriteTime(
                id = schedule.id,
                routeId = schedule.routeId,
                routeNumber = route?.routeNumber ?: "N/A",
                routeName = route?.name ?: "Маршрут",
                stopName = schedule.stopName,
                departureTime = schedule.departureTime,
                dayOfWeek = schedule.dayOfWeek,
                departurePoint = schedule.departurePoint,
                isActive = true
            )
            try {
                AlarmScheduler.scheduleAlarm(getApplication(), favoriteForScheduler)
            } catch (e: Exception) {
                Log.e("BusViewModel", "Error scheduling alarm for new favorite: ${schedule.id}", e)
            }
        }
    }

    fun removeFavoriteTime(scheduleId: String) {
        viewModelScope.launch {
            favoriteTimeDao.removeFavoriteTime(scheduleId)
            try {
                AlarmScheduler.cancelAlarm(getApplication(), scheduleId)
            } catch (e: Exception) {
                Log.e("BusViewModel", "Error cancelling alarm for removed favorite: $scheduleId", e)
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
                        AlarmScheduler.scheduleAlarm(getApplication(), updatedFavoriteForScheduler)
                    } catch (e: Exception) {
                        Log.e("BusViewModel", "Error rescheduling alarm for favorite: ${favoriteTime.id}", e)
                    }
                } else {
                    Log.d("BusViewModel", "FavoriteTime ${favoriteTime.id} is already active.")
                }
            }
        }
    }

    fun isFavoriteTime(scheduleId: String): Boolean {
        return favoriteTimes.value.any { it.id == scheduleId }
    }
}