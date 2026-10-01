package com.bavly.chirp.core.presentation.util

import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.error_already_exists
import chirp.shared.generated.resources.error_no_internet
import chirp.shared.generated.resources.error_not_found
import chirp.shared.generated.resources.error_permission_denied
import chirp.shared.generated.resources.error_quota_exceeded
import chirp.shared.generated.resources.error_unauthenticated
import chirp.shared.generated.resources.error_unknown
import com.bavly.chirp.core.domain.util.DataError

fun DataError.toUiText(): UiText {
    val resource = when (this) {
        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
        DataError.Remote.UNAUTHENTICATED -> Res.string.error_unauthenticated
        DataError.Remote.PERMISSION_DENIED -> Res.string.error_permission_denied
        DataError.Remote.NOT_FOUND -> Res.string.error_not_found
        DataError.Remote.ALREADY_EXISTS -> Res.string.error_already_exists
        DataError.Remote.QUOTA_EXCEEDED -> Res.string.error_quota_exceeded
        DataError.Remote.UNKNOWN, DataError.Local.UNKNOWN -> Res.string.error_unknown
    }
    return UiText.Resource(resource)
}