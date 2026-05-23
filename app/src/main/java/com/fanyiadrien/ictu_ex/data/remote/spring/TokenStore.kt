package com.fanyiadrien.ictu_ex.data.remote.spring

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the JWT returned by the Spring Boot login endpoint.
 *
 * Production note: replace SharedPreferences with EncryptedSharedPreferences
 * from androidx.security.crypto for secure token storage.
 */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("ictu_ex_prefs", Context.MODE_PRIVATE)

    fun save(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun get(): String? = prefs.getString(KEY_TOKEN, null)
    fun clear() = prefs.edit().remove(KEY_TOKEN).apply()

    companion object {
        private const val KEY_TOKEN = "jwt_token"
    }
}
