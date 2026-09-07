package com.mr10.vello.data.repository

import com.mr10.vello.data.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun getAllProfiles(): List<UserProfile>
    suspend fun updateProfile(profile: UserProfile)
    suspend fun uploadProfilePicture(userId: String, byteArray: ByteArray): String
    suspend fun deleteProfilePicture(userId: String, fileName: String)
    suspend fun updateEmailVisibility(userId: String, isHidden: Boolean)
    suspend fun scheduleAccountDeletion(userId: String, timestamp: String?)
}
