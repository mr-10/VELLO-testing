package com.mr10.vello.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.model.Chat
import com.mr10.vello.data.model.DeliveryStatus
import com.mr10.vello.data.model.Message
import com.mr10.vello.data.repository.AuthRepository
import com.mr10.vello.data.repository.AuthRepositoryImpl
import com.mr10.vello.data.repository.ChatRepository
import com.mr10.vello.data.repository.ChatRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private var messageObservationJob: Job? = null
    private var typingObservationJob: Job? = null

    fun setTyping(chatId: String, userId: String, typing: Boolean) {
        viewModelScope.launch {
            repository.sendTypingStatus(chatId, userId, typing)
        }
    }


    fun loadChats() {
        viewModelScope.launch {
            _isLoading.value = true
            _chats.value = repository.getChats()
            _isLoading.value = false
        }
    }

    fun loadMessages(chatId: String) {
        messageObservationJob?.cancel()
        typingObservationJob?.cancel()
        _messages.value = emptyList()

        messageObservationJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Register membership/update last read
                val currentUser = authRepository.currentUser.first()
                currentUser?.let { user ->
                    Log.d("ChatViewModel", "Registering member ${user.id} for chat $chatId")
                    repository.createOrUpdateChatMembers(chatId, user.id)
                }
                
                // 2. Start observing BEFORE fetching to avoid race condition
                val observationFlow = repository.observeMessages(chatId)
                
                // 3. Fetch history
                val history = repository.getMessages(chatId)
                _messages.value = history
                _isLoading.value = false

                // 4. Collect and merge new messages, avoiding duplicates
                observationFlow.collectLatest { newMessage ->
                    _messages.value = (_messages.value + newMessage)
                        .distinctBy { it.id ?: it.hashCode() }
                        .sortedBy { it.sentAt }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("ChatViewModel", "Error loading messages", e)
            }
        }

        typingObservationJob = viewModelScope.launch {
            repository.observeTypingStatus(chatId).collectLatest { (userId, typing) ->
                _isTyping.value = typing
            }
        }
    }

    fun observeMessages(chatId: String): Flow<Message> {
        return repository.observeMessages(chatId)
    }

    fun observeAllMessages(): Flow<Message> {
        return repository.observeAllMessages()
    }

    fun sendMessage(chatId: String, senderId: String, content: String) {
        viewModelScope.launch {
            // Find the correct recipient by looking at the chat metadata
            val chat = _chats.value.find { it.id == chatId }
            val recipientId = if (chat?.id == senderId) "error" else chat?.id ?: "unknown"

            val pendingMessage = Message(
                conversationId = chatId,
                senderId = senderId,
                recipientId = recipientId,
                content = content,
                deliveryStatus = DeliveryStatus.PENDING
            )
            
            // Add to local list for immediate UI feedback
            _messages.value = _messages.value + pendingMessage
            
            try {
                // Send to repository
                repository.sendMessage(pendingMessage.copy(deliveryStatus = com.mr10.vello.data.model.DeliveryStatus.SENT))
                Log.d("ChatViewModel", "Message sent successfully")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message: ${e.message}", e)
                // Update the last message to failed status (optional: remove it or show error)
                _messages.value = _messages.value.map { 
                    if (it == pendingMessage) it.copy(deliveryStatus = DeliveryStatus.FAILED)
                    else it
                }
            }
        }
    }
}
