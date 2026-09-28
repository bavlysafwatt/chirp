package com.bavly.chirp.features.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDocument(
    val username: String,
    val email: String,
    val profilePictureUrl: String? = null,
    val fcmTokens: List<String> = emptyList()
)

@Serializable
data class UsernameClaimDocument(
    val uid: String
)