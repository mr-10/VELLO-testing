package com.mr10.vello.ui.chat

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    var reactionMessageId by remember { mutableStateOf<String?>(null) }
    
    val listState = rememberLazyListState()

    val callPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
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
                            TypingIndicator(isTyping)
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
                    containerColor = VelloPrimaryContainer,
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (showAttachmentMenu) {
                AttachmentMenu(
                    onDismiss = { showAttachmentMenu = false },
                    onItemClick = { 
                        showAttachmentMenu = false
                    }
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WhatsAppBackgroundLight)
            ) {
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
                            enter = slideInHorizontally(
                                initialOffsetX = { if (isMine) it else -it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) + fadeIn(animationSpec = tween(500))
                        ) {
                            ChatBubbleWrapper(
                                message = message, 
                                isMine = isMine,
                                onLongPress = { reactionMessageId = message.id },
                                isReactionVisible = reactionMessageId == message.id,
                                onReactionSelected = { reactionMessageId = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(isTyping: Boolean) {
    AnimatedContent(
        targetState = isTyping,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) { typing ->
        if (typing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "typing",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(2.dp))
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition()
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .padding(horizontal = 0.5.dp)
                            .graphicsLayer(alpha = alpha)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        } else {
            Text(
                text = "online",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ChatBubbleWrapper(
    message: Message, 
    isMine: Boolean,
    onLongPress: () -> Unit,
    isReactionVisible: Boolean,
    onReactionSelected: (String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column {
        ReactionsBar(isVisible = isReactionVisible, onReactionSelected = onReactionSelected)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            if (!isMine && dragAmount > 0) {
                                offsetX = (offsetX + dragAmount).coerceAtMost(80f)
                            } else if (isMine && dragAmount < 0) {
                                offsetX = (offsetX + dragAmount).coerceAtLeast(-80f)
                            }
                        },
                        onDragEnd = { offsetX = 0f },
                        onDragCancel = { offsetX = 0f }
                    )
                }
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
        ) {
            if (animatedOffsetX != 0f) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "Reply",
                    tint = WhatsAppGreen,
                    modifier = Modifier
                        .align(if (isMine) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            val progress = Math.abs(animatedOffsetX) / 80f
                            scaleX = progress
                            scaleY = progress
                            alpha = progress
                            translationX = if (isMine) (1 - progress) * 20 else -(1 - progress) * 20
                        }
                )
            }
            ChatBubble(message, isMine)
        }
    }
}

@Composable
fun ChatBubble(message: Message, isMine: Boolean) {
    val bubbleColor = if (isMine) WhatsAppSentBubbleLight else WhatsAppReceivedBubbleLight
    val textColor = Color.Black // Pure Black as requested
    
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
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
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
                    placeholder = { Text("Message", style = MaterialTheme.typography.bodyLarge, color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
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
            containerColor = VelloSecondary,
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
