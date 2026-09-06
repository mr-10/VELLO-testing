package com.mr10.vello.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val name: String,
    val phoneNumber: String,
    val profilePictureUrl: String? = null,
    val isRegistered: Boolean = false,
    val userId: String? = null,
    val statusQuote: String? = null
)
