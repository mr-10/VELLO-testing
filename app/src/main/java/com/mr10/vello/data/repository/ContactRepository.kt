package com.mr10.vello.data.repository

import com.mr10.vello.data.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    suspend fun getDeviceContacts(): List<Contact>
    suspend fun syncContacts(deviceContacts: List<Contact>): List<Contact>
    suspend fun fetchRegisteredUsers(): List<Contact>
    fun searchUsers(query: String): Flow<List<Contact>>
}
