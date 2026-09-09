package com.mr10.vello.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "initiator_id") val initiatorId: String,
    @ColumnInfo(name = "recipient_id") val recipientId: String,
    @ColumnInfo(name = "call_type") val callType: String,
    val status: String,
    @ColumnInfo(name = "started_at") val startedAt: String?,
    @ColumnInfo(name = "ended_at") val endedAt: String?,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Int = 0,
    @ColumnInfo(name = "is_missed") val isMissed: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: String?
)
