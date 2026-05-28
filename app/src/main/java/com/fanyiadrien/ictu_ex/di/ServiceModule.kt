package com.fanyiadrien.ictu_ex.di

import com.fanyiadrien.ictu_ex.BuildConfig
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.remote.spring.RetrofitClient
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringAuthApi
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringAuthRepository
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringListingApi
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringListingRepository
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringMessagingApi
import com.fanyiadrien.ictu_ex.data.remote.spring.TokenStore
import com.fanyiadrien.ictu_ex.data.repository.AuthRepository
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Strategy Pattern switcher.
 *
 * BuildConfig.USE_SPRING_BACKEND is read from local.properties:
 *   useSpringBackend=true  → Spring Boot REST API (https://api.ictuex.teamnest.me)
 *   useSpringBackend=false → Firebase (original behaviour)
 *
 * To switch: edit local.properties → File → Sync Project with Gradle Files.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    // ── Retrofit API interfaces ───────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideSpringAuthApi(tokenStore: TokenStore): SpringAuthApi =
        RetrofitClient.build(tokenStore).create(SpringAuthApi::class.java)

    @Provides
    @Singleton
    fun provideSpringListingApi(tokenStore: TokenStore): SpringListingApi =
        RetrofitClient.build(tokenStore).create(SpringListingApi::class.java)

    @Provides
    @Singleton
    fun provideSpringMessagingApi(tokenStore: TokenStore): SpringMessagingApi =
        RetrofitClient.build(tokenStore).create(SpringMessagingApi::class.java)

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

                override suspend fun postListing(listing: Listing) =
                    springListingRepository.postListing(listing)

                override suspend fun deleteListing(listingId: String) =
                    springListingRepository.deleteListing(listingId)

                // getMyListings has no Spring endpoint — falls back to Firebase filter
                // or use searchListings with sellerId when API supports it
            }
        } else {
            ListingRepository(firebaseAuth, firestore)
        }
    }
}
