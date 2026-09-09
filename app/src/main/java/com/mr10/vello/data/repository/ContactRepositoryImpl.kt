package com.mr10.vello.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.mr10.vello.data.model.Contact
import com.mr10.vello.data.model.UserProfile
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postgrest: Postgrest
) : ContactRepository {

    override suspend fun getDeviceContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex).replace("\\s".toRegex(), "").replace("-", "")
                contacts.add(Contact(name = name, phoneNumber = number))
            }
        }
        return contacts.distinctBy { it.phoneNumber }
    }

    override suspend fun syncContacts(deviceContacts: List<Contact>): List<Contact> {
        val phoneNumbers = deviceContacts.map { it.phoneNumber }
        
        val registeredProfiles = postgrest["profiles"]
            .select(Columns.ALL) {
                filter {
                    isIn("phone_number", phoneNumbers)
                }
            }
            .decodeList<UserProfile>()
            
        return deviceContacts.map { contact ->
            val profile = registeredProfiles.find { it.phoneNumber == contact.phoneNumber }
            contact.copy(
                isRegistered = profile != null,
                userId = profile?.id,
                profilePictureUrl = profile?.profilePictureUrl,
                statusQuote = profile?.statusQuote
            )
        }
    }

    override suspend fun fetchRegisteredUsers(): List<Contact> {
        return postgrest["profiles"]
            .select {
                order("created_at", order = Order.ASCENDING)
            }
            .decodeList<UserProfile>()
            .map {
                Contact(
                    name = it.name,
                    phoneNumber = it.phoneNumber ?: "",
                    profilePictureUrl = it.profilePictureUrl,
                    isRegistered = true,
                    userId = it.id,
                    statusQuote = it.statusQuote
                )
            }
    }

    override fun searchUsers(query: String): Flow<List<Contact>> = flow {
        val results = postgrest["profiles"]
            .select {
                filter {
                    or {
                        ilike("name", "%$query%")
                        ilike("phone_number", "%$query%")
                        ilike("email", "%$query%")
                    }
                }
            }
            .decodeList<UserProfile>()
            .map { Contact(
                name = it.name, 
                phoneNumber = it.phoneNumber ?: "", 
                profilePictureUrl = it.profilePictureUrl, 
                isRegistered = true, 
                userId = it.id
            ) }
        emit(results)
    }
}
