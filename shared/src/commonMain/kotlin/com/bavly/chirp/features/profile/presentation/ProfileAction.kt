package com.bavly.chirp.features.profile.presentation

sealed interface ProfileAction {
    data object OnDismiss : ProfileAction
    data object OnToggleCurrentPasswordVisibility : ProfileAction
    data object OnToggleNewPasswordVisibility : ProfileAction
    data object OnChangePasswordClick : ProfileAction
}