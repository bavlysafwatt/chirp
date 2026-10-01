package com.bavly.chirp.features.chat.data.repository

import com.bavly.chirp.core.data.firebase.toDataError
import com.bavly.chirp.core.domain.util.DataError
import com.bavly.chirp.core.domain.util.Result
import com.bavly.chirp.core.domain.validation.UsernameValidator
import com.bavly.chirp.features.auth.data.dto.UserDocument
import com.bavly.chirp.features.auth.data.dto.UsernameClaimDocument
import com.bavly.chirp.features.chat.domain.model.ChatParticipant
import com.bavly.chirp.features.chat.domain.repository.ParticipantRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class FirestoreParticipantRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ParticipantRepository {

    override suspend fun getLocalParticipant(): Result<ChatParticipant, DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHENTICATED)
        return fetchParticipant(uid)
    }

    override suspend fun searchParticipant(query: String): Result<ChatParticipant, DataError.Remote> {
        val key = UsernameValidator.toKey(query.trim())
        if (key.isBlank()) return Result.Failure(DataError.Remote.NOT_FOUND)

        return try {
            val claim = firestore.collection(USERNAMES).document(key).get()
            if (!claim.exists) return Result.Failure(DataError.Remote.NOT_FOUND)
            val uid = claim.data(UsernameClaimDocument.serializer()).uid
            fetchParticipant(uid)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toDataError())
        }
    }

    private suspend fun fetchParticipant(uid: String): Result<ChatParticipant, DataError.Remote> {
        return try {
            val doc = firestore.collection(USERS).document(uid).get()
            if (!doc.exists) return Result.Failure(DataError.Remote.NOT_FOUND)
            val data = doc.data(UserDocument.serializer())
            Result.Success(
                ChatParticipant(
                    userId = uid,
                    username = data.username,
                    profilePictureUrl = data.profilePictureUrl
                )
            )
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toDataError())
        }
    }

    private companion object {
        const val USERS = "users"
        const val USERNAMES = "usernames"
    }
}