@file:OptIn(ExperimentalComposeUiApi::class)

package com.bavly.chirp.features.chat.presentation.chat_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.no_chat_selected
import chirp.shared.generated.resources.select_a_chat
import com.bavly.chirp.core.designsystem.theme.extended
import com.bavly.chirp.core.presentation.util.ObserveAsEvents
import com.bavly.chirp.core.presentation.util.clearFocusOnTap
import com.bavly.chirp.core.presentation.util.currentDeviceConfiguration
import com.bavly.chirp.features.chat.presentation.chat_detail.components.ChatDetailHeader
import com.bavly.chirp.features.chat.presentation.chat_detail.components.MessageBox
import com.bavly.chirp.features.chat.presentation.chat_detail.components.MessageList
import com.bavly.chirp.features.chat.presentation.components.ChatHeader
import com.bavly.chirp.features.chat.presentation.components.EmptySection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatDetailRoot(
    chatId: String?,
    isDetailPresent: Boolean,
    onBack: () -> Unit,
    onChatMembersClick: () -> Unit,
    viewModel: ChatDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarState = remember { SnackbarHostState() }
    val messageListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ChatDetailEvent.OnChatLeft -> onBack()
            ChatDetailEvent.OnNewMessage -> {
                scope.launch { messageListState.animateScrollToItem(0) }
            }

            is ChatDetailEvent.OnError -> snackbarState.showSnackbar(event.error.asStringAsync())
        }
    }

    LaunchedEffect(chatId) {
        viewModel.onAction(ChatDetailAction.OnSelectChat(chatId))
    }

    LaunchedEffect(chatId) {
        if (chatId != null) messageListState.scrollToItem(0)
    }

    BackHandler(enabled = !isDetailPresent) {
        scope.launch {
            delay(300)
            viewModel.onAction(ChatDetailAction.OnSelectChat(null))
        }
        onBack()
    }

    ChatDetailScreen(
        state = state,
        messageListState = messageListState,
        isDetailPresent = isDetailPresent,
        onAction = { action ->
            when (action) {
                is ChatDetailAction.OnChatMembersClick -> onChatMembersClick()
                is ChatDetailAction.OnBackClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackbarState = snackbarState
    )
}

@Composable
fun ChatDetailScreen(
    state: ChatDetailState,
    messageListState: LazyListState,
    isDetailPresent: Boolean,
    snackbarState: SnackbarHostState,
    onAction: (ChatDetailAction) -> Unit,
) {
    val configuration = currentDeviceConfiguration()

    Scaffold(
        modifier = Modifier,
        containerColor = if (!configuration.isWideScreen) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.extended.surfaceLower
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .clearFocusOnTap()
                .padding(innerPadding)
                .then(if (configuration.isWideScreen) Modifier.padding(horizontal = 8.dp) else Modifier)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DynamicRoundedCornerColumn(
                    isCornersRounded = configuration.isWideScreen,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    if (state.chatUi == null) {
                        EmptySection(
                            title = stringResource(Res.string.no_chat_selected),
                            description = stringResource(Res.string.select_a_chat),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ChatHeader {
                            ChatDetailHeader(
                                chatUi = state.chatUi,
                                isDetailPresent = isDetailPresent,
                                isChatOptionsDropDownOpen = state.isChatOptionsOpen,
                                onChatOptionsClick = { onAction(ChatDetailAction.OnChatOptionsClick) },
                                onDismissChatOptions = { onAction(ChatDetailAction.OnDismissChatOptions) },
                                onManageChatClick = { onAction(ChatDetailAction.OnChatMembersClick) },
                                onLeaveChatClick = { onAction(ChatDetailAction.OnLeaveChatClick) },
                                onBackClick = { onAction(ChatDetailAction.OnBackClick) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        MessageList(
                            messages = state.messages,
                            messageWithOpenMenu = state.messageWithOpenMenu,
                            listState = messageListState,
                            onMessageLongClick = { onAction(ChatDetailAction.OnMessageLongClick(it)) },
                            onMessageRetryClick = { onAction(ChatDetailAction.OnRetryClick(it)) },
                            onDismissMessageMenu = { onAction(ChatDetailAction.OnDismissMessageMenu) },
                            onDeleteMessageClick = {
                                onAction(
                                    ChatDetailAction.OnDeleteMessageClick(
                                        it
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                        AnimatedVisibility(visible = !configuration.isWideScreen) {
                            MessageBox(
                                messageTextFieldState = state.messageTextFieldState,
                                isSendButtonEnabled = state.canSendMessage,
                                connectionState = state.connectionState,
                                onSendClick = { onAction(ChatDetailAction.OnSendMessageClick) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .imePadding()
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = configuration.isWideScreen && state.chatUi != null) {
                    DynamicRoundedCornerColumn(isCornersRounded = configuration.isWideScreen) {
                        MessageBox(
                            messageTextFieldState = state.messageTextFieldState,
                            isSendButtonEnabled = state.canSendMessage,
                            connectionState = state.connectionState,
                            onSendClick = { onAction(ChatDetailAction.OnSendMessageClick) },
                            modifier = Modifier.fillMaxWidth().imePadding().padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicRoundedCornerColumn(
    isCornersRounded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = if (isCornersRounded) 8.dp else 0.dp,
                shape = if (isCornersRounded) RoundedCornerShape(24.dp) else RectangleShape,
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = if (isCornersRounded) RoundedCornerShape(24.dp) else RectangleShape
            )
    ) {
        content()
    }
}