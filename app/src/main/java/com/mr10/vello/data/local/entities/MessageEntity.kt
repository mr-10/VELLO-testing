package com.mr10.vello.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conversation_id"),
        Index("delivery_status")
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    @ColumnInfo(name = "recipient_id") val recipientId: String,
    val content: String,
    @ColumnInfo(name = "message_type") val messageType: String,
    @ColumnInfo(name = "media_url") val mediaUrl: String?,
    @ColumnInfo(name = "media_thumbnail_url") val mediaThumbnailUrl: String?,
    @ColumnInfo(name = "delivery_status") val deliveryStatus: String,
    @ColumnInfo(name = "sent_at") val sentAt: String?,
    @ColumnInfo(name = "delivered_at") val deliveredAt: String?,
    @ColumnInfo(name = "read_at") val readAt: String?,
    @ColumnInfo(name = "is_edited") val isEdited: Boolean = false,
    @ColumnInfo(name = "edited_at") val editedAt: String?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "deleted_at") val deletedAt: String?,
    @ColumnInfo(name = "is_forwarded") val isForwarded: Boolean = false,
    @ColumnInfo(name = "original_message_id") val originalMessageId: String?,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: String?
)
