package com.mr10.vello.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.ui.theme.WhatsAppGreen
import com.mr10.vello.ui.theme.WhatsAppHeaderLight
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    var timer by remember { mutableIntStateOf(60) }

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
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Verifying your email", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        color = WhatsAppHeaderLight
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhatsAppHeaderLight)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                    }
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
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "We have sent a verification code to",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            
            Text(
                text = identifier,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Text(
                text = "Wrong email?",
                color = Color(0xFF027EB5),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onBack() }
            )

            Spacer(modifier = Modifier.height(48.dp))

            OtpInputField(
                otp = otp,
                onOtpChange = { if (it.length <= 6) otp = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Enter 6-digit code",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            AnimatedVisibility(
                visible = uiState is AuthUiState.Error,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = (uiState as? AuthUiState.Error)?.message ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Resend code in ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${timer}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (timer > 0) Color.Gray else WhatsAppGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = timer == 0) {
                            timer = 60
                            if (isEmail) viewModel.signInWithEmailOtp(identifier) else viewModel.signInWithPhone(identifier)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.verifyOtp(otp) },
                    modifier = Modifier
                        .width(150.dp)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        disabledContainerColor = Color(0xFFE9EDEF)
                    ),
                    enabled = otp.length == 6 && uiState !is AuthUiState.Loading,
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
                        AnimatedVisibility(visible = otp.length == 6) {
                            Text("VERIFY", fontWeight = FontWeight.Bold)
                        }
                        if (otp.length < 6) {
                            Text("VERIFY", fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }
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
                            .width(40.dp)
                            .height(50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(if (isFocused) WhatsAppGreen else Color(0xFFE9EDEF))
                        )
                    }
                    
                    if (index == 2) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(2.dp)
                                .background(Color(0xFFE9EDEF))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    )
}
