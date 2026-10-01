package com.bavly.chirp.features.chat.presentation.create_chat

import com.bavly.chirp.features.chat.domain.model.Chat

sealed interface CreateChatEvent {
    data class OnChatCreated(val chat: Chat) : CreateChatEvent
}