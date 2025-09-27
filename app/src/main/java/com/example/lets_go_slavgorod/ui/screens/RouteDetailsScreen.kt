package com.example.lets_go_slavgorod.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.lets_go_slavgorod.R
import com.example.lets_go_slavgorod.data.model.BusRoute
import com.example.lets_go_slavgorod.ui.components.SettingsSwipeableContainer
import com.example.lets_go_slavgorod.ui.navigation.Screen
import android.util.Log

@Composable
fun RouteNumberHeader(
    routeNumber: String,
    colorString: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    try {
                        Color(colorString.toColorInt())
                    } catch (_: IllegalArgumentException) {
                        MaterialTheme.colorScheme.primary
                    }.copy(alpha = 0.9f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = routeNumber,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String?, modifier: Modifier = Modifier) {
    if (!value.isNullOrBlank()) {
        Column(modifier = modifier.padding(bottom = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailsScreen(
    route: BusRoute?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    navController: androidx.navigation.NavController? = null
) {
    SettingsSwipeableContainer(
        onSwipeToNext = {
            // Свайп влево - переход к маршрутам
            Log.d("RouteDetailsScreen", "Swipe left detected, navigating to Home")
            if (navController != null) {
                try {
                    navController.navigate(Screen.Home.route)
                    Log.d("RouteDetailsScreen", "Navigation to Home completed")
                } catch (e: Exception) {
                    Log.e("RouteDetailsScreen", "Navigation to Home failed", e)
                }
            } else {
                Log.e("RouteDetailsScreen", "navController is null, cannot navigate")
            }
        },
        onSwipeToPrevious = {
            // Свайп вправо - переход к избранному
            Log.d("RouteDetailsScreen", "Swipe right detected, navigating to FavoriteTimes")
            if (navController != null) {
                try {
                    navController.navigate(Screen.FavoriteTimes.route)
                    Log.d("RouteDetailsScreen", "Navigation to FavoriteTimes completed")
                } catch (e: Exception) {
                    Log.e("RouteDetailsScreen", "Navigation to FavoriteTimes failed", e)
                }
            } else {
                Log.e("RouteDetailsScreen", "navController is null, cannot navigate")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            route?.name ?: stringResource(R.string.route_details_title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back_button_description)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = route?.color?.let {
                            try {
                                Color(it.toColorInt())
                            } catch (_: IllegalArgumentException) {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        } ?: MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            contentWindowInsets = WindowInsets(0),
            modifier = modifier
        ) { paddingValues ->
            if (route == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.route_info_not_found))
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                RouteNumberHeader(
                    routeNumber = route.routeNumber,
                    colorString = route.color,
                )
                Spacer(modifier = Modifier.height(24.dp))

                DetailItem(stringResource(R.string.detail_label_name), route.name)
                DetailItem(stringResource(R.string.detail_label_description), route.description)
                DetailItem(stringResource(R.string.detail_label_detailedPrice), route.pricePrimary)
                DetailItem(stringResource(R.string.detail_label_travel_time), route.travelTime)
                DetailItem(
                    stringResource(R.string.detail_label_price_paymentMethods),
                    route.paymentMethods
                )
            }
        }
    }
}