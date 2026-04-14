package com.fanyiadrien.ictu_ex.data.repository

import com.fanyiadrien.ictu_ex.data.model.CartItem
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.remote.EmailService
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository,
    private val emailService: EmailService
) {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addListing(listing: Listing) {
        _items.update { current ->
            val existing = current.find { it.listingId == listing.id }
            if (existing != null) {
                current.map {
                    if (it.listingId == listing.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                current + CartItem(
                    id        = UUID.randomUUID().toString(),
                    listingId = listing.id,
                    title     = listing.title,
                    imageUrl  = listing.imageUrl,
                    priceXaf  = listing.price,
                    size      = "STD",
                    colorHex  = "#6200EE",
                    quantity  = 1
                )
            }
        }
    }

    fun increment(itemId: String) {
        _items.update { list ->
            list.map { if (it.id == itemId) it.copy(quantity = it.quantity + 1) else it }
        }
    }

    fun decrement(itemId: String) {
        _items.update { list ->
            list.map {
                if (it.id == itemId && it.quantity > 1) it.copy(quantity = it.quantity - 1) else it
            }
        }
    }

    fun remove(itemId: String) {
        _items.update { list -> list.filter { it.id != itemId } }
    }

    fun isInCart(listingId: String): Boolean =
        _items.value.any { it.listingId == listingId }

    suspend fun checkout(
        discountPercent: Int,
        promoCode: String
    ): AppResult<String> {
        val buyerId = auth.currentUser?.uid
            ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)

        val cartItems = _items.value
        if (cartItems.isEmpty()) return AppResult.Error("Cart is empty.")

        val orderId  = UUID.randomUUID().toString()
        val subtotal = cartItems.sumOf { it.lineTotal }
        val discount = subtotal * discountPercent / 100.0
        val total    = subtotal - discount
        val now      = System.currentTimeMillis()

        return try {
            val buyerName = firestore.collection("users").document(buyerId)
                .get().await().getString("displayName") ?: "A student"

            val sellerItems = mutableMapOf<String, MutableList<CartItem>>()
            for (item in cartItems) {
                val sellerId = firestore.collection("listings")
                    .document(item.listingId).get().await()
                    .getString("sellerId") ?: continue
                sellerItems.getOrPut(sellerId) { mutableListOf() }.add(item)
            }

            val batch = firestore.batch()

            val orderRef = firestore.collection("orders").document(orderId)
            batch.set(orderRef, mapOf(
                "orderId"         to orderId,
                "buyerId"         to buyerId,
                "buyerName"       to buyerName,
                "sellerIds"       to sellerItems.keys.toList(),
                "items"           to cartItems.map { item ->
                    mapOf(
                        "listingId" to item.listingId,
                        "title"     to item.title,
                        "quantity"  to item.quantity,
                        "priceXaf"  to item.priceXaf,
                        "lineTotal" to item.lineTotal
                    )
                },
                "subtotalXaf"     to subtotal,
                "discountXaf"     to discount,
                "totalXaf"        to total,
                "status"          to "PENDING",
                "createdAt"       to now
            ))
            batch.commit().await()

            for ((sellerId, items) in sellerItems) {
                val summary = items.joinToString(", ") { "${it.title} ×${it.quantity}" }
                val sellerTotal = items.sumOf { it.lineTotal }
                
                notificationRepository.notifySellerNewOrder(
                    sellerId    = sellerId,
                    buyerId     = buyerId,
                    buyerName   = buyerName,
                    orderId     = orderId,
                    itemSummary = summary,
                    totalXaf    = sellerTotal
                )

                val sellerDoc = firestore.collection("users").document(sellerId).get().await()
                val sellerEmail = sellerDoc.getString("email")
                val sellerName = sellerDoc.getString("displayName") ?: "Seller"
                
                if (sellerEmail != null) {
                    emailService.sendOrderConfirmation(
                        sellerEmail = sellerEmail,
                        sellerName  = sellerName,
                        buyerName   = buyerName,
                        listingTitle = summary,
                        orderId     = orderId
                    )
                }
            }

            val allSummary = cartItems.joinToString(", ") { "${it.title} ×${it.quantity}" }
            notificationRepository.notifyBuyerOrderPlaced(
                buyerId     = buyerId,
                orderId     = orderId,
                itemSummary = allSummary,
                totalXaf    = total
            )

            _items.update { emptyList() }
            AppResult.Success(orderId)

        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }
}
