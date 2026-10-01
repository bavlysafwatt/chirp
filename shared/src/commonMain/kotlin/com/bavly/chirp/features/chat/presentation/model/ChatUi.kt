package com.bavly.chirp.features.chat.presentation.model

import com.bavly.chirp.core.designsystem.components.avatar.ChatParticipantUi
import com.bavly.chirp.features.chat.domain.model.ChatMessage

data class ChatUi(
    val id: String,
    val localParticipant: ChatParticipantUi,
    val otherParticipants: List<ChatParticipantUi>,
    val lastMessage: ChatMessage?,
    val lastMessageSenderUsername: String?
)