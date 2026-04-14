package com.fanyiadrien.ictu_ex.data.model

data class Order(
    val orderId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerIds: List<String> = emptyList(),
    val items: List<OrderItem> = emptyList(),
    val subtotalXaf: Double = 0.0,
    val discountXaf: Double = 0.0,
    val totalXaf: Double = 0.0,
    val promoCode: String = "",
    val status: String = "PENDING", // PENDING, COMPLETED, CANCELLED
    val createdAt: Long = 0L
)

data class OrderItem(
    val listingId: String = "",
    val title: String = "",
    val quantity: Int = 0,
    val priceXaf: Double = 0.0,
    val lineTotal: Double = 0.0
)
