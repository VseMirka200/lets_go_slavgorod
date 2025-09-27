package com.example.lets_go_slavgorod.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lets_go_slavgorod.ui.screens.UpdateAvailableDialog

/**
 * Менеджер для показа диалога обновления приложения
 * 
 * Отвечает за:
 * - Управление состоянием показа диалога обновления
 * - Отображение диалога с информацией о новой версии
 * - Обработку действий пользователя (скачать/отложить)
 * 
 * @author VseMirka200
 * @version 1.0
 */
@Composable
fun UpdateDialogManager(
    availableUpdateVersion: String?,
    availableUpdateUrl: String?,
    availableUpdateNotes: String?,
    onDownloadUpdate: (String) -> Unit,
    onClearAvailableUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    // Показываем диалог, если есть доступное обновление
    LaunchedEffect(availableUpdateVersion, availableUpdateUrl) {
        if (!availableUpdateVersion.isNullOrBlank() && !availableUpdateUrl.isNullOrBlank()) {
            showUpdateDialog = true
        }
    }
    
    // Диалог обновления
    if (showUpdateDialog && !availableUpdateVersion.isNullOrBlank() && !availableUpdateUrl.isNullOrBlank()) {
        UpdateAvailableDialog(
            versionName = availableUpdateVersion,
            releaseNotes = availableUpdateNotes,
            downloadUrl = availableUpdateUrl,
            onDismissRequest = {
                showUpdateDialog = false
                onClearAvailableUpdate()
            },
            onDownload = { url ->
                showUpdateDialog = false
                onDownloadUpdate(url)
                onClearAvailableUpdate()
            },
            onLater = {
                showUpdateDialog = false
                onClearAvailableUpdate()
            }
        )
    }
}
