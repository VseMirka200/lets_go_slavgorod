package com.example.lets_go_slavgorod.ui.components

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Упрощенный контейнер для обработки свайпов в настройках
 * 
 * Функциональность:
 * - Обнаруживает только четкие горизонтальные свайпы
 * - Игнорирует вертикальную прокрутку
 * - Простая и надежная логика
 */
@Composable
fun SettingsSwipeableContainer(
    onSwipeToNext: () -> Unit,
    onSwipeToPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var hasTriggered by remember { mutableStateOf(false) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        totalDragX = 0f
                        totalDragY = 0f
                        hasTriggered = false
                        dragStartTime = System.currentTimeMillis()
                        Log.d("SettingsSwipeableContainer", "Drag started")
                    },
                    onDragEnd = { 
                        val dragDuration = System.currentTimeMillis() - dragStartTime
                        Log.d("SettingsSwipeableContainer", "Drag ended: X=$totalDragX, Y=$totalDragY, Duration=${dragDuration}ms")
                        
                        if (!hasTriggered) {
                            // Улучшенные условия для свайпа
                            val minSwipeDistance = 120f // Уменьшено для более чувствительного свайпа
                            val maxVerticalMovement = 80f // Уменьшено для более точного определения
                            val maxSwipeDuration = 800L // Максимальная длительность свайпа
                            val minSwipeDuration = 100L // Минимальная длительность свайпа
                            
                            val isHorizontalSwipe = kotlin.math.abs(totalDragX) > minSwipeDistance &&
                                                   kotlin.math.abs(totalDragY) < maxVerticalMovement &&
                                                   kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY) * 1.5f &&
                                                   dragDuration <= maxSwipeDuration &&
                                                   dragDuration >= minSwipeDuration
                            
                            if (isHorizontalSwipe) {
                                when {
                                    totalDragX > minSwipeDistance -> {
                                        Log.d("SettingsSwipeableContainer", "Swipe right detected")
                                        onSwipeToPrevious()
                                        hasTriggered = true
                                    }
                                    totalDragX < -minSwipeDistance -> {
                                        Log.d("SettingsSwipeableContainer", "Swipe left detected")
                                        onSwipeToNext()
                                        hasTriggered = true
                                    }
                                }
                            } else {
                                Log.d("SettingsSwipeableContainer", "Not a valid horizontal swipe")
                            }
                        }
                        
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        if (!hasTriggered) {
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                            
                            // Предварительное определение направления свайпа
                            val currentDistance = kotlin.math.abs(totalDragX)
                            val currentVertical = kotlin.math.abs(totalDragY)
                            
                            // Если уже достаточно движения для определения свайпа
                            if (currentDistance > 80f && currentDistance > currentVertical * 1.2f) {
                                when {
                                    totalDragX > 0 -> {
                                        Log.d("SettingsSwipeableContainer", "Swipe right in progress")
                                    }
                                    totalDragX < 0 -> {
                                        Log.d("SettingsSwipeableContainer", "Swipe left in progress")
                                    }
                                }
                            }
                        }
                    }
                )
            }
    ) {
        content()
    }
}
