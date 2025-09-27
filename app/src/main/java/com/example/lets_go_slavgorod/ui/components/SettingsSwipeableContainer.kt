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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        totalDragX = 0f
                        totalDragY = 0f
                        hasTriggered = false
                        Log.d("SettingsSwipeableContainer", "Drag started")
                    },
                    onDragEnd = { 
                        Log.d("SettingsSwipeableContainer", "Drag ended: X=$totalDragX, Y=$totalDragY")
                        
                        if (!hasTriggered) {
                            // Более строгие условия для свайпа
                            val minSwipeDistance = 150f // Минимальное расстояние свайпа
                            val maxVerticalMovement = 100f // Максимальное вертикальное движение
                            
                            val isHorizontalSwipe = kotlin.math.abs(totalDragX) > minSwipeDistance &&
                                                   kotlin.math.abs(totalDragY) < maxVerticalMovement &&
                                                   kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY) * 2
                            
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
                                Log.d("SettingsSwipeableContainer", "Not a horizontal swipe")
                            }
                        }
                        
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        if (!hasTriggered) {
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        }
                    }
                )
            }
    ) {
        content()
    }
}
