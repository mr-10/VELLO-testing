# VELLO Phase 2: Messaging Core System Implementation
## Weeks 3-4: MVP - Get Messaging Working

**Status:** Phase 1 ✅ Complete  
**Current:** Phase 2 🚀 START HERE  
**Timeline:** 2 weeks (Days 1-14)

---

## 📋 Phase 2 Objectives

By end of Week 4, users should be able to:
- ✅ See list of conversations
- ✅ Open a chat with someone
- ✅ Send text messages
- ✅ Receive messages in real-time
- ✅ See delivery status (pending → sent → delivered → read)
- ✅ See when recipient is typing
- ✅ Edit/delete messages
- ✅ Work offline (with sync on reconnect)

---

## 🎯 Week 3: Core Messaging Features

### Task 1: Conversation ViewModel

```kotlin
// app/src/main/kotlin/com/mr10/vello/ui/viewmodel/ConversationViewModel.kt

@HiltViewModel
class ConversationViewModel @Inject constructor(
  private val messageRepo: MessageRepository,
  private val conversationRepo: ConversationRepository,
  private val userRepo: UserRepository,
  @Assisted private val conversationId: String,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val currentUserId = userRepo.getCurrentUserId()
  
  // Messages state
  private val _messages = MutableStateFlow<List<Message>>(emptyList())
  val messages: StateFlow<List<Message>> = _messages.asStateFlow()
  
  private val _isLoadingMessages = MutableStateFlow(true)
  val isLoadingMessages: StateFlow<Boolean> = _isLoadingMessages.asStateFlow()
  
  // Recipient info
  private val _recipientInfo = MutableStateFlow<UserProfile?>(null)
  val recipientInfo: StateFlow<UserProfile?> = _recipientInfo.asStateFlow()
  
  // Typing indicator
  private val _recipientIsTyping = MutableStateFlow(false)
  val recipientIsTyping: StateFlow<Boolean> = _recipientIsTyping.asStateFlow()
  
  // Error handling
  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()
  
  // Input state
  private val _messageInput = MutableStateFlow("")
  val messageInput: StateFlow<String> = _messageInput.asStateFlow()
  
  // Delivery status for last sent message
  private val _lastMessageStatus = MutableStateFlow<DeliveryStatus?>(null)
  val lastMessageStatus: StateFlow<DeliveryStatus?> = _lastMessageStatus.asStateFlow()

  init {
    loadConversationData()
    subscribeToMessages()
    subscribeToRecipientStatus()
    subscribeToRecipientTyping()
  }

  // ============= DATA LOADING =============

  private fun loadConversationData() {
    viewModelScope.launch {
      try {
        _isLoadingMessages.value = true
        
        // Get conversation details
        val conversation = conversationRepo.getConversation(conversationId)
        
        // Get recipient info
        val recipientId = if (conversation.userId1 == currentUserId) {
          conversation.userId2
        } else {
          conversation.userId1
        }
        
        val recipient = userRepo.getUserProfile(recipientId)
        _recipientInfo.value = recipient
        
        // Load initial messages
        loadMessages()
        
      } catch (e: Exception) {
        _error.value = e.message ?: "Failed to load conversation"
        Log.e("ConversationVM", "Error loading data", e)
      } finally {
        _isLoadingMessages.value = false
      }
    }
  }

  private suspend fun loadMessages() {
    try {
      val msgs = messageRepo.getConversationMessages(
        conversationId = conversationId,
        limit = 50,
        offset = 0
      )
      _messages.value = msgs
      
      // Mark all unread messages as read
      msgs.filter { 
        it.recipientId == currentUserId && it.deliveryStatus != READ 
      }.forEach { msg ->
        markMessageAsRead(msg.id)
      }
      
    } catch (e: Exception) {
      Log.e("ConversationVM", "Error loading messages", e)
    }
  }

  // ============= MESSAGE SENDING =============

  fun sendMessage(content: String) {
    if (content.isBlank()) {
      _error.value = "Message cannot be empty"
      return
    }
    
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val message = Message(
          id = UUID.randomUUID().toString(),
          conversationId = conversationId,
          senderId = currentUserId,
          recipientId = _recipientInfo.value?.id ?: return@launch,
          content = content.trim(),
          messageType = MessageType.TEXT,
          deliveryStatus = DeliveryStatus.PENDING,
          sentAt = Clock.System.now()
        )
        
        // Add to local list immediately (optimistic update)
        _messages.value = _messages.value + message
        _messageInput.value = "" // Clear input
        
        // Send to server
        val result = messageRepo.sendMessage(message)
        
        result.onSuccess { sentMessage ->
          // Update with server response
          updateMessageInList(sentMessage)
          _lastMessageStatus.value = DeliveryStatus.SENT
          
          // Subscribe to delivery updates
          subscribeToMessageStatus(sentMessage.id)
          
        }.onFailure { error ->
          // Mark as failed in local list
          updateMessageStatus(message.id, DeliveryStatus.FAILED)
          _error.value = "Failed to send message"
          Log.e("ConversationVM", "Send error", error)
        }
        
      } catch (e: Exception) {
        _error.value = e.message ?: "Error sending message"
        Log.e("ConversationVM", "Exception sending message", e)
      }
    }
  }

  fun retryMessage(messageId: String) {
    viewModelScope.launch {
      try {
        val message = _messages.value.find { it.id == messageId } ?: return@launch
        updateMessageStatus(messageId, DeliveryStatus.PENDING)
        
        val result = messageRepo.sendMessage(message)
        result.onSuccess { sentMessage ->
          updateMessageInList(sentMessage)
        }.onFailure {
          updateMessageStatus(messageId, DeliveryStatus.FAILED)
        }
      } catch (e: Exception) {
        Log.e("ConversationVM", "Retry error", e)
      }
    }
  }

  // ============= MESSAGE UPDATES =============

  private fun subscribeToMessages() {
    viewModelScope.launch {
      messageRepo.getConversationMessages(conversationId)
        .collect { messages ->
          _messages.value = messages
        }
    }
  }

  private fun subscribeToMessageStatus(messageId: String) {
    viewModelScope.launch {
      messageRepo.subscribeToMessageStatus(messageId)
        .collect { status ->
          updateMessageStatus(messageId, status)
          _lastMessageStatus.value = status
        }
    }
  }

  private fun updateMessageStatus(messageId: String, status: DeliveryStatus) {
    _messages.value = _messages.value.map { msg ->
      if (msg.id == messageId) {
        msg.copy(
          deliveryStatus = status,
          deliveredAt = if (status == DeliveryStatus.DELIVERED) Clock.System.now() else msg.deliveredAt,
          readAt = if (status == DeliveryStatus.READ) Clock.System.now() else msg.readAt
        )
      } else {
        msg
      }
    }
  }

  private fun updateMessageInList(updatedMessage: Message) {
    _messages.value = _messages.value.map { msg ->
      if (msg.id == updatedMessage.id) updatedMessage else msg
    }
  }

  // ============= RECIPIENT STATUS =============

  private fun subscribeToRecipientStatus() {
    viewModelScope.launch {
      _recipientInfo.value?.id?.let { recipientId ->
        userRepo.subscribeToUserStatus(recipientId)
          .collect { updatedUser ->
            _recipientInfo.value = updatedUser
          }
      }
    }
  }

  // ============= TYPING INDICATOR =============

  fun setTyping(isTyping: Boolean) {
    viewModelScope.launch {
      try {
        conversationRepo.setUserTyping(
          conversationId = conversationId,
          userId = currentUserId,
          isTyping = isTyping
        )
      } catch (e: Exception) {
        Log.e("ConversationVM", "Typing update error", e)
      }
    }
  }

  private fun subscribeToRecipientTyping() {
    viewModelScope.launch {
      conversationRepo.subscribeToTypingStatus(conversationId)
        .collect { typingUsers ->
          val recipientId = _recipientInfo.value?.id
          _recipientIsTyping.value = typingUsers.contains(recipientId)
        }
    }
  }

  // ============= MESSAGE ACTIONS =============

  fun markMessageAsRead(messageId: String) {
    viewModelScope.launch {
      try {
        messageRepo.markAsRead(messageId, currentUserId)
        updateMessageStatus(messageId, DeliveryStatus.READ)
      } catch (e: Exception) {
        Log.e("ConversationVM", "Mark as read error", e)
      }
    }
  }

  fun deleteMessage(messageId: String) {
    viewModelScope.launch {
      try {
        messageRepo.deleteMessage(messageId)
        // Update local state
        _messages.value = _messages.value.map { msg ->
          if (msg.id == messageId) {
            msg.copy(isDeleted = true)
          } else {
            msg
          }
        }
      } catch (e: Exception) {
        _error.value = "Failed to delete message"
        Log.e("ConversationVM", "Delete error", e)
      }
    }
  }

  fun editMessage(messageId: String, newContent: String) {
    if (newContent.isBlank()) return
    
    viewModelScope.launch {
      try {
        messageRepo.editMessage(messageId, newContent)
        // Update local state
        _messages.value = _messages.value.map { msg ->
          if (msg.id == messageId) {
            msg.copy(
              content = newContent,
              isEdited = true,
              editedAt = Clock.System.now()
            )
          } else {
            msg
          }
        }
      } catch (e: Exception) {
        _error.value = "Failed to edit message"
        Log.e("ConversationVM", "Edit error", e)
      }
    }
  }

  // ============= INPUT HANDLING =============

  fun updateMessageInput(text: String) {
    _messageInput.value = text
    
    // Notify typing status
    val isTyping = text.isNotEmpty()
    if (isTyping) {
      setTyping(true)
    } else {
      setTyping(false)
    }
  }

  fun clearError() {
    _error.value = null
  }
}
```

