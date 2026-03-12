package com.fanyiadrien.ictu_ex.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// ─────────────────────────────────────────────────────────────────────────────
// OnboardingViewModel
//
// Manages the current page index for the onboarding pager.
// Currently the screen shows a single static slide (page 0).
// When you add swipe/pager support, increment currentPage here
// and the PageIndicator composable will react automatically.
// ─────────────────────────────────────────────────────────────────────────────

class OnboardingViewModel : ViewModel() {

    // The currently active onboarding page (0-indexed, max = totalPages - 1)
    var currentPage by mutableIntStateOf(0)
        private set

    // Total number of onboarding pages
    val totalPages: Int = 3

    /** Move to the next page. Call this from a HorizontalPager or a swipe gesture. */
    fun nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++
        }
    }

    /** Move to the previous page. */
    fun previousPage() {
        if (currentPage > 0) {
            currentPage--
        }
    }

    /** Jump to a specific page — useful if you add tap-on-dot navigation. */
    fun goToPage(page: Int) {
        if (page in 0 until totalPages) {
            currentPage = page
        }
    }
}