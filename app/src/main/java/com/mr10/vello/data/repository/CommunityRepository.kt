package com.mr10.vello.data.repository

import com.mr10.vello.data.model.Community
import com.mr10.vello.data.model.Message
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    suspend fun getCommunities(): List<Community>
    suspend fun getCommunity(id: String): Community?
    suspend fun joinCommunity(userId: String, communityId: String)
    suspend fun leaveCommunity(userId: String, communityId: String)
    fun observeCommunityMessages(communityId: String): Flow<Message>
}
