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
import androidx.compose.material.icons.filled.CalendarToday
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
import com.mr10.vello.ui.theme.WhatsAppGreen
import com.mr10.vello.ui.theme.WhatsAppHeaderLight
import com.mr10.vello.ui.theme.WhatsAppTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: AuthViewModel,
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf("") }
    
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
                        color = WhatsAppHeaderLight
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
                color = Color.Gray
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
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFB1B3B5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Name Field (Underlined)
            TextField(
                value = name,
                onValueChange = { if (it.length <= 25) name = it },
                placeholder = { Text("Type your name here", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = WhatsAppGreen,
                    unfocusedIndicatorColor = Color(0xFFE9EDEF),
                    cursorColor = WhatsAppGreen
                ),
                trailingIcon = {
                    Text(
                        text = (25 - name.length).toString(),
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // DOB Field (Underlined)
            TextField(
                value = selectedDateText,
                onValueChange = { },
                placeholder = { Text("Date of Birth (Optional)", color = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = false,
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color(0xFFE9EDEF),
                    disabledTextColor = Color.Black,
                    disabledPlaceholderColor = Color.LightGray
                ),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = WhatsAppGreen)
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val date = Date(it)
                                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                selectedDateText = format.format(date)
                            }
                            showDatePicker = false
                        }) {
                            Text("OK", color = WhatsAppGreen)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = WhatsAppGreen)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

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
                    viewModel.setupProfile(name, selectedDateText, bytes)
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(44.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhatsAppGreen,
                    disabledContainerColor = Color(0xFFE9EDEF)
                ),
                enabled = name.isNotBlank() && uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(4.dp),
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
