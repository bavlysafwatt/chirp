package com.bavly.chirp.features.chat.presentation.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bavly.chirp.core.domain.util.Result
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.chat.domain.repository.ChatRepository
import com.bavly.chirp.features.chat.domain.repository.ParticipantRepository
import com.bavly.chirp.features.chat.presentation.mappers.toUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val participantRepository: ParticipantRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val eventChannel = Channel<ChatListEvent>()
    val events = eventChannel.receiveAsFlow()

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ChatListState(isLoading = true))
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeChats()
                loadLocalParticipant()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value
        )

    fun onAction(action: ChatListAction) {
        when (action) {
            is ChatListAction.OnSelectChat -> _state.update { it.copy(selectedChatId = action.chatId) }
            ChatListAction.OnUserAvatarClick -> _state.update { it.copy(isUserMenuOpen = true) }
            ChatListAction.OnLogoutClick -> showLogoutConfirmation()
            ChatListAction.OnConfirmLogout -> logout()
            ChatListAction.OnDismissLogoutDialog -> _state.update { it.copy(showLogoutConfirmation = false) }
            ChatListAction.OnProfileSettingsClick,
            ChatListAction.OnDismissUserMenu -> _state.update { it.copy(isUserMenuOpen = false) }

            else -> Unit
        }
    }

    private fun observeChats() {
        chatRepository.getChats()
            .onEach { chats ->
                val localId =
                    (participantRepository.getLocalParticipant() as? Result.Success)?.data?.userId
                _state.update {
                    it.copy(
                        chats = if (localId != null) chats.map { chat -> chat.toUi(localId) } else emptyList(),
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadLocalParticipant() {
        viewModelScope.launch {
            val local = participantRepository.getLocalParticipant()
            if (local is Result.Success) {
                _state.update { it.copy(localParticipant = local.data.toUi()) }
            }
        }
    }

    private fun logout() {
        _state.update { it.copy(showLogoutConfirmation = false) }
        viewModelScope.launch {
            authRepository.logout()
            eventChannel.send(ChatListEvent.OnLogoutSuccess)
        }
    }

    private fun showLogoutConfirmation() {
        _state.update { it.copy(isUserMenuOpen = false, showLogoutConfirmation = true) }
    }
}