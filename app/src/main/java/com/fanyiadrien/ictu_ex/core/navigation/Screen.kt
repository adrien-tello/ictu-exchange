package com.fanyiadrien.ictu_ex.core.navigation

sealed class Screen(val route: String) {

    object Onboarding    : Screen("onboarding")
    object CheckStatus   : Screen("check_status")

    object SignUp : Screen("sign_up/{userType}") {
        fun createRoute(userType: String) = "sign_up/$userType"
    }
    object SignIn : Screen("sign_in")

    object Home          : Screen("home")
    object Search        : Screen("search")
    object Wishlist      : Screen("wishlist")
    object PostItem      : Screen("post_item")
    object Profile       : Screen("profile")
    object EditProfile   : Screen("edit_profile")
    object Settings      : Screen("settings")
    object Camera        : Screen("camera")
    object Cart          : Screen("cart")
    object Notifications : Screen("notifications")

    object ChatList : Screen("chat_list")

    object Chat : Screen("chat/{threadId}") {
        fun createRoute(threadId: String) = "chat/$threadId"
    }

    object Messages : Screen("messages") {
        const val sellerIdArg  = "sellerId"
        const val listingIdArg = "listingId"
        const val routeWithArgs = "messages?sellerId={sellerId}&listingId={listingId}"
        fun createRoute(sellerId: String? = null, listingId: String? = null): String {
            val params = mutableListOf<String>()
            if (!sellerId.isNullOrBlank()) params.add("sellerId=$sellerId")
            if (!listingId.isNullOrBlank()) params.add("listingId=$listingId")

            return if (params.isEmpty()) "messages" else "messages?${params.joinToString("&")}"
        }
    }
    object MyActivity    : Screen("my_activity")

    object OrderSuccess : Screen("order_success/{orderId}") {
        fun createRoute(orderId: String) = "order_success/$orderId"
    }

    object ItemDetail : Screen("item_detail/{listingId}") {
        fun createRoute(listingId: String) = "item_detail/$listingId"
    }
}
