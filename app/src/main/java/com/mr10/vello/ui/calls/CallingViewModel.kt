package com.mr10.vello.ui.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.repository.CallRepository
import com.mr10.vello.data.repository.CallRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallingViewModel(
    private val repository: CallRepository = CallRepositoryImpl()
) : ViewModel() {

    private val _callState = MutableStateFlow<CallUiState>(CallUiState.Idle)
    val callState: StateFlow<CallUiState> = _callState.asStateFlow()

    fun startOutgoingCall(receiverId: String, receiverName: String, isVideo: Boolean) {
        _callState.value = CallUiState.Outgoing(receiverId, receiverName, isVideo)
        viewModelScope.launch {
            repository.makeCall(receiverId, isVideo)
        }
    }

    fun receiveIncomingCall(callerId: String, callerName: String, isVideo: Boolean) {
        _callState.value = CallUiState.Incoming("temp_call_id", callerId, callerName, isVideo)
    }

    fun acceptCall() {
        val currentState = _callState.value
        if (currentState is CallUiState.Incoming) {
            viewModelScope.launch {
                repository.acceptCall(currentState.callId)
                _callState.value = CallUiState.Ongoing(currentState.callId, currentState.callerName, currentState.isVideo)
            }
        }
    }

    fun rejectCall() {
        val currentState = _callState.value
        if (currentState is CallUiState.Incoming) {
            viewModelScope.launch {
                repository.rejectCall(currentState.callId)
                _callState.value = CallUiState.Idle
            }
        }
    }

    fun endCall() {
        val currentState = _callState.value
        if (currentState is CallUiState.Ongoing) {
            viewModelScope.launch {
                repository.endCall(currentState.callId)
                _callState.value = CallUiState.Idle
            }
        } else if (currentState is CallUiState.Outgoing) {
            viewModelScope.launch {
                // repository.cancelCall(...)
                _callState.value = CallUiState.Idle
            }
        }
    }
}

sealed class CallUiState {
    object Idle : CallUiState()
    data class Incoming(val callId: String, val callerId: String, val callerName: String, val isVideo: Boolean) : CallUiState()
    data class Outgoing(val receiverId: String, val receiverName: String, val isVideo: Boolean) : CallUiState()
    data class Ongoing(val callId: String, val partnerName: String, val isVideo: Boolean) : CallUiState()
}
