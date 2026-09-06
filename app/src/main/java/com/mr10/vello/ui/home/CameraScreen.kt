package com.mr10.vello.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CameraScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview Placeholder
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera Preview", color = Color.White)
        }

        // Top Controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, "Close", tint = Color.White)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.FlashOn, "Flash", tint = Color.White)
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp).align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.FlipCameraAndroid, "Flip", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(4.dp, Color.White)
            ) {
                IconButton(onClick = {}) {
                    // Shutter
                }
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.PhotoCamera, "Shutter", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}
