package com.mr10.vello.data.repository

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.functions.Functions
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
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val functions: Functions
) : AuthRepository {

    override val currentUser: Flow<UserInfo?> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user
            else -> null
        }
    }

    override val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    override suspend fun checkUserExists(email: String): Boolean = withContext(Dispatchers.IO) {
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

    override suspend fun signUpWithPassword(
        email: String,
        password: String,
        metadata: JsonObject?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                metadata?.let {
                    data = it
                }
            }
            Unit
        }
    }

    override suspend fun signInWithPassword(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Unit
        }
    }

    override suspend fun signInWithPhone(phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWith(OTP) {
                this.phone = phone
            }
            Unit
        }
    }

    override suspend fun sendEmailOtp(
        email: String,
        shouldCreateUser: Boolean,
        metadata: JsonObject?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWith(OTP) {
                this.email = email
                createUser = shouldCreateUser
                metadata?.let {
                    data = it
                }
            }
            Unit
        }
    }

    override suspend fun verifyPhoneOtp(phone: String, otp: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.verifyPhoneOtp(
                type = OtpType.Phone.SMS,
                phone = phone,
                token = otp
            )
            Unit
        }
    }

    override suspend fun verifyOtpCode(
        email: String,
        code: String,
        isSignUp: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val targetType = if (isSignUp) OtpType.Email.SIGNUP else OtpType.Email.MAGIC_LINK
            try {
                auth.verifyEmailOtp(
                    type = targetType,
                    email = email,
                    token = code
                )
            } catch (e: Exception) {
                auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = email,
                    token = code
                )
            }
            Unit
        }
    }

    override suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signOut()
            Unit
        }
    }
}
