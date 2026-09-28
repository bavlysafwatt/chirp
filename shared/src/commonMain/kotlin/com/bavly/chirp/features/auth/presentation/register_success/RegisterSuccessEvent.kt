package com.bavly.chirp.features.auth.presentation.register_success

sealed interface RegisterSuccessEvent {
    data object ResendVerificationEmailSuccess : RegisterSuccessEvent
}