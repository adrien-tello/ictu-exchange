package com.fanyiadrien.ictu_ex.di

import com.fanyiadrien.ictu_ex.data.remote.EmailService
import com.fanyiadrien.ictu_ex.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCartRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        notificationRepository: NotificationRepository,
        emailService: EmailService
    ): CartRepository = CartRepository(auth, firestore, notificationRepository, emailService)

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository = UserRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideListingRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ListingRepository = ListingRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): NotificationRepository = NotificationRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideMessagesRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        emailService: EmailService
    ): MessagesRepository = MessagesRepository(auth, firestore, emailService)

    @Provides
    @Singleton
    fun provideWishlistRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): WishlistRepository = WishlistRepository(auth, firestore)
}
