package com.bavly.chirp.core.data.firebase

import com.bavly.chirp.core.domain.util.DataError
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.FirebaseTooManyRequestsException

fun Throwable.toDataError(): DataError.Remote {
    if (this is FirebaseNetworkException) return DataError.Remote.NO_INTERNET
    if (this is FirebaseTooManyRequestsException) return DataError.Remote.QUOTA_EXCEEDED

    val description = message.orEmpty().lowercase()
    return when {
        description.contains("permission") -> DataError.Remote.PERMISSION_DENIED
        description.contains("not-found") || description.contains("not found") -> DataError.Remote.NOT_FOUND
        description.contains("already-exists") || description.contains("already exists") -> DataError.Remote.ALREADY_EXISTS
        description.contains("unavailable") || description.contains("network") -> DataError.Remote.NO_INTERNET
        description.contains("resource-exhausted") || description.contains("quota") -> DataError.Remote.QUOTA_EXCEEDED
        description.contains("unauthenticated") -> DataError.Remote.UNAUTHENTICATED
        else -> DataError.Remote.UNKNOWN
    }
}