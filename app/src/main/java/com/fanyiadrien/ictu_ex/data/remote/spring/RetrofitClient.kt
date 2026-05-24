package com.fanyiadrien.ictu_ex.data.remote.spring

import com.fanyiadrien.ictu_ex.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * Base URL is read from BuildConfig, which is set from local.properties:
     *
     *   Production:  SPRING_BASE_URL=https://api.ictuex.teamnest.me/
     *   Emulator:    SPRING_BASE_URL=http://10.0.2.2:8081/
     *   Physical:    SPRING_BASE_URL=http://192.168.X.X:8081/
     *
     * Change the value in local.properties then sync Gradle — no code change needed.
     */
    val BASE_URL: String = BuildConfig.SPRING_BASE_URL

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun buildOkHttpClient(tokenStore: TokenStore): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // Attach JWT to every request that has a token
            .addInterceptor { chain ->
                val token = tokenStore.get()
                val request = if (!token.isNullOrBlank()) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    fun build(tokenStore: TokenStore): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildOkHttpClient(tokenStore))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
}
