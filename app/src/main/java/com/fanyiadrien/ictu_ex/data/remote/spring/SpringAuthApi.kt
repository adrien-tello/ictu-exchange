package com.fanyiadrien.ictu_ex.data.remote.spring

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

// ── Request DTOs ──────────────────────────────────────────────────────────────

/** POST /api/auth/register — no userType field per Swagger schema */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val studentId: String
)

/** POST /api/auth/login */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

/** POST /api/auth/verify-code */
@JsonClass(generateAdapter = true)
data class VerifyCodeRequest(
    val email: String,
    val code: String
)

/** POST /api/auth/resend-token */
@JsonClass(generateAdapter = true)
data class ResendTokenRequest(
    val email: String
)

/** PATCH /api/auth/user-type */
@JsonClass(generateAdapter = true)
data class UpdateUserTypeRequest(
    val userType: String
)

// ── Response DTOs ─────────────────────────────────────────────────────────────

/**
 * Nested user object inside AuthResult.
 * Swagger schema: { id, email, displayName, studentId, userType }
 */
@JsonClass(generateAdapter = true)
data class AuthUser(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val studentId: String = "",
    val userType: String = ""
)

/**
 * Top-level auth response.
 * Swagger schema: { token, user: AuthUser, message }
 */
@JsonClass(generateAdapter = true)
data class AuthResult(
    val token: String = "",
    val user: AuthUser = AuthUser(),
    val message: String = ""
)

/** Generic message response used by verify-code, resend-token, logout */
@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: String = ""
)

// ── API Interface ─────────────────────────────────────────────────────────────

interface SpringAuthApi {

    /** Register new ICTU student — only @ictuniversity.edu.cm emails accepted */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResult

    /** Student login */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResult

    /** Verify 6-digit email code sent after registration */
    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): MessageResponse

    /** Resend account verification code */
    @POST("api/auth/resend-token")
    suspend fun resendToken(@Body request: ResendTokenRequest): MessageResponse

    /** Logout — invalidates JWT immediately via Redis blacklist */
    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") bearerToken: String): MessageResponse

    /** Validate JWT token — returns user info if valid */
    @GET("api/auth/validate")
    suspend fun validate(@Header("Authorization") bearerToken: String): Any

    /** Update user role type (SELLER / BUYER) */
    @PATCH("api/auth/user-type")
    suspend fun updateUserType(@Body request: UpdateUserTypeRequest): MessageResponse
}
