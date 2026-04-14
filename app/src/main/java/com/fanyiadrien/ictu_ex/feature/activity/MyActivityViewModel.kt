package com.fanyiadrien.ictu_ex.feature.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.Order
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class MyActivityUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val isSeller: Boolean = false,
    val userName: String = "Student",
    val myListings: List<Listing> = emptyList(),
    val myOrders: List<Order> = emptyList(), // Personal orders (Buyer) or Received orders (Seller)
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class MyActivityViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var uiState by mutableStateOf(MyActivityUiState())
        private set

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            
            val userResult = userRepository.getCurrentUser()
            when (userResult) {
                is AppResult.Success -> {
                    val user = userResult.data
                    val isSeller = user.userType == "SELLER"
                    uiState = uiState.copy(
                        isSeller = isSeller,
                        userName = user.displayName
                    )
                    
                    if (isSeller) {
                        fetchSellerListings()
                        fetchReceivedOrders(user.uid)
                    } else {
                        fetchMyOrders(user.uid)
                    }
                }
                is AppResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = userResult.message)
                }
                is AppResult.Loading -> { /* Wait */ }
            }
        }
    }

    private suspend fun fetchSellerListings() {
        when (val result = listingRepository.getMyListings()) {
            is AppResult.Success -> {
                uiState = uiState.copy(isLoading = false, myListings = result.data)
            }
            is AppResult.Error -> {
                uiState = uiState.copy(isLoading = false, errorMessage = result.message)
            }
            else -> Unit
        }
    }

    private suspend fun fetchReceivedOrders(sellerId: String) {
        try {
            val snapshot = firestore.collection("orders")
                .whereArrayContains("sellerIds", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val orders = snapshot.toObjects(Order::class.java)
            uiState = uiState.copy(myOrders = orders)
        } catch (e: Exception) {
            uiState = uiState.copy(errorMessage = "Failed to load received orders")
        }
    }

    private suspend fun fetchMyOrders(buyerId: String) {
        try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("buyerId", buyerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val orders = snapshot.toObjects(Order::class.java)
            uiState = uiState.copy(isLoading = false, myOrders = orders)
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Failed to load your orders")
        }
    }

    fun deleteListing(listingId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isActionLoading = true)
            when (val result = listingRepository.deleteListing(listingId)) {
                is AppResult.Success -> {
                    fetchSellerListings()
                    uiState = uiState.copy(isActionLoading = false, snackbarMessage = "Listing deleted successfully")
                }
                is AppResult.Error -> uiState = uiState.copy(isActionLoading = false, snackbarMessage = "Delete failed: ${result.message}")
                else -> uiState = uiState.copy(isActionLoading = false)
            }
        }
    }

    fun markAsSold(listingId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isActionLoading = true)
            when (val result = listingRepository.markListingUnavailable(listingId)) {
                is AppResult.Success -> {
                    fetchSellerListings()
                    uiState = uiState.copy(isActionLoading = false, snackbarMessage = "Item marked as sold")
                }
                is AppResult.Error -> uiState = uiState.copy(isActionLoading = false, snackbarMessage = "Update failed: ${result.message}")
                else -> uiState = uiState.copy(isActionLoading = false)
            }
        }
    }

    fun updateListing(listing: Listing) {
        viewModelScope.launch {
            uiState = uiState.copy(isActionLoading = true)
            when (val result = listingRepository.updateListing(listing)) {
                is AppResult.Success -> {
                    fetchSellerListings()
                    uiState = uiState.copy(isActionLoading = false, snackbarMessage = "Listing updated successfully")
                }
                is AppResult.Error -> uiState = uiState.copy(isActionLoading = false, snackbarMessage = "Update failed: ${result.message}")
                else -> uiState = uiState.copy(isActionLoading = false)
            }
        }
    }

    fun dismissSnackbar() {
        uiState = uiState.copy(snackbarMessage = null)
    }
}
