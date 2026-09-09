package com.mr10.vello.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr10.vello.R
import com.mr10.vello.data.model.Message
import com.mr10.vello.ui.components.*

@Composable
fun ChatScreen(
    conversationId: String,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onVideoClick: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(
        creationCallback = { factory: ConversationViewModel.Factory ->
            factory.create(conversationId)
        }
    )
) {
    val messages by viewModel.messages.collectAsState()
    val recipientInfo by viewModel.recipientInfo.collectAsState()
    val isLoadingMessages by viewModel.isLoadingMessages.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val error by viewModel.error.collectAsState()
    val recipientIsTyping by viewModel.recipientIsTyping.collectAsState()

    var showDeleteMenu by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf<Message?>(null) }
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatHeader(
                recipientName = recipientInfo?.name ?: "Loading...",
                isOnline = false, // TODO: Implement online status
                lastSeen = recipientInfo?.createdAt, // TODO: Use actual last seen
                onBackClick = onBackClick,
                onCallClick = onCallClick,
                onVideoClick = onVideoClick
            )
        },
        bottomBar = {
            ChatInputField(
                messageText = messageInput,
                onMessageChange = { viewModel.updateMessageInput(it) },
                onSendClick = { 
                    if (messageInput.isNotBlank()) {
                        viewModel.sendMessage(messageInput)
                    }
                },
                onAttachmentClick = { /* TODO */ },
                modifier = Modifier.imePadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            error?.let { errorMsg ->
                Surface(
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            errorMsg,
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close",
                                tint = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (isLoadingMessages) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No messages yet.\nStart the conversation!",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = messages,
                            key = { it.id ?: it.hashCode() }
                        ) { message ->
                            val isOutgoing = message.senderId == viewModel.currentUserId
                            
                            MessageBubbleAnimation(
                                isOutgoing = isOutgoing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MessageBubble(
                                    message = message,
                                    isOutgoing = isOutgoing,
                                    onLongPress = { 
                                        if (isOutgoing) showDeleteMenu = message.id 
                                    }
                                )
                            }
                        }
                    }
                }

                if (recipientIsTyping) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        TypingIndicator()
                    }
                }
            }
        }
    }

    if (showDeleteMenu != null) {
        DeleteMessageDialog(
            onDelete = {
                viewModel.deleteMessage(showDeleteMenu!!)
                showDeleteMenu = null
            },
            onDismiss = { showDeleteMenu = null }
        )
    }

    showEditDialog?.let { message ->
        EditMessageDialog(
            message = message,
            onSave = { newContent ->
                viewModel.editMessage(message.id ?: "", newContent)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }
}

@Composable
fun DeleteMessageDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete message?") },
        text = { Text("This cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditMessageDialog(
    message: Message,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editedText by remember { mutableStateOf(message.content) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit message") },
        text = {
            TextField(
                value = editedText,
                onValueChange = { editedText = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(editedText) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
