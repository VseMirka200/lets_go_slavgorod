package com.example.slavgorodbus.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slavgorodbus.R
import com.example.slavgorodbus.ui.viewmodel.AppTheme
import com.example.slavgorodbus.ui.viewmodel.NotificationMode
import com.example.slavgorodbus.ui.viewmodel.NotificationSettingsViewModel
import com.example.slavgorodbus.ui.viewmodel.ThemeViewModel
import com.example.slavgorodbus.ui.viewmodel.UpdateSettingsViewModel
import com.example.slavgorodbus.ui.viewmodel.UpdateMode
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = viewModel(),
    notificationSettingsViewModel: NotificationSettingsViewModel = viewModel(),
    updateSettingsViewModel: UpdateSettingsViewModel? = null,
    onNavigateToAbout: () -> Unit,
) {
    val context = LocalContext.current
    val updateSettingsVM = updateSettingsViewModel ?: viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return UpdateSettingsViewModel(context) as T
            }
        }
    )
    val currentAppTheme by themeViewModel.currentTheme.collectAsState()
    var showThemeDropdown by remember { mutableStateOf(false) }
    val themeOptions = remember { AppTheme.entries.toTypedArray() }

    val currentNotificationMode by notificationSettingsViewModel.currentNotificationMode.collectAsState()
    var showNotificationModeDropdown by remember { mutableStateOf(false) }
    val notificationModeOptions = remember { NotificationMode.entries.toTypedArray() }

    var showSelectDaysDialog by remember { mutableStateOf(false) }
    val selectedDaysFromVM by notificationSettingsViewModel.selectedNotificationDays.collectAsState()
    
    // Update settings state
    val currentUpdateMode by updateSettingsVM.currentUpdateMode.collectAsState(initial = UpdateMode.AUTOMATIC)
    var showUpdateModeDropdown by remember { mutableStateOf(false) }
    val updateModeOptions = remember { UpdateMode.entries.toTypedArray() }
            val isCheckingUpdates by updateSettingsVM.isCheckingUpdates.collectAsState(initial = false)
            val updateCheckError by updateSettingsVM.updateCheckError.collectAsState(initial = null)
            val updateCheckStatus by updateSettingsVM.updateCheckStatus.collectAsState(initial = null)
            val lastUpdateCheckTime by updateSettingsVM.lastUpdateCheckTime.collectAsState(initial = 0L)
            val availableUpdateVersion by updateSettingsVM.availableUpdateVersion.collectAsState(initial = null)
            val availableUpdateUrl by updateSettingsVM.availableUpdateUrl.collectAsState(initial = null)
            val availableUpdateNotes by updateSettingsVM.availableUpdateNotes.collectAsState(initial = null)


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_screen_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.settings_section_theme_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ThemeSettingsCard(
                currentAppTheme = currentAppTheme,
                showThemeDropdown = showThemeDropdown,
                onShowThemeDropdownChange = { showThemeDropdown = it },
                themeOptions = themeOptions,
                onThemeSelected = { theme ->
                    themeViewModel.setTheme(theme)
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_section_notifications_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            NotificationSettingsCard(
                currentNotificationMode = currentNotificationMode,
                showNotificationModeDropdown = showNotificationModeDropdown,
                onShowNotificationModeDropdownChange = { showNotificationModeDropdown = it },
                notificationModeOptions = notificationModeOptions,
                onNotificationModeSelected = { mode ->
                    if (mode == NotificationMode.SELECTED_DAYS) {
                        showSelectDaysDialog = true
                    } else {
                        notificationSettingsViewModel.setNotificationMode(mode)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Обновления",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            UpdateSettingsCard(
                currentUpdateMode = currentUpdateMode,
                showUpdateModeDropdown = showUpdateModeDropdown,
                onShowUpdateModeDropdownChange = { showUpdateModeDropdown = it },
                updateModeOptions = updateModeOptions,
                onUpdateModeSelected = { mode ->
                    updateSettingsVM.setUpdateMode(mode)
                },
                isCheckingUpdates = isCheckingUpdates,
                updateCheckError = updateCheckError,
                updateCheckStatus = updateCheckStatus,
                lastUpdateCheckTime = lastUpdateCheckTime,
                availableUpdateVersion = availableUpdateVersion,
                availableUpdateUrl = availableUpdateUrl,
                availableUpdateNotes = availableUpdateNotes,
                onCheckForUpdates = {
                    updateSettingsVM.checkForUpdates()
                },
                onClearAvailableUpdate = {
                    updateSettingsVM.clearAvailableUpdate()
                },
                onClearUpdateStatus = {
                    updateSettingsVM.clearUpdateCheckStatus()
                },
                onDownloadUpdate = { url ->
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                        url.toUri())
                    context.startActivity(intent)
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_section_about_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AboutSettingsCard(
                onNavigateToAbout = onNavigateToAbout
            )
        }
    }


    if (showSelectDaysDialog) {
        SelectDaysDialog(
            currentlySelectedDays = selectedDaysFromVM,
            onDismissRequest = { showSelectDaysDialog = false },
            onConfirm = { newSelectedDays ->
                showSelectDaysDialog = false
                notificationSettingsViewModel.setSelectedNotificationDays(newSelectedDays)
                Log.d("SettingsScreen", "Selected days confirmed: $newSelectedDays")
            }
        )
    }

}

@Composable
fun ThemeSettingsCard(
    currentAppTheme: AppTheme,
    showThemeDropdown: Boolean,
    onShowThemeDropdownChange: (Boolean) -> Unit,
    themeOptions: Array<AppTheme>,
    onThemeSelected: (AppTheme) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowThemeDropdownChange(true) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = stringResource(R.string.settings_appearance_icon_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.settings_appearance_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(
                            when (currentAppTheme) {
                                AppTheme.SYSTEM -> R.string.theme_system
                                AppTheme.LIGHT -> R.string.theme_light
                                AppTheme.DARK -> R.string.theme_dark
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = stringResource(R.string.settings_select_theme_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = showThemeDropdown,
                onDismissRequest = { onShowThemeDropdownChange(false) },
            ) {
                themeOptions.forEach { theme ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    when (theme) {
                                        AppTheme.SYSTEM -> R.string.theme_system
                                        AppTheme.LIGHT -> R.string.theme_light
                                        AppTheme.DARK -> R.string.theme_dark
                                    }
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onThemeSelected(theme)
                            onShowThemeDropdownChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsCard(
    currentNotificationMode: NotificationMode,
    showNotificationModeDropdown: Boolean,
    onShowNotificationModeDropdownChange: (Boolean) -> Unit,
    notificationModeOptions: Array<NotificationMode>,
    onNotificationModeSelected: (NotificationMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowNotificationModeDropdownChange(true) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.settings_notification_icon_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.settings_notification_mode_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(
                            when (currentNotificationMode) {
                                NotificationMode.WEEKDAYS -> R.string.notification_mode_weekdays
                                NotificationMode.ALL_DAYS -> R.string.notification_mode_all_days
                                NotificationMode.SELECTED_DAYS -> R.string.notification_mode_selected_days
                                NotificationMode.DISABLED -> R.string.notification_mode_disabled
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = stringResource(R.string.settings_select_notification_mode_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = showNotificationModeDropdown,
                onDismissRequest = { onShowNotificationModeDropdownChange(false) },
            ) {
                notificationModeOptions.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    when (mode) {
                                        NotificationMode.WEEKDAYS -> R.string.notification_mode_weekdays
                                        NotificationMode.ALL_DAYS -> R.string.notification_mode_all_days
                                        NotificationMode.SELECTED_DAYS -> R.string.notification_mode_selected_days
                                        NotificationMode.DISABLED -> R.string.notification_mode_disabled
                                    }
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onNotificationModeSelected(mode)
                            onShowNotificationModeDropdownChange(false)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun AboutSettingsCard(
    onNavigateToAbout: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                Log.d("SettingsScreen", "AboutSettingsCard clicked")
                onNavigateToAbout() 
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = stringResource(R.string.settings_about_icon_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.about_screen_title),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectDaysDialog(
    currentlySelectedDays: Set<DayOfWeek>,
    onDismissRequest: () -> Unit,
    onConfirm: (Set<DayOfWeek>) -> Unit,
) {
    val daysOfWeek = remember { DayOfWeek.entries.toList() }
    val tempSelectedDays = remember { mutableStateOf(currentlySelectedDays) }

    LaunchedEffect(currentlySelectedDays) {
        tempSelectedDays.value = currentlySelectedDays
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.dialog_select_days_title)) },
        text = {
            Column {
                daysOfWeek.forEach { day ->
                    val isChecked = tempSelectedDays.value.contains(day)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val currentSelection = tempSelectedDays.value.toMutableSet()
                                if (isChecked) {
                                    currentSelection.remove(day)
                                } else {
                                    currentSelection.add(day)
                                }
                                tempSelectedDays.value = currentSelection
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = day.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempSelectedDays.value) }) {
                Text(stringResource(R.string.dialog_button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

@Composable
fun UpdateSettingsCard(
    currentUpdateMode: UpdateMode,
    showUpdateModeDropdown: Boolean,
    onShowUpdateModeDropdownChange: (Boolean) -> Unit,
    updateModeOptions: Array<UpdateMode>,
    onUpdateModeSelected: (UpdateMode) -> Unit,
    isCheckingUpdates: Boolean,
    updateCheckError: String?,
    updateCheckStatus: String?,
    lastUpdateCheckTime: Long,
    availableUpdateVersion: String?,
    availableUpdateUrl: String?,
    availableUpdateNotes: String?,
    onCheckForUpdates: () -> Unit,
    onClearAvailableUpdate: () -> Unit,
    onClearUpdateStatus: () -> Unit,
    onDownloadUpdate: (String) -> Unit,
) {
    Column {
        // Основная карточка с выпадающим меню режима обновлений
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowUpdateModeDropdownChange(true) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Update,
                            contentDescription = "Режим обновлений",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "Режим:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when (currentUpdateMode) {
                                UpdateMode.AUTOMATIC -> "Автоматически"
                                UpdateMode.MANUAL -> "Вручную"
                                UpdateMode.DISABLED -> "Отключено"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Выбрать режим обновлений",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = showUpdateModeDropdown,
                    onDismissRequest = { onShowUpdateModeDropdownChange(false) },
                ) {
                    updateModeOptions.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (mode) {
                                        UpdateMode.AUTOMATIC -> "Автоматически"
                                        UpdateMode.MANUAL -> "Вручную"
                                        UpdateMode.DISABLED -> "Отключено"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onUpdateModeSelected(mode)
                                onShowUpdateModeDropdownChange(false)
                            }
                        )
                    }
                }
            }
        }
        
        // Дополнительная карточка для ручной проверки (если режим не отключен)
        if (currentUpdateMode != UpdateMode.DISABLED) {
            Spacer(Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Проверка обновлений",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onCheckForUpdates,
                            enabled = !isCheckingUpdates,
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (isCheckingUpdates) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Проверяем...")
                            } else {
                                Text("Проверить")
                            }
                        }
                    }
                    
                    // Показываем время последней проверки, если есть
                    if (lastUpdateCheckTime > 0L) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Последняя проверка: ${formatLastCheckTime(lastUpdateCheckTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Показываем статус проверки обновлений
                    updateCheckStatus?.let { status ->
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (status.contains("последняя версия")) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (status.contains("последняя версия")) {
                                            Icons.Filled.CheckCircle
                                        } else {
                                            Icons.Filled.Update
                                        },
                                        contentDescription = "Статус обновления",
                                        tint = if (status.contains("последняя версия")) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.secondary
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (status.contains("последняя версия")) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                }
                                IconButton(
                                    onClick = onClearUpdateStatus,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Закрыть",
                                        tint = if (status.contains("последняя версия")) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Показываем ошибку, если есть
                    updateCheckError?.let { error ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    // Показываем доступное обновление, если есть
                    availableUpdateVersion?.let { version ->
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Доступно обновление $version",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                availableUpdateNotes?.let { notes ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                Spacer(Modifier.height(8.dp))
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            availableUpdateUrl?.let { url ->
                                                onDownloadUpdate(url)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Скачать")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = onClearAvailableUpdate,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Позже")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Форматирует время последней проверки обновлений в читаемый вид
 */
private fun formatLastCheckTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "только что" // менее минуты
        diff < 3600_000 -> "${diff / 60_000} мин. назад" // менее часа
        diff < 86400_000 -> "${diff / 3600_000} ч. назад" // менее суток
        else -> {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
