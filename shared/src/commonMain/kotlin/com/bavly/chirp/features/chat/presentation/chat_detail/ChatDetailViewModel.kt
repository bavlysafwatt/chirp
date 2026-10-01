package com.bavly.chirp.features.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bavly.chirp.core.domain.util.onFailure
import com.bavly.chirp.core.domain.util.onSuccess
import com.bavly.chirp.core.presentation.util.toUiText
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.chat.domain.repository.ChatRepository
import com.bavly.chirp.features.chat.domain.repository.MessageRepository
import com.bavly.chirp.features.chat.presentation.mappers.toUi
import com.bavly.chirp.features.chat.presentation.mappers.toUiList
import com.bavly.chirp.features.chat.presentation.model.MessageUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val eventChannel = Channel<ChatDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _chatId = MutableStateFlow<String?>(null)

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ChatDetailState())

    private val canSendMessage = snapshotFlow { _state.value.messageTextFieldState.text.toString() }
        .map { it.isNotBlank() }

    private val chatWithMessagesFlow = _chatId.flatMapLatest { chatId ->
        if (chatId == null) return@flatMapLatest emptyFlow()

        combine(
            chatRepository.getChatById(chatId),
            chatRepository.getActiveParticipantsByChatId(chatId),
            messageRepository.getMessagesForChat(chatId),
            authRepository.observeSession(),
            _state
        ) { chat, participants, messages, authUser, currentState ->
            if (chat == null || authUser == null) {
                currentState
            } else {
                currentState.copy(
                    chatUi = chat.toUi(authUser.uid),
                    messages = messages.toUiList(authUser.uid, participants)
                )
            }
        }
    }

    val state = _chatId
        .flatMapLatest { chatId ->
            observeCanSendMessage()
            if (chatId != null) chatWithMessagesFlow else _state
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value
        )

    fun onAction(action: ChatDetailAction) {
        when (action) {
            is ChatDetailAction.OnSelectChat -> switchChat(action.chatId)
            ChatDetailAction.OnChatOptionsClick -> _state.update { it.copy(isChatOptionsOpen = true) }
            ChatDetailAction.OnDismissChatOptions -> _state.update { it.copy(isChatOptionsOpen = false) }
            is ChatDetailAction.OnDeleteMessageClick -> deleteMessage(action.message)
            is ChatDetailAction.OnMessageLongClick -> _state.update { it.copy(messageWithOpenMenu = action.message) }
            ChatDetailAction.OnDismissMessageMenu -> _state.update { it.copy(messageWithOpenMenu = null) }
            is ChatDetailAction.OnRetryClick -> retryMessage(action.message)
            ChatDetailAction.OnSendMessageClick -> sendMessage()
            ChatDetailAction.OnLeaveChatClick -> leaveChat()
            else -> Unit
        }
    }

    private fun observeCanSendMessage() {
        combine(canSendMessage, messageRepository.connectionState) { canSend, connectionState ->
            _state.update { it.copy(canSendMessage = canSend, connectionState = connectionState) }
        }.launchIn(viewModelScope)
    }

    private fun sendMessage() {
        val chatId = _chatId.value ?: return
        val content = _state.value.messageTextFieldState.text.toString().trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            messageRepository
                .sendMessage(chatId, content)
                .onSuccess {
                    _state.value.messageTextFieldState.clearText()
                    eventChannel.send(ChatDetailEvent.OnNewMessage)
                }
                .onFailure { error -> eventChannel.send(ChatDetailEvent.OnError(error.toUiText())) }
        }
    }

    private fun deleteMessage(message: MessageUi.LocalUserMessage) {
        val chatId = _chatId.value ?: return
        viewModelScope.launch {
            messageRepository.deleteMessage(chatId, message.id)
                .onFailure { error -> eventChannel.send(ChatDetailEvent.OnError(error.toUiText())) }
        }
    }

    private fun retryMessage(message: MessageUi.LocalUserMessage) {
        val chatId = _chatId.value ?: return
        viewModelScope.launch {
            messageRepository.retryMessage(chatId, message.id)
                .onFailure { error -> eventChannel.send(ChatDetailEvent.OnError(error.toUiText())) }
        }
    }

    private fun leaveChat() {
        val chatId = _chatId.value ?: return
        _state.update { it.copy(isChatOptionsOpen = false) }

        viewModelScope.launch {
            chatRepository.leaveChat(chatId)
                .onSuccess {
                    _state.value.messageTextFieldState.clearText()
                    _chatId.update { null }
                    _state.update { ChatDetailState() }
                    eventChannel.send(ChatDetailEvent.OnChatLeft)
                }
                .onFailure { error -> eventChannel.send(ChatDetailEvent.OnError(error.toUiText())) }
        }
    }

    private fun switchChat(chatId: String?) {
        _chatId.update { chatId }
        if (chatId == null) _state.update { ChatDetailState() }
    }
}