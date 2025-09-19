package com.example.slavgorodbus.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

object ComponentAnimations {
    
    // Анимация для появления карточек маршрутов
    val cardSlideIn = slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = 100,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = 100,
            easing = FastOutSlowInEasing
        )
    )
    
    // Анимация для исчезновения карточек
    val cardSlideOut = slideOutVertically(
        targetOffsetY = { it / 2 },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    
    // Анимация для поисковой строки
    val searchBarExpand = expandVertically(
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    
    val searchBarCollapse = shrinkVertically(
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        )
    )
    
    // Анимация для избранных элементов
    val favoriteScaleIn = scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        )
    )
    
    val favoriteScaleOut = scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        )
    )
    
    // Анимация для уведомлений
    val notificationSlideIn = slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    
    val notificationSlideOut = slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        )
    )
}
