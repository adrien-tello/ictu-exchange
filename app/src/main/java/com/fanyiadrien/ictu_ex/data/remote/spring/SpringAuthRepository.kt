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

    /**
     * Registers a new student. Note: Swagger RegisterRequest has no userType field.
     * userType is set separately via PATCH /api/auth/user-type after registration.
     */
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        studentId: String,
        userType: String
    ): AppResult<User> {
        if (!isValidIctuEmail(email)) return AppResult.Error(AppError.INVALID_ICTU_EMAIL)
        return try {
            val result = api.register(
                RegisterRequest(
                    email       = email.trim(),
                    password    = password,
                    displayName = displayName.trim(),
                    studentId   = studentId.trim()
                )
            )
            tokenStore.save(result.token)
            // Set userType after registration if provided
            if (userType.isNotBlank()) {
                runCatching { api.updateUserType(UpdateUserTypeRequest(userType)) }
            }
            AppResult.Success(result.user.toUser())
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String): AppResult<User> {
        if (!isValidIctuEmail(email)) return AppResult.Error(AppError.INVALID_ICTU_EMAIL)
        return try {
            val result = api.login(LoginRequest(email.trim(), password))
            tokenStore.save(result.token)
            AppResult.Success(result.user.toUser())
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    // ── OTP ───────────────────────────────────────────────────────────────────

    /** POST /api/auth/verify-code — verify the 6-digit code sent to email */
    suspend fun verifyCode(email: String, code: String): AppResult<String> {
        return try {
            val response = api.verifyCode(VerifyCodeRequest(email.trim(), code.trim()))
            AppResult.Success(response.message)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    /** POST /api/auth/resend-token — resend the verification code */
    suspend fun resendToken(email: String): AppResult<String> {
        return try {
            val response = api.resendToken(ResendTokenRequest(email.trim()))
            AppResult.Success(response.message)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /** POST /api/auth/logout — invalidates JWT on server via Redis blacklist */
    suspend fun logout(): AppResult<String> {
        val token = tokenStore.get()
            ?: return AppResult.Error("Not logged in.")
        return try {
            val response = api.logout("Bearer $token")
            tokenStore.clear()
            AppResult.Success(response.message)
        } catch (e: Exception) {
            tokenStore.clear() // clear locally even if server call fails
            AppResult.Success("Logged out.")
        }
    }

    // ── User type ─────────────────────────────────────────────────────────────

    /** PATCH /api/auth/user-type — switch between SELLER and BUYER */
    suspend fun updateUserType(userType: String): AppResult<String> {
        return try {
            val response = api.updateUserType(UpdateUserTypeRequest(userType))
            AppResult.Success(response.message)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage(), e)
        }
    }

    // ── Session ───────────────────────────────────────────────────────────────

    fun signOut() = tokenStore.clear()
    fun isUserLoggedIn(): Boolean = tokenStore.get() != null

    // ── Mappers ───────────────────────────────────────────────────────────────

    /** AuthUser { id, email, displayName, studentId, userType } → domain User */
    private fun AuthUser.toUser() = User(
        uid         = id,
        email       = email,
        displayName = displayName,
        studentId   = studentId,
        userType    = userType
    )

    // ── Error handling ────────────────────────────────────────────────────────

    private fun Exception.toReadableMessage(): String {
        if (this is IOException) return AppError.NETWORK_ERROR
        if (this is HttpException) {
            val body = response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                runCatching {
                    @Suppress("UNCHECKED_CAST")
                    val map = moshi.adapter(Map::class.java).fromJson(body) as? Map<String, Any>
                    val msg = map?.get("message") as? String ?: map?.get("error") as? String
                    if (!msg.isNullOrBlank()) return msg
                }
            }
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
        return if (message?.contains("Unable to resolve", ignoreCase = true) == true ||
            message?.contains("Failed to connect", ignoreCase = true) == true ||
            message?.contains("timeout", ignoreCase = true) == true)
            AppError.NETWORK_ERROR
        else AppError.UNKNOWN_AUTH_ERROR
    }
}
