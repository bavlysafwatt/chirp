package com.bavly.chirp.features.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.TextFieldState
import com.bavly.chirp.core.presentation.util.UiText
import com.bavly.chirp.features.chat.domain.model.ConnectionState
import com.bavly.chirp.features.chat.presentation.model.ChatUi
import com.bavly.chirp.features.chat.presentation.model.MessageUi

data class ChatDetailState(
    val chatUi: ChatUi? = null,
    val messages: List<MessageUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val messageTextFieldState: TextFieldState = TextFieldState(),
    val canSendMessage: Boolean = false,
    val messageWithOpenMenu: MessageUi.LocalUserMessage? = null,
    val isChatOptionsOpen: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.CONNECTED
)