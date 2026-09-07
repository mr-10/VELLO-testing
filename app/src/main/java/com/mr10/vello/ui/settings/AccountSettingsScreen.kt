package com.mr10.vello.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mr10.vello.ui.auth.AuthViewModel
import com.mr10.vello.ui.theme.VelloOnSurface
import com.mr10.vello.ui.theme.VelloPrimaryContainer
import com.mr10.vello.ui.theme.VelloSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(viewModel: AuthViewModel, onBack: () -> Unit, onSignOut: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    
    var showEmailDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showEmailDialog) {
        EmailVisibilityDialog(
            email = currentUser?.email ?: "No email linked",
            isHidden = profile?.isEmailHidden ?: false,
            onDismiss = { showEmailDialog = false },
            onToggle = { isHidden ->
                viewModel.updateEmailVisibility(isHidden)
                showEmailDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.scheduleAccountDeletion()
                showDeleteDialog = false
                onSignOut()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VelloPrimaryContainer)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF7F9FC))) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        AccountOption(
                            icon = Icons.Default.Mail, 
                            title = "Email address",
                            onClick = { showEmailDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = Color(0xFFF0F2F5))
                        AccountOption(
                            icon = Icons.Default.Delete, 
                            title = "Delete account",
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SignOutOption(onClick = {
                    viewModel.signOut()
                    onSignOut()
                })
            }
        }
    }
}

@Composable
fun EmailVisibilityDialog(
    email: String,
    isHidden: Boolean,
    onDismiss: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    var selectedHidden by remember { mutableStateOf(isHidden) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Email address", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = email,
                    fontSize = 16.sp,
                    color = VelloOnSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Column(Modifier.selectableGroup()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = !selectedHidden,
                                onClick = { selectedHidden = false },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = !selectedHidden, onClick = null)
                        Text(
                            text = "Show to public (Default)",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = selectedHidden,
                                onClick = { selectedHidden = true },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedHidden, onClick = null)
                        Text(
                            text = "Hide email address",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onToggle(selectedHidden) }) {
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
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this account?", color = Color.Red, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "24 hours later your account will delete permanently, if you stop this please login again under this time."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("DELETE ACCOUNT", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = VelloOnSurface)
            }
        }
    )
}

@Composable
fun SignOutOption(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.clickable { onClick() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Red, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Sign out", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        }
    }
}

@Composable
fun AccountOption(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VelloOnSurface)
    }
}
