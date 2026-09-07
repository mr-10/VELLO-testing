package com.mr10.vello.ui.communities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.model.UserProfile
import com.mr10.vello.data.repository.ProfileRepository
import com.mr10.vello.data.repository.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunitiesViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunitiesUiState>(CommunitiesUiState.Loading)
    val uiState: StateFlow<CommunitiesUiState> = _uiState

    init {
        fetchAllProfiles()
    }

    fun fetchAllProfiles() {
        viewModelScope.launch {
            _uiState.value = CommunitiesUiState.Loading
            try {
                val profiles = profileRepository.getAllProfiles()
                _uiState.value = CommunitiesUiState.Success(profiles)
            } catch (e: Exception) {
                _uiState.value = CommunitiesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CommunitiesUiState {
    object Loading : CommunitiesUiState()
    data class Success(val profiles: List<UserProfile>) : CommunitiesUiState()
    data class Error(val message: String) : CommunitiesUiState()
}
