package com.mr10.vello.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: String,
    val name: String,
    @SerialName("profile_picture_url")
    val profilePictureUrl: String? = null,
    @SerialName("last_message")
    val lastMessage: String? = null,
    @SerialName("last_message_timestamp")
    val lastMessageTimestamp: String? = null,
    @SerialName("unread_count")
    val unreadCount: Int = 0
)
