package com.mr10.vello.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class AuthRepository(private val supabaseClient: SupabaseClient) {

    private val auth = supabaseClient.auth
    private val functions = supabaseClient.functions

    val currentUser: Flow<UserInfo?> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user
            else -> null
        }
    }

    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    /**
     * 1. TRIGGER OTP (FOR BOTH SIGNUP & SIGNIN)
     * Distinguishes based on whether the user should be created.
     */
    suspend fun sendEmailOtp(
        emailVal: String,
        shouldCreateUser: Boolean,
        metadata: JsonObject? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWith(OTP) {
                email = emailVal
                createUser = shouldCreateUser
                metadata?.let {
                    data = it
                }
            }
            Unit
        }
    }

    /**
     * 2. SIGN UP WITH PASSWORD (FALLBACK OR ALTERNATIVE)
     */
    suspend fun signUpWithPassword(
        emailVal: String,
        passVal: String,
        metadata: JsonObject? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signUpWith(Email) {
                email = emailVal
                password = passVal
                metadata?.let {
                    data = it
                }
            }
            Unit
        }
    }

    /**
     * 3. VERIFY OTP CODE (FOR BOTH SIGNUP & SIGNIN)
     */
    suspend fun verifyOtpCode(emailVal: String, codeVal: String, isSignUp: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Match the OTP type to the action
            val targetType = if (isSignUp) OtpType.Email.SIGNUP else OtpType.Email.EMAIL

            auth.verifyEmailOtp(
                type = targetType,
                email = emailVal,
                token = codeVal
            )
            Unit
        }
    }

    suspend fun checkUserExists(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = functions.invoke("check-user", buildJsonObject {
                put("email", email)
            })
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            json["exists"]?.jsonPrimitive?.content?.toBoolean() ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signInWithPhone(phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWith(OTP) {
                this.phone = phone
            }
            Unit
        }
    }

    suspend fun verifyPhoneOtp(phone: String, otp: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.verifyPhoneOtp(
                type = OtpType.Phone.SMS,
                phone = phone,
                token = otp
            )
            Unit
        }
    }

    suspend fun signInWithPassword(emailVal: String, passVal: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWith(Email) {
                email = emailVal
                password = passVal
            }
            Unit
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signOut()
            Unit
        }
    }
}
