package com.example.slavgorodbus.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.slavgorodbus.data.model.BusRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusRouteCard(
    route: BusRoute,
    onRouteClick: (BusRoute) -> Unit,
    onInfoClick: (BusRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(100),
        label = "card_scale"
    )
    
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
            .scale(scale)
            .clickable { onRouteClick(route) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceVariantColor
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
                val boxBackgroundColor = remember(route.color, primaryColorFromTheme) {
                    try {
                        Color(route.color.toColorInt()).copy(alpha = 0.9f)
                    } catch (_: IllegalArgumentException) {
                        primaryColorFromTheme.copy(alpha = 0.9f)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(boxBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = route.routeNumber,
                        color = Color.White,
                        style = titleLargeStyle.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = route.name,
                    style = titleMediumStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariantColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            IconButton(onClick = { onInfoClick(route) }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Подробная информация о маршруте ${route.name}",
                    tint = primaryColorFromTheme
                )
            }
        }
    }
}