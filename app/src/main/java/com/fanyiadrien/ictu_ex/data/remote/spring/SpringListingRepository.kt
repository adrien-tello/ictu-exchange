package com.fanyiadrien.ictu_ex.data.remote.spring

import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpringListingRepository @Inject constructor(
    private val api: SpringListingApi
) {

    suspend fun getAllListings(): AppResult<List<Listing>> = safeCall {
        api.getAllListings().map { it.toListing() }
    }

    suspend fun getListingById(listingId: String): AppResult<Listing> = safeCall {
        api.getListingById(listingId).toListing()
    }

    suspend fun searchListings(
        title: String? = null,
        category: String? = null
    ): AppResult<List<Listing>> = safeCall {
        api.searchListings(title, category).map { it.toListing() }
    }

    suspend fun postListing(listing: Listing): AppResult<Listing> = safeCall {
        api.createListing(
            CreateListingRequest(
                title       = listing.title,
                description = listing.description,
                price       = listing.price,
                category    = listing.category,
                condition   = "Used",           // default; UI should pass this
                imageUrls   = if (listing.imageUrl.isNotBlank())
                                  listOf(listing.imageUrl)
                              else emptyList()
            )
        ).toListing()
    }

    suspend fun updateListing(
        listingId: String,
        request: UpdateListingRequest
    ): AppResult<Listing> = safeCall {
        api.updateListing(listingId, request).toListing()
    }

    suspend fun deleteListing(listingId: String): AppResult<Unit> = safeCall {
        api.deleteListing(listingId)
    }

    suspend fun analyzeImage(imageFile: File): AppResult<AiListingSuggestion> = safeCall {
        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
        api.analyzeImage(part)
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    /**
     * Maps the Spring ListingDto to the domain Listing model.
     * - imageUrls[] → imageUrl (first element, or empty)
     * - status "ACTIVE" → available = true
     * - condition stored in description prefix for now (UI can be updated later)
     */
    private fun ListingDto.toListing() = Listing(
        id          = id,
        sellerId    = sellerId,
        title       = title,
        description = description,
        price       = price,
        imageUrl    = imageUrls.firstOrNull() ?: "",
        category    = category,
        available   = status.equals("ACTIVE", ignoreCase = true) ||
                      status.equals("AVAILABLE", ignoreCase = true),
        createdAt   = 0L  // Spring returns ISO string; parse if needed
    )

    // ── Error handling ────────────────────────────────────────────────────────

    private suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (e: IOException) {
            AppResult.Error(AppError.NETWORK_ERROR, e)
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                401 -> "Unauthorized. Please sign in again."
                403 -> "Access denied."
                404 -> AppError.FETCH_FAILED
                500, 502, 503 -> "Server error. Please try again later."
                else -> AppError.FETCH_FAILED
            }
            AppResult.Error(msg, e)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }
}
