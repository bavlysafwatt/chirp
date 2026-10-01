package com.bavly.chirp.features.chat.presentation.manage_chat

sealed interface ManageChatEvent {
    data object OnMembersAdded : ManageChatEvent
}