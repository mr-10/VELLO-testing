package com.mr10.vello.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mr10.vello.ui.theme.VelloOnSurface
import com.mr10.vello.ui.theme.VelloOnSurfaceVariant
import com.mr10.vello.ui.theme.VelloPrimaryContainer
import com.mr10.vello.ui.theme.VelloSecondary
import com.mr10.vello.ui.theme.VelloTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAvatar: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VelloPrimaryContainer,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
                .padding(innerPadding)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                UserProfileSection(
                    name = profile?.name ?: currentUser?.email?.substringBefore("@") ?: "User",
                    status = profile?.statusQuote ?: "Hey there! I am using Vello.",
                    imageUrl = profile?.profilePictureUrl,
                    onClick = onNavigateToProfileEdit
                )
            }
            item {
                Text(
                    "Settings & Preferences",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp).padding(top = 8.dp)
                )
            }
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Key,
                            title = "Account",
                            subtitle = "Security notifications, passkeys, change number",
                            onClick = onNavigateToAccount
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFF0F2F5))
                        SettingsItem(
                            icon = Icons.Default.Lock,
                            title = "Privacy",
                            subtitle = "Block contacts, disappearing messages",
                            onClick = onNavigateToPrivacy
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFF0F2F5))
                        SettingsItem(
                            icon = Icons.Default.Mood,
                            title = "Avatar",
                            subtitle = "Create, edit, profile photo",
                            onClick = onNavigateToAvatar
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Chat,
                            title = "Chats",
                            subtitle = "Theme, wallpapers, chat history",
                            onClick = onNavigateToChats
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFF0F2F5))
                        SettingsItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Message, group & call tones",
                            onClick = onNavigateToNotifications
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFF0F2F5))
                        SettingsItem(
                            icon = Icons.Default.PieChart,
                            title = "Storage and data",
                            subtitle = "Network usage, auto-download",
                            onClick = onNavigateToStorage
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    SettingsItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Help",
                        subtitle = "Help centre, contact us, privacy policy",
                        onClick = onNavigateToHelp
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("from", fontSize = 12.sp, color = Color.Gray)
                    Text("VELLO", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = VelloOnSurface)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun UserProfileSection(name: String, status: String, imageUrl: String?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(64.dp)
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
                Text(text = name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VelloOnSurface)
                Text(text = status, fontSize = 14.sp, color = Color.Gray, maxLines = 1)
            }
            Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = VelloSecondary)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color(0xFFF0F2F5)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = VelloOnSurfaceVariant, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VelloOnSurface)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}
