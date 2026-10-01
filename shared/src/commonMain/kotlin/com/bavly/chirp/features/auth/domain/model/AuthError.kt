package com.bavly.chirp.features.auth.domain.model

import com.bavly.chirp.core.domain.util.Error

enum class AuthError : Error {
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    USERNAME_TAKEN,
    EMAIL_NOT_VERIFIED,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    NOT_SIGNED_IN,
    REQUIRES_RECENT_LOGIN,
    UNKNOWN
}