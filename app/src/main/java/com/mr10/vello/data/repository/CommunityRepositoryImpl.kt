package com.mr10.vello.data.repository

import android.util.Log
import com.mr10.vello.VelloApplication
import com.mr10.vello.data.model.Community
import com.mr10.vello.data.model.Message
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import java.time.Instant

class CommunityRepositoryImpl : CommunityRepository {
    private val postgrest by lazy { VelloApplication.supabaseClient.postgrest }
    private val realtime by lazy { VelloApplication.supabaseClient.realtime }

    override suspend fun getCommunities(): List<Community> {
        return try {
            postgrest["communities"]
                .select()
                .decodeList<Community>()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error fetching communities: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getCommunity(id: String): Community? {
        return try {
            postgrest["communities"]
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<Community>()
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error fetching community $id: ${e.message}", e)
            null
        }
    }

    override suspend fun joinCommunity(userId: String, communityId: String) {
        try {
            postgrest["community_members"].upsert(
                mapOf(
                    "community_id" to communityId,
                    "user_id" to userId,
                    "joined_at" to Instant.now().toString()
                )
            )
            Log.d("CommunityRepository", "User $userId joined community $communityId")
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error joining community: ${e.message}", e)
            throw e
        }
    }

    override suspend fun leaveCommunity(userId: String, communityId: String) {
        try {
            postgrest["community_members"].delete {
                filter {
                    eq("community_id", communityId)
                    eq("user_id", userId)
                }
            }
            Log.d("CommunityRepository", "User $userId left community $communityId")
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error leaving community: ${e.message}", e)
            throw e
        }
    }

    override fun observeCommunityMessages(communityId: String): Flow<Message> {
        val channel = realtime.channel("community_$communityId")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
        }
        return flow
            .onStart { channel.subscribe() }
            .mapNotNull { 
                val msg = it.decodeRecord<Message>()
                if (msg.communityId == communityId) msg else null
            }
    }
}
