package com.mr10.vello.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name")
    val name: String,
    @SerialName("date_of_birth")
    val dob: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    @SerialName("avatar_url")
    val profilePictureUrl: String? = null,
    @SerialName("status_quote")
    val statusQuote: String? = "Hey there! I am using Vello.",
    @SerialName("created_at")
    val createdAt: String? = null
)
