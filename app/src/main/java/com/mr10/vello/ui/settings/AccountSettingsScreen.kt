package com.mr10.vello.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun AccountSettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account", fontWeight = FontWeight.Bold, color = Color.White) },
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
                AccountOption(Icons.Default.Security, "Security notifications")
                AccountOption(Icons.Default.Key, "Passkeys")
                AccountOption(Icons.Default.Mail, "Email address")
                AccountOption(Icons.Default.PhonelinkSetup, "Two-step verification")
                AccountOption(Icons.Default.Smartphone, "Change number")
                AccountOption(Icons.Default.Description, "Request account info")
                AccountOption(Icons.Default.Delete, "Delete account", isLast = true)
            }
        }
    }
}

@Composable
fun AccountOption(icon: ImageVector, title: String, isLast: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = Color.White,
        shape = if (isLast) RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp) else RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier.clickable {}.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
