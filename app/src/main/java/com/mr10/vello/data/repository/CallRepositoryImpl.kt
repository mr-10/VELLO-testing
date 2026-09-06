package com.mr10.vello.data.repository

import com.mr10.vello.VelloApplication
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class CallRepositoryImpl : CallRepository {
    private val realtime by lazy { VelloApplication.supabaseClient.realtime }

    override suspend fun makeCall(receiverId: String, isVideo: Boolean) {
        // TODO: Implement Supabase Realtime Broadcast to signal the receiver
    }

    override suspend fun acceptCall(callId: String) {
        // TODO: Signal acceptance
    }

    override suspend fun rejectCall(callId: String) {
        // TODO: Signal rejection
    }

    override suspend fun endCall(callId: String) {
        // TODO: Signal end of call
    }

    override fun observeIncomingCalls(userId: String): Flow<CallEvent> {
        // TODO: Observe Supabase Realtime channel for call events
        return emptyFlow()
    }
}