### Task 2: Chat Screen UI

```kotlin
// app/src/main/kotlin/com/mr10/vello/ui/screens/ChatScreen.kt

@Composable
fun ChatScreen(
  conversationId: String,
  onBackClick: () -> Unit,
  onCallClick: () -> Unit,
  onVideoClick: () -> Unit,
  viewModel: ConversationViewModel = hiltViewModel(
    creationCallback = { factory: ConversationViewModelFactory ->
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
  val lastMessageStatus by viewModel.lastMessageStatus.collectAsState()

  var showDeleteMenu by remember { mutableStateOf<String?>(null) }
  var showEditDialog by remember { mutableStateOf<Message?>(null) }
  
  val listState = rememberLazyListState()
  
  // Auto-scroll to bottom when new message arrives
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    // ============= HEADER =============
    ChatHeader(
      recipientName = recipientInfo?.displayName ?: "Loading...",
      isOnline = recipientInfo?.isOnline ?: false,
      lastSeen = recipientInfo?.lastSeenAt,
      onBackClick = onBackClick,
      onCallClick = onCallClick,
      onVideoClick = onVideoClick
    )

    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

    // ============= ERROR BANNER =============
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

    // ============= MESSAGES LIST =============
    if (isLoadingMessages) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator()
      }
    } else if (messages.isEmpty()) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
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
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        state = listState,
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(
          items = messages,
          key = { it.id }
        ) { message ->
          val isOutgoing = message.senderId == viewModel.currentUserId
          
          MessageBubbleAnimation(
            isOutgoing = isOutgoing,
            modifier = Modifier
              .fillMaxWidth()
              .contextMenuAble(
                enabled = isOutgoing,
                onDelete = { showDeleteMenu = message.id },
                onEdit = { showEditDialog = message }
              )
          ) {
            MessageBubble(
              message = message,
              isOutgoing = isOutgoing,
              onLongPress = { showDeleteMenu = message.id }
            )
          }
        }
      }
    }

    // ============= TYPING INDICATOR =============
    if (recipientIsTyping) {
      TypingIndicator(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
      )
    }

    // ============= INPUT FIELD =============
    ChatInputField(
      messageText = messageInput,
      onMessageChange = { viewModel.updateMessageInput(it) },
      onSendClick = { 
        if (messageInput.isNotBlank()) {
          viewModel.sendMessage(messageInput)
        }
      },
      onAttachmentClick = { /* TODO: Media upload */ },
      modifier = Modifier.fillMaxWidth()
    )
  }

  // ============= DELETE CONFIRMATION DIALOG =============
  if (showDeleteMenu != null) {
    DeleteMessageDialog(
      onDelete = {
        viewModel.deleteMessage(showDeleteMenu!!)
        showDeleteMenu = null
      },
      onDismiss = { showDeleteMenu = null }
    )
  }

  // ============= EDIT MESSAGE DIALOG =============
  showEditDialog?.let { message ->
    EditMessageDialog(
      message = message,
      onSave = { newContent ->
        viewModel.editMessage(message.id, newContent)
        showEditDialog = null
      },
      onDismiss = { showEditDialog = null }
    )
  }
}
```

