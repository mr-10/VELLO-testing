package com.mr10.vello.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mr10.vello.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: AuthViewModel,
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("Hey there! I am using Vello.") }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSetupComplete()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Profile info", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        color = VelloOnSurface
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Please provide your name and an optional profile photo",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = VelloOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF7F8FA))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFF0F2F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFB1B3B5)
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = VelloTertiary,
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Name Field (Stitch Style: Underlined with character count)
            TextField(
                value = name,
                onValueChange = { if (it.length <= 25) name = it },
                placeholder = { Text("Type your name here", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = VelloSecondary,
                    unfocusedIndicatorColor = Color(0xFFE9EDEF),
                    cursorColor = VelloSecondary
                ),
                trailingIcon = {
                    Text(
                        text = (25 - name.length).toString(),
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    color = VelloOnSurface,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About Field (Optional but good to have in setup)
            TextField(
                value = about,
                onValueChange = { if (it.length <= 100) about = it },
                placeholder = { Text("About (Optional)", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = VelloSecondary,
                    unfocusedIndicatorColor = Color(0xFFE9EDEF),
                    cursorColor = VelloSecondary
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = VelloOnSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    val bytes = imageUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    // dob is required by UserProfile but we can pass dummy or empty if not needed in UI
                    viewModel.setupProfile(name, "2000-01-01", bytes, about)
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(44.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VelloPrimaryContainer,
                    disabledContainerColor = Color(0xFFE9EDEF)
                ),
                enabled = name.isNotBlank() && uiState !is AuthUiState.Loading,
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "NEXT",
                        fontWeight = FontWeight.Bold,
                        color = if (name.isNotBlank()) Color.White else Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
