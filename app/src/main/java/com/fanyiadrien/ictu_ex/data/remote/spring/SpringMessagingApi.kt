package com.fanyiadrien.ictu_ex.data.remote.spring

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// ── DTOs ──────────────────────────────────────────────────────────────────────

/**
 * POST /api/messaging/conversations
 * Swagger StartConversationRequest: { otherUserId (required), listingId (optional) }
 */
@JsonClass(generateAdapter = true)
data class StartConversationRequest(
    val otherUserId: String,
    val listingId: String? = null
)

/**
 * Swagger ConversationView: { id, participantA, participantB, listingId, createdAt }
 */
@JsonClass(generateAdapter = true)
data class ConversationView(
    val id: String = "",
    val participantA: String = "",
    val participantB: String = "",
    val listingId: String? = null,
    val createdAt: String = ""
)

/**
 * POST /api/messaging/conversations/{conversationId}/messages
 * Swagger SendMessageRequest: { content }
 */
@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    val content: String
)

/**
 * Swagger MessageView: { id, conversationId, senderId, content, sentAt }
 */
@JsonClass(generateAdapter = true)
data class MessageView(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val content: String = "",
    val sentAt: String = ""
)

// ── API Interface ─────────────────────────────────────────────────────────────

interface SpringMessagingApi {

    /**
     * GET /api/messaging/conversations
     * Get all conversations for the current authenticated user.
     * Requires Bearer token.
     */
    @GET("api/messaging/conversations")
    suspend fun getMyConversations(): List<ConversationView>

    /**
     * POST /api/messaging/conversations
     * Start a new conversation or retrieve an existing one between two users.
     * Optionally linked to a listing.
     * Requires Bearer token.
     */
    @POST("api/messaging/conversations")
    suspend fun startConversation(@Body request: StartConversationRequest): ConversationView

    /**
     * GET /api/messaging/conversations/{conversationId}/messages
     * Retrieve the message history for a given conversation.
     * Requires Bearer token.
     */
    @GET("api/messaging/conversations/{conversationId}/messages")
    suspend fun getMessages(@Path("conversationId") conversationId: String): List<MessageView>

    /**
     * POST /api/messaging/conversations/{conversationId}/messages
     * Send a new message to a specified conversation.
     * Requires Bearer token.
     */
    @POST("api/messaging/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body request: SendMessageRequest
    ): MessageView
}
