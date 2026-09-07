package com.mr10.vello.data.repository

import android.util.Log
import com.mr10.vello.VelloApplication
import com.mr10.vello.data.model.Chat
import com.mr10.vello.data.model.Message
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import java.time.Instant

class ChatRepositoryImpl : ChatRepository {
    private val postgrest by lazy { VelloApplication.supabaseClient.postgrest }
    private val realtime by lazy { VelloApplication.supabaseClient.realtime }

    override suspend fun getChats(): List<Chat> {
        return try {
            postgrest["chats"]
                .select()
                .decodeList<Chat>()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching chats: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMessages(chatId: String): List<Message> {
        return try {
            postgrest["messages"]
                .select {
                    filter {
                        eq("chat_id", chatId)
                    }
                }
                .decodeList<Message>()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching messages for chat $chatId: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun sendMessage(message: Message) {
        try {
            val messageToSend = if (message.created_at == null) {
                message.copy(created_at = Instant.now().toString())
            } else {
                message
            }
            postgrest["messages"].insert(messageToSend)
            Log.d("ChatRepository", "Message insert successful")
        } catch (e: RestException) {
            Log.e("ChatRepository", "Supabase Rest Error: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("ChatRepository", "Unexpected error sending message: ${e.message}", e)
            throw e
        }
    }

    override fun observeMessages(chatId: String): Flow<Message> {
        val channel = realtime.channel("chat_$chatId")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
        }
        return flow
            .onStart { channel.subscribe() }
            .mapNotNull { 
                val msg = it.decodeRecord<Message>()
                if (msg.chatId == chatId) msg else null
            }
    }

    override fun observeAllMessages(): Flow<Message> {
        val channel = realtime.channel("global_messages")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
        }
        return flow
            .onStart { channel.subscribe() }
            .mapNotNull { it.decodeRecord<Message>() }
    }

    override suspend fun sendTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        // Implement Supabase Realtime Broadcast if available in the SDK version
    }

    override fun observeTypingStatus(chatId: String): Flow<Pair<String, Boolean>> {
        // Return a flow that emits typing status changes
        return kotlinx.coroutines.flow.emptyFlow()
    }
}
