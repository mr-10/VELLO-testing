package com.mr10.vello.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.R
import com.mr10.vello.data.model.DeliveryStatus
import com.mr10.vello.data.model.Message
import com.mr10.vello.util.toFormattedTime
import kotlinx.coroutines.launch

@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isOutgoing) Color(0xFFDCF8C6) else Color(0xFFE8E8EA),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isOutgoing) 12.dp else 0.dp,
                bottomEnd = if (isOutgoing) 0.dp else 12.dp
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() }
                    )
                }
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                if (message.isDeleted) {
                    Text(
                        "This message was deleted",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    Text(
                        message.content,
                        fontSize = 13.sp,
                        color = Color.Black,
                        lineHeight = 18.sp
                    )
                    
                    if (message.isEdited) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "(edited)",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        message.sentAt.toFormattedTime(),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    if (isOutgoing) {
                        when (message.deliveryStatus) {
                            DeliveryStatus.PENDING -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_clock),
                                    contentDescription = "Pending",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Gray
                                )
                            }
                            DeliveryStatus.SENT -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check_single),
                                    contentDescription = "Sent",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Gray
                                )
                            }
                            DeliveryStatus.DELIVERED -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check_double),
                                    contentDescription = "Delivered",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Gray
                                )
                            }
                            DeliveryStatus.READ -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check_double),
                                    contentDescription = "Read",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFF007AFF)
                                )
                            }
                            DeliveryStatus.FAILED -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_error),
                                    contentDescription = "Failed",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubbleAnimation(
    isOutgoing: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = 400f
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(200, easing = EaseIn)
            )
        }
    }

    Box(
        modifier = modifier
            .scale(scale.value)
            .alpha(alpha.value)
            .graphicsLayer {
                transformOrigin = if (isOutgoing) {
                    TransformOrigin(1f, 1f)
                } else {
                    TransformOrigin(0f, 1f)
                }
            }
    ) {
        content()
    }
}
