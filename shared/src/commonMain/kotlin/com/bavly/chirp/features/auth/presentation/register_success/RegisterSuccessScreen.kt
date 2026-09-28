package com.bavly.chirp.features.auth.presentation.register_success

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.account_successfully_created
import chirp.shared.generated.resources.login
import chirp.shared.generated.resources.resend_verification_email
import chirp.shared.generated.resources.resent_verification_email
import chirp.shared.generated.resources.verification_email_sent_to_x
import com.bavly.chirp.core.designsystem.components.brand.ChirpSuccessIcon
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButton
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButtonStyle
import com.bavly.chirp.core.designsystem.components.layouts.ChirpAdaptiveResultLayout
import com.bavly.chirp.core.designsystem.components.layouts.ChirpSimpleResultLayout
import com.bavly.chirp.core.designsystem.components.layouts.ChirpSnackbarScaffold
import com.bavly.chirp.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterSuccessRoot(
    viewModel: RegisterSuccessViewModel = koinViewModel(),
    onLoginClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            RegisterSuccessEvent.ResendVerificationEmailSuccess -> {
                snackbarHostState.showSnackbar(
                    message = getString(Res.string.resent_verification_email)
                )
            }
        }
    }

    RegisterSuccessScreen(
        state = state,
        onAction = { action ->
            when (action) {
                RegisterSuccessAction.OnLoginClick -> onLoginClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RegisterSuccessScreen(
    state: RegisterSuccessState,
    onAction: (RegisterSuccessAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    ChirpSnackbarScaffold(snackbarHostState = snackbarHostState) {
        ChirpAdaptiveResultLayout {
            ChirpSimpleResultLayout(
                title = stringResource(Res.string.account_successfully_created),
                description = stringResource(
                    Res.string.verification_email_sent_to_x,
                    state.registeredEmail
                ),
                icon = { ChirpSuccessIcon() },
                primaryButton = {
                    ChirpButton(
                        text = stringResource(Res.string.login),
                        onClick = { onAction(RegisterSuccessAction.OnLoginClick) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                secondaryButton = {
                    ChirpButton(
                        text = stringResource(Res.string.resend_verification_email),
                        onClick = { onAction(RegisterSuccessAction.OnResendVerificationEmailClick) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isResendingVerificationEmail,
                        isLoading = state.isResendingVerificationEmail,
                        style = ChirpButtonStyle.SECONDARY
                    )
                },
                secondaryError = state.resendVerificationError?.asString()
            )
        }
    }
}
