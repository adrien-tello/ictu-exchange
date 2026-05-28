package com.fanyiadrien.ictu_ex.data.remote.spring

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody

// ── DTOs ──────────────────────────────────────────────────────────────────────

/**
 * Swagger Listing schema:
 * { id, title, description, price, category, condition, sellerId,
 *   status, imageUrls[], createdAt, updatedAt }
 */
@JsonClass(generateAdapter = true)
data class ListingDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val condition: String = "",
    val sellerId: String = "",
    val status: String = "",
    val imageUrls: List<String> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * POST /api/listings
 * Swagger CreateListingRequest: { title, description, price, category, condition, imageUrls[] }
 */
@JsonClass(generateAdapter = true)
data class CreateListingRequest(
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val condition: String,
    val imageUrls: List<String>
)

/**
 * PUT /api/listings/{id}
 * Swagger UpdateListingRequest: { title, description, price, category, condition, status, imageUrls[] }
 */
@JsonClass(generateAdapter = true)
data class UpdateListingRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val category: String? = null,
    val condition: String? = null,
    val status: String? = null,
    val imageUrls: List<String>? = null
)

/**
 * POST /api/listings/analyze-image response
 * Swagger AIListingSuggestion: { title, description, suggestedPrice, category, condition }
 */
@JsonClass(generateAdapter = true)
data class AiListingSuggestion(
    val title: String = "",
    val description: String = "",
    val suggestedPrice: Double = 0.0,
    val category: String = "",
    val condition: String = ""
)

// ── API Interface ─────────────────────────────────────────────────────────────

interface SpringListingApi {

    /** GET /api/listings — Get all active listings */
    @GET("api/listings")
    suspend fun getAllListings(): List<ListingDto>

    /** GET /api/listings/{id} — Get listing details by ID */
    @GET("api/listings/{id}")
    suspend fun getListingById(@Path("id") id: String): ListingDto

    /** POST /api/listings — Create a new marketplace listing */
    @POST("api/listings")
    suspend fun createListing(@Body request: CreateListingRequest): ListingDto

    /** PUT /api/listings/{id} — Update an existing listing */
    @PUT("api/listings/{id}")
    suspend fun updateListing(
        @Path("id") id: String,
        @Body request: UpdateListingRequest
    ): ListingDto

    /** DELETE /api/listings/{id} — Delete a listing */
    @DELETE("api/listings/{id}")
    suspend fun deleteListing(@Path("id") id: String)

    /**
     * GET /api/listings/search — Search active listings by title and/or category
     * Both params are optional.
     */
    @GET("api/listings/search")
    suspend fun searchListings(
        @Query("title") title: String? = null,
        @Query("category") category: String? = null
    ): List<ListingDto>

    /**
     * POST /api/listings/analyze-image — AI-powered listing analyzer
     * Upload item image, get AI-generated listing details.
     */
    @Multipart
    @POST("api/listings/analyze-image")
    suspend fun analyzeImage(
        @Part image: MultipartBody.Part
    ): AiListingSuggestion
}
