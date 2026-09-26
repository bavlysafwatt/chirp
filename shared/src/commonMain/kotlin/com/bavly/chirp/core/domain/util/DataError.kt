package com.bavly.chirp.core.domain.util

interface Error

sealed interface DataError : Error {
    enum class Remote : DataError {
        NO_INTERNET,
        UNAUTHENTICATED,
        PERMISSION_DENIED,
        NOT_FOUND,
        ALREADY_EXISTS,
        QUOTA_EXCEEDED,
        UNKNOWN
    }

    enum class Local : DataError {
        UNKNOWN
    }
}