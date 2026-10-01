@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bavly.chirp.features.chat.presentation.mappers

import com.bavly.chirp.features.chat.domain.model.ChatMessage
import com.bavly.chirp.features.chat.domain.model.ChatParticipant
import com.bavly.chirp.features.chat.presentation.model.MessageUi
import com.bavly.chirp.features.chat.presentation.util.DateUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun List<ChatMessage>.toUiList(
    localUserId: String,
    participants: List<ChatParticipant>
): List<MessageUi> {
    return this
        .sortedByDescending { it.createdAt }
        .groupBy { it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date }
        .flatMap { (date, messages) ->
            messages.map { it.toUi(localUserId, participants) } + MessageUi.DateSeparator(
                id = date.toString(),
                date = DateUtils.formatDateSeparator(date)
            )
        }
}

fun ChatMessage.toUi(
    localUserId: String,
    participants: List<ChatParticipant>
): MessageUi {
    return if (senderId == localUserId) {
        MessageUi.LocalUserMessage(
            id = id,
            content = content,
            deliveryStatus = deliveryStatus,
            formattedSentTime = DateUtils.formatMessageTime(instant = createdAt)
        )
    } else {
        val sender = participants.find { it.userId == senderId }
            ?: ChatParticipant(userId = senderId, username = "Unknown", profilePictureUrl = null)
        MessageUi.OtherUserMessage(
            id = id,
            content = content,
            formattedSentTime = DateUtils.formatMessageTime(instant = createdAt),
            sender = sender.toUi()
        )
    }
}