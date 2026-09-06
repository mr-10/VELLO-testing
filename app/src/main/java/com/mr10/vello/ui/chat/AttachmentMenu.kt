package com.mr10.vello.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentMenu(
    onDismiss: () -> Unit,
    onItemClick: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttachmentItem("Document", Icons.Default.Description, Color(0xFF7F66FF), onItemClick)
                AttachmentItem("Camera", Icons.Default.PhotoCamera, Color(0xFFFF4081), onItemClick)
                AttachmentItem("Gallery", Icons.Default.Image, Color(0xFFC052D2), onItemClick)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttachmentItem("Audio", Icons.Default.Headphones, Color(0xFFFF9800), onItemClick)
                AttachmentItem("Location", Icons.Default.LocationOn, Color(0xFF4CAF50), onItemClick)
                AttachmentItem("Contact", Icons.Default.Person, Color(0xFF009688), onItemClick)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttachmentItem("Poll", Icons.Default.Poll, Color(0xFF00BCD4), onItemClick)
                AttachmentItem("Event", Icons.Default.Event, Color(0xFFF44336), onItemClick)
                Spacer(modifier = Modifier.size(64.dp)) // Spacer to keep grid alignment
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AttachmentItem(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        IconButton(
            onClick = { onClick(label) },
            modifier = Modifier
                .size(60.dp)
                .background(color, CircleShape)
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
