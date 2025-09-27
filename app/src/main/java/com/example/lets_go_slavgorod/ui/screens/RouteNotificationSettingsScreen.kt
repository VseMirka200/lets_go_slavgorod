package com.example.lets_go_slavgorod.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.ui.viewmodel.NotificationMode
import com.example.lets_go_slavgorod.ui.viewmodel.NotificationSettingsViewModel
import java.time.DayOfWeek

/**
 * Экран настроек уведомлений для конкретного маршрута
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteNotificationSettingsScreen(
    route: BusRoute,
    notificationSettingsViewModel: NotificationSettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentNotificationMode by notificationSettingsViewModel.getRouteNotificationMode(route.id).collectAsState()
    val selectedDays by notificationSettingsViewModel.getRouteSelectedDays(route.id).collectAsState()
    
    var showModeDropdown by remember { mutableStateOf(false) }
    var showDaysDropdown by remember { mutableStateOf(false) }
    
    val notificationModeOptions = arrayOf(
        NotificationMode.ALL_DAYS,
        NotificationMode.WEEKDAYS,
        NotificationMode.SELECTED_DAYS,
        NotificationMode.DISABLED
    )
    
    val dayOptions = DayOfWeek.values().toList()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Уведомления ${route.name}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Режим уведомлений
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Режим уведомлений",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = showModeDropdown,
                        onExpandedChange = { showModeDropdown = !showModeDropdown }
                    ) {
                        OutlinedTextField(
                            value = when (currentNotificationMode) {
                                NotificationMode.ALL_DAYS -> "Все дни"
                                NotificationMode.WEEKDAYS -> "Только будни"
                                NotificationMode.SELECTED_DAYS -> "Выбранные дни"
                                NotificationMode.DISABLED -> "Отключено"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Выберите режим") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showModeDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showModeDropdown,
                            onDismissRequest = { showModeDropdown = false }
                        ) {
                            notificationModeOptions.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (mode) {
                                                NotificationMode.ALL_DAYS -> "Все дни"
                                                NotificationMode.WEEKDAYS -> "Только будни"
                                                NotificationMode.SELECTED_DAYS -> "Выбранные дни"
                                                NotificationMode.DISABLED -> "Отключено"
                                            }
                                        )
                                    },
                                    onClick = {
                                        notificationSettingsViewModel.setRouteNotificationMode(route.id, mode)
                                        showModeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Выбор дней (только если выбран режим "Выбранные дни")
            if (currentNotificationMode == NotificationMode.SELECTED_DAYS) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Дни недели",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        dayOptions.forEach { day ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (day) {
                                        DayOfWeek.MONDAY -> "Понедельник"
                                        DayOfWeek.TUESDAY -> "Вторник"
                                        DayOfWeek.WEDNESDAY -> "Среда"
                                        DayOfWeek.THURSDAY -> "Четверг"
                                        DayOfWeek.FRIDAY -> "Пятница"
                                        DayOfWeek.SATURDAY -> "Суббота"
                                        DayOfWeek.SUNDAY -> "Воскресенье"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Checkbox(
                                    checked = selectedDays.contains(day),
                                    onCheckedChange = { isChecked ->
                                        val newDays = if (isChecked) {
                                            selectedDays + day
                                        } else {
                                            selectedDays - day
                                        }
                                        notificationSettingsViewModel.setRouteSelectedDays(route.id, newDays)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Информация о маршруте
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Информация о маршруте",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Маршрут: ${route.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    route.travelTime?.let { time ->
                        Text(
                            text = "Время в пути: $time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    route.pricePrimary?.let { price ->
                        Text(
                            text = "Стоимость: $price",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
