package com.bavly.chirp.features.chat.presentation.components.manage_chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.cancel
import com.bavly.chirp.core.designsystem.components.brand.ChirpHorizontalDivider
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButton
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButtonStyle
import com.bavly.chirp.core.presentation.util.DeviceConfiguration
import com.bavly.chirp.core.presentation.util.clearFocusOnTap
import com.bavly.chirp.core.presentation.util.currentDeviceConfiguration
import com.bavly.chirp.features.chat.presentation.components.ChatParticipantSearchTextSection
import com.bavly.chirp.features.chat.presentation.components.ChatParticipantsSelectionSection
import com.bavly.chirp.features.chat.presentation.components.ManageChatButtonSection
import com.bavly.chirp.features.chat.presentation.components.ManageChatHeaderRow
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManageChatScreen(
    headerText: String,
    primaryButtonText: String,
    state: ManageChatState,
    onAction: (ManageChatAction) -> Unit,
) {
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val imeHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val isKeyboardVisible = imeHeight > 0
    val configuration = currentDeviceConfiguration()

    val shouldHideHeader = configuration == DeviceConfiguration.MOBILE_LANDSCAPE &&
            (isKeyboardVisible || isTextFieldFocused)

    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(visible = !shouldHideHeader) {
            Column {
                ManageChatHeaderRow(
                    title = headerText,
                    onCloseClick = { onAction(ManageChatAction.OnDismissDialog) },
                    modifier = Modifier.fillMaxWidth()
                )
                ChirpHorizontalDivider()
            }
        }
        ChatParticipantSearchTextSection(
            queryState = state.queryTextState,
            onAddClick = { onAction(ManageChatAction.OnAddClick) },
            isSearchEnabled = state.canAddParticipant,
            isLoading = state.isSearching,
            modifier = Modifier.fillMaxWidth(),
            error = state.searchError,
            onFocusChanged = { isTextFieldFocused = it }
        )
        ChirpHorizontalDivider()
        ChatParticipantsSelectionSection(
            existingParticipants = state.existingChatParticipants,
            selectedParticipants = state.selectedChatParticipants,
            modifier = Modifier.fillMaxWidth(),
            searchResult = state.currentSearchResult
        )
        ChirpHorizontalDivider()
        ManageChatButtonSection(
            primaryButton = {
                ChirpButton(
                    text = primaryButtonText,
                    onClick = { onAction(ManageChatAction.OnPrimaryActionClick) },
                    enabled = state.selectedChatParticipants.isNotEmpty(),
                    isLoading = state.isSubmitting
                )
            },
            secondaryButton = {
                ChirpButton(
                    text = stringResource(Res.string.cancel),
                    onClick = { onAction(ManageChatAction.OnDismissDialog) },
                    style = ChirpButtonStyle.SECONDARY
                )
            },
            error = state.submitError?.asString(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}