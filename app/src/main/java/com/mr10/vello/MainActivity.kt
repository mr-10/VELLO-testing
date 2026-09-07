package com.mr10.vello

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import com.mr10.vello.ui.calls.CallUiState
import com.mr10.vello.ui.calls.IncomingCallScreen
import com.mr10.vello.ui.calls.OutgoingCallScreen
import com.mr10.vello.data.model.Message
import com.mr10.vello.ui.chat.ChatDetailScreen
import com.mr10.vello.ui.chat.ChatViewModel
import com.mr10.vello.ui.communities.CommunitiesScreen
import com.mr10.vello.ui.home.*
import com.mr10.vello.ui.navigation.NavKey
import com.mr10.vello.ui.settings.*
import com.mr10.vello.data.local.LocalSettingsManager
import com.mr10.vello.ui.util.SoundHelper
import com.mr10.vello.ui.util.NotificationHelper
import com.mr10.vello.ui.theme.VelloTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mr10.vello.data.repository.ContactRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainContent() {
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= 33) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    LaunchedEffect(Unit) {
        notificationPermissionState?.let {
            if (!it.status.isGranted) {
                it.launchPermissionRequest()
            }
        }
    }

    val authViewModel: AuthViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val callingViewModel: CallingViewModel = viewModel()
    val context = LocalContext.current
    val contactViewModel: ContactViewModel = viewModel {
        ContactViewModel(ContactRepositoryImpl(context.applicationContext))
    }
    val currentUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val isProfileChecked by authViewModel.isProfileChecked.collectAsState()
    val sessionStatus by authViewModel.sessionStatus.collectAsState()
    val deletionMessage by authViewModel.deletionMessage.collectAsState()
    val callState by callingViewModel.callState.collectAsState()
    var isSplashFinished by remember { mutableStateOf(false) }
    var hasNotifiedLogin by remember { mutableStateOf(false) }

    val initialKey: NavKey = NavKey.Splash
    val backStack = rememberNavBackStack(initialKey)

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            callingViewModel.init(user.id)
            
            // Global Message Notification Sound
            val settingsManager = LocalSettingsManager.getInstance(context)
            chatViewModel.observeAllMessages().collect { message: Message ->
                if (message.senderId != user.id) {
                    SoundHelper.playSound(
                        context, 
                        SoundHelper.getNotificationSoundRes(settingsManager.notificationSoundIndex.value)
                    )
                }
            }
        }
    }

    LaunchedEffect(callState) {
        val settingsManager = LocalSettingsManager.getInstance(context)
        when (val state = callState) {
            is CallUiState.Incoming -> {
                if (backStack.lastOrNull() !is NavKey.IncomingCall) {
                    backStack.add(NavKey.IncomingCall(state.callerName, state.callerId, state.isVideo))
                }
                // Play ringtone
                SoundHelper.playSound(
                    context, 
                    SoundHelper.getRingtoneRes(settingsManager.ringtoneIndex.value),
                    loop = true
                )
                // Stop after 20s if not handled
                delay(20000)
                if (callingViewModel.callState.value is CallUiState.Incoming) {
                    callingViewModel.endCall()
                }
            }
            is CallUiState.Outgoing -> {
                if (backStack.lastOrNull() !is NavKey.OutgoingCall) {
                    backStack.add(NavKey.OutgoingCall(state.receiverName, state.receiverId, state.isVideo))
                }
                // Play dialing tone
                SoundHelper.playSound(
                    context, 
                    SoundHelper.getDialingToneRes(),
                    loop = true
                )
                // Stop after 20s if not answered
                delay(20000)
                if (callingViewModel.callState.value is CallUiState.Outgoing) {
                    callingViewModel.endCall()
                }
            }
            is CallUiState.Ongoing -> {
                SoundHelper.stopSound()
            }
            is CallUiState.Idle -> {
                SoundHelper.stopSound()
                if (backStack.lastOrNull() is NavKey.IncomingCall || backStack.lastOrNull() is NavKey.OutgoingCall) {
                    if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(deletionMessage) {
        deletionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearDeletionMessage()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(500)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(500)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(500)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(500)
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(500)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(500)
            )
        },
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
                is NavKey.ChatDetail -> NavEntry(key) { k ->
                    val detail = k as NavKey.ChatDetail
                    ChatDetailScreen(
                        chatId = detail.chatId,
                        chatName = detail.chatName,
                        viewModel = chatViewModel,
                        authViewModel = authViewModel,
                        callingViewModel = callingViewModel,
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) }
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
                        isRinging = (callState as? CallUiState.Outgoing)?.isRinging ?: false,
                        onEndCall = {
                            callingViewModel.endCall()
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
                        onNavigateToChats = { backStack.add(NavKey.ChatSettings) },
                        onNavigateToNotifications = { backStack.add(NavKey.NotificationSettings) },
                        onNavigateToStorage = { backStack.add(NavKey.StorageData) },
                        onNavigateToHelp = { backStack.add(NavKey.Help) },
                        onNavigateToSettingsSearch = { backStack.add(NavKey.SettingsSearch) }
                    )
                }
                NavKey.SettingsSearch -> NavEntry(key) {
                    SettingsSearchScreen(
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                        onNavigateToAccount = { backStack.add(NavKey.AccountSettings) },
                        onNavigateToPrivacy = { backStack.add(NavKey.PrivacySettings) },
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
                    AccountSettingsScreen(
                        viewModel = authViewModel,
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                        onSignOut = {
                            backStack.clear()
                            backStack.add(NavKey.EmailLogin)
                        }
                    )
                }
                NavKey.PrivacySettings -> NavEntry(key) {
                    PrivacySettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.ChatSettings -> NavEntry(key) {
                    ChatSettingsScreen(
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                        onNavigateToFontSize = { backStack.add(NavKey.FontSizeSettings) },
                        onNavigateToWallpaper = { backStack.add(NavKey.WallpaperSettings) }
                    )
                }
                NavKey.FontSizeSettings -> NavEntry(key) {
                    FontSizeSettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.WallpaperSettings -> NavEntry(key) {
                    WallpaperSettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.NotificationSettings -> NavEntry(key) {
                    NotificationSettingsScreen(
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                        onNavigateToMessageSounds = { backStack.add(NavKey.MessageNotificationSettings) },
                        onNavigateToRingtones = { backStack.add(NavKey.RingtoneSettings) }
                    )
                }
                NavKey.MessageNotificationSettings -> NavEntry(key) {
                    MessageNotificationSettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.RingtoneSettings -> NavEntry(key) {
                    RingtoneSettingsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.StorageData -> NavEntry(key) {
                    StorageDataScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.Help -> NavEntry(key) {
                    HelpScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
                NavKey.Communities -> NavEntry(key) {
                    CommunitiesScreen(
                        onNavigateToChat = { user ->
                            backStack.add(NavKey.ChatDetail(user.id, user.name))
                        }
                    )
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

        // Trigger Login Notification
        if (userProfile != null && !hasNotifiedLogin) {
            NotificationHelper.showLoginSuccessNotification(context)
            hasNotifiedLogin = true
        }
        
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
