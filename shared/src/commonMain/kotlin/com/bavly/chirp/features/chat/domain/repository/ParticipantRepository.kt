package com.bavly.chirp.features.chat.domain.repository

import com.bavly.chirp.core.domain.util.DataError
import com.bavly.chirp.core.domain.util.Result
import com.bavly.chirp.features.chat.domain.model.ChatParticipant

interface ParticipantRepository {
    suspend fun getLocalParticipant(): Result<ChatParticipant, DataError.Remote>
    suspend fun searchParticipant(query: String): Result<ChatParticipant, DataError.Remote>
}