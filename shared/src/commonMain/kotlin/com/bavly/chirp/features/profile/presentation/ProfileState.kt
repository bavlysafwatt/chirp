package com.bavly.chirp.features.profile.presentation

import androidx.compose.foundation.text.input.TextFieldState
import com.bavly.chirp.core.presentation.util.UiText

data class ProfileState(
    val username: String = "",
    val emailTextState: TextFieldState = TextFieldState(),
    val currentPasswordTextState: TextFieldState = TextFieldState(),
    val newPasswordTextState: TextFieldState = TextFieldState(),
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isChangingPassword: Boolean = false,
    val newPasswordError: UiText? = null,
    val canChangePassword: Boolean = false,
    val isPasswordChangeSuccessful: Boolean = false
)