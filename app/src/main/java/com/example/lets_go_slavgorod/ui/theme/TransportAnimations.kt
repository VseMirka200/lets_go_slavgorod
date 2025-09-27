package com.example.lets_go_slavgorod.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Анимации в транспортном стиле
 * 
 * Особенности:
 * - Плавные переходы между состояниями
 * - Транспортные цвета для анимаций
 * - Оптимизированные для производительности
 */

/**
 * Анимация появления карточки
 */
@Composable
fun transportCardEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween(
            durationMillis = 400,
            easing = EaseOutCubic
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseOutCubic
        )
    )
}

/**
 * Анимация исчезновения карточки
 */
@Composable
fun transportCardExitTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInCubic
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 200,
            easing = EaseInCubic
        )
    )
}

/**
 * Анимация для списков
 */
@Composable
fun transportListTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        initialContentExit = slideOutVertically(
            targetOffsetY = { -it / 3 },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(200)
        )
    )
}

/**
 * Анимация для навигации
 */
@Composable
fun transportNavigationTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        initialContentExit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(200)
        )
    )
}

/**
 * Анимация для модальных окон
 */
@Composable
fun transportModalTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        initialContentExit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(200)
        )
    )
}

/**
 * Анимация для кнопок
 */
@Composable
fun transportButtonAnimation(): ContentTransform {
    return ContentTransform(
        targetContentEnter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(200, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(200)
        ),
        initialContentExit = scaleOut(
            targetScale = 1.1f,
            animationSpec = tween(150, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(150)
        )
    )
}

/**
 * Анимация пульсации для важных элементов
 */
@Composable
fun rememberPulseAnimation(): State<Float> {
    return rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
}

/**
 * Анимация для счетчика времени
 */
@Composable
fun rememberCountdownAnimation(): State<Float> {
    return rememberInfiniteTransition(label = "countdown").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "countdown"
    )
}

/**
 * Анимация для загрузки
 */
@Composable
fun rememberLoadingAnimation(): State<Float> {
    return rememberInfiniteTransition(label = "loading").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading"
    )
}

/**
 * Анимация для ошибок
 */
@Composable
fun rememberErrorAnimation(): State<Float> {
    return rememberInfiniteTransition(label = "error").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "error"
    )
}
