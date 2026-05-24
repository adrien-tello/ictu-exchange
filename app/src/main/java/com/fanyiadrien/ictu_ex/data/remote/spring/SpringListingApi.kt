package com.fanyiadrien.ictu_ex.data.remote.spring

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// ── DTOs ──────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ListingDto(
    val id: String = "",
    @Json(name = "seller_id")       val sellerId: String = "",
    @Json(name = "seller_student_id") val sellerStudentId: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    @Json(name = "image_url")  val imageUrl: String = "",
    val category: String = "",
    val available: Boolean = true,
    @Json(name = "created_at") val createdAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class PostListingRequest(
    val title: String,
    val description: String,
    val price: Double,
    @Json(name = "image_url")  val imageUrl: String,
    val category: String,
    @Json(name = "seller_student_id") val sellerStudentId: String = ""
)

// ── API Interface ─────────────────────────────────────────────────────────────

interface SpringListingApi {
    @GET("api/listings")
    suspend fun getAllListings(): List<ListingDto>

    @GET("api/listings/{id}")
    suspend fun getListingById(@Path("id") id: String): ListingDto

    @GET("api/listings/my")
    suspend fun getMyListings(): List<ListingDto>

    @POST("api/listings")
    suspend fun postListing(@Body request: PostListingRequest): ListingDto

    @DELETE("api/listings/{id}")
    suspend fun deleteListing(@Path("id") id: String)
}
