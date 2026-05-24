package com.fanyiadrien.ictu_ex.data.remote.spring

import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpringAuthRepository @Inject constructor(
    private val api: SpringAuthApi,
    private val tokenStore: TokenStore
) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private fun isValidIctuEmail(email: String) =
        email.trim().endsWith("@ictuniversity.edu.cm")

    // ── Register ──────────────────────────────────────────────────────────────

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        studentId: String,
        userType: String
    ): AppResult<User> {
        if (!isValidIctuEmail(email)) return AppResult.Error(AppError.INVALID_ICTU_EMAIL)
        return try {
            val response = api.register(
                RegisterRequest(email.trim(), password, displayName.trim(), studentId.trim(), userType)
            )
            tokenStore.save(response.token)
            AppResult.Success(response.toUser())
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String): AppResult<User> {
        if (!isValidIctuEmail(email)) return AppResult.Error(AppError.INVALID_ICTU_EMAIL)
        return try {
            val response = api.login(LoginRequest(email.trim(), password))
            tokenStore.save(response.token)
            AppResult.Success(response.toUser())
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    // ── OTP ───────────────────────────────────────────────────────────────────

    suspend fun sendOtp(email: String): AppResult<String> {
        return try {
            val response = api.sendOtp(OtpRequest(email.trim()))
            AppResult.Success(response.message)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    suspend fun verifyOtp(email: String, code: String): AppResult<Boolean> {
        return try {
            val response = api.verifyOtp(OtpVerifyRequest(email.trim(), code.trim()))
            if (response.verified) AppResult.Success(true)
            else AppResult.Error("Invalid or expired code. Please try again.")
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    // ── Session ───────────────────────────────────────────────────────────────

    fun signOut() = tokenStore.clear()

    fun isUserLoggedIn(): Boolean = tokenStore.get() != null

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun AuthResponse.toUser() = User(
        uid             = uid,
        email           = email,
        displayName     = displayName,
        studentId       = studentId,
        userType        = userType,
        profileImageUrl = profileImageUrl,
        createdAt       = createdAt
    )

    /**
     * Converts any exception into a user-readable message.
     *
     * Spring Boot returns JSON error bodies like:
     *   { "message": "Email already registered", "status": 409 }
     * or the standard Spring error format:
     *   { "error": "Conflict", "message": "...", "status": 409 }
     *
     * We try to parse those before falling back to generic messages.
     */
    private fun Exception.toReadableMessage(): String {
        if (this is IOException) return AppError.NETWORK_ERROR

        if (this is HttpException) {
            val body = response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                // Try to extract "message" field from Spring error JSON
                runCatching {
                    val adapter = moshi.adapter(Map::class.java)
                    val map = adapter.fromJson(body)
                    val msg = map?.get("message") as? String
                        ?: map?.get("error") as? String
                    if (!msg.isNullOrBlank()) return msg
                }
                // Fallback: map HTTP status to friendly message
                return when (code()) {
                    400 -> "Invalid request. Please check your details."
                    401 -> AppError.WRONG_PASSWORD
                    403 -> "Access denied."
                    404 -> AppError.USER_NOT_FOUND
                    409 -> AppError.EMAIL_ALREADY_IN_USE
                    422 -> "Validation failed. Please check your input."
                    500, 502, 503 -> "Server error. Please try again later."
                    else -> AppError.UNKNOWN_AUTH_ERROR
                }
            }
        }

        return if (message?.contains("Unable to resolve", ignoreCase = true) == true ||
            message?.contains("Failed to connect", ignoreCase = true) == true ||
            message?.contains("timeout", ignoreCase = true) == true)
            AppError.NETWORK_ERROR
        else
            AppError.UNKNOWN_AUTH_ERROR
    }
}
