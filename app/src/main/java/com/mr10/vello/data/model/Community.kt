package com.mr10.vello.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Community(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("profile_picture_url")
    val profilePictureUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("member_count")
    val memberCount: Int = 0
)