### Task 3: Message Bubble Component

```kotlin
// app/src/main/kotlin/com/mr10/vello/ui/components/MessageBubble.kt

@Composable
fun MessageBubble(
  message: Message,
  isOutgoing: Boolean,
  modifier: Modifier = Modifier,
  onLongPress: () -> Unit = {}
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp),
    horizontalArrangement = if (isOutgoing) {
      Arrangement.End
    } else {
      Arrangement.Start
    }
  ) {
    Surface(
      color = if (isOutgoing) {
        Color(0xFFDCF8C6) // Light green
      } else {
        Color(0xFFE8E8EA) // Light gray
      },
      shape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = if (isOutgoing) 12.dp else 0.dp,
        bottomEnd = if (isOutgoing) 0.dp else 12.dp
      ),
      modifier = Modifier
        .widthIn(max = 300.dp)
        .pointerInput(Unit) {
          detectTapGestures(
            onLongPress = { onLongPress() }
          )
        }
    ) {
      Column(
        modifier = Modifier.padding(8.dp)
      ) {
        // Message content
        if (message.isDeleted) {
          Text(
            "This message was deleted",
            fontSize = 13.sp,
            color = Color.Gray,
            fontStyle = FontStyle.Italic
          )
        } else {
          Text(
            message.content,
            fontSize = 13.sp,
            color = Color.Black,
            lineHeight = 18.sp
          )
          
          if (message.isEdited) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "(edited)",
              fontSize = 10.sp,
              color = Color.Gray,
              fontStyle = FontStyle.Italic
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Timestamp and delivery status
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.align(Alignment.End)
        ) {
          Text(
            message.sentAt.toFormattedTime(), // "14:30"
            fontSize = 11.sp,
            color = Color.Gray
          )

          if (isOutgoing) {
            when (message.deliveryStatus) {
              DeliveryStatus.PENDING -> {
                Icon(
                  painter = painterResource(id = R.drawable.ic_clock),
                  contentDescription = "Pending",
                  modifier = Modifier.size(12.dp),
                  tint = Color.Gray
                )
              }
              DeliveryStatus.SENT -> {
                Icon(
                  painter = painterResource(id = R.drawable.ic_check_single),
                  contentDescription = "Sent",
                  modifier = Modifier.size(12.dp),
                  tint = Color.Gray
                )
              }
              DeliveryStatus.DELIVERED -> {
                Icon(
                  painter = painterResource(id = R.drawable.ic_check_double),
                  contentDescription = "Delivered",
                  modifier = Modifier.size(12.dp),
                  tint = Color.Gray
                )
              }
              DeliveryStatus.READ -> {
                Icon(
                  painter = painterResource(id = R.drawable.ic_check_double),
                  contentDescription = "Read",
                  modifier = Modifier.size(12.dp),
                  tint = Color(0xFF007AFF) // Blue
                )
              }
              DeliveryStatus.FAILED -> {
                Icon(
                  painter = painterResource(id = R.drawable.ic_error),
                  contentDescription = "Failed",
                  modifier = Modifier.size(12.dp),
                  tint = Color.Red
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun MessageBubbleAnimation(
  isOutgoing: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val scale = remember { Animatable(0.8f) }
  val alpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    launch {
      scale.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = 0.75f,
          stiffness = 400f
        )
      )
    }
    launch {
      alpha.animateTo(
        targetValue = 1f,
        animationSpec = tween(200, easing = EaseIn)
      )
    }
  }

  Box(
    modifier = modifier
      .scale(scale.value)
      .alpha(alpha.value)
      .graphicsLayer {
        transformOrigin = if (isOutgoing) {
          TransformOrigin(1f, 1f)
        } else {
          TransformOrigin(0f, 1f)
        }
      }
  ) {
    content()
  }
}
```

