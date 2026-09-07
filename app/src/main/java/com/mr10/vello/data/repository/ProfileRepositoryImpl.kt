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

    override suspend fun getAllProfiles(): List<UserProfile> {
        return try {
            postgrest["profiles"]
                .select()
                .decodeList<UserProfile>()
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error fetching all profiles", e)
            emptyList()
        }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        postgrest["profiles"].upsert(profile)
    }

    override suspend fun uploadProfilePicture(userId: String, byteArray: ByteArray): String {
        return try {
            val bucket = storage["profile_pictures"]
            val timestamp = System.currentTimeMillis()
            val path = "$userId/profile_$timestamp.jpg"
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

    override suspend fun deleteProfilePicture(userId: String, fileName: String) {
        try {
            val bucket = storage["profile_pictures"]
            val path = "$userId/$fileName"
            bucket.delete(path)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error deleting profile picture: ${e.message}")
        }
    }

    override suspend fun updateEmailVisibility(userId: String, isHidden: Boolean) {
        postgrest["profiles"].update(
            mapOf("is_email_hidden" to isHidden)
        ) {
            filter {
                eq("id", userId)
            }
        }
    }

    override suspend fun scheduleAccountDeletion(userId: String, timestamp: String?) {
        postgrest["profiles"].update(
            mapOf("deletion_scheduled_at" to timestamp)
        ) {
            filter {
                eq("id", userId)
            }
        }
    }
}
