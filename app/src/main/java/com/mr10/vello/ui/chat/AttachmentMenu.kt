package com.mr10.vello.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.ui.theme.*

@Composable
fun AttachmentMenu(
    onDismiss: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
            .background(Color.Black.copy(alpha = 0.2f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Simulating Circular Reveal with Scale + Fade + Spring
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AttachmentItem(Icons.Default.Description, "Document", Color(0xFF7F66FF), onItemClick)
                        AttachmentItem(Icons.Default.CameraAlt, "Camera", Color(0xFFFF4599), onItemClick)
                        AttachmentItem(Icons.Default.Image, "Gallery", Color(0xFFBB66FF), onItemClick)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AttachmentItem(Icons.Default.Headset, "Audio", Color(0xFFF79A2E), onItemClick)
                        AttachmentItem(Icons.Default.LocationOn, "Location", Color(0xFF06D755), onItemClick)
                        AttachmentItem(Icons.Default.Person, "Contact", Color(0xFF0EA5F5), onItemClick)
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentItem(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(label) }
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = backgroundColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}
