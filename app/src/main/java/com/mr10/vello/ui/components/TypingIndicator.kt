package com.mr10.vello.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )
    
    LaunchedEffect(Unit) {
        dots.forEachIndexed { index, animatable ->
            launch {
                delay(index * 150L)
                animatable.animateTo(
                    targetValue = 8f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 600
                            0f at 0
                            8f at 150
                            0f at 300
                        },
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }
    }
    
    Row(
        modifier = modifier
            .padding(8.dp)
            .background(Color(0xFFE8E8E8), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { offset ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = (-offset.value).dp)
                    .background(Color.Gray, shape = CircleShape)
            )
        }
    }
}
