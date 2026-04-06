package com.fanyiadrien.ictu_ex.feature.messages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.data.model.ChatMessage
import com.fanyiadrien.ictu_ex.data.model.ChatThread
import com.fanyiadrien.ictu_ex.data.repository.MessagesRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var messagesJob: Job? = null

    init {
        observeThreads()

        val sellerId = savedStateHandle.get<String>(Screen.Messages.sellerIdArg)
        val listingId = savedStateHandle.get<String>(Screen.Messages.listingIdArg)
        if (!sellerId.isNullOrBlank()) {
            openOrCreateThread(sellerId, listingId)
        }
    }

    private fun observeThreads() {
        viewModelScope.launch {
            messagesRepository.observeThreads().collect { threads ->
                _uiState.update { state ->
                    val selected = state.selectedThreadId
                    val nextSelected = when {
                        selected.isNullOrBlank() && threads.isNotEmpty() -> threads.first().id
                        selected.isNullOrBlank() -> null
                        threads.any { it.id == selected } -> selected
                        else -> threads.firstOrNull()?.id
                    }
                    state.copy(threads = threads, selectedThreadId = nextSelected)
                }
                uiState.value.selectedThreadId?.let { collectMessages(it) }
            }
        }
    }

    fun selectThread(threadId: String) {
        _uiState.update { it.copy(selectedThreadId = threadId, composerText = "") }
        collectMessages(threadId)
        viewModelScope.launch { messagesRepository.markThreadRead(threadId) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedThreadId = null, composerText = "", messages = emptyList()) }
        messagesJob?.cancel()
    }

    private fun collectMessages(threadId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            messagesRepository.observeMessages(threadId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private fun openOrCreateThread(otherUserId: String, listingId: String?) {
        viewModelScope.launch {
            when (val result = messagesRepository.ensureThread(otherUserId, listingId)) {
                is AppResult.Success -> selectThread(result.data)
                is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                else -> Unit
            }
        }
    }

    fun onComposerTextChange(text: String) {
        _uiState.update { it.copy(composerText = text) }
    }

    fun sendMessage() {
        val threadId = _uiState.value.selectedThreadId ?: return
        val text = _uiState.value.composerText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            when (val result = messagesRepository.sendMessage(threadId, text)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(composerText = "", errorMessage = null) }
                    messagesRepository.markThreadRead(threadId)
                }
                is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                else -> Unit
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class MessagesUiState(
    val threads: List<ChatThread> = emptyList(),
    val selectedThreadId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val composerText: String = "",
    val errorMessage: String? = null
)