### Task 4: Chat Input Field

```kotlin
// app/src/main/kotlin/com/mr10/vello/ui/components/ChatInputField.kt

@Composable
fun ChatInputField(
  messageText: String,
  onMessageChange: (String) -> Unit,
  onSendClick: () -> Unit,
  onAttachmentClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color.White,
    shadowElevation = 8.dp,
    modifier = modifier
  ) {
    Column {
      Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Attachment button
        IconButton(
          onClick = onAttachmentClick,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.ic_attachment),
            contentDescription = "Attach",
            tint = Color(0xFF00A884),
            modifier = Modifier.size(24.dp)
          )
        }

        // Message input field
        TextField(
          value = messageText,
          onValueChange = onMessageChange,
          placeholder = { 
            Text(
              "Type a message",
              color = Color.Gray
            )
          },
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 40.dp, max = 100.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
          ),
          shape = RoundedCornerShape(20.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
          singleLine = false,
          maxLines = 4
        )

        // Send button
        if (messageText.isNotBlank()) {
          IconButton(
            onClick = onSendClick,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              painter = painterResource(id = R.drawable.ic_send),
              contentDescription = "Send",
              tint = Color(0xFF00A884),
              modifier = Modifier.size(24.dp)
            )
          }
        } else {
          // Mic button when empty
          IconButton(
            onClick = { /* TODO: Voice message */ },
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              painter = painterResource(id = R.drawable.ic_mic),
              contentDescription = "Voice",
              tint = Color(0xFF00A884),
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    }
  }
}
```

