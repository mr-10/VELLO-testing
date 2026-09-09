package com.mr10.vello.data.repository

import android.util.Log
import com.mr10.vello.data.model.Conversation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject

class ConversationRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val postgrest = supabase.postgrest
    private val realtime = supabase.realtime

    suspend fun getConversation(conversationId: String): Conversation {
        return postgrest["conversations"]
            .select {
                filter {
                    eq("id", conversationId)
                }
            }
            .decodeSingle<Conversation>()
    }

    suspend fun setUserTyping(
        conversationId: String,
        userId: String,
        isTyping: Boolean
    ) {
        try {
            postgrest["typing_status"]
                .upsert(
                    mapOf(
                        "conversation_id" to conversationId,
                        "user_id" to userId,
                        "is_typing" to isTyping,
                        "updated_at" to Instant.now().toString()
                    )
                )
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Typing update failed", e)
        }
    }

    fun subscribeToTypingStatus(conversationId: String): Flow<List<String>> = flow {
        try {
            val typingUsers = mutableSetOf<String>()
            val channel = realtime.channel("typing_$conversationId")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "typing_status"
            }
            channel.subscribe()
            
            emitAll(changeFlow.map { action ->
                when (action) {
                    is PostgresAction.Insert -> {
                        val record = action.decodeRecord<TypingRecord>()
                        if (record.conversationId == conversationId) {
                            if (record.isTyping) typingUsers.add(record.userId) else typingUsers.remove(record.userId)
                        }
                    }
                    is PostgresAction.Update -> {
                        val record = action.decodeRecord<TypingRecord>()
                        if (record.conversationId == conversationId) {
                            if (record.isTyping) typingUsers.add(record.userId) else typingUsers.remove(record.userId)
                        }
                    }
                    is PostgresAction.Delete -> {
                        val userId = action.oldRecord["user_id"]?.toString()?.replace("\"", "")
                        userId?.let { typingUsers.remove(it) }
                    }
                    else -> {}
                }
                typingUsers.toList()
            })
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Subscribe typing failed", e)
        }
    }

    @Serializable
    private data class TypingRecord(
        @SerialName("conversation_id")
        val conversationId: String,
        @SerialName("user_id")
        val userId: String,
        @SerialName("is_typing")
        val isTyping: Boolean
    )
}
