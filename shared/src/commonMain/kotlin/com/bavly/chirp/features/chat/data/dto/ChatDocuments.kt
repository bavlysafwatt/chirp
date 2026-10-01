package com.bavly.chirp.features.chat.data.dto

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class ChatDocument(
    val participantIds: List<String> = emptyList(),
    val lastMessage: LastMessageDto? = null,
    val lastActivityAt: Timestamp? = null
)

@Serializable
data class LastMessageDto(
    val content: String = "",
    val senderId: String = "",
    val createdAt: Timestamp? = null
)

@Serializable
data class MessageDocument(
    val senderId: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null
)