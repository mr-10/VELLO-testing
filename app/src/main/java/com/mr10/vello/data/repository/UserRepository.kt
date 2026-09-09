package com.mr10.vello.data.repository

import com.mr10.vello.data.model.UserProfile
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val realtime: Realtime
) {
    fun getCurrentUserId(): String? {
        val status = auth.sessionStatus.value
        return if (status is SessionStatus.Authenticated) {
            status.session.user?.id
        } else {
            null
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            null
        }
    }

    fun subscribeToUserStatus(userId: String): Flow<UserProfile> {
        val channel = realtime.channel("user_status_$userId")
        val flow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "profiles"
        }
        return flow
            .onStart { channel.subscribe() }
            .map { action -> action.decodeRecord<UserProfile>() }
    }
}
