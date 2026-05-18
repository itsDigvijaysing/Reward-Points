package com.rewardpoints.app.ui.screen.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardpoints.app.ai.AgentAuthException
import com.rewardpoints.app.ai.AgentMessage
import com.rewardpoints.app.ai.AgentRateLimitException
import com.rewardpoints.app.ai.AgentRepository
import com.rewardpoints.app.ai.AgentSafetyException
import com.rewardpoints.app.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AgentViewModel(
    private val agentRepository: AgentRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val key = userPreferences.getGeminiApiKey()
            _uiState.update { it.copy(isConfigured = !key.isNullOrBlank()) }
        }
    }

    /**
     * Refresh whether Gemini is configured. Cheap — just reads the StateFlow.
     * Called from the screen on resume in case the user just added a key in Settings.
     */
    fun refreshConfigured() {
        viewModelScope.launch {
            val key = userPreferences.getGeminiApiKey()
            _uiState.update { it.copy(isConfigured = !key.isNullOrBlank()) }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _uiState.value.isSending) return

        val userMsg = AgentMessage(role = AgentMessage.Role.USER, content = trimmed)
        val pendingMsg = AgentMessage(role = AgentMessage.Role.MODEL, content = "", isPending = true)
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg + pendingMsg,
                isSending = true,
                error = null
            )
        }

        viewModelScope.launch {
            // Transcript excludes the pending placeholder so we don't send an empty assistant turn.
            val transcript = _uiState.value.messages.filter { !it.isPending }
            agentRepository.sendMessage(transcript).fold(
                onSuccess = { reply ->
                    _uiState.update { state ->
                        val withoutPending = state.messages.dropLast(1) // remove the pending placeholder
                        state.copy(
                            messages = withoutPending + AgentMessage(
                                role = AgentMessage.Role.MODEL,
                                content = reply
                            ),
                            isSending = false,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { state ->
                        val withoutPending = state.messages.dropLast(1)
                        val errorMsg = when (e) {
                            is AgentAuthException -> "Your Gemini API key was rejected. Update it in Settings."
                            is AgentRateLimitException -> "Rate limit reached. Wait a minute and try again."
                            is AgentSafetyException -> "Gemini blocked that response. Try rephrasing."
                            else -> e.message ?: "Something went wrong. Please try again."
                        }
                        state.copy(
                            messages = withoutPending,
                            isSending = false,
                            error = errorMsg
                        )
                    }
                }
            )
        }
    }

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList(), error = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AgentUiState(
    val messages: List<AgentMessage> = emptyList(),
    val isSending: Boolean = false,
    val isConfigured: Boolean = false,
    val error: String? = null
)
