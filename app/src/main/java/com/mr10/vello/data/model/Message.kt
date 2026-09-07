package com.mr10.vello.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String? = null,
    @SerialName("chat_id")
    val chatId: String,
    @SerialName("sender_id")
    val senderId: String,
    val content: String,
    @SerialName("created_at")
    val created_at: String? = null,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    PENDING, SENT, DELIVERED, READ
}