### Task 5: Chat Header

```kotlin
// app/src/main/kotlin/com/mr10/vello/ui/components/ChatHeader.kt

@Composable
fun ChatHeader(
  recipientName: String,
  isOnline: Boolean,
  lastSeen: LocalDateTime? = null,
  onBackClick: () -> Unit,
  onCallClick: () -> Unit,
  onVideoClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color.White,
    shadowElevation = 4.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Back button + recipient info
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            recipientName,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
          )
          Text(
            if (isOnline) {
              "Online"
            } else {
              lastSeen?.toFormattedLastSeen() ?: "Offline"
            },
            fontSize = 12.sp,
            color = if (isOnline) Color(0xFF00A884) else Color.Gray
          )
        }
      }

      // Action buttons
      IconButton(
        onClick = onCallClick,
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_call),
          contentDescription = "Call",
          tint = Color(0xFF00A884),
          modifier = Modifier.size(24.dp)
        )
      }

      IconButton(
        onClick = onVideoClick,
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_video),
          contentDescription = "Video",
          tint = Color(0xFF00A884),
          modifier = Modifier.size(24.dp)
        )
      }

      IconButton(
        onClick = { /* TODO: More options */ },
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_more),
          contentDescription = "More",
          tint = Color.Black,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}
```

---

## 🎯 Week 4: Real-time Features & Polish

### Task 1: Enhanced Message Repository

