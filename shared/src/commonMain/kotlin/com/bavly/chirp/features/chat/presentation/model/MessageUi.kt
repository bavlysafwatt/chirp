package com.bavly.chirp.features.chat.presentation.model

import com.bavly.chirp.core.designsystem.components.avatar.ChatParticipantUi
import com.bavly.chirp.core.presentation.util.UiText
import com.bavly.chirp.features.chat.domain.model.ChatMessageDeliveryStatus

sealed class MessageUi(open val id: String) {
    data class LocalUserMessage(
        override val id: String,
        val content: String,
        val deliveryStatus: ChatMessageDeliveryStatus,
        val formattedSentTime: UiText
    ) : MessageUi(id)

    data class OtherUserMessage(
        override val id: String,
        val content: String,
        val formattedSentTime: UiText,
        val sender: ChatParticipantUi
    ) : MessageUi(id)

    data class DateSeparator(
        override val id: String,
        val date: UiText,
    ) : MessageUi(id)
}