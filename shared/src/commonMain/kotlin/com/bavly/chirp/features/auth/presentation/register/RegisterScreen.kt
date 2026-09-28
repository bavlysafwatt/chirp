package com.bavly.chirp.features.auth.presentation.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.email
import chirp.shared.generated.resources.email_placeholder
import chirp.shared.generated.resources.login
import chirp.shared.generated.resources.password
import chirp.shared.generated.resources.password_hint
import chirp.shared.generated.resources.register
import chirp.shared.generated.resources.username
import chirp.shared.generated.resources.username_hint
import chirp.shared.generated.resources.username_placeholder
import chirp.shared.generated.resources.welcome_to_chirp

import com.bavly.chirp.core.designsystem.components.brand.ChirpBrandLogo
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButton
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButtonStyle
import com.bavly.chirp.core.designsystem.components.layouts.ChirpAdaptiveFormLayout
import com.bavly.chirp.core.designsystem.components.layouts.ChirpSnackbarScaffold
import com.bavly.chirp.core.designsystem.components.textfields.ChirpPasswordTextField
import com.bavly.chirp.core.designsystem.components.textfields.ChirpTextField
import com.bavly.chirp.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterRoot(
    viewModel: RegisterViewModel = koinViewModel(),
    onRegisterSuccess: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RegisterEvent.Success -> onRegisterSuccess(event.email)
        }
    }

    RegisterScreen(
        state = state,
        onAction = { action ->
            when (action) {
                RegisterAction.OnLoginClick -> onLoginClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
) {
    ChirpSnackbarScaffold {
        ChirpAdaptiveFormLayout(
            headerText = stringResource(Res.string.welcome_to_chirp),
            errorText = state.registrationError?.asString(),
            logo = { ChirpBrandLogo() }
        ) {
            ChirpTextField(
                state = state.usernameTextState,
                placeholder = stringResource(Res.string.username_placeholder),
                title = stringResource(Res.string.username),
                supportingText = state.usernameError?.asString()
                    ?: stringResource(Res.string.username_hint),
                isError = state.usernameError != null,
                singleLine = true,
                onFocusChanged = { isFocused ->
                    if (isFocused) onAction(RegisterAction.OnInputTextFocusGain)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ChirpTextField(
                state = state.emailTextState,
                placeholder = stringResource(Res.string.email_placeholder),
                title = stringResource(Res.string.email),
                supportingText = state.emailError?.asString(),
                isError = state.emailError != null,
                singleLine = true,
                onFocusChanged = { isFocused ->
                    if (isFocused) onAction(RegisterAction.OnInputTextFocusGain)
                },
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(16.dp))
            ChirpPasswordTextField(
                state = state.passwordTextState,
                placeholder = stringResource(Res.string.password),
                title = stringResource(Res.string.password),
                supportingText = state.passwordError?.asString()
                    ?: stringResource(Res.string.password_hint),
                isError = state.passwordError != null,
                onFocusChanged = { isFocused ->
                    if (isFocused) onAction(RegisterAction.OnInputTextFocusGain)
                },
                onToggleVisibilityClick = {
                    onAction(RegisterAction.OnTogglePasswordVisibilityClick)
                },
                isPasswordVisible = state.isPasswordVisible
            )
            Spacer(modifier = Modifier.height(16.dp))

            ChirpButton(
                text = stringResource(Res.string.register),
                onClick = { onAction(RegisterAction.OnRegisterClick) },
                enabled = state.canRegister,
                isLoading = state.isRegistering,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChirpButton(
                text = stringResource(Res.string.login),
                onClick = { onAction(RegisterAction.OnLoginClick) },
                style = ChirpButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
