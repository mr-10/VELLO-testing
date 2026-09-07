package com.mr10.vello.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.data.local.LocalSettingsManager
import com.mr10.vello.ui.theme.Palettes
import com.mr10.vello.ui.theme.VelloOnSurface
import com.mr10.vello.ui.theme.VelloPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSettingsScreen(
    onBack: () -> Unit,
    onNavigateToFontSize: () -> Unit,
    onNavigateToWallpaper: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { LocalSettingsManager.getInstance(context) }
    val themeIndex by settingsManager.themeIndex.collectAsState()
    val fontSize by settingsManager.fontSize.collectAsState()
    val enterIsSend by settingsManager.enterIsSend.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentIndex = themeIndex,
            onDismiss = { showThemeDialog = false },
            onSelect = { 
                settingsManager.setThemeIndex(it)
                showThemeDialog = false
            }
        )
    }

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
                SettingsDetailOption(
                    icon = Icons.Default.LightMode, 
                    title = "Theme", 
                    subtitle = "Current Palette: ${themeIndex + 1}",
                    onClick = { showThemeDialog = true }
                )
                SettingsDetailOption(
                    icon = Icons.Default.Wallpaper, 
                    title = "Wallpaper", 
                    subtitle = "Chat background customization",
                    onClick = onNavigateToWallpaper
                )
            }
            item {
                Text(
                    "Chat settings",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                SettingsDetailOption(
                    null, 
                    "Enter is send", 
                    "Enter key will send your message", 
                    isSwitch = true,
                    switchChecked = enterIsSend,
                    onSwitchChange = { settingsManager.setEnterIsSend(it) }
                )
                SettingsDetailOption(
                    null, 
                    "Font size", 
                    fontSize,
                    onClick = onNavigateToFontSize
                )
            }
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose theme palette") },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                itemsIndexed(Palettes) { index, scheme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(scheme.primary)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Palette ${index + 1}", modifier = Modifier.weight(1f))
                        if (index == currentIndex) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = scheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun SettingsDetailOption(
    icon: ImageVector?, 
    title: String, 
    subtitle: String, 
    isSwitch: Boolean = false,
    switchChecked: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.clickable { if (!isSwitch) onClick() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VelloOnSurface)
                if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
            if (isSwitch) {
                Switch(checked = switchChecked, onCheckedChange = onSwitchChange)
            }
        }
    }
}
