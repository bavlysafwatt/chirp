@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bavly.chirp.features.chat.data

import com.bavly.chirp.features.chat.data.dto.ChatDocument
import com.bavly.chirp.features.chat.data.dto.MessageDocument
import com.bavly.chirp.features.chat.domain.model.Chat
import com.bavly.chirp.features.chat.domain.model.ChatMessage
import com.bavly.chirp.features.chat.domain.model.ChatMessageDeliveryStatus
import com.bavly.chirp.features.chat.domain.model.ChatParticipant
import dev.gitlive.firebase.firestore.Timestamp
import kotlin.time.Clock
import kotlin.time.Instant

fun Timestamp.toInstant(): Instant = Instant.fromEpochSeconds(seconds, nanoseconds)

fun Instant.toFirestoreTimestamp(): Timestamp = Timestamp(epochSeconds, nanosecondsOfSecond)

fun ChatDocument.toDomain(id: String, participants: List<ChatParticipant>): Chat {
    val last = lastMessage
    return Chat(
        id = id,
        participants = participants,
        lastActivityAt = lastActivityAt?.toInstant() ?: Clock.System.now(),
        lastMessage = last?.let {
            ChatMessage(
                id = "",
                chatId = id,
                content = it.content,
                createdAt = it.createdAt?.toInstant() ?: Clock.System.now(),
                senderId = it.senderId,
                deliveryStatus = ChatMessageDeliveryStatus.SENT
            )
        },
        lastMessageSenderUsername = last?.let { lm -> participants.find { p -> p.userId == lm.senderId }?.username }
    )
}

fun MessageDocument.toDomain(id: String, chatId: String, hasPendingWrites: Boolean): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt?.toInstant() ?: Clock.System.now(),
        senderId = senderId,
        deliveryStatus = if (hasPendingWrites) ChatMessageDeliveryStatus.SENDING else ChatMessageDeliveryStatus.SENT
    )
}