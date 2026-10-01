package com.bavly.chirp.features.chat.presentation.chat_list

sealed interface ChatListEvent {
    data object OnLogoutSuccess : ChatListEvent
}