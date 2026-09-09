package com.mr10.vello.ui.communities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.model.Community
import com.mr10.vello.data.model.UserProfile
import com.mr10.vello.data.repository.CommunityRepository
import com.mr10.vello.data.repository.CommunityRepositoryImpl
import com.mr10.vello.data.repository.ProfileRepository
import com.mr10.vello.data.repository.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunitiesViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val communityRepository: CommunityRepository = CommunityRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunitiesUiState>(CommunitiesUiState.Loading)
    val uiState: StateFlow<CommunitiesUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = CommunitiesUiState.Loading
            try {
                val profiles = profileRepository.getAllProfiles()
                val communities = communityRepository.getCommunities()
                _uiState.value = CommunitiesUiState.Success(profiles, communities)
            } catch (e: Exception) {
                _uiState.value = CommunitiesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CommunitiesUiState {
    object Loading : CommunitiesUiState()
    data class Success(val profiles: List<UserProfile>, val communities: List<Community> = emptyList()) : CommunitiesUiState()
    data class Error(val message: String) : CommunitiesUiState()
}
