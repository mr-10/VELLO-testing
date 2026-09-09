package com.mr10.vello.data.repository

import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.broadcast.BroadcastPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.UUID
import javax.inject.Inject

@Serializable
data class CallSignal(
    val type: String, // "INVITE", "RINGING", "ACCEPT", "REJECT", "END"
    val callId: String,
    val callerId: String,
    val callerName: String,
    val isVideo: Boolean
)

class CallRepositoryImpl @Inject constructor(
    private val realtime: Realtime
) : CallRepository {

    private suspend fun sendCallSignal(targetId: String, signal: CallSignal) {
        val channel = realtime.channel("calls_$targetId")
        channel.broadcast(
            event = "call_event",
            payload = BroadcastPayload.Json(Json.encodeToJsonElement(signal))
        )
    }

    override suspend fun makeCall(receiverId: String, callerId: String, callerName: String, isVideo: Boolean) {
        val callId = UUID.randomUUID().toString()
        sendCallSignal(receiverId, CallSignal("INVITE", callId, callerId, callerName, isVideo))
    }

    override suspend fun sendRinging(callerId: String, callId: String) {
        sendCallSignal(callerId, CallSignal("RINGING", callId, "", "", false))
    }

    override suspend fun acceptCall(callerId: String, callId: String) {
        sendCallSignal(callerId, CallSignal("ACCEPT", callId, "", "", false))
    }

    override suspend fun rejectCall(callerId: String, callId: String) {
        sendCallSignal(callerId, CallSignal("REJECT", callId, "", "", false))
    }

    override suspend fun endCall(targetId: String, callId: String) {
        sendCallSignal(targetId, CallSignal("END", callId, "", "", false))
    }

    override fun observeIncomingCalls(userId: String): Flow<CallEvent> {
        val channel = realtime.channel("calls_$userId")
        return channel.broadcastFlow<CallSignal>(event = "call_event")
            .onStart { channel.subscribe() }
            .mapNotNull { signal ->
                when (signal.type) {
                    "INVITE" -> CallEvent.Incoming(signal.callId, signal.callerId, signal.callerName, signal.isVideo)
                    "RINGING" -> CallEvent.Ringing(signal.callId)
                    "ACCEPT" -> CallEvent.Accepted(signal.callId)
                    "REJECT" -> CallEvent.Rejected(signal.callId)
                    "END" -> CallEvent.Ended(signal.callId)
                    else -> null
                }
            }
    }
}
