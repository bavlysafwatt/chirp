package com.bavly.chirp.features.auth.presentation.login

sealed interface LoginAction {
    data object OnTogglePasswordVisibility : LoginAction
    data object OnForgotPasswordClick : LoginAction
    data object OnLoginClick : LoginAction
    data object OnSignUpClick : LoginAction
}