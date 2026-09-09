package com.mr10.vello.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.model.UserProfile
import com.mr10.vello.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowsePeoplesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _suggestedUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val suggestedUsers: StateFlow<List<UserProfile>> = _suggestedUsers.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSuggestedUsers()
        subscribeToSearchQuery()
    }

    private fun loadSuggestedUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val users = profileRepository.getAllProfiles()
                _suggestedUsers.value = users
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun subscribeToSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotEmpty()) {
                        _isLoading.value = true
                        try {
                            // Search logic in ProfileRepository if available, or filter local
                            val all = profileRepository.getAllProfiles()
                            _searchResults.value = all.filter { 
                                it.name.contains(query, ignoreCase = true) 
                            }
                        } catch (e: Exception) {
                            // Handle error
                        } finally {
                            _isLoading.value = false
                        }
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun sendConnectionRequest(userId: String) {
        // Implement connection request logic
    }
}
