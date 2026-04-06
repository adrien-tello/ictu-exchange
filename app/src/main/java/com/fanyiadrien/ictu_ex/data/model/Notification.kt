package com.fanyiadrien.ictu_ex.data.model

/**
<<<<<<< Updated upstream
 * Represents a notification stored in Firestore.
 *
 * Collection: "notifications/{userId}/items"
 *
 * Types:
 *  - NEW_LISTING  → sent to all buyers when a seller posts a new item
 *  - NEW_ORDER    → sent to the seller when a buyer checks out
 *  - ORDER_PLACED → sent to the buyer confirming their own checkout
 */
data class Notification(
    val notifId: String = "",
    val type: String = "",
    val orderId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
=======
 * Represents a notification for a user (usually a seller).
 *
 * Collection: "notifications/{userId}/items"
 */
data class Notification(
    val notifId: String = "",
    val type: String = "NEW_ORDER", // e.g., "NEW_ORDER", "PRICE_DROP", "SYSTEM"
    val orderId: String = "",
    val buyerId: String = "",
>>>>>>> Stashed changes
    val itemSummary: String = "",
    val totalXaf: Double = 0.0,
    val read: Boolean = false,
    val createdAt: Long = 0L
)
