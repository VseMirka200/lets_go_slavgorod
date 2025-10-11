package com.example.lets_go_slavgorod.ui.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

val Context.displayDataStore: DataStore<Preferences> by preferencesDataStore(name = "display_settings")

enum class RouteDisplayMode {
    GRID,    // Сетка
    LIST     // Список
}

class DisplaySettingsViewModel(
    private val context: Context
) : ViewModel() {

    private val _displayMode = MutableStateFlow(RouteDisplayMode.GRID)
    val displayMode: Flow<RouteDisplayMode> = _displayMode.asStateFlow()
    
    private val _gridColumns = MutableStateFlow(2)
    val gridColumns: Flow<Int> = _gridColumns.asStateFlow()

    init {
        loadDisplaySettings()
    }

    private fun loadDisplaySettings() {
        viewModelScope.launch {
            context.displayDataStore.data.collect { preferences ->
                val isGridMode = preferences[DISPLAY_MODE_GRID] ?: true
                _displayMode.value = if (isGridMode) RouteDisplayMode.GRID else RouteDisplayMode.LIST
                
                val columns = preferences[GRID_COLUMNS] ?: 2
                _gridColumns.value = columns.coerceIn(1, 4) // Ограничиваем от 1 до 4 колонок
            }
        }
    }

    fun setDisplayMode(mode: RouteDisplayMode) {
        viewModelScope.launch {
            _displayMode.value = mode
            context.displayDataStore.edit { preferences ->
                preferences[DISPLAY_MODE_GRID] = (mode == RouteDisplayMode.GRID)
            }
        }
    }
    
    fun setGridColumns(columns: Int) {
        viewModelScope.launch {
            val validColumns = columns.coerceIn(1, 4)
            _gridColumns.value = validColumns
            context.displayDataStore.edit { preferences ->
                preferences[GRID_COLUMNS] = validColumns
            }
        }
    }

    companion object {
        private val DISPLAY_MODE_GRID = booleanPreferencesKey("display_mode_grid")
        private val GRID_COLUMNS = intPreferencesKey("grid_columns")
    }
}
