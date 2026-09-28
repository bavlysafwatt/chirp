package com.bavly.chirp.features.auth.presentation.util

import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.error_email_in_use
import chirp.shared.generated.resources.error_email_not_verified
import chirp.shared.generated.resources.error_invalid_credentials
import chirp.shared.generated.resources.error_no_internet
import chirp.shared.generated.resources.error_not_signed_in
import chirp.shared.generated.resources.error_too_many_requests
import chirp.shared.generated.resources.error_unknown
import chirp.shared.generated.resources.error_username_taken
import com.bavly.chirp.core.presentation.util.UiText
import com.bavly.chirp.features.auth.domain.model.AuthError


fun AuthError.toUiText(): UiText {
    val resource = when (this) {
        AuthError.INVALID_CREDENTIALS -> Res.string.error_invalid_credentials
        AuthError.EMAIL_ALREADY_IN_USE -> Res.string.error_email_in_use
        AuthError.USERNAME_TAKEN -> Res.string.error_username_taken
        AuthError.EMAIL_NOT_VERIFIED -> Res.string.error_email_not_verified
        AuthError.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests
        AuthError.NO_INTERNET -> Res.string.error_no_internet
        AuthError.NOT_SIGNED_IN -> Res.string.error_not_signed_in
        AuthError.UNKNOWN -> Res.string.error_unknown
    }
    return UiText.Resource(resource)
}