package com.mr10.vello.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "auth_id") val authId: String,
    val username: String,
    @ColumnInfo(name = "display_name") val displayName: String?,
    val bio: String?,
    @ColumnInfo(name = "profile_picture_url") val profilePictureUrl: String?,
    @ColumnInfo(name = "phone_number") val phoneNumber: String?,
    @ColumnInfo(name = "user_status") val status: String?,
    @ColumnInfo(name = "status_updated_at") val statusUpdatedAt: String?,
    @ColumnInfo(name = "last_seen_at") val lastSeenAt: String?,
    @ColumnInfo(name = "is_online") val isOnline: Boolean = false,
    @ColumnInfo(name = "is_blocked") val isBlocked: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: String?
)
