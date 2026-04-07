package com.fanyiadrien.ictu_ex.data.repository

import com.fanyiadrien.ictu_ex.data.model.Listing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun getWishlistCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("wishlist")
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun toggleWishlist(listingId: String): Boolean {
        val collection = getWishlistCollection() ?: return false
        val doc = collection.document(listingId).get().await()
        
        return if (doc.exists()) {
            collection.document(listingId).delete().await()
            false
        } else {
            collection.document(listingId).set(mapOf("listingId" to listingId, "timestamp" to System.currentTimeMillis())).await()
            true
        }
    }

    suspend fun getWishlistedIds(): List<String> {
        val collection = getWishlistCollection() ?: return emptyList()
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Real-time stream of wishlisted listing IDs. */
    fun observeWishlistedIds(): Flow<Set<String>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptySet())
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    // Logic to handle auth state change could go here if needed
                }
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
            return@callbackFlow
        }

        val collection = firestore.collection("users").document(uid).collection("wishlist")
        val registration = collection.addSnapshotListener { snapshot, _ ->
            val ids = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
            trySend(ids)
        }
        awaitClose { registration.remove() }
    }

    /** Fetches full Listing objects for all wishlisted IDs. */
    suspend fun getWishlistedListings(): List<Listing> {
        val ids = getWishlistedIds()
        if (ids.isEmpty()) return emptyList()
        return try {
            ids.mapNotNull { id ->
                val doc = firestore.collection("listings").document(id).get().await()
                doc.toObject(Listing::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
