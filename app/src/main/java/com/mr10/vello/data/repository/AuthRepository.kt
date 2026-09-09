package com.mr10.vello.data.repository

import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

interface AuthRepository {
    val currentUser: Flow<UserInfo?>
    val sessionStatus: Flow<SessionStatus>
    suspend fun checkUserExists(email: String): Boolean
    suspend fun signUpWithPassword(email: String, password: String, metadata: JsonObject? = null): Result<Unit>
    suspend fun signInWithPassword(email: String, password: String): Result<Unit>
    suspend fun signInWithPhone(phone: String): Result<Unit>
    suspend fun sendEmailOtp(email: String, shouldCreateUser: Boolean, metadata: JsonObject? = null): Result<Unit>
    suspend fun verifyPhoneOtp(phone: String, otp: String): Result<Unit>
    suspend fun verifyOtpCode(email: String, code: String, isSignUp: Boolean): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
