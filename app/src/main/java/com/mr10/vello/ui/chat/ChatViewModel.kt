package com.mr10.vello.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.model.Chat
import com.mr10.vello.data.model.Message
import com.mr10.vello.data.repository.ChatRepository
import com.mr10.vello.data.repository.ChatRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl()
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
            _messages.value = repository.getMessages(chatId)
            _isLoading.value = false
            
            repository.observeMessages(chatId).collectLatest { newMessage ->
                _messages.value = _messages.value + newMessage
            }
        }

        typingObservationJob = viewModelScope.launch {
            repository.observeTypingStatus(chatId).collectLatest { (userId, typing) ->
                // Typically you'd track per-user typing status, but for simplicity:
                _isTyping.value = typing
            }
        }
    }

    fun sendMessage(chatId: String, senderId: String, content: String) {
        viewModelScope.launch {
            val pendingMessage = Message(
                chatId = chatId,
                senderId = senderId,
                content = content,
                status = com.mr10.vello.data.model.MessageStatus.PENDING
            )
            _messages.value = _messages.value + pendingMessage
            
            try {
                repository.sendMessage(pendingMessage.copy(status = com.mr10.vello.data.model.MessageStatus.SENT))
                // The actual message will be received via observeMessages and replace the pending one
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
