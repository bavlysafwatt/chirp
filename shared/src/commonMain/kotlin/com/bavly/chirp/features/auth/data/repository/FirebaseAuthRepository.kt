package com.bavly.chirp.features.auth.data.repository

import com.bavly.chirp.core.domain.util.ChirpLogger
import com.bavly.chirp.core.domain.util.EmptyResult
import com.bavly.chirp.core.domain.util.Result
import com.bavly.chirp.core.platform.requiresEmailVerification
import com.bavly.chirp.features.auth.data.dto.UserDocument
import com.bavly.chirp.features.auth.data.dto.UsernameClaimDocument
import com.bavly.chirp.features.auth.domain.model.AuthError
import com.bavly.chirp.features.auth.domain.model.AuthUser
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.auth.domain.validation.UsernameValidator
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val logger: ChirpLogger
) : AuthRepository {

    override fun observeSession(): Flow<AuthUser?> {
        return auth.authStateChanged
            .map { user ->
                user?.takeIf {
                    !requiresEmailVerification || it.isEmailVerified
                }?.toDomain()
            }
            .distinctUntilChanged()
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): EmptyResult<AuthError> {
        val user: FirebaseUser = try {
            auth.createUserWithEmailAndPassword(email, password).user
                ?: return Result.Failure(AuthError.UNKNOWN)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            return Result.Failure(e.toAuthError())
        }

        val usernameRef = firestore
            .collection(USERNAMES)
            .document(UsernameValidator.toKey(username))

        val userRef = firestore
            .collection(USERS)
            .document(user.uid)

        try {
            val batch = firestore.batch()

            batch.set(
                usernameRef,
                UsernameClaimDocument(uid = user.uid)
            )

            batch.set(
                userRef,
                UserDocument(
                    username = username,
                    email = email
                )
            )

            batch.commit()
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()

            // Rules only allow creating a username claim,
            // so a failed batch usually means the username exists.
            val error = try {
                if (usernameRef.get().exists) {
                    AuthError.USERNAME_TAKEN
                } else {
                    e.toAuthError()
                }
            } catch (_: Exception) {
                e.toAuthError()
            }

            runCatching { user.delete() }
            runCatching { auth.signOut() }

            return Result.Failure(error)
        }

        if (requiresEmailVerification) {
            runCatching {
                user.sendEmailVerification()
            }.onFailure {
                logger.warn(
                    "Failed to send verification email: ${it.message}"
                )
            }
        }

        return Result.Success(Unit)
    }

    override suspend fun login(
        email: String,
        password: String
    ): EmptyResult<AuthError> {
        return try {
            val user = auth
                .signInWithEmailAndPassword(email, password)
                .user
                ?: return Result.Failure(AuthError.UNKNOWN)

            if (requiresEmailVerification && !user.isEmailVerified) {
                auth.signOut()
                Result.Failure(AuthError.EMAIL_NOT_VERIFIED)
            } else {
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toAuthError())
        }
    }

    override suspend fun resendVerificationEmail(): EmptyResult<AuthError> {
        if (!requiresEmailVerification) {
            return Result.Success(Unit)
        }

        val user = auth.currentUser
            ?: return Result.Failure(AuthError.NOT_SIGNED_IN)

        return try {
            user.sendEmailVerification()
            Result.Success(Unit)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toAuthError())
        }
    }

    override suspend fun sendPasswordResetEmail(
        email: String
    ): EmptyResult<AuthError> {
        return try {
            auth.sendPasswordResetEmail(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Result.Failure(e.toAuthError())
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    private fun FirebaseUser.toDomain(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email.orEmpty()
        )
    }

    private fun Throwable.toAuthError(): AuthError {
        return when (this) {
            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException ->
                AuthError.INVALID_CREDENTIALS

            is FirebaseAuthUserCollisionException ->
                AuthError.EMAIL_ALREADY_IN_USE

            is FirebaseNetworkException ->
                AuthError.NO_INTERNET

            is FirebaseTooManyRequestsException ->
                AuthError.TOO_MANY_REQUESTS

            else ->
                AuthError.UNKNOWN
        }
    }

    private companion object {
        const val USERS = "users"
        const val USERNAMES = "usernames"
    }
}