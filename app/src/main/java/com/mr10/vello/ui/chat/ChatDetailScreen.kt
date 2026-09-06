package com.mr10.vello.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mr10.vello.R
import com.mr10.vello.data.model.Message
import com.mr10.vello.data.model.MessageStatus
import com.mr10.vello.ui.auth.AuthViewModel
import com.mr10.vello.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    chatName: String,
    viewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val callPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    )

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            color = Color.LightGray
                        ) {
                            AsyncImage(
                                model = "https://ui-avatars.com/api/?name=$chatName&background=random",
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = chatName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            AnimatedContent(targetState = isTyping) { typing ->
                                Text(
                                    text = if (typing) "typing..." else "online",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (callPermissionsState.allPermissionsGranted) onVideoCall()
                        else callPermissionsState.launchMultiplePermissionRequest()
                    }) { Icon(Icons.Default.Videocam, null, tint = Color.White) }
                    IconButton(onClick = {
                        if (callPermissionsState.allPermissionsGranted) onVoiceCall()
                        else callPermissionsState.launchMultiplePermissionRequest()
                    }) { Icon(Icons.Default.Call, null, tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = { 
                    inputText = it
                    currentUser?.let { user -> viewModel.setTyping(chatId, user.id, it.isNotEmpty()) }
                },
                onSend = {
                    if (inputText.isNotBlank() && currentUser != null) {
                        viewModel.sendMessage(chatId, currentUser!!.id, inputText)
                        inputText = ""
                        viewModel.setTyping(chatId, currentUser!!.id, false)
                    }
                },
                onAttachClick = { showAttachmentMenu = true }
            )
        }
    ) { innerPadding ->
        if (showAttachmentMenu) {
            AttachmentMenu(
                onDismiss = { showAttachmentMenu = false },
                onItemClick = { 
                    showAttachmentMenu = false
                    // Handle attachment selection
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WhatsAppBackgroundLight)
        ) {
            // Optional: Add wallpaper pattern here if available as resource
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages, key = { it.id ?: it.hashCode() }) { message ->
                    val isMine = message.senderId == currentUser?.id
                    
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(
                            initialOffsetY = { 8 },
                            animationSpec = spring(dampingRatio = 0.75f)
                        ) + fadeIn()
                    ) {
                        ChatBubble(message, isMine)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, isMine: Boolean) {
    val bubbleColor = if (isMine) WhatsAppSentBubbleLight else WhatsAppReceivedBubbleLight
    val textColor = WhatsAppTextPrimaryLight
    
    // Stitch: 1.125rem (18dp) corners, 0.25rem (4dp) tail corner
    val bubbleShape = if (isMine) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = bubbleShape,
            tonalElevation = 1.dp,
            shadowElevation = 0.5.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "12:00 PM",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = WhatsAppTextSecondaryLight
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(message.status)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageStatusIcon(status: MessageStatus) {
    val icon = when (status) {
        MessageStatus.PENDING -> Icons.Default.Schedule
        MessageStatus.SENT -> Icons.Default.Check
        MessageStatus.DELIVERED -> Icons.Default.DoneAll
        MessageStatus.READ -> Icons.Default.DoneAll
    }
    val color = if (status == MessageStatus.READ) WhatsAppTicksBlue else WhatsAppTicksGray
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = color
    )
}


@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                IconButton(onClick = {}) { 
                    Icon(Icons.Default.EmojiEmotions, null, tint = WhatsAppTextSecondaryLight) 
                }
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("Message", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                IconButton(onClick = onAttachClick) { 
                    Icon(Icons.Default.AttachFile, null, tint = WhatsAppTextSecondaryLight) 
                }
                if (text.isEmpty()) {
                    IconButton(onClick = {}) { 
                        Icon(Icons.Default.PhotoCamera, null, tint = WhatsAppTextSecondaryLight) 
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        FloatingActionButton(
            onClick = onSend,
            containerColor = Color(0xFF008069),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
        ) {
            AnimatedContent(targetState = text.isEmpty()) { isMic ->
                Icon(
                    imageVector = if (isMic) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Action"
                )
            }
        }
    }
}