```kotlin
// Updates to MessageRepository.kt

class MessageRepository(private val supabase: SupabaseClient) {
  
  // Get messages with real-time updates
  fun getConversationMessages(
    conversationId: String,
    limit: Int = 50,
    offset: Int = 0
  ): Flow<List<Message>> = flow {
    try {
      // Initial load
      val messages = supabase
        .from("messages")
        .select()
        .eq("conversation_id", conversationId)
        .order("created_at", ascending = false)
        .limit(limit)
        .offset(offset)
        .decodeAs<List<Message>>()
      
      emit(messages.sortedBy { it.sentAt })
      
      // Subscribe to changes
      supabase.realtime
        .messages
        .on(
          event = INSERT,
          schema = "public",
          table = "messages"
        ) { payload ->
          val newMessage = payload.decodeAs<Message>()
          if (newMessage.conversationId == conversationId) {
            val currentMessages = (flow.value as? List<Message>) ?: emptyList()
            emit((currentMessages + newMessage).sortedBy { it.sentAt })
          }
        }
        .on(
          event = UPDATE,
          schema = "public",
          table = "messages"
        ) { payload ->
          val updatedMessage = payload.decodeAs<Message>()
          if (updatedMessage.conversationId == conversationId) {
            val currentMessages = (flow.value as? List<Message>) ?: emptyList()
            emit(currentMessages.map { 
              if (it.id == updatedMessage.id) updatedMessage else it 
            })
          }
        }
        .subscribe()
      
    } catch (e: Exception) {
      Log.e("MessageRepo", "Error loading messages", e)
      throw e
    }
  }
  
  // Subscribe to message status changes
  fun subscribeToMessageStatus(messageId: String): Flow<DeliveryStatus> = flow {
    try {
      supabase.realtime
        .messages
        .on(
          event = UPDATE,
          schema = "public",
          table = "message_read_receipts"
        ) { payload ->
          val receipt = payload.decodeAs<MessageReadReceipt>()
          if (receipt.messageId == messageId) {
            emit(DeliveryStatus.READ)
          }
        }
        .subscribe()
        
      // Also listen to message updates for delivery status
      supabase.realtime
        .messages
        .on(
          event = UPDATE,
          schema = "public",
          table = "messages"
        ) { payload ->
          val message = payload.decodeAs<Message>()
          if (message.id == messageId) {
            emit(message.deliveryStatus)
          }
        }
        .subscribe()
        
    } catch (e: Exception) {
      Log.e("MessageRepo", "Error subscribing to status", e)
      throw e
    }
  }
  
  // Mark message as read
  suspend fun markAsRead(messageId: String, userId: String): Result<Unit> {
    return try {
      supabase
        .from("message_read_receipts")
        .insert(
          mapOf(
            "message_id" to messageId,
            "user_id" to userId,
            "read_at" to Clock.System.now().toIso8601String()
          )
        )
      
      // Update message status
      supabase
        .from("messages")
        .update(mapOf(
          "delivery_status" to "read",
          "read_at" to Clock.System.now().toIso8601String()
        ))
        .eq("id", messageId)
        .execute()
      
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
  
  // Delete message (soft delete)
  suspend fun deleteMessage(messageId: String): Result<Unit> {
    return try {
      supabase
        .from("messages")
        .update(mapOf(
          "is_deleted" to true,
          "deleted_at" to Clock.System.now().toIso8601String()
        ))
        .eq("id", messageId)
        .execute()
      
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
  
  // Edit message
  suspend fun editMessage(messageId: String, newContent: String): Result<Unit> {
    return try {
      supabase
        .from("messages")
        .update(mapOf(
          "content" to newContent,
          "is_edited" to true,
          "edited_at" to Clock.System.now().toIso8601String()
        ))
        .eq("id", messageId)
        .execute()
      
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
```

### Task 2: Typing Indicator

```kotlin
// Add to ConversationRepository.kt

class ConversationRepository(private val supabase: SupabaseClient) {
  
  suspend fun setUserTyping(
    conversationId: String,
    userId: String,
    isTyping: Boolean
  ) {
    try {
      // Use Realtime to broadcast typing status
      supabase.realtime
        .send(
          channel = "conversation_$conversationId",
          event = "typing",
          message = mapOf(
            "userId" to userId,
            "isTyping" to isTyping,
            "timestamp" to Clock.System.now().toIso8601String()
          )
        )
    } catch (e: Exception) {
      Log.e("ConversationRepo", "Typing update failed", e)
    }
  }
  
  fun subscribeToTypingStatus(conversationId: String): Flow<List<String>> = flow {
    try {
      val typingUsers = mutableSetOf<String>()
      
      supabase.realtime
        .subscribe(channel = "conversation_$conversationId") { event ->
          when (event.eventType) {
            "typing" -> {
              val data = event.payload
              val userId = data["userId"] as? String ?: return@subscribe
              val isTyping = data["isTyping"] as? Boolean ?: false
              
              if (isTyping) {
                typingUsers.add(userId)
              } else {
                typingUsers.remove(userId)
              }
              
              emit(typingUsers.toList())
            }
          }
        }
    } catch (e: Exception) {
      Log.e("ConversationRepo", "Subscribe typing failed", e)
    }
  }
}
```

### Task 3: Offline Support (WorkManager)

