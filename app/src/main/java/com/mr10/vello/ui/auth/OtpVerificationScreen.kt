package com.mr10.vello.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    identifier: String,
    isEmail: Boolean,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    var timer by remember { mutableIntStateOf(45) }

    LaunchedEffect(key1 = timer) {
        if (timer > 0) {
            delay(1000L)
            timer--
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthSuccess()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Verify Your Email", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VelloOnSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VelloOnSurfaceVariant)
                    }
                },
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
            // Status & Brand Moment
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = VelloSecondary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VerifiedUser, null, tint = VelloSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sign In & Account Setup", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VelloSecondary)
                }
            }

            Text(
                text = "Check your email",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = VelloOnSurface
            )
            
            Text(
                text = "We sent a 6-digit verification code to $identifier. Not your email? Edit email",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = VelloOnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Slots with Stitch style (rounded, dash separator)
            OtpInputField(
                otp = otp,
                onOtpChange = { if (it.length <= 6) otp = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Verification Alternates
            Column(modifier = Modifier.fillMaxWidth()) {
                VerificationAlternateItem(
                    icon = Icons.Default.MarkEmailRead,
                    title = "Resend Code via Email",
                    subtitle = "Request a new 6-digit code",
                    trailingText = if (timer > 0) "0:${timer.toString().padStart(2, '0')}" else "Resend now",
                    onClick = { if (timer == 0) { timer = 45; if (isEmail) viewModel.signInWithEmailOtp(identifier) else viewModel.signInWithPhone(identifier) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                VerificationAlternateItem(
                    icon = Icons.Default.Link,
                    title = "Send Magic Sign-In Link",
                    subtitle = "Log in directly with one tap",
                    trailingIcon = Icons.Default.ChevronRight,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.verifyOtp(otp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VelloPrimaryContainer,
                    disabledContainerColor = VelloPrimaryContainer.copy(alpha = 0.5f)
                ),
                enabled = otp.length == 6 && uiState !is AuthUiState.Loading,
                shape = CircleShape
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Verify & Continue", fontWeight = FontWeight.Bold)
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
fun VerificationAlternateItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0F2F5)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VelloOnSurface)
                Text(subtitle, fontSize = 12.sp, color = VelloOnSurfaceVariant)
            }
            if (trailingText != null) {
                Surface(
                    color = Color.White,
                    shape = CircleShape
                ) {
                    Text(
                        trailingText, 
                        fontSize = 12.sp, 
                        color = Color.Gray, 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            if (trailingIcon != null) {
                Icon(trailingIcon, null, tint = VelloSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun OtpInputField(
    otp: String,
    onOtpChange: (String) -> Unit
) {
    BasicTextField(
        value = otp,
        onValueChange = {
            if (it.all { char -> char.isDigit() }) {
                onOtpChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = when {
                        index >= otp.length -> ""
                        else -> otp[index].toString()
                    }
                    val isFocused = index == otp.length
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFocused) Color.White else Color(0xFFF0F2F5))
                            .then(if (isFocused) Modifier.shadow(4.dp, RoundedCornerShape(8.dp)) else Modifier),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isFocused && char.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(20.dp)
                                    .background(VelloSecondary)
                            )
                        } else {
                            Text(
                                text = if (char.isEmpty()) "—" else char,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (char.isEmpty()) VelloOnSurfaceVariant else VelloOnSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(if (isFocused) VelloSecondary else if (char.isNotEmpty()) VelloPrimaryContainer else Color.Transparent)
                        )
                    }
                    
                    if (index == 2) {
                        Text("-", fontSize = 18.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
            }
        }
    )
}
