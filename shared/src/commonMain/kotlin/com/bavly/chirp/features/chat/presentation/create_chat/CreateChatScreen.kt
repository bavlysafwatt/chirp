package com.bavly.chirp.features.chat.presentation.create_chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.create_chat
import com.bavly.chirp.core.designsystem.components.dialogs.ChirpAdaptiveDialogSheetLayout
import com.bavly.chirp.core.presentation.util.ObserveAsEvents
import com.bavly.chirp.features.chat.domain.model.Chat
import com.bavly.chirp.features.chat.presentation.components.manage_chat.ManageChatAction
import com.bavly.chirp.features.chat.presentation.components.manage_chat.ManageChatScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateChatRoot(
    onDismiss: () -> Unit,
    onChatCreated: (Chat) -> Unit,
    viewModel: CreateChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CreateChatEvent.OnChatCreated -> onChatCreated(event.chat)
        }
    }

    ChirpAdaptiveDialogSheetLayout(onDismiss = onDismiss) {
        ManageChatScreen(
            headerText = stringResource(Res.string.create_chat),
            primaryButtonText = stringResource(Res.string.create_chat),
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