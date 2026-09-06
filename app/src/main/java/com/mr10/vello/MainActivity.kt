package com.mr10.vello

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.mr10.vello.ui.auth.AuthViewModel
import com.mr10.vello.ui.auth.EmailLoginScreen
import com.mr10.vello.ui.auth.OtpVerificationScreen
import com.mr10.vello.ui.auth.ProfileSetupScreen
import com.mr10.vello.ui.auth.SplashScreen
import com.mr10.vello.ui.calls.CallingViewModel
import com.mr10.vello.ui.calls.IncomingCallScreen
import com.mr10.vello.ui.calls.OutgoingCallScreen
import com.mr10.vello.ui.chat.ChatDetailScreen
import com.mr10.vello.ui.chat.ChatViewModel
import com.mr10.vello.ui.communities.CommunitiesScreen
import com.mr10.vello.ui.home.*
import com.mr10.vello.ui.navigation.NavKey
import com.mr10.vello.ui.settings.*
import com.mr10.vello.ui.theme.VelloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VelloTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val authViewModel: AuthViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val callingViewModel: CallingViewModel = viewModel()
    val context = LocalContext.current
    val contactViewModel: ContactViewModel = viewModel {
        ContactViewModel(com.mr10.vello.data.repository.ContactRepositoryImpl(context.applicationContext))
    }
    val currentUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val isProfileChecked by authViewModel.isProfileChecked.collectAsState()
    val sessionStatus by authViewModel.sessionStatus.collectAsState()
    var isSplashFinished by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val initialKey: NavKey = NavKey.Splash
    val backStack = rememberNavBackStack(initialKey)

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
        entryProvider = { key ->
            when (key) {
                NavKey.Splash -> NavEntry(key) {
                    SplashScreen(
                        onTimeout = {
                            isSplashFinished = true
                        }
                    )
                }
                NavKey.EmailLogin -> NavEntry(key) {
                    EmailLoginScreen(
                        viewModel = authViewModel,
                        onOtpSent = { identifier ->
                            backStack.add(NavKey.OtpVerification(identifier, true))
                        }
                    )
                }
                is NavKey.OtpVerification -> NavEntry(key) { k ->
                    val otpKey = k as NavKey.OtpVerification
                    OtpVerificationScreen(
                        identifier = otpKey.identifier,
                        isEmail = otpKey.isEmail,
                        viewModel = authViewModel,
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                        onAuthSuccess = {
                            // Handled by LaunchedEffect
                        }
                    )
                }
                NavKey.ProfileSetup -> NavEntry(key) {
                    ProfileSetupScreen(
                        viewModel = authViewModel,
                        onSetupComplete = {
                            backStack.clear()
                            backStack.add(NavKey.Home)
                        }
                    )
                }
                NavKey.Home -> NavEntry(key) {
                    HomeScreen(
                        authViewModel = authViewModel,
                        chatViewModel = chatViewModel,
                        onNavigateToChat = { chat ->
                            backStack.add(NavKey.ChatDetail(chat.id, chat.name))
                        },
                        onNavigateToSettings = { backStack.add(NavKey.Settings) },
                        onNavigateToContactList = { backStack.add(NavKey.ContactList) },
                        onNavigateToCamera = { backStack.add(NavKey.Camera) },
                        onNavigateToSearch = { backStack.add(NavKey.Search) },
                        onSignOut = {
                            authViewModel.signOut()
                            backStack.clear()
                            backStack.add(NavKey.EmailLogin)
                        }
                    )
                }
                NavKey.ContactList -> NavEntry(key) {
                    ContactListScreen(
                        viewModel = contactViewModel,
                        onContactClick = { contact ->
                            backStack.add(NavKey.ChatDetail(contact.userId ?: "new", contact.name))
                        },
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) }
                    )
                }
                is NavKey.ChatDetail -> NavEntry(key) {
                    ChatDetailScreen(
                        chatId = key.chatId,
                        chatName = key.chatName,
                        viewModel = chatViewModel,
                        authViewModel = authViewModel,
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                        onVoiceCall = { backStack.add(NavKey.OutgoingCall(key.chatName, key.chatId, false)) },
                        onVideoCall = { backStack.add(NavKey.OutgoingCall(key.chatName, key.chatId, true)) }
                    )
                }
                is NavKey.IncomingCall -> NavEntry(key) {
                    IncomingCallScreen(
                        callerName = key.callerName,
                        isVideo = key.isVideo,
                        onAccept = {
                            callingViewModel.acceptCall()
                        },
                        onReject = {
                            callingViewModel.rejectCall()
                            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                        }
                    )
                }
                is NavKey.OutgoingCall -> NavEntry(key) {
                    OutgoingCallScreen(
                        receiverName = key.receiverName,
                        isVideo = key.isVideo,
                        onEndCall = {
                            callingViewModel.endCall()
                            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                        }
                    )
                }
                NavKey.Settings -> NavEntry(key) {
                    SettingsScreen(
                        viewModel = authViewModel,
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                        onNavigateToProfileEdit = { backStack.add(NavKey.ProfileEdit) },
                        onNavigateToAccount = { backStack.add(NavKey.AccountSettings) },
                        onNavigateToPrivacy = { backStack.add(NavKey.PrivacySettings) },
                        onNavigateToAvatar = { backStack.add(NavKey.AvatarPersona) },
                        onNavigateToChats = { backStack.add(NavKey.ChatSettings) },
                        onNavigateToNotifications = { backStack.add(NavKey.NotificationSettings) },
                        onNavigateToStorage = { backStack.add(NavKey.StorageData) },
                        onNavigateToHelp = { backStack.add(NavKey.Help) }
                    )
                }
                NavKey.ProfileEdit -> NavEntry(key) {
                    ProfileEditScreen(
                        viewModel = authViewModel,
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) }
                    )
                }
                NavKey.AccountSettings -> NavEntry(key) {
                    AccountSettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.PrivacySettings -> NavEntry(key) {
                    PrivacySettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.AvatarPersona -> NavEntry(key) {
                    AvatarPersonaScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.ChatSettings -> NavEntry(key) {
                    ChatSettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.NotificationSettings -> NavEntry(key) {
                    NotificationSettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.StorageData -> NavEntry(key) {
                    StorageDataScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.Help -> NavEntry(key) {
                    HelpScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.Communities -> NavEntry(key) {
                    CommunitiesScreen()
                }
                NavKey.Camera -> NavEntry(key) {
                    CameraScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.Search -> NavEntry(key) {
                    SearchScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                else -> NavEntry(key) { }
            }
        }
    )

    LaunchedEffect(currentUser, userProfile, isProfileChecked, isSplashFinished) {
        val current = backStack.lastOrNull() ?: return@LaunchedEffect
        
        if (current is NavKey.Splash) {
            if (isSplashFinished) {
                if (currentUser == null) {
                    backStack.clear()
                    backStack.add(NavKey.EmailLogin)
                } else if (isProfileChecked) {
                    backStack.clear()
                    if (userProfile != null) {
                        backStack.add(NavKey.Home)
                    } else {
                        backStack.add(NavKey.ProfileSetup)
                    }
                }
            }
            return@LaunchedEffect
        }

        if (currentUser == null) {
            if (current !is NavKey.EmailLogin && current !is NavKey.OtpVerification) {
                backStack.clear()
                backStack.add(NavKey.EmailLogin)
            }
        } else if (isProfileChecked) {
            if (userProfile == null) {
                if (current !is NavKey.ProfileSetup) {
                    backStack.clear()
                    backStack.add(NavKey.ProfileSetup)
                }
            } else {
                if (current is NavKey.EmailLogin || current is NavKey.OtpVerification || current is NavKey.ProfileSetup) {
                    backStack.clear()
                    backStack.add(NavKey.Home)
                }
            }
        }
    }
}
