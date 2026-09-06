package com.mr10.vello.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: String,
    val name: String,
    val profilePictureUrl: String? = null,
    val lastMessage: String? = null,
    val lastMessageTimestamp: String? = null,
    val unreadCount: Int = 0
)
