package com.fanyiadrien.ictu_ex.data.model

/**
 * Lightweight conversation model used by the in-app messages screen.
 */
data class ChatThread(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val unreadCountByUser: Map<String, Long> = emptyMap(),
    val listingId: String? = null,
    val listingTitle: String? = null
)

