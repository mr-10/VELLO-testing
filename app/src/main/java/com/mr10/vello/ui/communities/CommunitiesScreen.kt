package com.mr10.vello.ui.communities

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.mr10.vello.data.model.UserProfile
import com.mr10.vello.ui.components.UserProfileDetailBottomSheet
import com.mr10.vello.ui.components.UserProfilePreviewDialog
import com.mr10.vello.ui.theme.VelloPrimaryContainer

@Composable
fun CommunitiesScreen(
    viewModel: CommunitiesViewModel = viewModel(),
    onNavigateToChat: (UserProfile) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedUserForPreview by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUserForDetail by remember { mutableStateOf<UserProfile?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is CommunitiesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VelloPrimaryContainer)
                }
            }
            is CommunitiesUiState.Success -> {
                CommunitiesContent(
                    profiles = state.profiles,
                    onProfileClick = { selectedUserForPreview = it }
                )
            }
            is CommunitiesUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.message}", color = Color.Red)
                }
            }
        }

        selectedUserForPreview?.let { user ->
            UserProfilePreviewDialog(
                user = user,
                onDismiss = { selectedUserForPreview = null },
                onMessageClick = {
                    selectedUserForPreview = null
                    onNavigateToChat(user)
                },
                onInfoClick = {
                    selectedUserForPreview = null
                    selectedUserForDetail = user
                }
            )
        }

        selectedUserForDetail?.let { user ->
            UserProfileDetailBottomSheet(
                user = user,
                onDismiss = { selectedUserForDetail = null },
                onChatClick = {
                    selectedUserForDetail = null
                    onNavigateToChat(user)
                }
            )
        }
    }
}

@Composable
fun CommunitiesContent(
    profiles: List<UserProfile>,
    onProfileClick: (UserProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            CommunitiesHeader()
        }
        
        item {
            Text(
                text = "Global Directory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = Color.Gray
            )
        }

        items(profiles) { profile ->
            UserListItem(profile = profile, onClick = { onProfileClick(profile) })
            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun UserListItem(
    profile: UserProfile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            if (profile.profilePictureUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(profile.profilePictureUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF0F2F5)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111B21)
            )
            Text(
                text = profile.statusQuote ?: "Hey there! I am using Vello.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CommunitiesHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFFF0F2F5)
        ) {
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                modifier = Modifier.padding(16.dp).size(48.dp),
                tint = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Vello Global Community",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Text(
            text = "Connect with anyone on the platform instantly.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}
