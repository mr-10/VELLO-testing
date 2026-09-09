package com.mr10.vello.data.repository

import android.util.Log
import com.mr10.vello.data.model.DeliveryStatus
import com.mr10.vello.data.model.Message
import com.mr10.vello.data.model.MessageReadReceipt
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import java.time.Instant
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val postgrest = supabase.postgrest
    private val realtime = supabase.realtime

    suspend fun sendMessage(message: Message): Result<Message> {
        return try {
            val sentMessage = postgrest["messages"]
                .insert(message) {
                    select()
                }
                .decodeSingle<Message>()
            Result.success(sentMessage)
        } catch (e: Exception) {
            Log.e("MessageRepo", "Error sending message", e)
            Result.failure(e)
        }
    }

    fun getConversationMessages(
        conversationId: String,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Message>> = flow {
        try {
            // Initial load
            val messages = postgrest["messages"]
                .select {
                    filter {
                        eq("conversation_id", conversationId)
                    }
                    order("sent_at", Order.ASCENDING)
                    limit(limit.toLong())
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<Message>()

            val currentMessages = messages.toMutableList()
            emit(currentMessages.toList())

            // Subscribe to changes
            val channel = realtime.channel("messages_$conversationId")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
            }

            channel.subscribe()

            emitAll(changeFlow.map { action ->
                when (action) {
                    is PostgresAction.Insert -> {
                        val newMessage = action.decodeRecord<Message>()
                        if (newMessage.conversationId == conversationId && currentMessages.none { it.id == newMessage.id }) {
                            currentMessages.add(newMessage)
                        }
                    }
                    is PostgresAction.Update -> {
                        val updatedMessage = action.decodeRecord<Message>()
                        if (updatedMessage.conversationId == conversationId) {
                            val index = currentMessages.indexOfFirst { it.id == updatedMessage.id }
                            if (index != -1) {
                                currentMessages[index] = updatedMessage
                            }
                        }
                    }
                    is PostgresAction.Delete -> {
                        val deletedId = action.oldRecord["id"]?.toString()?.replace("\"", "")
                        currentMessages.removeAll { it.id == deletedId }
                    }
                    else -> {}
                }
                currentMessages.toList().sortedBy { it.sentAt }
            })

        } catch (e: Exception) {
            Log.e("MessageRepo", "Error loading messages", e)
            throw e
        }
    }

    fun subscribeToMessageStatus(messageId: String): Flow<DeliveryStatus> = flow {
        try {
            val channel = realtime.channel("status_$messageId")
            val changeFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "messages"
            }
            channel.subscribe()
            emitAll(changeFlow.mapNotNull { action ->
                val msg = action.decodeRecord<Message>()
                if (msg.id == messageId) msg.deliveryStatus else null
            })
        } catch (e: Exception) {
            Log.e("MessageRepo", "Error subscribing to status", e)
            throw e
        }
    }

    suspend fun markAsRead(messageId: String, userId: String): Result<Unit> {
        return try {
            postgrest["message_read_receipts"]
                .insert(
                    MessageReadReceipt(
                        messageId = messageId,
                        userId = userId,
                        readAt = Instant.now().toString()
                    )
                )

            postgrest["messages"]
                .update(
                    mapOf(
                        "delivery_status" to "read",
                        "read_at" to Instant.now().toString()
                    )
                ) {
                    filter {
                        eq("id", messageId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            postgrest["messages"]
                .update(
                    mapOf(
                        "is_deleted" to true
                    )
                ) {
                    filter {
                        eq("id", messageId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editMessage(messageId: String, newContent: String): Result<Unit> {
        return try {
            postgrest["messages"]
                .update(
                    mapOf(
                        "content" to newContent,
                        "is_edited" to true,
                        "edited_at" to Instant.now().toString()
                    )
                ) {
                    filter {
                        eq("id", messageId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
