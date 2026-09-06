package com.mr10.vello.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr10.vello.auth.AuthRepository
import com.mr10.vello.VelloApplication
import com.mr10.vello.data.repository.ProfileRepository
import com.mr10.vello.data.repository.ProfileRepositoryImpl
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

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(VelloApplication.supabaseClient),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
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
            _isProfileChecked.value = true
        } catch (e: Exception) {
            _isProfileChecked.value = true
        }
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
                var imageUrl: String? = null
                if (imageBytes != null) {
                    imageUrl = profileRepository.uploadProfilePicture(userId, imageBytes)
                }
                val currentProfile = _userProfile.value
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
