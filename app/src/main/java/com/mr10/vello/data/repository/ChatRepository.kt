package com.mr10.vello.data.repository

import com.mr10.vello.data.model.Chat
import com.mr10.vello.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChats(): List<Chat>
    suspend fun getMessages(chatId: String): List<Message>
    suspend fun sendMessage(message: Message)
    suspend fun createOrUpdateChatMembers(chatId: String, userId: String)
    fun observeMessages(chatId: String): Flow<Message>
    fun observeAllMessages(): Flow<Message>
    suspend fun sendTypingStatus(chatId: String, userId: String, isTyping: Boolean)
    fun observeTypingStatus(chatId: String): Flow<Pair<String, Boolean>>
}