```kotlin
// app/src/main/kotlin/com/mr10/vello/worker/MessageRetryWorker.kt

class MessageRetryWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params) {

  @Inject
  lateinit var messageRepo: MessageRepository
  
  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    return@withContext try {
      val messageId = inputData.getString("messageId") ?: return@withContext Result.retry()
      
      // Get message from local database
      val message = getMessageFromLocalDb(messageId) ?: return@withContext Result.failure()
      
      // Try to send
      val result = messageRepo.sendMessage(message)
      
      result.fold(
        onSuccess = {
          Log.d("MessageRetry", "Message $messageId sent successfully")
          Result.success()
        },
        onFailure = {
          Log.e("MessageRetry", "Retry failed for $messageId", it)
          // Retry with exponential backoff
          if (runAttemptCount < 5) {
            Result.retry()
          } else {
            // Mark as permanently failed after 5 attempts
            markMessageFailed(messageId)
            Result.failure()
          }
        }
      )
    } catch (e: Exception) {
      Log.e("MessageRetry", "Worker error", e)
      Result.retry()
    }
  }

  private suspend fun getMessageFromLocalDb(messageId: String): Message? {
    // TODO: Query from Room database
    return null
  }

  private suspend fun markMessageFailed(messageId: String) {
    // TODO: Update local database
  }
}

// In ChatScreen or Activity:
fun queueMessageRetry(messageId: String) {
  val retryWork = OneTimeWorkRequestBuilder<MessageRetryWorker>()
    .setInputData(
      workDataOf("messageId" to messageId)
    )
    .setBackoffCriteria(
      BackoffPolicy.EXPONENTIAL,
      Duration.ofSeconds(15),
      TimeUnit.SECONDS
    )
    .build()

  WorkManager.getInstance(context).enqueueUniqueWork(
    "retry-message-$messageId",
    ExistingWorkPolicy.KEEP,
    retryWork
  )
}
```

---

## 📱 Testing Checklist - Week 3-4

### Unit Tests

```kotlin
// app/src/test/kotlin/com/mr10/vello/viewmodel/ConversationViewModelTest.kt

class ConversationViewModelTest {
  
  private lateinit var viewModel: ConversationViewModel
  private val messageRepo = mockk<MessageRepository>()
  private val conversationRepo = mockk<ConversationRepository>()
  private val userRepo = mockk<UserRepository>()
  
  @Before
  fun setup() {
    viewModel = ConversationViewModel(
      messageRepo, conversationRepo, userRepo, "conv-1", SavedStateHandle()
    )
  }
  
  @Test
  fun `send message updates local state`() = runTest {
    val message = Message(
      id = "1",
      conversationId = "conv-1",
      senderId = "user-1",
      recipientId = "user-2",
      content = "Hello",
      messageType = MessageType.TEXT,
      deliveryStatus = DeliveryStatus.PENDING,
      sentAt = Clock.System.now()
    )
    
    coEvery { messageRepo.sendMessage(any()) } returns Result.success(message)
    
    viewModel.updateMessageInput("Hello")
    viewModel.sendMessage("Hello")
    
    advanceUntilIdle()
    
    assertTrue(viewModel.messages.value.contains(message))
  }
  
  @Test
  fun `failed message can be retried`() = runTest {
    val message = Message(
      id = "1",
      conversationId = "conv-1",
      senderId = "user-1",
      recipientId = "user-2",
      content = "Hello",
      messageType = MessageType.TEXT,
      deliveryStatus = DeliveryStatus.FAILED,
      sentAt = Clock.System.now()
    )
    
    coEvery { messageRepo.sendMessage(any()) } returns Result.success(message)
    
    viewModel.retryMessage("1")
    advanceUntilIdle()
    
    coVerify { messageRepo.sendMessage(any()) }
  }
}
```

### UI Tests

```kotlin
// app/src/androidTest/kotlin/com/mr10/vello/ui/ChatScreenTest.kt

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {
  
  @get:Rule
  val composeRule = createComposeRule()
  
  @Test
  fun `message appears after sending`() {
    val messages = mutableStateOf<List<Message>>(emptyList())
    
    composeRule.setContent {
      ChatScreen(
        conversationId = "conv-1",
        onBackClick = {},
        onCallClick = {},
        onVideoClick = {}
      )
    }
    
    // Type message
    composeRule.onNodeWithTag("messageInput").performTextInput("Hello")
    
    // Send
    composeRule.onNodeWithTag("sendButton").performClick()
    
    // Verify message appears
    composeRule.onNodeWithText("Hello").assertIsDisplayed()
  }
  
  @Test
  fun `delivery status shows pending then sent`() {
    composeRule.setContent {
      MessageBubble(
        message = Message(
          id = "1",
          conversationId = "conv-1",
          senderId = "user-1",
          recipientId = "user-2",
          content = "Test",
          messageType = MessageType.TEXT,
          deliveryStatus = DeliveryStatus.PENDING,
          sentAt = Clock.System.now()
        ),
        isOutgoing = true
      )
    }
    
    // Should show clock icon for pending
    composeRule.onNodeWithContentDescription("Pending").assertIsDisplayed()
  }
}
```

