package com.example.lets_go_slavgorod.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Компоненты для отображения различных состояний ошибок
 */

/**
 * Общий компонент ошибки
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
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
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Ошибка",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Произошла ошибка",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            onRetry?.let { retryAction ->
                Button(
                    onClick = retryAction,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Повторить")
                }
            }
        }
    }
}

/**
 * Состояние отсутствия сети
 */
@Composable
fun NoNetworkState(
    onRetry: (() -> Unit)? = null,
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
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "Нет сети",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Нет интернет-соединения",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Проверьте подключение к интернету и попробуйте снова",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            onRetry?.let { retryAction ->
                Button(
                    onClick = retryAction,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Повторить")
                }
            }
        }
    }
}

/**
 * Состояние ошибки загрузки данных
 */
@Composable
fun DataLoadingErrorState(
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ErrorState(
        message = "Не удалось загрузить данные. Проверьте соединение и попробуйте снова.",
        onRetry = onRetry,
        modifier = modifier
    )
}

/**
 * Состояние ошибки добавления в избранное
 */
@Composable
fun AddToFavoritesErrorState(
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ErrorState(
        message = "Не удалось добавить в избранное. Попробуйте снова.",
        onRetry = onRetry,
        modifier = modifier
    )
}

/**
 * Состояние ошибки удаления из избранного
 */
@Composable
fun RemoveFromFavoritesErrorState(
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ErrorState(
        message = "Не удалось удалить из избранного. Попробуйте снова.",
        onRetry = onRetry,
        modifier = modifier
    )
}

/**
 * Состояние ошибки проверки обновлений
 */
@Composable
fun UpdateCheckErrorState(
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ErrorState(
        message = "Не удалось проверить обновления. Проверьте соединение и попробуйте снова.",
        onRetry = onRetry,
        modifier = modifier
    )
}
