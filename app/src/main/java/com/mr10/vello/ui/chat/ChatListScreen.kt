package com.mr10.vello.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mr10.vello.data.model.Chat
import com.mr10.vello.ui.theme.WhatsAppGreen

@Composable
fun ChatListScreen(
    viewModel: ChatViewModel,
    onChatClick: (Chat) -> Unit
) {
    val chats by viewModel.chats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedChatContext by remember { mutableStateOf<Chat?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    if (selectedChatContext != null) {
        ChatContextMenu(
            onDismiss = { selectedChatContext = null },
            onAction = { action ->
                // Handle action
                selectedChatContext = null
            }
        )
    }

    if (isLoading && chats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = WhatsAppGreen)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chats, key = { it.id }) { chat ->
                ChatItem(
                    chat = chat, 
                    onClick = { onChatClick(chat) },
                    onLongClick = { selectedChatContext = chat }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 76.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape),
            color = Color.LightGray
        ) {
            AsyncImage(
                model = chat.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${chat.name}&background=random",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = chat.lastMessageTimestamp ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (chat.unreadCount > 0) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.lastMessage ?: "No messages yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
