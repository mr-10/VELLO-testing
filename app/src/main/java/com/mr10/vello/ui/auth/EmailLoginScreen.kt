package com.mr10.vello.ui.auth

import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mr10.vello.R
import com.mr10.vello.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailLoginScreen(
    viewModel: AuthViewModel,
    onOtpSent: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    val otpIdentifier by viewModel.otpIdentifier.collectAsState()
    val isSignUpMode by viewModel.isNewUser.collectAsState()

    val isEmailValid = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    LaunchedEffect(otpIdentifier) {
        if (otpIdentifier.isNotEmpty()) {
            onOtpSent(otpIdentifier)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Sign In or Sign Up", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VelloOnSurface) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
            // Top Hero / Brand Moment
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape),
                    color = VelloSecondary.copy(alpha = 0.1f)
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida/AEtjO1X3iytcBr05eClyHt9AQn7d90o5ubKxZKLAsrzRxo0zT5UICb7Cu7-uR2Jrec_ntmns8Iu5C2xlea0KfvfQTBJ8SNNudKT3XLQ-ILwWQPmSu4RtrtyA66qWqjNqDn1zBgvI_EqryqfZlyHECPDqOMOVwPvvKJNCatblIgP6FMXBO8CZ25eP21qY_nZW2RpaMNO3wSaZLs8j5cSzcqkpt5Oy-cIy-cXwdRpOcYNzMrqmwldRX72qyYnfh50",
                        contentDescription = "Logo",
                        modifier = Modifier.padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = Color.White,
                    tonalElevation = 2.dp
                ) {
                    Icon(Icons.Default.Verified, null, tint = VelloSecondary, modifier = Modifier.padding(2.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enter your email address",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = VelloOnSurface
            )
            Text(
                text = "Vello will send a verification link or code to your email address.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = VelloOnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mode Segmented Control (Stitch style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = CircleShape,
                color = Color(0xFFF0F2F5)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    SegmentedButton(
                        text = "Sign In",
                        isSelected = !isSignUpMode,
                        onClick = { viewModel.toggleAuthMode(false) },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentedButton(
                        text = "Create Account",
                        isSelected = isSignUpMode,
                        onClick = { viewModel.toggleAuthMode(true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated header text based on mode
            AnimatedContent(targetState = isSignUpMode) { isSignUp ->
                Text(
                    text = if (isSignUp) "Join Vello today" else "Welcome back to Vello",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = VelloOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Email Input Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "EMAIL ADDRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VelloOnSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F2F5)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Mail, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        TextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            placeholder = { Text("elena.rostova@vello.app", color = Color.Gray.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = VelloSecondary
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = VelloOnSurface)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Logins (Placeholders as per Stitch)
            SocialButton(
                text = "Continue with Google",
                iconUrl = "https://www.gstatic.com/images/branding/product/1x/gsa_512dp.png", // Simplified
                onClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            SocialButton(
                text = "Continue with Apple",
                icon = null, // Use system icon if needed
                onClick = {}
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.signInWithEmailOtp(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VelloPrimaryContainer,
                    disabledContainerColor = VelloPrimaryContainer.copy(alpha = 0.5f)
                ),
                enabled = uiState !is AuthUiState.Loading && isEmailValid,
                shape = CircleShape
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Continue", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SegmentedButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .clickable { onClick() },
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) VelloOnSurface else VelloOnSurfaceVariant
            )
        }
    }
}

@Composable
fun SocialButton(
    text: String,
    iconUrl: String? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = Color(0xFFF0F2F5)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconUrl != null) {
                AsyncImage(model = iconUrl, contentDescription = null, modifier = Modifier.size(20.dp))
            } else if (icon != null) {
                icon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontWeight = FontWeight.Medium, color = VelloOnSurface)
        }
    }
}
