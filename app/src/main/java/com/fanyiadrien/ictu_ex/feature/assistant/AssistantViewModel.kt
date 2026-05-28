package com.fanyiadrien.ictu_ex.feature.assistant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val inputText: String = "",
    val userName: String = "Student",
    val isListening: Boolean = false,
    val lastPrompt: String? = null
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(AssistantUiState())
        private set

    init {
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> {
                    val firstName = result.data.displayName.split(" ").firstOrNull() ?: "Student"
                    uiState = uiState.copy(userName = firstName)
                }
                else -> Unit
            }
        }
    }

    fun onInputChanged(text: String) {
        uiState = uiState.copy(inputText = text)
    }

    fun onSend() {
        if (uiState.inputText.isBlank()) return
        uiState = uiState.copy(lastPrompt = uiState.inputText, inputText = "")
    }

    fun onChipSelected(prompt: String) {
        uiState = uiState.copy(lastPrompt = prompt)
    }

    fun onMicToggle() {
        uiState = uiState.copy(isListening = !uiState.isListening)
    }
}
