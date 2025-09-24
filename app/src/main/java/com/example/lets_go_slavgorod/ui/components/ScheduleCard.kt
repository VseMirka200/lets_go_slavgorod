package com.example.lets_go_slavgorod.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lets_go_slavgorod.data.model.BusSchedule

@Composable
fun ScheduleCard(
    schedule: BusSchedule,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    routeNumber: String? = null,
    routeName: String? = null,
    isNextUpcoming: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNextUpcoming) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNextUpcoming) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isNextUpcoming) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!routeNumber.isNullOrBlank() || !routeName.isNullOrBlank()) {
                    val routeText = listOfNotNull(routeNumber?.takeIf { it.isNotBlank() }, routeName?.takeIf { it.isNotBlank() })
                        .joinToString(" ")
                        .trim()
                    if (routeText.isNotEmpty()) {
                        Text(
                            text = routeText,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isNextUpcoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            fontSize = 15.sp
                        )
                    }
                }

                Text(
                    text = "Отправление в ${schedule.departureTime}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isNextUpcoming) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 18.sp
                    ),
                    color = if (isNextUpcoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (isNextUpcoming) {
                    Text(
                        text = "Ближайший рейс",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                schedule.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Text(
                            text = "Примечание: $notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = (if (isNextUpcoming) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}