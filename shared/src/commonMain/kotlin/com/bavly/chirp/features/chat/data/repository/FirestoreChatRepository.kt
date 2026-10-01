@file:OptIn(ExperimentalTime::class)

package com.bavly.chirp.features.chat.data.repository

import com.bavly.chirp.core.data.firebase.toDataError
import com.bavly.chirp.core.domain.util.DataError
import com.bavly.chirp.core.domain.util.EmptyResult
import com.bavly.chirp.core.domain.util.Result
import com.bavly.chirp.features.auth.data.dto.UserDocument
import com.bavly.chirp.features.chat.data.dto.ChatDocument
import com.bavly.chirp.features.chat.data.toDomain
import com.bavly.chirp.features.chat.domain.model.Chat
import com.bavly.chirp.features.chat.domain.model.ChatParticipant
import com.bavly.chirp.features.chat.domain.repository.ChatRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.Uuid

class FirestoreChatRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun getChats(): Flow<List<Chat>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())

        return firestore.collection(CHATS)
            .where("participantIds", arrayContains = uid)
            .orderBy("lastActivityAt", Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    val chatDoc = doc.data(ChatDocument.serializer())
                    chatDoc.toDomain(
                        id = doc.id,
                        participants = resolveParticipants(chatDoc.participantIds)
                    )
                }
            }.catch { emit(emptyList()) }
    }

    override fun getChatById(chatId: String): Flow<Chat?> {
        return firestore.collection(CHATS).document(chatId).snapshots()
            .map { snapshot ->
                if (!snapshot.exists) return@map null
                val chatDoc = snapshot.data(ChatDocument.serializer())
                chatDoc.toDomain(
                    id = snapshot.id,
                    participants = resolveParticipants(chatDoc.participantIds)
                )
            }.catch { emit(null) }
    }

    override fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipant>> {
        return getChatById(chatId).map { it?.participants.orEmpty() }
    }

    override suspend fun createChat(otherUserIds: List<String>): Result<Chat, DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHENTICATED)
        val participantIds = (listOf(uid) + otherUserIds).distinct()

        return try {
            val chatRef = firestore.collection(CHATS).document(Uuid.random().toString())
            chatRef.set(
                ChatDocument.serializer(),
                ChatDocument(participantIds = participantIds, lastActivityAt = Timestamp.now())
            )
            Result.Success(
                Chat(
                    id = chatRef.id,
                    participants = resolveParticipants(participantIds),
                    lastActivityAt = Clock.System.now(),
                    lastMessage = null,
                    lastMessageSenderUsername = null
                )
            )
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toDataError())
        }
    }

    override suspend fun addParticipantsToChat(
        chatId: String,
        userIds: List<String>
    ): Result<Chat, DataError.Remote> {
        return try {
            val chatRef = firestore.collection(CHATS).document(chatId)
            chatRef.update("participantIds" to FieldValue.arrayUnion(*userIds.toTypedArray()))

            val snapshot = chatRef.get()
            if (!snapshot.exists) return Result.Failure(DataError.Remote.NOT_FOUND)
            val chatDoc = snapshot.data(ChatDocument.serializer())
            Result.Success(
                chatDoc.toDomain(
                    id = chatId,
                    participants = resolveParticipants(chatDoc.participantIds)
                )
            )
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toDataError())
        }
    }

    override suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHENTICATED)

        return try {
            val chatRef = firestore.collection(CHATS).document(chatId)
            val snapshot = chatRef.get()
            if (!snapshot.exists) return Result.Success(Unit)

            val remaining = snapshot.data(ChatDocument.serializer()).participantIds - uid
            if (remaining.isEmpty()) {
                chatRef.delete()
            } else {
                chatRef.update("participantIds" to remaining)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toDataError())
        }
    }

    private suspend fun resolveParticipants(ids: List<String>): List<ChatParticipant> =
        coroutineScope {
            ids.map { id ->
                async {
                    val doc = firestore.collection(USERS).document(id).get()
                    if (doc.exists) {
                        val data = doc.data(UserDocument.serializer())
                        ChatParticipant(
                            userId = id,
                            username = data.username,
                            profilePictureUrl = data.profilePictureUrl
                        )
                    } else {
                        ChatParticipant(userId = id, username = "Unknown", profilePictureUrl = null)
                    }
                }
            }.awaitAll()
        }

    private companion object {
        const val CHATS = "chats"
        const val USERS = "users"
    }
}