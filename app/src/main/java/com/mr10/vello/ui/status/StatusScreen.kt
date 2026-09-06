package com.mr10.vello.ui.status

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun StatusScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            MyStatusItem()
        }
        item {
            PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            Text(
                text = "Recent updates",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(5) { index ->
            StatusItem(index)
        }
    }
}

@Composable
fun MyStatusItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                color = Color.LightGray
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=My+Status&background=random",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Surface(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = Color(0xFF25D366),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "My status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Tap to add status update", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun StatusItem(index: Int) {
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
            color = Color.LightGray,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF25D366))
        ) {
            AsyncImage(
                model = "https://ui-avatars.com/api/?name=Contact+$index&background=random",
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "Contact $index", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Today, 10:00 AM", color = Color.Gray, fontSize = 14.sp)
        }
    }
}
