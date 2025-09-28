package com.example.lets_go_slavgorod.ui.viewmodel

// Android системные импорты
import android.annotation.SuppressLint
import android.app.Application
import timber.log.Timber

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
 * Состояние UI для экрана с маршрутами автобусов
 * 
 * Оптимизированное состояние для эффективного управления UI:
 * - Минимальные перекомпозиции через неизменяемые данные
 * - Четкое разделение состояний загрузки и ошибок
 * - Флаги для отслеживания асинхронных операций
 * 
 * @param routes список доступных маршрутов автобусов
 * @param isLoading флаг загрузки данных (показывает индикатор загрузки)
 * @param error сообщение об ошибке (если есть, блокирует UI)
 * @param isAddingFavorite флаг добавления в избранное (показывает прогресс)
 * @param isRemovingFavorite флаг удаления из избранного (показывает прогресс)
 * 
 * @author VseMirka200
 * @version 1.2
 * @since 1.0
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
 * Оптимизированный ViewModel для максимальной производительности:
 * - Кэширование данных для быстрого доступа
 * - Асинхронная загрузка без блокировки UI
 * - Эффективное управление состоянием через StateFlow
 * - Интеграция с Room базой данных и системой уведомлений
 * 
 * Основные функции:
 * - Загрузка и поиск маршрутов автобусов
 * - Управление избранными временами отправления
 * - Планирование уведомлений для избранных времен
 * - Валидация данных и обработка ошибок
 * 
 * Оптимизации производительности:
 * - Локальное кэширование маршрутов и избранных времен
 * - Минимизация запросов к базе данных
 * - Эффективные StateFlow с SharingStarted.WhileSubscribed
 * - Асинхронная обработка всех операций
 * 
 * @param application контекст приложения для доступа к базе данных
 * 
 * @author VseMirka200
 * @version 1.2
 * @since 1.0
 */
class BusViewModel(application: Application) : AndroidViewModel(application) {

    // =====================================================================================
    //                              СОСТОЯНИЕ UI
    // =====================================================================================
    
    /** Текущее состояние UI с маршрутами */
    private val _uiState = MutableStateFlow(BusUiState(isLoading = true))
    val uiState: StateFlow<BusUiState> = _uiState.asStateFlow()
    
    /** Поисковый запрос пользователя */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // =====================================================================================
    //                              КЭШИРОВАНИЕ ДАННЫХ
    // =====================================================================================
    
    /** Кэш маршрутов для быстрого доступа */
    private var cachedRoutes: List<BusRoute> = emptyList()
    
    /** Кэш избранных времен для оптимизации */
    private var cachedFavoriteTimes: List<FavoriteTime> = emptyList()

    // =====================================================================================
    //                              РЕПОЗИТОРИИ И DAO
    // =====================================================================================
    
    /** DAO для работы с избранными временами */
    private val favoriteTimeDao = AppDatabase.getDatabase(application).favoriteTimeDao()
    
    /** Репозиторий для работы с маршрутами */
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

