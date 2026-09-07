package com.mr10.vello.ui.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.repository.CallEvent
import com.mr10.vello.data.repository.CallRepository
import com.mr10.vello.data.repository.CallRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CallingViewModel(
    private val repository: CallRepository = CallRepositoryImpl()
) : ViewModel() {

    private val _callState = MutableStateFlow<CallUiState>(CallUiState.Idle)
    val callState: StateFlow<CallUiState> = _callState.asStateFlow()

    fun init(userId: String) {
        viewModelScope.launch {
            repository.observeIncomingCalls(userId).collect { event ->
                when (event) {
                    is CallEvent.Incoming -> {
                        _callState.value = CallUiState.Incoming(event.callId, event.callerId, event.callerName, event.isVideo)
                        // Automatically send RINGING signal back
                        repository.sendRinging(event.callerId, event.callId)
                    }
                    is CallEvent.Ringing -> {
                        val current = _callState.value
                        if (current is CallUiState.Outgoing && current.callId == event.callId) {
                            _callState.value = current.copy(isRinging = true)
                        }
                    }
                    is CallEvent.Accepted -> {
                        val current = _callState.value
                        if (current is CallUiState.Outgoing && current.callId == event.callId) {
                            _callState.value = CallUiState.Ongoing(event.callId, current.receiverId, current.receiverName, current.isVideo)
                        }
                    }
                    is CallEvent.Rejected -> {
                        val current = _callState.value
                        if ((current is CallUiState.Outgoing && current.callId == event.callId) ||
                            (current is CallUiState.Incoming && current.callId == event.callId)) {
                            _callState.value = CallUiState.Idle
                        }
                    }
                    is CallEvent.Ended -> {
                        val current = _callState.value
                        if (current is CallUiState.Ongoing && current.callId == event.callId) {
                            _callState.value = CallUiState.Idle
                        }
                    }
                }
            }
        }
    }

    fun startOutgoingCall(receiverId: String, receiverName: String, callerId: String, callerName: String, isVideo: Boolean) {
        val callId = UUID.randomUUID().toString()
        _callState.value = CallUiState.Outgoing(callId, receiverId, receiverName, isVideo)
        viewModelScope.launch {
            repository.makeCall(receiverId, callerId, callerName, isVideo) 
        }
    }

    fun acceptCall() {
        val currentState = _callState.value
        if (currentState is CallUiState.Incoming) {
            viewModelScope.launch {
                repository.acceptCall(currentState.callerId, currentState.callId)
                _callState.value = CallUiState.Ongoing(currentState.callId, currentState.callerId, currentState.callerName, currentState.isVideo)
            }
        }
    }

    fun rejectCall() {
        val currentState = _callState.value
        if (currentState is CallUiState.Incoming) {
            viewModelScope.launch {
                repository.rejectCall(currentState.callerId, currentState.callId)
                _callState.value = CallUiState.Idle
            }
        }
    }

    fun endCall() {
        val currentState = _callState.value
        when (currentState) {
            is CallUiState.Ongoing -> {
                viewModelScope.launch {
                    repository.endCall(currentState.partnerId, currentState.callId)
                    _callState.value = CallUiState.Idle
                }
            }
            is CallUiState.Outgoing -> {
                viewModelScope.launch {
                    repository.endCall(currentState.receiverId, currentState.callId)
                    _callState.value = CallUiState.Idle
                }
            }
            else -> {
                _callState.value = CallUiState.Idle
            }
        }
    }
}

sealed class CallUiState {
    object Idle : CallUiState()
    data class Incoming(val callId: String, val callerId: String, val callerName: String, val isVideo: Boolean) : CallUiState()
    data class Outgoing(val callId: String, val receiverId: String, val receiverName: String, val isVideo: Boolean, val isRinging: Boolean = false) : CallUiState()
    data class Ongoing(val callId: String, val partnerId: String, val partnerName: String, val isVideo: Boolean) : CallUiState()
}
