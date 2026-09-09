package com.mr10.vello.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    @SerialName("user_id1")
    val userId1: String,
    @SerialName("user_id2")
    val userId2: String,
    @SerialName("last_message")
    val lastMessage: String? = null,
    @SerialName("last_message_at")
    val lastMessageAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