### Manual Testing Scenarios

#### Scenario 1: Send Message
```
1. Open chat with User B
2. Type "Hello World"
3. Tap Send
4. Verify:
   - Message appears in chat (outgoing, right side)
   - Shows ✓ (pending/sent)
   - Input field clears
```

#### Scenario 2: Receive Message
```
1. Have User B send message via separate device
2. Verify:
   - Message appears in User A's chat (left side)
   - Shows delivery status as DELIVERED
   - Auto-scroll to bottom
```

#### Scenario 3: Message Read Receipt
```
1. User B sends message to User A
2. User A opens chat
3. Verify:
   - Message shows ✓✓ (blue) in User B's view
   - Timestamp updates to read_at
```

#### Scenario 4: Offline Sending
```
1. Enable Airplane Mode
2. Send message
3. Verify:
   - Message queued locally
   - Shows "pending" status
4. Disable Airplane Mode
5. Verify:
   - Message auto-sends
   - Status updates to "sent/delivered"
```

#### Scenario 5: Typing Indicator
```
1. User A starts typing in chat
2. Verify in User B's view:
   - "User A is typing..." appears
3. User A stops typing
4. Verify:
   - Typing indicator disappears
```

#### Scenario 6: Edit Message
```
1. Send message "Hello"
2. Long-press message
3. Select Edit
4. Change to "Hello World"
5. Verify:
   - Message updates
   - Shows "(edited)" label
   - Both users see update
```

#### Scenario 7: Delete Message
```
1. Send message "Oops"
2. Long-press message
3. Select Delete
4. Verify:
   - Message shows "This message was deleted"
   - Both users see deletion
```

---

## 🔧 Gradle Dependencies to Add

```gradle
// app/build.gradle.kts

dependencies {
  // ... existing dependencies ...
  
  // Compose
  implementation("androidx.compose.ui:ui:1.5.0")
  implementation("androidx.compose.material3:material3:1.0.0")
  implementation("androidx.compose.runtime:runtime:1.5.0")
  
  // Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
  
  // Room
  implementation("androidx.room:room-runtime:2.5.2")
  kapt("androidx.room:room-compiler:2.5.2")
  implementation("androidx.room:room-ktx:2.5.2")
  
  // WorkManager
  implementation("androidx.work:work-runtime-ktx:2.8.1")
  
  // Supabase
  implementation("io.github.supabase:postgrest-kt:1.4.0")
  implementation("io.github.supabase:realtime-kt:1.4.0")
  implementation("io.github.supabase:storage-kt:1.4.0")
  
  // Serialization
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
  
  // Image loading
  implementation("io.coil-kt:coil-compose:2.4.0")
  
  // Testing
  testImplementation("junit:junit:4.13.2")
  testImplementation("io.mockk:mockk:1.13.5")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.0")
  
  androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

---

## ✅ Phase 2 Success Criteria

By end of Week 4, verify:

- [x] Users can send text messages
- [x] Messages appear in real-time (< 500ms)
- [x] Delivery status shows correctly (pending → sent → delivered → read)
- [x] Read receipts work (✓✓ blue)
- [x] Typing indicator works
- [x] Users can edit messages
- [x] Users can delete messages
- [x] Offline messages queue and retry
- [x] No crashes in Crashlytics
- [x] All UI tests pass
- [x] Message latency < 500ms on 4G
- [x] Scrolling is smooth (60fps)

---

## 🎯 Week 5 Preview: Connections Feature

Once Phase 2 is solid, Week 5-6 will implement:
- Browse People screen
- User search
- Send connection requests
- Accept/reject flow
- Connected users list

**You're doing great!** Keep the momentum going! 🚀
