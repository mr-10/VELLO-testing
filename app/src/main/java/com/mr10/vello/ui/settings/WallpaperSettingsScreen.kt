package com.mr10.vello.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mr10.vello.data.local.LocalSettingsManager
import com.mr10.vello.ui.theme.VelloPrimaryContainer
import com.mr10.vello.ui.theme.WhatsAppSentBubbleLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { LocalSettingsManager.getInstance(context) }
    val wallpaperUri by settingsManager.wallpaperUri.collectAsState()
    val opacity by settingsManager.wallpaperOpacity.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { settingsManager.setWallpaperUri(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallpaper", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Live Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Wallpaper
                        if (wallpaperUri != null) {
                            AsyncImage(
                                model = wallpaperUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE9EDEF))
                            )
                        }

                        // Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 1f - opacity))
                        )

                        // Dummy Content
                        Column(modifier = Modifier.padding(16.dp)) {
                            Surface(
                                color = WhatsAppSentBubbleLight,
                                shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    "This is how your chat will look.",
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VelloPrimaryContainer)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CHANGE WALLPAPER")
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Wallpaper Opacity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Slider(
                        value = opacity,
                        onValueChange = { settingsManager.setWallpaperOpacity(it) },
                        valueRange = 0.1f..0.8f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = VelloPrimaryContainer,
                            activeTrackColor = VelloPrimaryContainer
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10%", fontSize = 12.sp, color = Color.Gray)
                        Text("${(opacity * 100).toInt()}%", fontWeight = FontWeight.Bold)
                        Text("80%", fontSize = 12.sp, color = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = { settingsManager.setWallpaperUri(null) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("REMOVE WALLPAPER", color = Color.Red)
                    }
                }
            }
        }
    }
}
