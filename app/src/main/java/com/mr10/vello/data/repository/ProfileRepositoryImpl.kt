package com.mr10.vello.data.repository

import android.util.Log
import com.mr10.vello.VelloApplication
import com.mr10.vello.data.model.UserProfile
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.exceptions.RestException

class ProfileRepositoryImpl : ProfileRepository {
    private val postgrest by lazy { VelloApplication.supabaseClient.postgrest }
    private val storage by lazy { VelloApplication.supabaseClient.storage }

    override suspend fun getProfile(userId: String): UserProfile? {
        return postgrest["profiles"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingleOrNull<UserProfile>()
    }

    override suspend fun updateProfile(profile: UserProfile) {
        postgrest["profiles"].upsert(profile)
    }

    override suspend fun uploadProfilePicture(userId: String, byteArray: ByteArray): String {
        return try {
            val bucket = storage["profile_pictures"]
            val path = "$userId/profile.jpg"
            bucket.upload(path, byteArray) {
                upsert = true
            }
            bucket.publicUrl(path)
        } catch (e: RestException) {
            val message = e.message ?: ""
            if (message.contains("Bucket not found", ignoreCase = true)) {
                throw Exception("Storage bucket 'profile_pictures' not found. Please create it in Supabase dashboard.")
            }
            Log.e("ProfileRepository", "Supabase error uploading profile picture: $message")
            throw e
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Unexpected error uploading profile picture", e)
            throw e
        }
    }
}
