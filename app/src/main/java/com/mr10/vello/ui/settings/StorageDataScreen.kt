package com.mr10.vello.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.ui.theme.VelloOnSurface
import com.mr10.vello.ui.theme.VelloPrimaryContainer
import java.io.File
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDataScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var totalStorage by remember { mutableStateOf("Calculating...") }

    LaunchedEffect(Unit) {
        totalStorage = calculateAppStorage(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage and data", fontWeight = FontWeight.Bold, color = Color.White) },
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
                StorageOption(Icons.Default.Folder, "Manage storage", totalStorage)
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFE9EDEF))
                StorageOption(Icons.Default.PieChart, "Network usage", "0 KB sent • 0 KB received")
            }
            item {
                Text(
                    "Media auto-download",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                StorageOption(null, "When using mobile data", "Photos")
                StorageOption(null, "When connected on Wi-Fi", "All media")
                StorageOption(null, "When roaming", "No media")
            }
        }
    }
}

@Composable
fun StorageOption(icon: ImageVector?, title: String, subtitle: String) {
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
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VelloOnSurface)
                Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

private fun calculateAppStorage(context: Context): String {
    val internalDir = context.filesDir
    val cacheDir = context.cacheDir
    val totalSize = getFolderSize(internalDir) + getFolderSize(cacheDir)
    return formatFileSize(totalSize)
}

private fun getFolderSize(file: File): Long {
    var size: Long = 0
    if (file.isDirectory) {
        file.listFiles()?.forEach {
            size += getFolderSize(it)
        }
    } else {
        size = file.length()
    }
    return size
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}
