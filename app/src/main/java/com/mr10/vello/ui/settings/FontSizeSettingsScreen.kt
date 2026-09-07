package com.mr10.vello.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mr10.vello.ui.theme.WhatsAppSentBubbleLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSizeSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { LocalSettingsManager.getInstance(context) }
    val currentFontSize by settingsManager.fontSize.collectAsState()

    val options = listOf("Small", "Medium", "Large")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Font size", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VelloPrimaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F9FC))
        ) {
            // Preview Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = WhatsAppSentBubbleLight,
                    shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "This is a preview of the chat font size. How does it look to you?",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "12:00 PM",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            // Selector Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Choose font size",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = VelloPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentFontSize == option,
                                onClick = { settingsManager.setFontSize(option) }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
