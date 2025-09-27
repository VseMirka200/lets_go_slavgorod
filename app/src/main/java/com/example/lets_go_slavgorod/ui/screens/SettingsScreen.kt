package com.example.lets_go_slavgorod.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lets_go_slavgorod.R
import com.example.lets_go_slavgorod.ui.components.SettingsSwipeableContainer
import com.example.lets_go_slavgorod.ui.navigation.Screen
import com.example.lets_go_slavgorod.ui.viewmodel.AppTheme
import com.example.lets_go_slavgorod.ui.viewmodel.ThemeViewModel
import com.example.lets_go_slavgorod.ui.viewmodel.UpdateMode
import com.example.lets_go_slavgorod.ui.viewmodel.UpdateSettingsViewModel

/**
 * Экран настроек приложения
 * 
 * Содержит настройки для:
 * - Темы приложения (светлая/темная/системная)
 * - Обновлений (автоматические/ручные/отключено)
 * - О программе (информация о приложении, ссылки, обратная связь, поддержка)
 * 
 * @param modifier модификатор для настройки внешнего вида
 * @param themeViewModel ViewModel для управления темой приложения
 * @param updateSettingsViewModel ViewModel для настроек обновлений (опционально)
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = viewModel(),
    updateSettingsViewModel: UpdateSettingsViewModel? = null,
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
    

    SettingsSwipeableContainer(
        onSwipeToNext = {
            // Свайп влево - переход к избранному
            Log.d("SettingsScreen", "Swipe left detected, navigating to FavoriteTimes")
            if (navController != null) {
                try {
                    navController.navigate(Screen.FavoriteTimes.route)
                    Log.d("SettingsScreen", "Navigation to FavoriteTimes completed")
                } catch (e: Exception) {
                    Log.e("SettingsScreen", "Navigation to FavoriteTimes failed", e)
                }
            } else {
                Log.e("SettingsScreen", "navController is null, cannot navigate")
            }
        },
        onSwipeToPrevious = {
            // Свайп вправо - переход к маршрутам
            Log.d("SettingsScreen", "Swipe right detected, navigating to Home")
            if (navController != null) {
                try {
                    navController.navigate(Screen.Home.route)
                    Log.d("SettingsScreen", "Navigation to Home completed")
                } catch (e: Exception) {
                    Log.e("SettingsScreen", "Navigation to Home failed", e)
                }
            } else {
                Log.e("SettingsScreen", "navController is null, cannot navigate")
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
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
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
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
                    if (navController != null) {
                        // Открываем ссылку в WebView внутри приложения
                        val route = Screen.WebView.createRoute(url, "Скачать обновление")
                        navController.navigate(route)
                    } else {
                        // Fallback: открываем в браузере
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                            url.toUri())
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // Раздел "О программе" - кнопка для перехода к отдельному экрану
            Text(
                text = stringResource(R.string.settings_section_about_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            AboutNavigationCard(
                navController = navController
            )
        }
    }
    }
}


/**
 * Карточка настроек темы приложения
 * 
 * Позволяет пользователю выбрать тему:
 * - Системная (следует настройкам устройства)
 * - Светлая
 * - Темная
 * 
 * @param currentAppTheme текущая выбранная тема
 * @param showThemeDropdown флаг отображения выпадающего меню
 * @param onShowThemeDropdownChange callback для изменения состояния меню
 * @param themeOptions доступные варианты тем
 * @param onThemeSelected callback для выбора темы
 */
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
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
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
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
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
 * Диалог предложения обновления приложения
 * 
 * Показывается пользователю при обнаружении новой версии приложения.
 * Предлагает скачать обновление или отложить это действие.
 * 
 * @param versionName версия доступного обновления
 * @param releaseNotes описание изменений в обновлении
 * @param downloadUrl ссылка для скачивания обновления
 * @param onDismissRequest callback для закрытия диалога без действий
 * @param onDownload callback для скачивания обновления
 * @param onLater callback для отложения обновления
 */
@Composable
fun UpdateAvailableDialog(
    versionName: String,
    releaseNotes: String?,
    downloadUrl: String,
    onDismissRequest: () -> Unit,
    onDownload: (String) -> Unit,
    onLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Update,
                    contentDescription = "Обновление",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Доступно обновление",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Доступна новая версия приложения:",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Версия $versionName",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        if (!releaseNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Что нового:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = releaseNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Text(
                    text = "Рекомендуем обновиться для получения новых функций и исправлений.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDownload(downloadUrl) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Update,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Обновиться")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onLater
            ) {
                Text("Позже")
            }
        }
    )
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
            val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
            dateFormat.format(java.util.Date(timestamp))
        }
    }
}

/**
 * Карточка навигации к экрану "О программе"
 */
@Composable
private fun AboutNavigationCard(
    navController: NavController?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController?.navigate(Screen.About.route)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Перейти к разделу О программе",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}