package com.mr10.vello.data.repository

import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserInfo?>
    val sessionStatus: Flow<SessionStatus>
    suspend fun checkUserExists(email: String): Boolean
    suspend fun signUp(email: String, password: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signInWithPhone(phone: String)
    suspend fun signInWithEmailOtp(email: String)
    suspend fun verifyPhoneOtp(phone: String, otp: String)
    suspend fun verifyEmailOtp(email: String, otp: String)
    suspend fun verifyEmailSignup(email: String, otp: String)
    suspend fun signOut()
}
