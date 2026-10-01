package com.bavly.chirp.features.chat.presentation.manage_chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.chat_members
import chirp.shared.generated.resources.save
import com.bavly.chirp.core.designsystem.components.dialogs.ChirpAdaptiveDialogSheetLayout
import com.bavly.chirp.core.presentation.util.ObserveAsEvents
import com.bavly.chirp.features.chat.presentation.components.manage_chat.ManageChatAction
import com.bavly.chirp.features.chat.presentation.components.manage_chat.ManageChatScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ManageChatRoot(
    chatId: String?,
    onDismiss: () -> Unit,
    onMembersAdded: () -> Unit,
    viewModel: ManageChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(chatId) {
        viewModel.onAction(ManageChatAction.ChatParticipants.OnSelectChat(chatId))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ManageChatEvent.OnMembersAdded -> onMembersAdded()
        }
    }

    ChirpAdaptiveDialogSheetLayout(onDismiss = onDismiss) {
        ManageChatScreen(
            headerText = stringResource(Res.string.chat_members),
            primaryButtonText = stringResource(Res.string.save),
            state = state,
            onAction = { action ->
                when (action) {
                    ManageChatAction.OnDismissDialog -> onDismiss()
                    else -> Unit
                }
                viewModel.onAction(action)
            }
        )
    }
}