package com.bavly.chirp.features.chat.data.repository

import com.bavly.chirp.core.data.firebase.toDataError
import com.bavly.chirp.core.domain.util.DataError
import com.bavly.chirp.core.domain.util.EmptyResult
import com.bavly.chirp.core.domain.util.Result
import com.bavly.chirp.features.chat.data.dto.LastMessageDto
import com.bavly.chirp.features.chat.data.dto.MessageDocument
import com.bavly.chirp.features.chat.data.toDomain
import com.bavly.chirp.features.chat.domain.model.ChatMessage
import com.bavly.chirp.features.chat.domain.model.ConnectionState
import com.bavly.chirp.features.chat.domain.repository.MessageRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

class FirestoreMessageRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : MessageRepository {

    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>> {
        return firestore.collection(CHATS).document(chatId).collection(MESSAGES)
            .orderBy("createdAt", Direction.DESCENDING)
            .limit(PAGE_SIZE)
            .snapshots(includeMetadataChanges = true)
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data(MessageDocument.serializer())
                        .toDomain(
                            id = doc.id,
                            chatId = chatId,
                            hasPendingWrites = doc.metadata.hasPendingWrites
                        )
                }
            }.catch { emit(emptyList()) }
    }

    override suspend fun sendMessage(
        chatId: String,
        content: String
    ): EmptyResult<DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHENTICATED)
        val now = Timestamp.now()

        return try {
            val chatRef = firestore.collection(CHATS).document(chatId)
            val messageRef = chatRef.collection(MESSAGES).document(Uuid.random().toString())

            val batch = firestore.batch()
            batch.set(
                messageRef,
                MessageDocument.serializer(),
                MessageDocument(senderId = uid, content = content, createdAt = now)
            )
            batch.update(
                chatRef,
                "lastMessage" to LastMessageDto(content = content, senderId = uid, createdAt = now),
                "lastActivityAt" to now
            )
            batch.commit()
            Result.Success(Unit)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toDataError())
        }
    }

    override suspend fun retryMessage(
        chatId: String,
        messageId: String
    ): EmptyResult<DataError.Remote> {
        return try {
            val messageRef = firestore.collection(CHATS).document(chatId).collection(MESSAGES)
                .document(messageId)
            val existing = messageRef.get()
            if (!existing.exists) return Result.Failure(DataError.Remote.NOT_FOUND)
            messageRef.set(
                MessageDocument.serializer(),
                existing.data(MessageDocument.serializer())
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toDataError())
        }
    }

    override suspend fun deleteMessage(
        chatId: String,
        messageId: String
    ): EmptyResult<DataError.Remote> {
        return try {
            firestore.collection(CHATS).document(chatId).collection(MESSAGES).document(messageId)
                .delete()
            Result.Success(Unit)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toDataError())
        }
    }

    private companion object {
        const val CHATS = "chats"
        const val MESSAGES = "messages"
        const val PAGE_SIZE = 30L
    }
}