package com.example.lets_go_slavgorod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.utils.Constants

/**
 * Оптимизированная карточка маршрута автобуса
 * 
 * Высокопроизводительная карточка с агрессивными оптимизациями:
 * - Минимальные перекомпозиции через remember
 * - Кэширование вычислений цвета
 * - Оптимизированные модификаторы
 * - Быстрая обработка кликов
 * 
 * @param route данные маршрута для отображения
 * @param onRouteClick callback при клике на маршрут
 * @param modifier модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusRouteCard(
    route: BusRoute,
    onRouteClick: (BusRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    // Агрессивная оптимизация: кэшируем все вычисления
    val primaryColor = MaterialTheme.colorScheme.primary
    val boxBackgroundColor = remember(route.color, primaryColor) {
        try {
            androidx.compose.ui.graphics.Color(route.color.toColorInt()).copy(alpha = Constants.COLOR_ALPHA)
        } catch (_: IllegalArgumentException) {
            primaryColor.copy(alpha = Constants.COLOR_ALPHA)
        }
    }
    
    // Кэшируем InteractionSource отдельно
    val interactionSource = remember { MutableInteractionSource() }
    
    // Кэшируем модификаторы для избежания пересоздания
    val cardModifier = remember(route.id) {
        modifier
            .fillMaxWidth()
            .padding(
                start = Constants.PADDING_MEDIUM.dp,
                end = Constants.PADDING_MEDIUM.dp,
                top = Constants.PADDING_SMALL.dp,
                bottom = Constants.PADDING_SMALL.dp
            )
            .clickable(
                indication = null, // Отключаем анимацию для быстрого отклика
                interactionSource = interactionSource
            ) {
                onRouteClick(route)
            }
    }

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = Constants.CARD_ELEVATION.dp),
        shape = RoundedCornerShape(Constants.CARD_CORNER_RADIUS.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = Constants.PADDING_MEDIUM.dp,
                    end = Constants.PADDING_SMALL.dp,
                    top = Constants.PADDING_MEDIUM.dp,
                    bottom = Constants.PADDING_MEDIUM.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Constants.ROUTE_NUMBER_BOX_SIZE.dp)
                        .clip(RoundedCornerShape(Constants.ROUTE_NUMBER_BOX_CORNER_RADIUS.dp))
                        .background(boxBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = route.routeNumber,
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(Constants.PADDING_MEDIUM.dp))

                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = Constants.PADDING_SMALL.dp)
                )
            }
        }
    }
}