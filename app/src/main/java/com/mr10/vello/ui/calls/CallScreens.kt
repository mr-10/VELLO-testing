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
            Text("INCOMING ${if (isVideo) "VIDEO" else "VOICE"} CALL", color = Color.White.copy(alpha = 0.7f), letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = Color.LightGray
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=$callerName&size=256",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(callerName, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallButton(
                    icon = Icons.Default.CallEnd,
                    backgroundColor = Color.Red,
                    contentDescription = "Reject",
                    onClick = onReject
                )
                CallButton(
                    icon = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                    backgroundColor = Color.Green,
                    contentDescription = "Accept",
                    onClick = onAccept
                )
            }
        }
    }
}

@Composable
fun OutgoingCallScreen(
    receiverName: String,
    isVideo: Boolean,
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
            Text("CALLING", color = Color.White.copy(alpha = 0.7f), letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = Color.LightGray
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=$receiverName&size=256",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(receiverName, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Ringing...", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
            
            Spacer(modifier = Modifier.weight(1f))
            
            CallButton(
                icon = Icons.Default.CallEnd,
                backgroundColor = Color.Red,
                contentDescription = "End Call",
                onClick = onEndCall,
                modifier = Modifier.padding(bottom = 100.dp)
            )
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
