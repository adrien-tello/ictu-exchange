package com.fanyiadrien.ictu_ex.feature.post

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.ListingCategory
import com.fanyiadrien.ictu_ex.data.remote.CloudinaryService
import com.fanyiadrien.ictu_ex.data.repository.ListingRepository
import com.fanyiadrien.ictu_ex.data.repository.NotificationRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostItemViewModel @Inject constructor(
    private val cloudinaryService: CloudinaryService,
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var uiState by mutableStateOf(PostItemUiState())
        private set

    fun onTitleChanged(value: String)       { uiState = uiState.copy(title = value, errorMessage = null) }
    fun onDescriptionChanged(value: String) { uiState = uiState.copy(description = value) }
    fun onPriceChanged(value: String)       { uiState = uiState.copy(price = value, errorMessage = null) }
    fun onCategorySelected(cat: ListingCategory) { uiState = uiState.copy(selectedCategory = cat) }
    fun onImageSelected(uri: Uri)           { uiState = uiState.copy(selectedImageUri = uri, errorMessage = null) }
    fun onShareOnFacebookChanged(value: Boolean) { uiState = uiState.copy(shareOnFacebook = value) }
    fun clearError()                        { uiState = uiState.copy(errorMessage = null) }

    /**
     * Full pipeline:
     *   1. Validate form
     *   2. Upload image to Cloudinary
     *   3. Save listing to Firestore
     *   4. Fire-and-forget buyer notifications (never blocks or crashes the pipeline)
     *   5. Optional: Share to Facebook
     *   6. Call onSuccess — caller navigates away
     */
    fun postListing(context: Context, onSuccess: () -> Unit) {
        // ── Validate ──────────────────────────────────────────────────────────
        val imageUri = uiState.selectedImageUri
        if (imageUri == null) {
            uiState = uiState.copy(errorMessage = "Please select an image.")
            return
        }
        if (uiState.title.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please enter a title.")
            return
        }
        val priceValue = uiState.price.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            uiState = uiState.copy(errorMessage = "Please enter a valid price.")
            return
        }

        viewModelScope.launch {
            // ── Step 1: Upload image ──────────────────────────────────────────
            uiState = uiState.copy(isUploading = true, isSaving = false, errorMessage = null)

            val uploadResult = cloudinaryService.uploadImage(
                context  = context,
                imageUri = imageUri,
                folder   = "listings"
            )

            if (uploadResult is AppResult.Error) {
                uiState = uiState.copy(isUploading = false, errorMessage = uploadResult.message)
                return@launch
            }

            val imageUrl = (uploadResult as AppResult.Success).data

            // ── Step 2: Save listing to Firestore ─────────────────────────────
            uiState = uiState.copy(isUploading = false, isSaving = true)

            val sellerName = when (val u = userRepository.getCurrentUser()) {
                is AppResult.Success -> u.data.displayName
                else -> "A seller"
            }
            
            val sellerStudentId = when (val u = userRepository.getCurrentUser()) {
                is AppResult.Success -> u.data.studentId
                else -> ""
            }

            val listing = Listing(
                title           = uiState.title.trim(),
                description     = uiState.description.trim(),
                price           = priceValue,
                imageUrl        = imageUrl,
                category        = uiState.selectedCategory.displayName,
                sellerStudentId = sellerStudentId,
                available       = true
            )

            when (val saveResult = listingRepository.postListing(listing)) {
                is AppResult.Success -> {
                    uiState = uiState.copy(isSaving = false)

                    // ── Step 3: Notify buyers ─────────────────────────────────
                    viewModelScope.launch {
                        runCatching {
                            notificationRepository.notifyBuyersNewListing(
                                sellerId     = saveResult.data.sellerId,
                                sellerName   = sellerName,
                                listingTitle = saveResult.data.title,
                                listingId    = saveResult.data.id,
                                priceXaf     = saveResult.data.price
                            )
                        }
                    }

                    // ── Step 4: Share to Facebook if requested ────────────────
                    if (uiState.shareOnFacebook) {
                        shareToFacebook(context, saveResult.data)
                    }

                    // Navigate away immediately after save succeeds
                    onSuccess()
                }
                is AppResult.Error -> {
                    uiState = uiState.copy(isSaving = false, errorMessage = saveResult.message)
                }
                else -> uiState = uiState.copy(isSaving = false)
            }
        }
    }

    private fun shareToFacebook(context: Context, listing: Listing) {
        val shareText = "Check out this ${listing.title} on ICTU-Ex for XAF ${listing.price.toInt()}! \n\n${listing.description}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            // Target Facebook specifically if possible, otherwise show chooser
            setPackage("com.facebook.katana")
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Facebook app not installed, fallback to generic share
            val fallbackIntent = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }, "Share your listing")
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        }
    }
}

data class PostItemUiState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val selectedCategory: ListingCategory = ListingCategory.TEXTBOOKS,
    val selectedImageUri: Uri? = null,
    val shareOnFacebook: Boolean = false,
    val isUploading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val isLocked: Boolean get() = isUploading || isSaving

    val loadingMessage: String get() = when {
        isUploading -> "Uploading image…\nPlease wait"
        isSaving    -> "Saving your listing…"
        else        -> ""
    }

    val isFormValid: Boolean get() =
        title.isNotBlank() &&
        price.isNotBlank() &&
        price.toDoubleOrNull() != null &&
        price.toDoubleOrNull()!! > 0 &&
        selectedImageUri != null
}
