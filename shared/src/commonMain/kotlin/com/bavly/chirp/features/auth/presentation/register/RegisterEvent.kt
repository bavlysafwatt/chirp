package com.bavly.chirp.features.auth.presentation.register

sealed interface RegisterEvent {
    data class Success(val email: String) : RegisterEvent
}