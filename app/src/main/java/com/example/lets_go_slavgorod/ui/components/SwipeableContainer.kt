package com.example.lets_go_slavgorod.ui.components

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.lets_go_slavgorod.utils.Constants

/**
 * Контейнер для обработки горизонтальных свайп-жестов
 * 
 * Функциональность:
 * - Обнаруживает горизонтальные свайпы влево и вправо
 * - Вызывает соответствующие колбэки при достижении порога свайпа
 * - Предотвращает множественные срабатывания во время одного жеста
 * - Поддерживает плавные переходы между экранами
 * - Адаптивный порог свайпа для лучшего UX
 * 
 * @param currentIndex текущий индекс экрана (для отображения контента)
 * @param onSwipeToNext колбэк для свайпа влево (переход к следующему экрану)
 * @param onSwipeToPrevious колбэк для свайпа вправо (переход к предыдущему экрану)
 * @param modifier модификатор для настройки внешнего вида
 * @param content Composable функция для отображения контента
 */
@Composable
fun SwipeableContainer(
    currentIndex: Int,
    onSwipeToNext: () -> Unit,
    onSwipeToPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    // Состояние для отслеживания общего расстояния свайпа
    // Используем mutableFloatStateOf для оптимизации производительности
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    // Флаг для предотвращения множественных срабатываний во время одного жеста
    var hasTriggered by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    // Сброс состояния при начале жеста
                    // Инициализируем все переменные для нового жеста
                    onDragStart = { 
                        totalDragX = 0f
                        totalDragY = 0f
                        hasTriggered = false
                    },
                    // Обработка завершения жеста и определение направления свайпа
                    onDragEnd = { 
                        // Адаптивный порог свайпа для лучшего UX
                        val swipeThreshold = size.width * Constants.SWIPE_THRESHOLD_PERCENT
                        val verticalThreshold = size.height * 0.1f // Порог вертикального движения: 10% высоты экрана
                        
                        if (!hasTriggered) {
                            // Проверяем, что это горизонтальный свайп (не вертикальная прокрутка)
                            // Условия: горизонтальное движение больше вертикального И превышает порог И вертикальное движение меньше порога
                            val isHorizontalSwipe = kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY) && 
                                                  kotlin.math.abs(totalDragX) > swipeThreshold &&
                                                  kotlin.math.abs(totalDragY) < verticalThreshold
                            
                            if (isHorizontalSwipe) {
                                when {
                                    // Свайп вправо - переход к предыдущему экрану
                                    totalDragX > swipeThreshold -> {
                                        Log.d("SwipeableContainer", "Swipe right detected, navigating to previous screen")
                                        onSwipeToPrevious()
                                        hasTriggered = true
                                    }
                                    // Свайп влево - переход к следующему экрану
                                    totalDragX < -swipeThreshold -> {
                                        Log.d("SwipeableContainer", "Swipe left detected, navigating to next screen")
                                        onSwipeToNext()
                                        hasTriggered = true
                                    }
                                }
                            }
                        }
                        
                        // Сброс состояния после обработки
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    // Накопление расстояния свайпа
                    onDrag = { _, dragAmount ->
                        if (!hasTriggered) {
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        }
                    }
                )
            }
    ) {
        // Отображение контента с передачей текущего индекса
        Log.d("SwipeableContainer", "Displaying content for index: $currentIndex")
        content(currentIndex)
    }
}
