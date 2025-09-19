package com.example.slavgorodbus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.slavgorodbus.data.model.BusRoute
// MaterialTheme уже импортируется через androidx.compose.material3.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusRouteCard(
    route: BusRoute,
    onRouteClick: (BusRoute) -> Unit,
    onInfoClick: (BusRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Извлекаем значения из MaterialTheme в начале функции
    val currentColorScheme = MaterialTheme.colorScheme
    val currentTypography = MaterialTheme.typography

    val primaryColorFromTheme = currentColorScheme.primary
    val surfaceVariantColor = currentColorScheme.surfaceVariant
    val onSurfaceVariantColor = currentColorScheme.onSurfaceVariant
    val titleLargeStyle = currentTypography.titleLarge
    val titleMediumStyle = currentTypography.titleMedium

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onRouteClick(route) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceVariantColor // Используем извлеченное значение
        )
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 2. Вычисляем цвет фона, передавая извлеченный primaryColorFromTheme
                val boxBackgroundColor = remember(route.color, primaryColorFromTheme) {
                    try {
                        Color(route.color.toColorInt()).copy(alpha = 0.9f)
                    } catch (_: IllegalArgumentException) {
                        primaryColorFromTheme.copy(alpha = 0.9f) // Теперь используем локальную переменную
                    }
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(boxBackgroundColor), // Используем вычисленный цвет
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = route.routeNumber,
                        color = Color.White,
                        style = titleLargeStyle.copy( // Используем извлеченный стиль
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = route.name,
                    style = titleMediumStyle, // Используем извлеченный стиль
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariantColor, // Используем извлеченное значение
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            IconButton(onClick = { onInfoClick(route) }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Подробная информация о маршруте ${route.name}",
                    tint = primaryColorFromTheme // Используем извлеченное значение
                )
            }
        }
    }
}