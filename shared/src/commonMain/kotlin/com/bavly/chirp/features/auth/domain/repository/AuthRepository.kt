package com.bavly.chirp.features.auth.domain.repository

import com.bavly.chirp.core.domain.util.EmptyResult
import com.bavly.chirp.features.auth.domain.model.AuthError
import com.bavly.chirp.features.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<AuthUser?>

    suspend fun register(email: String, username: String, password: String): EmptyResult<AuthError>
    suspend fun login(email: String, password: String): EmptyResult<AuthError>
    suspend fun resendVerificationEmail(): EmptyResult<AuthError>
    suspend fun sendPasswordResetEmail(email: String): EmptyResult<AuthError>
    suspend fun logout()
}