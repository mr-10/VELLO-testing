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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.ui.theme.VelloOnSurface
import com.mr10.vello.ui.theme.VelloPrimaryContainer

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mr10.vello.data.local.LocalSettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    onNavigateToMessageSounds: () -> Unit,
    onNavigateToRingtones: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { LocalSettingsManager.getInstance(context) }
    val highPriorityEnabled by settingsManager.highPriorityEnabled.collectAsState()
    val notificationSoundIndex by settingsManager.notificationSoundIndex.collectAsState()
    val ringtoneIndex by settingsManager.ringtoneIndex.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = Color.White) },
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
            item {
                Text(
                    "Messages",
                    fontSize = 13.sp,
                    color = VelloPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                NotificationOption(
                    title = "Message notification", 
                    subtitle = "Sound ${notificationSoundIndex + 1}",
                    onClick = onNavigateToMessageSounds
                )
                NotificationOption(
                    title = "Use high priority notifications", 
                    subtitle = "Show previews of notifications at the top of the screen", 
                    isSwitch = true,
                    switchChecked = highPriorityEnabled,
                    onSwitchChange = { settingsManager.setHighPriorityEnabled(it) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Calls",
                    fontSize = 13.sp,
                    color = VelloPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                NotificationOption(
                    title = "Ringtone notification", 
                    subtitle = "Ringtone ${ringtoneIndex + 1}",
                    onClick = onNavigateToRingtones
                )
            }
        }
    }
}

@Composable
fun NotificationOption(
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
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VelloOnSurface)
                Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
            if (isSwitch) {
                Switch(checked = switchChecked, onCheckedChange = onSwitchChange)
            }
        }
    }
}
