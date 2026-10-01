package com.bavly.chirp.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.cancel
import chirp.shared.generated.resources.contact_chirp_support_change_email
import chirp.shared.generated.resources.current_password
import chirp.shared.generated.resources.email
import chirp.shared.generated.resources.new_password
import chirp.shared.generated.resources.password
import chirp.shared.generated.resources.password_change_successful
import chirp.shared.generated.resources.password_hint
import chirp.shared.generated.resources.save
import com.bavly.chirp.core.designsystem.components.brand.ChirpHorizontalDivider
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButton
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButtonStyle
import com.bavly.chirp.core.designsystem.components.dialogs.ChirpAdaptiveDialogSheetLayout
import com.bavly.chirp.core.designsystem.components.textfields.ChirpPasswordTextField
import com.bavly.chirp.core.designsystem.components.textfields.ChirpTextField
import com.bavly.chirp.core.designsystem.theme.extended
import com.bavly.chirp.core.presentation.util.DeviceConfiguration
import com.bavly.chirp.core.presentation.util.clearFocusOnTap
import com.bavly.chirp.core.presentation.util.currentDeviceConfiguration
import com.bavly.chirp.features.profile.presentation.components.ProfileHeaderSection
import com.bavly.chirp.features.profile.presentation.components.ProfileSectionLayout
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoot(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChirpAdaptiveDialogSheetLayout(onDismiss = onDismiss) {
        ProfileScreen(
            state = state,
            onAction = { action ->
                when (action) {
                    is ProfileAction.OnDismiss -> onDismiss()
                    else -> Unit
                }
                viewModel.onAction(action)
            }
        )
    }
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeaderSection(
            username = state.username,
            onCloseClick = { onAction(ProfileAction.OnDismiss) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 20.dp)
        )
        ChirpHorizontalDivider()
        ProfileSectionLayout(headerText = stringResource(Res.string.email)) {
            ChirpTextField(
                state = state.emailTextState,
                enabled = false,
                supportingText = stringResource(Res.string.contact_chirp_support_change_email)
            )
        }
        ChirpHorizontalDivider()
        ProfileSectionLayout(headerText = stringResource(Res.string.password)) {
            ChirpPasswordTextField(
                state = state.currentPasswordTextState,
                isPasswordVisible = state.isCurrentPasswordVisible,
                onToggleVisibilityClick = { onAction(ProfileAction.OnToggleCurrentPasswordVisibility) },
                placeholder = stringResource(Res.string.current_password),
                isError = state.newPasswordError != null,
            )
            ChirpPasswordTextField(
                state = state.newPasswordTextState,
                isPasswordVisible = state.isNewPasswordVisible,
                onToggleVisibilityClick = { onAction(ProfileAction.OnToggleNewPasswordVisibility) },
                placeholder = stringResource(Res.string.new_password),
                isError = state.newPasswordError != null,
                supportingText = state.newPasswordError?.asString()
                    ?: stringResource(Res.string.password_hint)
            )
            if (state.isPasswordChangeSuccessful) {
                Text(
                    text = stringResource(Res.string.password_change_successful),
                    color = MaterialTheme.colorScheme.extended.success,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                ChirpButton(
                    text = stringResource(Res.string.cancel),
                    style = ChirpButtonStyle.SECONDARY,
                    onClick = { onAction(ProfileAction.OnDismiss) }
                )
                ChirpButton(
                    text = stringResource(Res.string.save),
                    onClick = { onAction(ProfileAction.OnChangePasswordClick) },
                    enabled = state.canChangePassword,
                    isLoading = state.isChangingPassword
                )
            }
        }
        val deviceConfiguration = currentDeviceConfiguration()
        if (deviceConfiguration in listOf(
                DeviceConfiguration.MOBILE_PORTRAIT,
                DeviceConfiguration.MOBILE_LANDSCAPE
            )
        ) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}