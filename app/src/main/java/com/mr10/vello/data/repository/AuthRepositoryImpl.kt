package com.mr10.vello.data.repository

import com.mr10.vello.VelloApplication
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class AuthRepositoryImpl : AuthRepository {
    private val auth by lazy { VelloApplication.supabaseClient.auth }
    private val functions by lazy { VelloApplication.supabaseClient.functions }

    override val currentUser: Flow<UserInfo?> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user
            else -> null
        }
    }

    override val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    override suspend fun checkUserExists(email: String): Boolean {
        val response = functions.invoke("check-user", buildJsonObject {
            put("email", email)
        })
        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject
        return json["exists"]?.jsonPrimitive?.content?.toBoolean() ?: false
    }

    override suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInWithPhone(phone: String) {
        auth.signInWith(OTP) {
            this.phone = phone
        }
    }

    override suspend fun signInWithEmailOtp(email: String) {
        auth.signInWith(OTP) {
            this.email = email
            createUser = false
        }
    }

    override suspend fun verifyPhoneOtp(phone: String, otp: String) {
        auth.verifyPhoneOtp(
            type = OtpType.Phone.SMS,
            phone = phone,
            token = otp
        )
    }

    override suspend fun verifyEmailOtp(email: String, otp: String) {
        auth.verifyEmailOtp(
            type = OtpType.Email.EMAIL,
            email = email,
            token = otp
        )
    }

    override suspend fun verifyEmailSignup(email: String, otp: String) {
        auth.verifyEmailOtp(
            type = OtpType.Email.SIGNUP,
            email = email,
            token = otp
        )
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
