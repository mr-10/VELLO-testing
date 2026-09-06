package com.mr10.vello.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.data.model.Contact
import com.mr10.vello.data.repository.ContactRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Contact>>(emptyList())
    val searchResults: StateFlow<List<Contact>> = _searchResults.asStateFlow()

    private val _discoverUsers = MutableStateFlow<List<Contact>>(emptyList())
    val discoverUsers: StateFlow<List<Contact>> = _discoverUsers.asStateFlow()

    fun syncContacts() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val deviceContacts = repository.getDeviceContacts()
                val syncedContacts = repository.syncContacts(deviceContacts)
                _contacts.value = syncedContacts
                
                // Fetch registered users for discovery
                val discovered = repository.fetchRegisteredUsers()
                _discoverUsers.value = discovered
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.length >= 3) {
            viewModelScope.launch {
                repository.searchUsers(query).collect { results ->
                    _searchResults.value = results
                }
            }
        } else {
            _searchResults.value = emptyList()
        }
    }
}
