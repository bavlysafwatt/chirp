package com.bavly.chirp.features.chat.domain.repository

import com.bavly.chirp.core.domain.util.DataError
import com.bavly.chirp.core.domain.util.EmptyResult
import com.bavly.chirp.features.chat.domain.model.ChatMessage
import com.bavly.chirp.features.chat.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    val connectionState: StateFlow<ConnectionState>

    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(chatId: String, content: String): EmptyResult<DataError.Remote>
    suspend fun retryMessage(chatId: String, messageId: String): EmptyResult<DataError.Remote>
    suspend fun deleteMessage(chatId: String, messageId: String): EmptyResult<DataError.Remote>
}