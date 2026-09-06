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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mr10.vello.ui.auth.AuthViewModel
import com.mr10.vello.ui.theme.WhatsAppGreen
import com.mr10.vello.ui.theme.WhatsAppTextSecondaryLight

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
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F8FA)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Image Section
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
                    color = Color.LightGray
                ) {
                    AsyncImage(
                        model = imageUri ?: profile?.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${profile?.name ?: "User"}&background=random",
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    color = WhatsAppGreen,
                    tonalElevation = 4.dp
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Info Fields
            ProfileInfoItem(
                icon = Icons.Default.Person,
                label = "Name",
                value = name,
                onEdit = { showNameEdit = true },
                subtitle = "This is not your username or pin. This name will be visible to your Vello contacts."
            )
            
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = Color(0xFFE9EDEF))
            
            ProfileInfoItem(
                icon = Icons.Default.Info,
                label = "About",
                value = about,
                onEdit = { showAboutEdit = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = Color(0xFFE9EDEF))
            
            ProfileInfoItem(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = currentUser?.email ?: "No email linked", // Using email as identifier for now
                isEditable = false
            )
            
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
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhatsAppGreen,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                enabled = isModified && name.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("SAVE PROFILE", fontWeight = FontWeight.Bold)
            }
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
                    focusedIndicatorColor = WhatsAppGreen
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("SAVE", color = WhatsAppGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = WhatsAppGreen)
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
            .background(Color.White)
            .clickable(enabled = isEditable) { onEdit() }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = WhatsAppTextSecondaryLight,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(32.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, color = WhatsAppTextSecondaryLight)
            Text(value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(subtitle, fontSize = 12.sp, color = WhatsAppTextSecondaryLight, lineHeight = 16.sp)
            }
        }
        if (isEditable) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                tint = WhatsAppGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
