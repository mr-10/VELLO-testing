package com.mr10.vello.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageReadReceipt(
    val id: String? = null,
    @SerialName("message_id")
    val messageId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("read_at")
    val readAt: String
)
