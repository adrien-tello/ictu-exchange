package com.fanyiadrien.ictu_ex.data.repository

import android.util.Log
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ListingRepository"

@Singleton
class ListingRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val listingsCollection = firestore.collection("listings")

    /**
     * Fetches all available listings, newest first.
     */
    suspend fun getAllListings(): AppResult<List<Listing>> {
        return try {
            val snapshot = listingsCollection
                .whereEqualTo("available", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val listings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Listing::class.java)?.copy(id = doc.id)
            }
            AppResult.Success(listings)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }

    /**
     * Fetches a single listing by its Firestore document ID.
     */
    suspend fun getListingById(listingId: String): AppResult<Listing> {
        return try {
            val doc = listingsCollection.document(listingId).get().await()
            val listing = doc.toObject(Listing::class.java)?.copy(id = doc.id)
                ?: return AppResult.Error(AppError.FETCH_FAILED)
            AppResult.Success(listing)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }

    /**
     * Posts a new listing to Firestore.
     */
    suspend fun postListing(listing: Listing): AppResult<Listing> {
        val uid = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

        return try {
            val docRef = listingsCollection.document()
            val listingWithId = listing.copy(
                id        = docRef.id,
                sellerId  = uid,
                createdAt = System.currentTimeMillis()
            )
            docRef.set(listingWithId).await()
            AppResult.Success(listingWithId)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Updates an existing listing in Firestore.
     */
    suspend fun updateListing(listing: Listing): AppResult<Unit> {
        return try {
            listingsCollection.document(listing.id)
                .set(listing)
                .await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Marks a listing as sold/unavailable.
     */
    suspend fun markListingUnavailable(listingId: String): AppResult<Unit> {
        return try {
            listingsCollection.document(listingId)
                .update("available", false)
                .await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Deletes a listing from Firestore.
     */
    suspend fun deleteListing(listingId: String): AppResult<Unit> {
        return try {
            listingsCollection.document(listingId).delete().await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /**
     * Fetches all listings posted by the current logged-in seller.
     */
    suspend fun getMyListings(): AppResult<List<Listing>> {
        val uid = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

        return try {
            val snapshot = listingsCollection
                .whereEqualTo("sellerId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val listings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Listing::class.java)?.copy(id = doc.id)
            }
            AppResult.Success(listings)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }
}