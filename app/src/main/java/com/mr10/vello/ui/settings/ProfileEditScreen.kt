package com.mr10.vello.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mr10.vello.ui.auth.AuthViewModel
import com.mr10.vello.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var about by remember(profile) { mutableStateOf(profile?.statusQuote ?: "Hey there! I am using Vello.") }
    
    var showNameEdit by remember { mutableStateOf(false) }
    var showAboutEdit by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    if (showNameEdit) {
        EditFieldDialog(
            title = "Enter your name",
            initialValue = name,
            onDismiss = { showNameEdit = false },
            onSave = { 
                name = it
                showNameEdit = false 
            }
        )
    }

    if (showAboutEdit) {
        EditFieldDialog(
            title = "About",
            initialValue = about,
            onDismiss = { showAboutEdit = false },
            onSave = { 
                about = it
                showAboutEdit = false 
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VelloPrimaryContainer,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F9FC)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Image Section with delightful Stitch transition
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    color = Color.LightGray,
                    shadowElevation = 4.dp
                ) {
                    AsyncImage(
                        model = imageUri ?: profile?.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${profile?.name ?: "User"}&background=random",
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    color = VelloTertiary,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Info Fields grouped in a Stitch-style card
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    ProfileInfoItem(
                        icon = Icons.Default.Person,
                        label = "Name",
                        value = name,
                        onEdit = { showNameEdit = true },
                        subtitle = "This is not your username or pin. This name will be visible to your Vello contacts."
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFF0F2F5))
                    
                    ProfileInfoItem(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        label = "About",
                        value = about,
                        onEdit = { showAboutEdit = true }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFF0F2F5))
                    
                    ProfileInfoItem(
                        icon = Icons.Default.Phone,
                        label = "Phone",
                        value = currentUser?.email ?: "No email linked",
                        isEditable = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button
            val isModified = name != (profile?.name ?: "") || about != (profile?.statusQuote ?: "") || imageUri != null
            
            Button(
                onClick = { 
                    val bytes = imageUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    viewModel.setupProfile(name, profile?.dob ?: "", bytes, about) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VelloPrimaryContainer,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                enabled = isModified && name.isNotBlank(),
                shape = CircleShape
            ) {
                Text("SAVE PROFILE", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditFieldDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = VelloSecondary
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("SAVE", color = VelloSecondary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = VelloSecondary)
            }
        }
    )
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    onEdit: () -> Unit = {},
    isEditable: Boolean = true,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEditable) { onEdit() }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color(0xFFF7F9FC)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = VelloOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VelloSecondary, letterSpacing = 0.5.sp)
            Text(value, fontSize = 16.sp, color = VelloOnSurface, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
        }
        if (isEditable) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                tint = VelloSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
