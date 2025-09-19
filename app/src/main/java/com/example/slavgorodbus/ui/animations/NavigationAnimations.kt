package com.example.slavgorodbus.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

object NavigationAnimations {
    
    // Классический слайд-эффект как в телефонах
    val slideInFromRight = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    
    val slideOutToLeft = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    val slideInFromLeft = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    val slideOutToRight = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    
    // Анимация для модальных экранов (Schedule, RouteDetails, About)
    val fadeIn = fadeIn(
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        )
    )
    
    val fadeOut = fadeOut(
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        )
    )
    
    // Анимация масштабирования для About экрана
    val scaleIn = scaleIn(
        initialScale = 0.8f,
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
    
    val scaleOut = scaleOut(
        targetScale = 0.8f,
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
    
    // Анимация для переходов к деталям маршрута
    val slideInFromBottom = slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        )
    )
    
    val slideOutToBottom = slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
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
    
    // Анимация для расписания
    val slideInFromBottomSchedule = slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight / 2 },
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        )
    )
    
    val slideOutToBottomSchedule = slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight / 2 },
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
}
