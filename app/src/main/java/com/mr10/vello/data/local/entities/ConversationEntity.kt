package com.mr10.vello.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id_1") val userId1: String,
    @ColumnInfo(name = "user_id_2") val userId2: String,
    @ColumnInfo(name = "last_message_id") val lastMessageId: String?,
    @ColumnInfo(name = "last_message_text") val lastMessageText: String?,
    @ColumnInfo(name = "last_message_timestamp") val lastMessageTimestamp: String?,
    @ColumnInfo(name = "last_message_sender_id") val lastMessageSenderId: String?,
    @ColumnInfo(name = "is_muted_by_1") val isMutedBy1: Boolean = false,
    @ColumnInfo(name = "is_muted_by_2") val isMutedBy2: Boolean = false,
    @ColumnInfo(name = "is_archived_by_1") val isArchivedBy1: Boolean = false,
    @ColumnInfo(name = "is_archived_by_2") val isArchivedBy2: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: String?
)
