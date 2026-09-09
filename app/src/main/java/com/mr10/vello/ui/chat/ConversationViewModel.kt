package com.mr10.vello.ui.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.model.DeliveryStatus
import com.mr10.vello.data.model.Message
import com.mr10.vello.data.model.MessageType
import com.mr10.vello.data.model.UserProfile
import com.mr10.vello.data.repository.ConversationRepository
import com.mr10.vello.data.repository.MessageRepository
import com.mr10.vello.data.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

@HiltViewModel(assistedFactory = ConversationViewModel.Factory::class)
class ConversationViewModel @AssistedInject constructor(
    private val messageRepo: MessageRepository,
    private val conversationRepo: ConversationRepository,
    private val userRepo: UserRepository,
    @Assisted val conversationId: String,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(conversationId: String): ConversationViewModel
    }

    val currentUserId = userRepo.getCurrentUserId() ?: ""

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
        if (conversationId.isNotEmpty()) {
            loadConversationData()
            subscribeToMessages()
            subscribeToRecipientTyping()
        }
    }

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
                
                // Subscribe to status updates
                recipient?.let {
                    subscribeToRecipientStatus(it.id)
                }
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load conversation"
                Log.e("ConversationVM", "Error loading data", e)
            } finally {
                _isLoadingMessages.value = false
            }
        }
    }

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
                    sentAt = Instant.now().toString()
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
                    subscribeToMessageStatus(sentMessage.id ?: return@onSuccess)
                    
                }.onFailure { error ->
                    // Mark as failed in local list
                    updateMessageStatus(message.id ?: return@onFailure, DeliveryStatus.FAILED)
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

    private fun subscribeToMessages() {
        viewModelScope.launch {
            messageRepo.getConversationMessages(conversationId)
                .collect { msgs ->
                    _messages.value = msgs
                    
                    // Mark all unread messages as read
                    msgs.filter { 
                        it.recipientId == currentUserId && it.deliveryStatus != DeliveryStatus.READ 
                    }.forEach { msg ->
                        markMessageAsRead(msg.id ?: return@forEach)
                    }
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
                    deliveredAt = if (status == DeliveryStatus.DELIVERED) Instant.now().toString() else msg.deliveredAt,
                    readAt = if (status == DeliveryStatus.READ) Instant.now().toString() else msg.readAt
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

    private fun subscribeToRecipientStatus(recipientId: String) {
        viewModelScope.launch {
            userRepo.subscribeToUserStatus(recipientId)
                .collect { updatedUser ->
                    _recipientInfo.value = updatedUser
                }
        }
    }

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
                            editedAt = Instant.now().toString()
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

    fun updateMessageInput(text: String) {
        _messageInput.value = text
        setTyping(text.isNotEmpty())
    }

    fun clearError() {
        _error.value = null
    }
}
