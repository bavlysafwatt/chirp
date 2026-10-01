package com.bavly.chirp.features.chat.presentation.mappers

import com.bavly.chirp.core.designsystem.components.avatar.ChatParticipantUi
import com.bavly.chirp.features.chat.domain.model.ChatParticipant

fun ChatParticipant.toUi(): ChatParticipantUi {
    return ChatParticipantUi(
        id = userId,
        username = username,
        initials = initials,
        imageUrl = profilePictureUrl
    )
}