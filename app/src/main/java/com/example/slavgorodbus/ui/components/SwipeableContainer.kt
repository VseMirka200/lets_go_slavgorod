package com.example.slavgorodbus.ui.components

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Контейнер для обработки горизонтальных свайп-жестов
 * 
 * Функциональность:
 * - Обнаруживает горизонтальные свайпы влево и вправо
 * - Вызывает соответствующие колбэки при достижении порога свайпа
 * - Предотвращает множественные срабатывания во время одного жеста
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
    var totalDragX by remember { mutableStateOf(0f) }
    // Флаг для предотвращения множественных срабатываний
    var hasTriggered by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    // Сброс состояния при начале жеста
                    onDragStart = { 
                        totalDragX = 0f
                        hasTriggered = false
                    },
                    // Обработка завершения жеста и определение направления свайпа
                    onDragEnd = { 
                        // Определяем направление свайпа по итоговому смещению
                        val swipeThreshold = size.width * 0.2f // Порог свайпа: 20% ширины экрана
                        
                        if (!hasTriggered) {
                            when {
                                // Свайп вправо - переход к предыдущему экрану
                                totalDragX > swipeThreshold -> {
                                    onSwipeToPrevious()
                                    hasTriggered = true
                                }
                                // Свайп влево - переход к следующему экрану
                                totalDragX < -swipeThreshold -> {
                                    onSwipeToNext()
                                    hasTriggered = true
                                }
                            }
                        }
                        
                        // Сброс состояния после обработки
                        totalDragX = 0f
                    },
                    // Накопление расстояния свайпа
                    onDrag = { _, dragAmount ->
                        if (!hasTriggered) {
                            totalDragX += dragAmount.x
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
