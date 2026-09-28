package com.bavly.chirp.features.auth.presentation.login

sealed interface LoginEvent {
    data object Success : LoginEvent
}