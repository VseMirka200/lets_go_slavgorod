package com.example.lets_go_slavgorod.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Компоненты для отображения различных состояний загрузки
 */

/**
 * Индикатор загрузки с текстом
 */
@Composable
fun LoadingIndicator(
    text: String = "Загрузка...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

/**
 * Индикатор загрузки для добавления в избранное
 */
@Composable
fun AddingToFavoritesIndicator(
    modifier: Modifier = Modifier
) {
    LoadingIndicator(
        text = "Добавляем в избранное...",
        modifier = modifier
    )
}

/**
 * Индикатор загрузки для удаления из избранного
 */
@Composable
fun RemovingFromFavoritesIndicator(
    modifier: Modifier = Modifier
) {
    LoadingIndicator(
        text = "Удаляем из избранного...",
        modifier = modifier
    )
}

/**
 * Индикатор загрузки для обновления данных
 */
@Composable
fun RefreshingDataIndicator(
    modifier: Modifier = Modifier
) {
    LoadingIndicator(
        text = "Обновляем данные...",
        modifier = modifier
    )
}

/**
 * Индикатор загрузки для проверки обновлений
 */
@Composable
fun CheckingUpdatesIndicator(
    modifier: Modifier = Modifier
) {
    LoadingIndicator(
        text = "Проверяем обновления...",
        modifier = modifier
    )
}

/**
 * Индикатор загрузки для синхронизации
 */
@Composable
fun SyncingIndicator(
    modifier: Modifier = Modifier
) {
    LoadingIndicator(
        text = "Синхронизируем данные...",
        modifier = modifier
    )
}
