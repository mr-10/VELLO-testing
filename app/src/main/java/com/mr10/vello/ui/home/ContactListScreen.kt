package com.mr10.vello.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mr10.vello.data.model.Contact
import com.mr10.vello.ui.components.ContactDetailBottomSheet
import com.mr10.vello.ui.components.PermissionHandler
import com.mr10.vello.ui.theme.WhatsAppHeaderLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    viewModel: ContactViewModel,
    onContactClick: (Contact) -> Unit,
    onBack: () -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val discoverUsers by viewModel.discoverUsers.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    PermissionHandler(
        permissions = listOf(android.Manifest.permission.READ_CONTACTS),
        rationaleText = "Vello needs to access your contacts to find friends."
    ) {
        LaunchedEffect(Unit) {
            viewModel.syncContacts()
        }

        if (selectedContact != null) {
            ContactDetailBottomSheet(
                contact = selectedContact!!,
                onDismiss = { selectedContact = null },
                onChatClick = {
                    onContactClick(it)
                    selectedContact = null
                },
                onVoiceCallClick = { /* Handle */ },
                onVideoCallClick = { /* Handle */ }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Contact", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = WhatsAppHeaderLight,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Search by name or number") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {}

                if (isSyncing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val displayList = if (searchQuery.isNotEmpty()) searchResults else contacts
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (searchQuery.isEmpty() && discoverUsers.isNotEmpty()) {
                            item {
                                Text(
                                    "Discover New Friends",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            items(discoverUsers.take(5)) { contact ->
                                ContactItem(contact = contact, onClick = { selectedContact = contact })
                            }
                        }

                        item {
                            Text(
                                "Contacts on Vello",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        items(displayList.filter { it.isRegistered }) { contact ->
                            ContactItem(contact = contact, onClick = { selectedContact = contact })
                        }
                        
                        if (searchQuery.isEmpty()) {
                            item {
                                Text(
                                    "Invite to Vello",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Gray
                                )
                            }
                            
                            items(displayList.filter { !it.isRegistered }) { contact ->
                                ContactItem(contact = contact, onClick = { /* Invite logic */ })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            color = Color.LightGray
        ) {
            if (contact.profilePictureUrl != null) {
                AsyncImage(
                    model = contact.profilePictureUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (contact.isRegistered && !contact.statusQuote.isNullOrEmpty()) {
                Text(contact.statusQuote, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
            } else {
                Text(contact.phoneNumber, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}
