package com.mr10.vello.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.Build
import com.mr10.vello.data.repository.ProfileRepository
import com.mr10.vello.data.model.UserProfile
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: com.mr10.vello.data.repository.AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _otpIdentifier = MutableStateFlow("")
    val otpIdentifier: StateFlow<String> = _otpIdentifier.asStateFlow()

    private val _isEmailOtp = MutableStateFlow(false)
    val isEmailOtp: StateFlow<Boolean> = _isEmailOtp.asStateFlow()

    private val _isNewUser = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> = _isNewUser.asStateFlow()

    fun toggleAuthMode(isSignUp: Boolean) {
        _isNewUser.value = isSignUp
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isProfileChecked = MutableStateFlow(false)
    val isProfileChecked: StateFlow<Boolean> = _isProfileChecked.asStateFlow()

    private val _deletionMessage = MutableStateFlow<String?>(null)
    val deletionMessage: StateFlow<String?> = _deletionMessage.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus?> = repository.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentUser: StateFlow<UserInfo?> = repository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        checkUserProfile(status.session.user?.id ?: "")
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _userProfile.value = null
                        _isProfileChecked.value = true
                    }
                    else -> {}
                }
            }
        }
    }

    private suspend fun checkUserProfile(userId: String) {
        try {
            val profile = profileRepository.getProfile(userId)
            _userProfile.value = profile
            
            // Cancel scheduled deletion if it exists
            if (profile?.deletionScheduledAt != null) {
                profileRepository.scheduleAccountDeletion(userId, null)
                _userProfile.value = profile.copy(deletionScheduledAt = null)
                _deletionMessage.value = "Account deletion process has been stopped automatically."
            }
            
            _isProfileChecked.value = true
        } catch (e: Exception) {
            _isProfileChecked.value = true
        }
    }

    fun updateEmailVisibility(isHidden: Boolean) {
        viewModelScope.launch {
            try {
                val userId = currentUser.value?.id ?: return@launch
                profileRepository.updateEmailVisibility(userId, isHidden)
                _userProfile.value = _userProfile.value?.copy(isEmailHidden = isHidden)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to update visibility")
            }
        }
    }

    fun scheduleAccountDeletion() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val userId = currentUser.value?.id ?: throw Exception("User not logged in")
                // Calculate 24 hours from now in ISO 8601
                val deletionTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Instant.now().plus(24, ChronoUnit.HOURS).toString()
                } else {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.HOUR, 24)
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    sdf.format(cal.time)
                }
                
                profileRepository.scheduleAccountDeletion(userId, deletionTime)
                _userProfile.value = _userProfile.value?.copy(deletionScheduledAt = deletionTime)
                _deletionMessage.value = "Account deletion is on the way. Your account will be deleted permanently in 24 hours."
                
                // Sign out after scheduling
                signOut()
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to schedule deletion")
            }
        }
    }

    fun clearDeletionMessage() {
        _deletionMessage.value = null
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.signUpWithPassword(email, password).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Sign up failed")
                }
            )
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.signInWithPassword(email, password).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Sign in failed")
                }
            )
        }
    }

    fun signInWithPhone(phone: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.signInWithPhone(phone).fold(
                onSuccess = {
                    _otpIdentifier.value = phone
                    _isEmailOtp.value = false
                    _uiState.value = AuthUiState.Idle
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Failed to send OTP")
                }
            )
        }
    }

    fun signInWithEmailOtp(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val exists = repository.checkUserExists(email)
            _isNewUser.value = !exists
            
            val metadata = if (!exists) {
                buildJsonObject {
                    put("full_name", email.substringBefore("@"))
                }
            } else null
            
            repository.sendEmailOtp(email, shouldCreateUser = !exists, metadata = metadata).fold(
                onSuccess = {
                    _otpIdentifier.value = email
                    _isEmailOtp.value = true
                    _uiState.value = AuthUiState.Idle
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Failed to send OTP")
                }
            )
        }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            if (_isEmailOtp.value) {
                repository.verifyOtpCode(_otpIdentifier.value, otp, _isNewUser.value).fold(
                    onSuccess = {
                        _uiState.value = AuthUiState.Success
                    },
                    onFailure = { e ->
                        _uiState.value = AuthUiState.Error(e.message ?: "Invalid OTP")
                    }
                )
            } else {
                repository.verifyPhoneOtp(_otpIdentifier.value, otp).fold(
                    onSuccess = {
                        _uiState.value = AuthUiState.Success
                    },
                    onFailure = { e ->
                        _uiState.value = AuthUiState.Error(e.message ?: "Invalid OTP")
                    }
                )
            }
        }
    }

    fun setupProfile(name: String, dob: String, imageBytes: ByteArray?, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val userId = currentUser.value?.id ?: throw Exception("User not logged in")
                val currentProfile = _userProfile.value
                
                var imageUrl: String? = null
                if (imageBytes != null) {
                    // Delete old picture if it exists
                    currentProfile?.profilePictureUrl?.let { oldUrl ->
                        try {
                            val fileName = oldUrl.substringAfterLast("/")
                            profileRepository.deleteProfilePicture(userId, fileName)
                        } catch (e: Exception) {
                            // Ignore deletion errors
                        }
                    }
                    imageUrl = profileRepository.uploadProfilePicture(userId, imageBytes)
                }
                
                val profile = UserProfile(
                    id = userId, 
                    name = name, 
                    dob = dob, 
                    profilePictureUrl = imageUrl ?: currentProfile?.profilePictureUrl,
                    statusQuote = status ?: currentProfile?.statusQuote ?: "Hey there! I am using Vello."
                )
                profileRepository.updateProfile(profile)
                _userProfile.value = profile
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Profile setup failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
        _otpIdentifier.value = ""
    }
}
