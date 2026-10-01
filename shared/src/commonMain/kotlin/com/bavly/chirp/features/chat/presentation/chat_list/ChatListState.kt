package com.bavly.chirp.features.chat.presentation.chat_list

import com.bavly.chirp.core.designsystem.components.avatar.ChatParticipantUi
import com.bavly.chirp.features.chat.presentation.model.ChatUi

data class ChatListState(
    val chats: List<ChatUi> = emptyList(),
    val localParticipant: ChatParticipantUi? = null,
    val isUserMenuOpen: Boolean = false,
    val showLogoutConfirmation: Boolean = false,
    val selectedChatId: String? = null,
    val isLoading: Boolean = false,
)