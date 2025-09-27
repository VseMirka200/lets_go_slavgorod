package com.example.lets_go_slavgorod.ui.components.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.lets_go_slavgorod.ui.theme.BusBlue
import com.example.lets_go_slavgorod.ui.theme.TransportGreen
import com.example.lets_go_slavgorod.ui.theme.TransportOrange
import com.example.lets_go_slavgorod.ui.theme.TransportRed
import com.example.lets_go_slavgorod.ui.theme.AccentTeal

/**
 * Современная карточка в транспортном стиле
 * 
 * Особенности:
 * - Улучшенные тени и скругления
 * - Транспортные цвета
 * - Адаптивная высота
 * - Поддержка состояний
 */
@Composable
fun TransportCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: CardElevation = CardDefaults.cardElevation(
        defaultElevation = 4.dp,
        pressedElevation = 8.dp,
        hoveredElevation = 6.dp,
        focusedElevation = 6.dp,
        draggedElevation = 8.dp,
        disabledElevation = 0.dp
    ),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    shape: Shape = MaterialTheme.shapes.medium,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        elevation = elevation,
        colors = colors,
        shape = shape,
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Информационная карточка маршрута
 */
@Composable
fun RouteInfoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    TransportCard(
        modifier = modifier,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        content()
    }
}

/**
 * Карточка расписания с улучшенным дизайном
 */
@Composable
fun ScheduleInfoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isHighlighted: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    TransportCard(
        modifier = modifier,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 8.dp else 4.dp,
            pressedElevation = if (isHighlighted) 12.dp else 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface,
            contentColor = if (isHighlighted) 
                MaterialTheme.colorScheme.onSecondaryContainer 
            else 
                MaterialTheme.colorScheme.onSurface
        )
    ) {
        content()
    }
}

/**
 * Карточка статуса с цветовой индикацией
 */
@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    status: TransportStatus,
    content: @Composable RowScope.() -> Unit
) {
    val statusColor = when (status) {
        TransportStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        TransportStatus.WARNING -> MaterialTheme.colorScheme.tertiary
        TransportStatus.ERROR -> MaterialTheme.colorScheme.error
        TransportStatus.INFO -> MaterialTheme.colorScheme.secondary
    }
    
    val statusContainerColor = when (status) {
        TransportStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        TransportStatus.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        TransportStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
        TransportStatus.INFO -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    TransportCard(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusContainerColor,
            contentColor = statusColor
        )
    ) {
        content()
    }
}

/**
 * Статусы транспорта
 */
enum class TransportStatus {
    SUCCESS,  // Успешно (зеленый)
    WARNING,  // Предупреждение (оранжевый)
    ERROR,    // Ошибка (красный)
    INFO      // Информация (синий)
}
