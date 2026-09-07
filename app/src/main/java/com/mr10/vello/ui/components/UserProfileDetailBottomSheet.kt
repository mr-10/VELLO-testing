package com.mr10.vello.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mr10.vello.data.model.UserProfile
import com.mr10.vello.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileDetailBottomSheet(
    user: UserProfile,
    onDismiss: () -> Unit,
    onChatClick: (UserProfile) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            ) {
                if (user.profilePictureUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profilePictureUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFE9EDEF)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(32.dp),
                            tint = Color(0xFFB1B3B5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = user.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111B21)
            )

            // Status Quote
            Text(
                text = user.statusQuote ?: "Hey there! I am using Vello.",
                fontSize = 16.sp,
                color = Color(0xFF667781),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info Section
            InfoItem(icon = Icons.Default.Phone, label = "Phone", value = user.phoneNumber ?: "Not provided")
            InfoItem(icon = Icons.Default.Email, label = "Email", value = user.email ?: "Not provided")
            InfoItem(icon = Icons.Default.Cake, label = "Date of Birth", value = user.dob)

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = { onChatClick(user) },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Live Chat", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, color = Color(0xFF111B21), fontWeight = FontWeight.Medium)
        }
    }
}
