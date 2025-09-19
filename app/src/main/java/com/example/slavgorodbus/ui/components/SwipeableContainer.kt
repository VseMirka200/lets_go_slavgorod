package com.example.slavgorodbus.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun SwipeableContainer(
    currentIndex: Int,
    onSwipeToNext: () -> Unit,
    onSwipeToPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    var totalDragX by remember { mutableStateOf(0f) }
    var hasTriggered by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        totalDragX = 0f
                        hasTriggered = false
                    },
                    onDragEnd = { 
                        // Определяем направление свайпа по итоговому смещению
                        val swipeThreshold = size.width * 0.2f // 20% ширины экрана
                        
                        if (!hasTriggered) {
                            when {
                                totalDragX > swipeThreshold -> {
                                    onSwipeToPrevious()
                                    hasTriggered = true
                                }
                                totalDragX < -swipeThreshold -> {
                                    onSwipeToNext()
                                    hasTriggered = true
                                }
                            }
                        }
                        
                        totalDragX = 0f
                    },
                    onDrag = { _, dragAmount ->
                        if (!hasTriggered) {
                            totalDragX += dragAmount.x
                        }
                    }
                )
            }
    ) {
        content(currentIndex)
    }
}
