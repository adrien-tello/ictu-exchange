package com.fanyiadrien.ictu_ex.feature.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.remote.spring.SpringAuthRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val springAuthRepository: SpringAuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // 6 individual digit slots
    private val _digits = MutableStateFlow(List(6) { "" })
    val digits: StateFlow<List<String>> = _digits.asStateFlow()

    init {
        val rawEmail = savedStateHandle.get<String>("email") ?: ""
        val email = URLDecoder.decode(rawEmail, "UTF-8")
        _uiState.update { it.copy(email = email) }
        startCountdown()
    }

    // ── Digit input ───────────────────────────────────────────────────────────

    fun onDigitChanged(index: Int, value: String) {
        val sanitized = value.filter { it.isDigit() }.take(1)
        _digits.update { current ->
            current.toMutableList().also { it[index] = sanitized }
        }
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun getCode(): String = _digits.value.joinToString("")

    // ── Verify ────────────────────────────────────────────────────────────────

    fun verify(onSuccess: () -> Unit) {
        val code = getCode()
        if (code.length < 6) {
            _uiState.update { it.copy(errorMessage = "Please enter all 6 digits.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, errorMessage = null) }
            when (val result = springAuthRepository.verifyOtp(_uiState.value.email, code)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isVerifying = false, isVerified = true) }
                    onSuccess()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isVerifying = false, errorMessage = result.message) }
                    // Clear digits on wrong code
                    _digits.update { List(6) { "" } }
                }
                else -> _uiState.update { it.copy(isVerifying = false) }
            }
        }
    }

    // ── Resend ────────────────────────────────────────────────────────────────

    fun resendOtp() {
        if (_uiState.value.secondsLeft > 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, errorMessage = null) }
            when (val result = springAuthRepository.sendOtp(_uiState.value.email)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isResending = false, resendMessage = "Code sent!") }
                    _digits.update { List(6) { "" } }
                    startCountdown()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isResending = false, errorMessage = result.message) }
                }
                else -> _uiState.update { it.copy(isResending = false) }
            }
        }
    }

    fun dismissResendMessage() {
        _uiState.update { it.copy(resendMessage = null) }
    }

    // ── Countdown ─────────────────────────────────────────────────────────────

    private fun startCountdown() {
        timerJob?.cancel()
        _uiState.update { it.copy(secondsLeft = 60) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsLeft > 0) {
                delay(1_000)
                _uiState.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class OtpUiState(
    val email: String = "",
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val isVerified: Boolean = false,
    val secondsLeft: Int = 60,
    val errorMessage: String? = null,
    val resendMessage: String? = null
)
