package com.mr10.vello.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mr10.vello.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToProfileEdit: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            item {
                UserProfileSection(
                    name = profile?.name ?: currentUser?.email?.substringBefore("@") ?: "User",
                    status = profile?.statusQuote ?: "Hey there! I am using Vello.",
                    imageUrl = profile?.profilePictureUrl,
                    onClick = onNavigateToProfileEdit
                )
            }
            item { HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f)) }
            item {
                SettingsItem(
                    icon = Icons.Default.Key,
                    title = "Account",
                    subtitle = "Security notifications, change number"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy",
                    subtitle = "Block contacts, disappearing messages"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Chat,
                    title = "Chats",
                    subtitle = "Theme, wallpapers, chat history"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Message, group & call tones"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.DataUsage,
                    title = "Storage and data",
                    subtitle = "Network usage, auto-download"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "App language",
                    subtitle = "English (device's language)"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Help",
                    subtitle = "Help center, contact us, privacy policy"
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Group,
                    title = "Invite a friend",
                    subtitle = ""
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("from", fontSize = 12.sp, color = Color.Gray)
                    Text("VELLO", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun UserProfileSection(name: String, status: String, imageUrl: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            color = Color.LightGray
        ) {
            AsyncImage(
                model = imageUrl ?: "https://ui-avatars.com/api/?name=$name&background=random",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(text = status, fontSize = 14.sp, color = Color.Gray, maxLines = 1)
        }
        IconButton(onClick = { /* QR Code */ }) {
            Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Color(0xFF008069))
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Action */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(text = title, fontSize = 16.sp)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
