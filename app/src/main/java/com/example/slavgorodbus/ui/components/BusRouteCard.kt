package com.example.slavgorodbus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.slavgorodbus.data.model.BusRoute
import com.example.slavgorodbus.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusRouteCard(
    route: BusRoute,
    onRouteClick: (BusRoute) -> Unit,
    onInfoClick: (BusRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    // Оптимизация: кэшируем вычисления цвета
    val primaryColor = MaterialTheme.colorScheme.primary
    val boxBackgroundColor = remember(route.color, primaryColor) {
        try {
            Color(route.color.toColorInt()).copy(alpha = Constants.COLOR_ALPHA)
        } catch (_: IllegalArgumentException) {
            primaryColor.copy(alpha = Constants.COLOR_ALPHA)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Constants.PADDING_MEDIUM.dp, vertical = Constants.PADDING_SMALL.dp)
            .clickable { onRouteClick(route) },
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
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(Constants.PADDING_MEDIUM.dp))

                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = Constants.PADDING_SMALL.dp)
                )
            }

            IconButton(onClick = { onInfoClick(route) }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Подробная информация о маршруте ${route.name}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}