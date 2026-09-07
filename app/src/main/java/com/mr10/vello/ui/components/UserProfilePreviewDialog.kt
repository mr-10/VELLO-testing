package com.mr10.vello.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.mr10.vello.data.model.UserProfile
import com.mr10.vello.ui.theme.VelloPrimaryContainer

@Composable
fun UserProfilePreviewDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onMessageClick: (UserProfile) -> Unit,
    onInfoClick: (UserProfile) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(320.dp)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column {
                    // Profile Image with Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        if (user.profilePictureUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(user.profilePictureUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE9EDEF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp),
                                    tint = Color(0xFFB1B3B5)
                                )
                            }
                        }

                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        ),
                                        startY = 400f
                                    )
                                )
                        )

                        // Name on top of image
                        Text(
                            text = user.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )
                    }

                    // Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF7F9FC))
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onMessageClick(user) }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Message", tint = Color(0xFF075E54))
                        }
                        IconButton(onClick = { /* Audio Call */ }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF075E54))
                        }
                        IconButton(onClick = { /* Video Call */ }) {
                            Icon(Icons.Default.VideoCall, contentDescription = "Video", tint = Color(0xFF075E54))
                        }
                        IconButton(onClick = { onInfoClick(user) }) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF075E54))
                        }
                    }
                }
            }
        }
    }
}
