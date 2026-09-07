package com.mr10.vello.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.ui.theme.VelloOnSurface
import com.mr10.vello.ui.theme.VelloOnSurfaceVariant
import com.mr10.vello.ui.theme.VelloPrimaryContainer

data class SettingsSearchItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSearchScreen(
    onBack: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val allItems = listOf(
        SettingsSearchItem(Icons.Default.Key, "Account", "Security notifications, passkeys, change number", onNavigateToAccount),
        SettingsSearchItem(Icons.Default.Lock, "Privacy", "Block contacts, disappearing messages", onNavigateToPrivacy),
        SettingsSearchItem(Icons.AutoMirrored.Filled.Chat, "Chats", "Theme, wallpapers, chat history", onNavigateToChats),
        SettingsSearchItem(Icons.Default.Notifications, "Notifications", "Message, group & call tones", onNavigateToNotifications),
        SettingsSearchItem(Icons.Default.PieChart, "Storage and data", "Network usage, auto-download", onNavigateToStorage),
        SettingsSearchItem(Icons.AutoMirrored.Filled.HelpOutline, "Help", "Help centre, contact us, privacy policy", onNavigateToHelp)
    )

    val filteredItems = remember(searchQuery) {
        if (searchQuery.isEmpty()) emptyList()
        else allItems.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search settings...", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VelloPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = filteredItems,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "SearchContentAnimation"
            ) { items ->
                if (items.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found for \"$searchQuery\"", color = Color.Gray)
                    }
                } else if (items.isEmpty() && searchQuery.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Type to search all settings", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            SearchResultItem(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(item: SettingsSearchItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFFF0F2F5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(item.icon, contentDescription = null, tint = VelloOnSurfaceVariant, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = item.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VelloOnSurface)
                Text(text = item.subtitle, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
            }
        }
    }
}
