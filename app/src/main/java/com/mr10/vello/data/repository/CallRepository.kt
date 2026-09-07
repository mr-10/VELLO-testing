package com.mr10.vello.data.repository

import kotlinx.coroutines.flow.Flow

interface CallRepository {
    suspend fun makeCall(receiverId: String, callerId: String, callerName: String, isVideo: Boolean)
    suspend fun sendRinging(callerId: String, callId: String)
    suspend fun acceptCall(callerId: String, callId: String)
    suspend fun rejectCall(callerId: String, callId: String)
    suspend fun endCall(targetId: String, callId: String)
    fun observeIncomingCalls(userId: String): Flow<CallEvent>
}

sealed class CallEvent {
    data class Incoming(val callId: String, val callerId: String, val callerName: String, val isVideo: Boolean) : CallEvent()
    data class Ringing(val callId: String) : CallEvent()
    data class Accepted(val callId: String) : CallEvent()
    data class Rejected(val callId: String) : CallEvent()
    data class Ended(val callId: String) : CallEvent()
}
