package com.mr10.vello.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.ui.theme.VelloPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VelloPrimaryContainer)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF7F9FC))) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Text(
                    "Display",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                SettingsDetailOption(Icons.Default.LightMode, "Theme", "System default")
                SettingsDetailOption(Icons.Default.Wallpaper, "Wallpaper", "")
            }
            item {
                Text(
                    "Chat settings",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                SettingsDetailOption(null, "Enter is send", "Enter key will send your message", isSwitch = true)
                SettingsDetailOption(null, "Media visibility", "Show newly downloaded media in your phone's gallery", isSwitch = true)
                SettingsDetailOption(null, "Font size", "Medium")
            }
        }
    }
}

@Composable
fun SettingsDetailOption(icon: ImageVector?, title: String, subtitle: String, isSwitch: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.clickable {}.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
            if (isSwitch) {
                Switch(checked = true, onCheckedChange = {})
            }
        }
    }
}