    /**
     * Загружает начальные маршруты с оптимизацией производительности
     * 
     * Оптимизации:
     * - Использует кэшированные данные при наличии
     * - Минимизирует количество обновлений UI
     * - Обрабатывает ошибки gracefully
     * - Избегает ненужных повторных загрузок
     */
    private fun loadInitialRoutes() {
        Timber.d("Starting to load initial routes")
        
        // Оптимизация: используем кэш если доступен
        if (cachedRoutes.isNotEmpty()) {
            Timber.d("Using cached routes: ${cachedRoutes.size} routes")
            _uiState.update { currentState ->
                currentState.copy(
                    routes = cachedRoutes,
                    isLoading = false,
                    error = null
                )
            }
            return
        }
        
        // Загружаем маршруты из репозитория
        val routes = routeRepository.getAllRoutes()
        Timber.d("Loading routes: ${routes.size} routes found")
        
        // Кэшируем маршруты для последующих обращений
        cachedRoutes = routes

        if (routes.isEmpty()) {
            Timber.w("No routes found! Repository may not be initialized yet.")
            // Попробуем загрузить еще раз через небольшую задержку
            viewModelScope.launch {
                kotlinx.coroutines.delay(100)
                val retryRoutes = routeRepository.getAllRoutes()
                Timber.d("Retry loading routes: ${retryRoutes.size} routes found")
                cachedRoutes = retryRoutes
                _uiState.update { currentState ->
                    currentState.copy(
                        routes = retryRoutes,
                        isLoading = false,
                        error = if (retryRoutes.isEmpty()) "Маршруты не найдены" else null
                    )
                }
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    routes = routes,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    /**
     * Обрабатывает изменение поискового запроса с оптимизацией
     * 
     * Оптимизации:
     * - Использует кэшированные данные для быстрого поиска
     * - Минимизирует обращения к репозиторию
     * - Обеспечивает мгновенный отклик на ввод пользователя
     * 
     * @param query поисковый запрос пользователя
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        
        // Оптимизация: используем кэшированные маршруты для поиска
        val routesToDisplay = if (cachedRoutes.isNotEmpty()) {
            cachedRoutes.filter { route ->
                route.name.contains(query, ignoreCase = true) ||
                route.routeNumber.contains(query, ignoreCase = true)
            }
        } else {
            routeRepository.searchRoutes(query)
        }
        
        _uiState.update { currentState ->
            currentState.copy(
                routes = routesToDisplay,
                isLoading = false,
                error = null
            )
        }
    }

    fun getRouteById(routeId: String?): BusRoute? {
        return routeRepository.getRouteById(routeId)
    }

    @SuppressLint("TimberArgCount")
    fun addFavoriteTime(schedule: BusSchedule) {
        viewModelScope.launch {
            try {
                _uiState.update { currentState ->
                    currentState.copy(isAddingFavorite = true, error = null)
                }
                
                // Валидация данных перед добавлением
                val sanitizedSchedule = schedule.sanitized()
                if (!sanitizedSchedule.isValid()) {
                    Timber.e("BusViewModel", "Invalid schedule data, cannot add to favorites: ${schedule.id}")
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
                Timber.d("Successfully added favorite time: ${sanitizedSchedule.id}")
                
                _uiState.update { currentState ->
                    currentState.copy(isAddingFavorite = false, error = null)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "BusViewModel: Error adding favorite time: ${schedule.id}")
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
                
                Timber.d("Successfully removed favorite time: $scheduleId")
                
                _uiState.update { currentState ->
                    currentState.copy(isRemovingFavorite = false, error = null)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "BusViewModel: Error removing favorite time: $scheduleId")
                _uiState.update { currentState ->
                    currentState.copy(
                        isRemovingFavorite = false,
                        error = "Ошибка при удалении из избранного: ${e.message}"
                    )
                }
            }
        }
    }

    @SuppressLint("TimberArgCount")
    fun updateFavoriteActiveState(favoriteTime: FavoriteTime, newActiveState: Boolean) {
        viewModelScope.launch {
            val entityInDb = favoriteTimeDao.getFavoriteTimeById(favoriteTime.id).firstOrNull()
            if (entityInDb == null) {
                Timber.e("BusViewModel", "FavoriteTime with id ${favoriteTime.id} not found for update/removal.")
                return@launch
            }

            if (!newActiveState) {
                favoriteTimeDao.removeFavoriteTime(favoriteTime.id)
                Timber.d("FavoriteTime ${favoriteTime.id} removed due to newActiveState=false.")
                try {
                    AlarmScheduler.cancelAlarm(getApplication(), favoriteTime.id)
                } catch (e: Exception) {
                    Timber.e(e, "BusViewModel: Error cancelling alarm for favorite: ${favoriteTime.id}")
                }
            } else {
                if (!entityInDb.isActive) {
                    favoriteTimeDao.updateFavoriteTime(entityInDb.copy(isActive = true))
                    Timber.d("FavoriteTime ${favoriteTime.id} activated.")
                    val updatedFavoriteForScheduler = favoriteTime.copy(isActive = true)
                    try {
                        AlarmScheduler.checkAndUpdateNotifications(getApplication(), updatedFavoriteForScheduler)
                    } catch (e: Exception) {
                        Timber.e(e, "BusViewModel: Error rescheduling alarm for favorite: ${favoriteTime.id}")
                    }
                } else {
                    Timber.d("FavoriteTime ${favoriteTime.id} is already active.")
                }
            }
        }
    }
    

}