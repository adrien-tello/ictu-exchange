package com.fanyiadrien.ictu_ex.data.remote.spring

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST

// ── Request / Response DTOs ───────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val studentId: String,
    val userType: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String = "",
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val studentId: String = "",
    val userType: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class OtpRequest(
    val email: String
)

@JsonClass(generateAdapter = true)
data class OtpVerifyRequest(
    val email: String,
    val code: String
)

@JsonClass(generateAdapter = true)
data class OtpResponse(
    val message: String,
    val verified: Boolean = false
)

// ── API Interface ─────────────────────────────────────────────────────────────

interface SpringAuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/otp/send")
    suspend fun sendOtp(@Body request: OtpRequest): OtpResponse

    @POST("api/auth/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): OtpResponse
}
