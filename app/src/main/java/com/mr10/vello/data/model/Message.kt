package com.mr10.vello.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String? = null,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("recipient_id")
    val recipientId: String,
    val content: String,
    @SerialName("message_type")
    val messageType: MessageType = MessageType.TEXT,
    @SerialName("media_url")
    val mediaUrl: String? = null,
    @SerialName("media_thumbnail_url")
    val mediaThumbnailUrl: String? = null,
    @SerialName("delivery_status")
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
    @SerialName("sent_at")
    val sentAt: String? = null,
    @SerialName("delivered_at")
    val deliveredAt: String? = null,
    @SerialName("read_at")
    val readAt: String? = null,
    @SerialName("is_edited")
    val isEdited: Boolean = false,
    @SerialName("edited_at")
    val editedAt: String? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false,
    @SerialName("community_id")
    val communityId: String? = null
)

enum class MessageType {
    @SerialName("text") TEXT,
    @SerialName("image") IMAGE,
    @SerialName("video") VIDEO,
    @SerialName("audio") AUDIO,
    @SerialName("file") FILE,
    @SerialName("location") LOCATION,
    @SerialName("contact") CONTACT
}

enum class DeliveryStatus {
    @SerialName("pending") PENDING,
    @SerialName("sent") SENT,
    @SerialName("delivered") DELIVERED,
    @SerialName("read") READ,
    @SerialName("failed") FAILED
}
