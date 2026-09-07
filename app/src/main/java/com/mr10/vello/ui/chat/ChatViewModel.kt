package com.mr10.vello.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.model.Chat
import com.mr10.vello.data.model.Message
import com.mr10.vello.data.model.MessageStatus
import com.mr10.vello.data.repository.ChatRepository
import com.mr10.vello.data.repository.ChatRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
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
            try {
                _messages.value = repository.getMessages(chatId)
                _isLoading.value = false
                
                repository.observeMessages(chatId).collectLatest { newMessage ->
                    _messages.value = _messages.value + newMessage
                }
            } catch (e: Exception) {
                _isLoading.value = false
                // Log or handle error
                Log.e("ChatViewModel", "Error loading messages", e)
            }
        }

        typingObservationJob = viewModelScope.launch {
            repository.observeTypingStatus(chatId).collectLatest { (userId, typing) ->
                // Typically you'd track per-user typing status, but for simplicity:
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
            val pendingMessage = Message(
                chatId = chatId,
                senderId = senderId,
                content = content,
                status = com.mr10.vello.data.model.MessageStatus.PENDING
            )
            
            // Add to local list for immediate UI feedback
            _messages.value = _messages.value + pendingMessage
            
            try {
                // Send to repository
                repository.sendMessage(pendingMessage.copy(status = MessageStatus.SENT))
                Log.d("ChatViewModel", "Message sent successfully")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message: ${e.message}", e)
                // Update the last message to failed status (optional: remove it or show error)
                _messages.value = _messages.value.map { 
                    if (it == pendingMessage) it.copy(status = MessageStatus.PENDING) // Keep as pending but maybe add an error flag
                    else it
                }
            }
        }
    }
}
