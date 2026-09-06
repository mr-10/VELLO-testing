package com.mr10.vello.ui.calls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun CallsScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            CreateCallLinkItem()
        }
        item {
            Text(
                text = "Recent",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(10) { index ->
            CallItem(index)
        }
    }
}

@Composable
fun CreateCallLinkItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            color = Color(0xFF25D366)
        ) {
            Icon(
                Icons.Default.Link,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "Create call link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Share a link for your Vello call", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun CallItem(index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            color = Color.LightGray
        ) {
            AsyncImage(
                model = "https://ui-avatars.com/api/?name=Contact+$index&background=random",
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Contact $index", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = null,
                    tint = if (index % 2 == 0) Color.Red else Color(0xFF25D366),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Today, 11:30 AM", color = Color.Gray, fontSize = 14.sp)
            }
        }
        IconButton(onClick = { }) {
            Icon(
                if (index % 3 == 0) Icons.Default.VideoCall else Icons.Default.Call,
                contentDescription = null,
                tint = Color(0xFF075E54)
            )
        }
    }
}
