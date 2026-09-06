package com.mr10.vello.ui.navigation

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey as BaseNavKey

sealed interface NavKey : BaseNavKey {
    @Serializable
    data object Splash : NavKey

    @Serializable
    data object EmailLogin : NavKey

    @Serializable
    data object Home : NavKey

    @Serializable
    data object ContactList : NavKey

    @Serializable
    data class OtpVerification(val identifier: String, val isEmail: Boolean) : NavKey

    @Serializable
    data object ProfileSetup : NavKey

    @Serializable
    data class ChatDetail(val chatId: String, val chatName: String) : NavKey

    @Serializable
    data class IncomingCall(val callerName: String, val callerId: String, val isVideo: Boolean) : NavKey

    @Serializable
    data class OutgoingCall(val receiverName: String, val receiverId: String, val isVideo: Boolean) : NavKey

    @Serializable
    data object Settings : NavKey

    @Serializable
    data object ProfileEdit : NavKey

    @Serializable
    data object Communities : NavKey

    @Serializable
    data object AccountSettings : NavKey

    @Serializable
    data object PrivacySettings : NavKey

    @Serializable
    data object AvatarPersona : NavKey

    @Serializable
    data object ChatSettings : NavKey

    @Serializable
    data object NotificationSettings : NavKey

    @Serializable
    data object StorageData : NavKey

    @Serializable
    data object Help : NavKey

    @Serializable
    data object Camera : NavKey

    @Serializable
    data object Search : NavKey
}
