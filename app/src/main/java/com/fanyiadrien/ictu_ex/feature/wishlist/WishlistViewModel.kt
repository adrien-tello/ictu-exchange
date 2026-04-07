package com.fanyiadrien.ictu_ex.feature.wishlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    var uiState by mutableStateOf(WishlistUiState())
        private set

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val listings = wishlistRepository.getWishlistedListings()
            uiState = uiState.copy(
                isLoading = false,
                wishlistedListings = listings
            )
        }
    }

    fun removeFromWishlist(listingId: String) {
        viewModelScope.launch {
            wishlistRepository.toggleWishlist(listingId)
            loadWishlist()
        }
    }
}

data class WishlistUiState(
    val wishlistedListings: List<Listing> = emptyList(),
    val isLoading: Boolean = false
)
