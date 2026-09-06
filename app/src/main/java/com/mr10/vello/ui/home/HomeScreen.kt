package com.mr10.vello.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import coil.compose.AsyncImage
import com.mr10.vello.R
import com.mr10.vello.data.model.Chat
import com.mr10.vello.ui.auth.AuthViewModel
import com.mr10.vello.ui.chat.ChatListScreen
import com.mr10.vello.ui.chat.ChatViewModel
import com.mr10.vello.ui.status.StatusScreen
import com.mr10.vello.ui.calls.CallsScreen
import com.mr10.vello.ui.communities.CommunitiesScreen
import com.mr10.vello.ui.theme.WhatsAppGreen
import com.mr10.vello.ui.theme.WhatsAppHeaderLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    onNavigateToChat: (Chat) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToContactList: () -> Unit,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    val currentUser by authViewModel.currentUser.collectAsState()
    val profile by authViewModel.userProfile.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        if (isExpanded) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                NavigationRailItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Chats") },
                    icon = { Icon(if (selectedTab == 0) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat, contentDescription = "Chats") }
                )
                NavigationRailItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Updates") },
                    icon = { Icon(if (selectedTab == 1) Icons.Default.Update else Icons.Outlined.Update, contentDescription = "Updates") }
                )
                NavigationRailItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Communities") },
                    icon = { Icon(if (selectedTab == 2) Icons.Default.Groups else Icons.Outlined.Groups, contentDescription = "Communities") }
                )
                NavigationRailItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    label = { Text("Calls") },
                    icon = { Icon(if (selectedTab == 3) Icons.Default.Call else Icons.Outlined.Call, contentDescription = "Calls") }
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // In a real app, you'd use a localized string and potentially a logo image
                            Text(
                                "Vello",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Camera */ }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White)
                        }
                        IconButton(onClick = { /* Search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                        IconButton(onClick = { /* Menu */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onNavigateToSettings() },
                            color = Color.LightGray
                        ) {
                            AsyncImage(
                                model = profile?.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${profile?.name ?: "User"}&background=random",
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = WhatsAppHeaderLight,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                if (!isExpanded) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Chats") },
                            icon = { 
                                Icon(
                                    if (selectedTab == 0) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = "Chats"
                                ) 
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WhatsAppGreen,
                                selectedTextColor = WhatsAppGreen,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFD9FDD3)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Updates") },
                            icon = { Icon(if (selectedTab == 1) Icons.Default.Update else Icons.Outlined.Update, contentDescription = "Updates") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WhatsAppGreen,
                                selectedTextColor = WhatsAppGreen,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFD9FDD3)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            label = { Text("Communities") },
                            icon = { Icon(if (selectedTab == 2) Icons.Default.Groups else Icons.Outlined.Groups, contentDescription = "Communities") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WhatsAppGreen,
                                selectedTextColor = WhatsAppGreen,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFD9FDD3)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            label = { Text("Calls") },
                            icon = { Icon(if (selectedTab == 3) Icons.Default.Call else Icons.Outlined.Call, contentDescription = "Calls") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WhatsAppGreen,
                                selectedTextColor = WhatsAppGreen,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFD9FDD3)
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = onNavigateToContactList,
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "New Chat")
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> ChatListScreen(viewModel = chatViewModel, onChatClick = onNavigateToChat)
                    1 -> StatusScreen()
                    2 -> CommunitiesScreen()
                    3 -> CallsScreen()
                }
            }
        }
    }
}
