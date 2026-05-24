package com.fanyiadrien.ictu_ex.di

import com.fanyiadrien.ictu_ex.BuildConfig
import com.fanyiadrien.ictu_ex.data.remote.spring.RetrofitClient
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringAuthApi
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringAuthRepository
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringListingApi
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringListingRepository
import com.fanyiadrien.ictu_ex.data.remote.spring.TokenStore
import com.fanyiadrien.ictu_ex.data.repository.AuthRepository
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provides all networking and repository dependencies.
 *
 * Backend is controlled by local.properties:
 *   useSpringBackend=true   → Spring Boot REST API  (BuildConfig.SPRING_BASE_URL)
 *   useSpringBackend=false  → Firebase Auth + Firestore
 *
 * To switch: edit local.properties → File → Sync Project with Gradle Files.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    // ── Single OkHttpClient ───────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenStore: TokenStore): OkHttpClient =
        RetrofitClient.buildOkHttpClient(tokenStore)

    // ── Single Retrofit instance shared by all API interfaces ─────────────────

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                retrofit2.converter.moshi.MoshiConverterFactory.create(
                    com.squareup.moshi.Moshi.Builder()
                        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()

    // ── API interfaces ────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideSpringAuthApi(retrofit: Retrofit): SpringAuthApi =
        retrofit.create(SpringAuthApi::class.java)

    @Provides
    @Singleton
    fun provideSpringListingApi(retrofit: Retrofit): SpringListingApi =
        retrofit.create(SpringListingApi::class.java)

    // ── AuthRepository strategy ───────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        springAuthRepository: SpringAuthRepository
    ): AuthRepository {
        return if (BuildConfig.USE_SPRING_BACKEND) {
            object : AuthRepository(firebaseAuth, firestore) {
                override suspend fun signUp(
                    email: String, password: String,
                    displayName: String, studentId: String, userType: String
                ) = springAuthRepository.signUp(email, password, displayName, studentId, userType)

                override suspend fun signIn(email: String, password: String) =
                    springAuthRepository.signIn(email, password)

                override fun signOut() = springAuthRepository.signOut()

                override fun isUserLoggedIn() = springAuthRepository.isUserLoggedIn()
            }
        } else {
            AuthRepository(firebaseAuth, firestore)
        }
    }

    // ── ListingRepository strategy ────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideListingRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        springListingRepository: SpringListingRepository
    ): ListingRepository {
        return if (BuildConfig.USE_SPRING_BACKEND) {
            object : ListingRepository(firebaseAuth, firestore) {
                override suspend fun getAllListings() =
                    springListingRepository.getAllListings()

                override suspend fun getListingById(listingId: String) =
                    springListingRepository.getListingById(listingId)

                override suspend fun getMyListings() =
                    springListingRepository.getMyListings()

                override suspend fun postListing(listing: com.fanyiadrien.ictu_ex.data.model.Listing) =
                    springListingRepository.postListing(listing)

                override suspend fun deleteListing(listingId: String) =
                    springListingRepository.deleteListing(listingId)
            }
        } else {
            ListingRepository(firebaseAuth, firestore)
        }
    }
}
