package com.fanyiadrien.ictu_ex.data.remote.spring

import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpringListingRepository @Inject constructor(
    private val api: SpringListingApi
) {

    suspend fun getAllListings(): AppResult<List<Listing>> = runCatching {
        api.getAllListings().map { it.toListing() }
    }.toAppResult()

    suspend fun getListingById(listingId: String): AppResult<Listing> = runCatching {
        api.getListingById(listingId).toListing()
    }.toAppResult()

    suspend fun getMyListings(): AppResult<List<Listing>> = runCatching {
        api.getMyListings().map { it.toListing() }
    }.toAppResult()

    suspend fun postListing(listing: Listing): AppResult<Listing> = runCatching {
        api.postListing(
            PostListingRequest(
                title             = listing.title,
                description       = listing.description,
                price             = listing.price,
                imageUrl          = listing.imageUrl,
                category          = listing.category,
                sellerStudentId   = listing.sellerStudentId
            )
        ).toListing()
    }.toAppResult()

    suspend fun deleteListing(listingId: String): AppResult<Unit> = runCatching {
        api.deleteListing(listingId)
    }.toAppResult()

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun ListingDto.toListing() = Listing(
        id               = id,
        sellerId         = sellerId,
        sellerStudentId  = sellerStudentId,
        title            = title,
        description      = description,
        price            = price,
        imageUrl         = imageUrl,
        category         = category,
        available        = available,
        createdAt        = createdAt
    )

    private fun <T> Result<T>.toAppResult(): AppResult<T> =
        fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { e ->
                val msg = when {
                    e is java.io.IOException -> AppError.NETWORK_ERROR
                    e is retrofit2.HttpException -> when (e.code()) {
                        401 -> "Unauthorized. Please sign in again."
                        403 -> "Access denied."
                        404 -> AppError.FETCH_FAILED
                        500, 502, 503 -> "Server error. Please try again later."
                        else -> AppError.FETCH_FAILED
                    }
                    e.message?.contains("Unable to resolve", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true -> AppError.NETWORK_ERROR
                    else -> AppError.FETCH_FAILED
                }
                AppResult.Error(msg, e as? Exception)
            }
        )
}
