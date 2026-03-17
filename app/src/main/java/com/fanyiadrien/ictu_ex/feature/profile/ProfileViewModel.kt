package com.fanyiadrien.ictu_ex.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.data.repository.AuthRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> uiState = uiState.copy(
                    user = result.data,
                    isLoading = false
                )
                is AppResult.Error -> uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
                else -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoggingOut = true)
            authRepository.signOut()
            uiState = uiState.copy(isLoggingOut = false)
            onComplete()
        }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null
)
