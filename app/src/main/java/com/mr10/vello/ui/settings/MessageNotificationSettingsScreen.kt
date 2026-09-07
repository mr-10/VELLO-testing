package com.mr10.vello.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.data.local.LocalSettingsManager
import com.mr10.vello.ui.theme.VelloPrimaryContainer
import com.mr10.vello.ui.util.SoundHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageNotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { LocalSettingsManager.getInstance(context) }
    val currentIndex by settingsManager.notificationSoundIndex.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            SoundHelper.stopSound()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Message notifications", fontWeight = FontWeight.Bold, color = Color.White) },
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
                    "Choose notification sound",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
            
            items(2) { index ->
                SoundOption(
                    title = "Sound ${index + 1}",
                    isSelected = currentIndex == index,
                    onSelect = {
                        settingsManager.setNotificationSoundIndex(index)
                    },
                    onPreview = {
                        SoundHelper.playSound(context, SoundHelper.getNotificationSoundRes(index))
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFF0F2F5))
            }
        }
    }
}

@Composable
fun SoundOption(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.clickable { onSelect() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onPreview) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = Color.Gray)
            }
        }
    }
}
