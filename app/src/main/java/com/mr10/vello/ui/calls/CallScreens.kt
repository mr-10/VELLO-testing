package com.mr10.vello.ui.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mr10.vello.ui.theme.WhatsAppTeal

@Composable
fun IncomingCallScreen(
    callerName: String,
    isVideo: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF075E54)) // Dark WhatsApp Teal
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("VELLO ${if (isVideo) "VIDEO" else "VOICE"} CALL", color = Color.White.copy(alpha = 0.7f), letterSpacing = 2.sp, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape),
                color = Color.LightGray,
                tonalElevation = 4.dp
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=$callerName&size=256&background=random",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(callerName, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Incoming...", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CallButton(
                        icon = Icons.Default.CallEnd,
                        backgroundColor = Color(0xFFEF5350),
                        contentDescription = "Reject",
                        onClick = onReject
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Decline", color = Color.White, fontSize = 12.sp)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CallButton(
                        icon = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        backgroundColor = Color(0xFF66BB6A),
                        contentDescription = "Accept",
                        onClick = onAccept
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Answer", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun OutgoingCallScreen(
    receiverName: String,
    isVideo: Boolean,
    isRinging: Boolean = false,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF075E54))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("VELLO ${if (isVideo) "VIDEO" else "VOICE"} CALL", color = Color.White.copy(alpha = 0.7f), letterSpacing = 2.sp, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape),
                color = Color.LightGray,
                tonalElevation = 4.dp
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=$receiverName&size=256&background=random",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(receiverName, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            
            // Real-time status: Calling vs Ringing
            Text(
                text = if (isRinging) "Ringing..." else "Calling...",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                CallButton(
                    icon = Icons.Default.CallEnd,
                    backgroundColor = Color(0xFFEF5350),
                    contentDescription = "End Call",
                    onClick = onEndCall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("End", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CallButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = backgroundColor,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = modifier.size(72.dp)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(32.dp))
    }
}
