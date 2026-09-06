package com.mr10.vello.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String? = null,
    val chatId: String,
    val senderId: String,
    val content: String,
    val createdAt: String? = null,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    PENDING, SENT, DELIVERED, READ
}
