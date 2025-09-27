package com.example.lets_go_slavgorod.ui.components.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lets_go_slavgorod.data.model.BusRoute

/**
 * Заголовок экрана расписания с информацией о маршруте
 * 
 * Функциональность:
 * - Отображение названия маршрута
 * - Кнопка "Назад"
 * - Информационная карточка с деталями маршрута
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleHeader(
    route: BusRoute?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = route?.name ?: "Расписание",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, "Назад")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

/**
 * Карточка с детальной информацией о маршруте
 */
@Composable
fun RouteDetailsSummaryCard(
    route: BusRoute,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            route.travelTime?.let { DetailRow("Время в пути:", it) }
            route.pricePrimary?.let { DetailRow("Стоимость:", it) }
            route.paymentMethods?.let { DetailRow("Способы оплаты:", it, allowMultiLineValue = false) }
            if (route.travelTime != null || route.pricePrimary != null || route.paymentMethods != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "Примечание: Указано время отправления от начальных/конечных остановок маршрута.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
        }
    }
}

/**
 * Строка деталей маршрута
 */
@Composable
private fun DetailRow(
    label: String, 
    value: String, 
    allowMultiLineValue: Boolean = true
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = if (allowMultiLineValue) Alignment.Top else Alignment.CenterVertically
    ) {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

