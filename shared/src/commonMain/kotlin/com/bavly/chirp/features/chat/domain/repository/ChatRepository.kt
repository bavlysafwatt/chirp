package com.bavly.chirp.features.chat.domain.repository

import com.bavly.chirp.core.domain.util.DataError
import com.bavly.chirp.core.domain.util.EmptyResult
import com.bavly.chirp.core.domain.util.Result
import com.bavly.chirp.features.chat.domain.model.Chat
import com.bavly.chirp.features.chat.domain.model.ChatParticipant
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<Chat>>
    fun getChatById(chatId: String): Flow<Chat?>
    fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipant>>
    suspend fun createChat(otherUserIds: List<String>): Result<Chat, DataError.Remote>
    suspend fun addParticipantsToChat(
        chatId: String,
        userIds: List<String>
    ): Result<Chat, DataError.Remote>

    suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote>
}