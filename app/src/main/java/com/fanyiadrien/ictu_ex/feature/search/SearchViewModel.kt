package com.fanyiadrien.ictu_ex.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.ListingCategory
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.fanyiadrien.ictu_ex.data.repository.WishlistRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set

    init {
        fetchListings()
        loadWishlist()
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            val ids = wishlistRepository.getWishlistedIds()
            uiState = uiState.copy(wishlistedIds = ids.toSet())
        }
    }

    fun toggleWishlist(listingId: String) {
        viewModelScope.launch {
            val isAdded = wishlistRepository.toggleWishlist(listingId)
            val current = uiState.wishlistedIds.toMutableSet()
            if (isAdded) current.add(listingId) else current.remove(listingId)
            uiState = uiState.copy(wishlistedIds = current)
        }
    }

    fun fetchListings() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = listingRepository.getAllListings()) {
                is AppResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        allListings = result.data,
                        filteredListings = applyFilter(result.data, uiState.searchQuery, uiState.selectedCategory)
                    )
                }
                is AppResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(
            searchQuery = query,
            filteredListings = applyFilter(uiState.allListings, query, uiState.selectedCategory)
        )
    }

    fun onCategorySelected(category: ListingCategory) {
        uiState = uiState.copy(
            selectedCategory = category,
            filteredListings = applyFilter(uiState.allListings, uiState.searchQuery, category)
        )
    }

    private fun applyFilter(
        listings: List<Listing>,
        query: String,
        category: ListingCategory
    ): List<Listing> {
        return listings.filter { listing ->
            val matchesQuery = query.isBlank() ||
                    listing.title.contains(query, ignoreCase = true) ||
                    listing.description.contains(query, ignoreCase = true)
            val matchesCategory = category == ListingCategory.ALL ||
                    listing.category.equals(category.displayName, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }
}

data class SearchUiState(
    val searchQuery: String = "",
    val selectedCategory: ListingCategory = ListingCategory.ALL,
    val allListings: List<Listing> = emptyList(),
    val filteredListings: List<Listing> = emptyList(),
    val wishlistedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
