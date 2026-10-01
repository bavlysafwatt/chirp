@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.bavly.chirp.features.chat.presentation.manage_chat

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.error_participant_not_found
import com.bavly.chirp.core.domain.util.DataError
import com.bavly.chirp.core.domain.util.onFailure
import com.bavly.chirp.core.domain.util.onSuccess
import com.bavly.chirp.core.presentation.util.UiText
import com.bavly.chirp.core.presentation.util.toUiText
import com.bavly.chirp.features.chat.domain.repository.ChatRepository
import com.bavly.chirp.features.chat.domain.repository.ParticipantRepository
import com.bavly.chirp.features.chat.presentation.components.manage_chat.ManageChatAction
import com.bavly.chirp.features.chat.presentation.components.manage_chat.ManageChatState
import com.bavly.chirp.features.chat.presentation.mappers.toUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class ManageChatViewModel(
    private val chatRepository: ChatRepository,
    private val participantRepository: ParticipantRepository
) : ViewModel() {

    private val _chatId = MutableStateFlow<String?>(null)

    private val eventChannel = Channel<ManageChatEvent>()
    val events = eventChannel.receiveAsFlow()

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ManageChatState())
    val state = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) chatRepository.getActiveParticipantsByChatId(chatId) else emptyFlow()
        }
        .combine(_state) { participants, currentState ->
            currentState.copy(existingChatParticipants = participants.map { it.toUi() })
        }
        .onStart {
            if (!hasLoadedInitialData) {
                searchFlow.launchIn(viewModelScope)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value
        )

    private val searchFlow = snapshotFlow { _state.value.queryTextState.text.toString() }
        .debounce(1.seconds)
        .onEach { query -> performSearch(query) }

    fun onAction(action: ManageChatAction) {
        when (action) {
            ManageChatAction.OnAddClick -> addParticipant()
            ManageChatAction.OnPrimaryActionClick -> addParticipantsToChat()
            is ManageChatAction.ChatParticipants.OnSelectChat -> _chatId.update { action.chatId }
            else -> Unit
        }
    }

    private fun addParticipant() {
        _state.value.currentSearchResult?.let { participantFromSearch ->
            val isAlreadySelected =
                _state.value.selectedChatParticipants.any { it.id == participantFromSearch.id }
            val isAlreadyInChat =
                _state.value.existingChatParticipants.any { it.id == participantFromSearch.id }
            val updatedParticipants = if (isAlreadyInChat || isAlreadySelected) {
                _state.value.selectedChatParticipants
            } else _state.value.selectedChatParticipants + participantFromSearch

            _state.value.queryTextState.clearText()
            _state.update {
                it.copy(
                    selectedChatParticipants = updatedParticipants,
                    canAddParticipant = false,
                    currentSearchResult = null
                )
            }
        }
    }

    private fun addParticipantsToChat() {
        val chatId = _chatId.value ?: return
        val selectedUserIds = _state.value.selectedChatParticipants.map { it.id }
        if (selectedUserIds.isEmpty()) return

        viewModelScope.launch {
            chatRepository
                .addParticipantsToChat(chatId, selectedUserIds)
                .onSuccess { eventChannel.send(ManageChatEvent.OnMembersAdded) }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, submitError = error.toUiText()) }
                }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _state.update {
                it.copy(
                    currentSearchResult = null,
                    canAddParticipant = false,
                    searchError = null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, canAddParticipant = false) }

            participantRepository
                .searchParticipant(query)
                .onSuccess { participant ->
                    _state.update {
                        it.copy(
                            currentSearchResult = participant.toUi(),
                            isSearching = false,
                            canAddParticipant = true,
                            searchError = null
                        )
                    }
                }
                .onFailure { error ->
                    val message = when (error) {
                        DataError.Remote.NOT_FOUND -> UiText.Resource(Res.string.error_participant_not_found)
                        else -> error.toUiText()
                    }
                    _state.update {
                        it.copy(
                            searchError = message,
                            isSearching = false,
                            canAddParticipant = false,
                            currentSearchResult = null
                        )
                    }
                }
        }
    }
}