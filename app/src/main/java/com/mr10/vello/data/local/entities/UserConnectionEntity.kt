package com.mr10.vello.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_connections")
data class UserConnectionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id_1") val userId1: String,
    @ColumnInfo(name = "user_id_2") val userId2: String,
    val status: String,
    @ColumnInfo(name = "initiated_by") val initiatedBy: String,
    @ColumnInfo(name = "connected_at") val connectedAt: String?,
    @ColumnInfo(name = "created_at") val createdAt: String?
)
