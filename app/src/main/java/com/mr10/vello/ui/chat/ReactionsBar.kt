package com.mr10.vello.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReactionsBar(
    isVisible: Boolean,
    onReactionSelected: (String) -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val reactions = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
                reactions.forEachIndexed { index, emoji ->
                    ReactionIcon(emoji, index, onReactionSelected)
                }
            }
        }
    }
}

@Composable
fun ReactionIcon(emoji: String, index: Int, onReactionSelected: (String) -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.5f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )

    Text(
        text = emoji,
        fontSize = 24.sp,
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { 
                onReactionSelected(emoji)
            }
            .padding(4.dp)
    )
}
