package com.example.lets_go_slavgorod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.utils.Constants

/**
 * Переиспользуемый компонент карточки автобусного маршрута
 * 
 * Отображает информацию о маршруте автобуса в виде интерактивной карточки.
 * Поддерживает два режима отображения: сетка (grid) и список (list).
 * 
 * Компонент включает в себя:
 * - Отображение номера маршрута с цветным фоном
 * - Название и описание маршрута
 * - Интерактивность (клик для перехода к деталям)
 * - Адаптивный дизайн под разные размеры экрана
 * - Поддержка Material Design 3
 * 
 * @author VseMirka200
 * @version 2.0
 * @since 1.0
 */

/**
 * Карточка маршрута для отображения в сетке (вертикальный дизайн)
 * 
 * Создает компактную вертикальную карточку с номером маршрута по центру,
 * названием сверху и описанием снизу. Оптимизирована для отображения
 * в сетке с ограниченной шириной.
 * 
 * @param route данные маршрута для отображения (не должно быть null)
 * @param onRouteClick callback-функция, вызываемая при клике на карточку маршрута
 * @param modifier модификатор для настройки внешнего вида и позиционирования
 * @param showNotificationButton флаг отображения кнопки настройки уведомлений
 * @param onNotificationClick callback-функция для обработки клика по кнопке уведомлений
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusRouteCardGrid(
    route: BusRoute,
    onRouteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showNotificationButton: Boolean = false,
    onNotificationClick: (() -> Unit)? = null
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
            .clickable(
                indication = null, // Отключаем анимацию для быстрого отклика
                interactionSource = interactionSource
            ) {
                onRouteClick()
            }
    }

    Card(
        modifier = cardModifier
            .height(Constants.ROUTE_CARD_HEIGHT_GRID.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = boxBackgroundColor.copy(alpha = 0.15f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Constants.ROUTE_CARD_HEIGHT_GRID.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.ROUTE_CARD_HEIGHT_GRID.dp)
                    .padding(Constants.ROUTE_CARD_PADDING_GRID.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Название автобуса сверху
                Text(
                    text = "АВТОБУС",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Номер маршрута с цветным фоном
                Box(
                    modifier = Modifier
                        .size(Constants.ROUTE_NUMBER_BOX_SIZE_GRID.dp)
                        .clip(RoundedCornerShape(Constants.ROUTE_NUMBER_BOX_CORNER_RADIUS_GRID.dp))
                        .background(boxBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = route.routeNumber,
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            
            // Кнопка уведомлений в правом верхнем углу (если showNotificationButton = true)
            if (showNotificationButton && onNotificationClick != null) {
                IconButton(
                    onClick = { onNotificationClick() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Настройки уведомлений",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Карточка маршрута для отображения в списке (горизонтальный дизайн)
 * 
 * Создает горизонтальную карточку с номером маршрута слева,
 * названием и описанием справа. Оптимизирована для отображения
 * в списке с полной шириной экрана.
 * 
 * @param route данные маршрута для отображения (не должно быть null)
 * @param onRouteClick callback-функция, вызываемая при клике на карточку маршрута
 * @param modifier модификатор для настройки внешнего вида и позиционирования
 * @param showNotificationButton флаг отображения кнопки настройки уведомлений
 * @param onNotificationClick callback-функция для обработки клика по кнопке уведомлений
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusRouteCardList(
    route: BusRoute,
    onRouteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showNotificationButton: Boolean = false,
    onNotificationClick: (() -> Unit)? = null
) {
    // Кэшируем все вычисления
    val primaryColor = MaterialTheme.colorScheme.primary
    val boxBackgroundColor = remember(route.color, primaryColor) {
        try {
            androidx.compose.ui.graphics.Color(route.color.toColorInt()).copy(alpha = Constants.COLOR_ALPHA)
        } catch (_: IllegalArgumentException) {
            primaryColor.copy(alpha = Constants.COLOR_ALPHA)
        }
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    
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
                indication = null,
                interactionSource = interactionSource
            ) {
                onRouteClick()
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
            
            // Кнопка уведомлений справа (если showNotificationButton = true)
            if (showNotificationButton && onNotificationClick != null) {
                IconButton(
                    onClick = { onNotificationClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Настройки уведомлений",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Универсальная карточка маршрута автобуса
 * Автоматически выбирает дизайн в зависимости от режима отображения
 * 
 * @param route данные маршрута для отображения
 * @param onClick callback при клике на маршрут (без параметров)
 * @param modifier модификатор для настройки внешнего вида
 * @param isGridMode режим отображения (true = сетка, false = список)
 * @param showNotificationButton показывать ли кнопку настройки уведомлений (для избранных)
 * @param onNotificationClick callback при клике на кнопку уведомлений
 */
@Composable
fun BusRouteCard(
    route: BusRoute,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGridMode: Boolean = true,
    showNotificationButton: Boolean = false,
    onNotificationClick: (() -> Unit)? = null
) {
    if (isGridMode) {
        BusRouteCardGrid(
            route = route, 
            onRouteClick = { onClick() },
            modifier = modifier,
            showNotificationButton = showNotificationButton,
            onNotificationClick = onNotificationClick
        )
    } else {
        BusRouteCardList(
            route = route, 
            onRouteClick = { onClick() },
            modifier = modifier,
            showNotificationButton = showNotificationButton,
            onNotificationClick = onNotificationClick
        )
    }
}