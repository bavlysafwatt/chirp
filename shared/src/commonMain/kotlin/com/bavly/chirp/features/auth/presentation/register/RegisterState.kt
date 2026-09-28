package com.bavly.chirp.features.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import com.bavly.chirp.core.presentation.util.UiText

data class RegisterState(
    val emailTextState: TextFieldState = TextFieldState(),
    val emailError: UiText? = null,
    val passwordTextState: TextFieldState = TextFieldState(),
    val passwordError: UiText? = null,
    val usernameTextState: TextFieldState = TextFieldState(),
    val usernameError: UiText? = null,
    val registrationError: UiText? = null,
    val isRegistering: Boolean = false,
    val canRegister: Boolean = false,
    val isPasswordVisible: Boolean = false
)