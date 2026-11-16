package com.example.ui.screens.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.localization.AppLanguage
import com.example.data.local.preferences.AppPreferencesDataStore
import com.example.domain.model.AssistantMessage
import com.example.domain.repository.HealthAssistantRepository
import com.example.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AssistantUiState(
    val messages: List<AssistantMessage> = emptyList(),
    val isThinking: Boolean = false,
    val activeUserId: Long? = null,
    val language: AppLanguage = AppLanguage.ENGLISH
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantRepository: HealthAssistantRepository,
    userRepository: UserRepository,
    preferences: AppPreferencesDataStore
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AssistantMessage>>(
        listOf(
            AssistantMessage(
                id = "welcome",
                isFromUser = false,
                text = "Welcome to MediGuide Local Assistant. Ask about today's doses, refill alerts, missed-dose rules, or care summaries — 100% offline."
            )
        )
    )
    private val _isThinking = MutableStateFlow(false)

    val uiState: StateFlow<AssistantUiState> = combine(
        _messages,
        _isThinking,
        userRepository.observeActiveProfile(),
        preferences.languageFlow
    ) { msgs, thinking, profile, lang ->
        AssistantUiState(
            messages = msgs,
            isThinking = thinking,
            activeUserId = profile?.id,
            language = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssistantUiState()
    )

    fun sendQuery(rawQuery: String) {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return
        val userId = uiState.value.activeUserId ?: return
        val lang = uiState.value.language

        val userMessage = AssistantMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = trimmed
        )
        _messages.update { it + userMessage }
        _isThinking.value = true

        viewModelScope.launch {
            val reply = assistantRepository.generateReply(
                userId = userId,
                query = trimmed,
                language = lang
            )
            val botMessage = AssistantMessage(
                id = UUID.randomUUID().toString(),
                isFromUser = false,
                text = reply
            )
            _messages.update { it + botMessage }
            _isThinking.value = false
        }
    }
}
