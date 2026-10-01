package com.bavly.chirp.features.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.arrow_left_icon
import chirp.shared.generated.resources.chat_members
import chirp.shared.generated.resources.dots_icon
import chirp.shared.generated.resources.go_back
import chirp.shared.generated.resources.leave_chat
import chirp.shared.generated.resources.log_out_icon
import chirp.shared.generated.resources.open_chat_options_menu
import chirp.shared.generated.resources.users_icon
import com.bavly.chirp.core.designsystem.components.buttons.ChirpIconButton
import com.bavly.chirp.core.designsystem.components.dropdown.ChirpDropDownMenu
import com.bavly.chirp.core.designsystem.components.dropdown.DropDownItem
import com.bavly.chirp.core.designsystem.theme.extended
import com.bavly.chirp.features.chat.presentation.components.ChatItemHeaderRow
import com.bavly.chirp.features.chat.presentation.model.ChatUi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChatDetailHeader(
    chatUi: ChatUi?,
    isDetailPresent: Boolean,
    isChatOptionsDropDownOpen: Boolean,
    onChatOptionsClick: () -> Unit,
    onDismissChatOptions: () -> Unit,
    onManageChatClick: () -> Unit,
    onLeaveChatClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isDetailPresent) {
            ChirpIconButton(onClick = onBackClick) {
                Icon(
                    imageVector = vectorResource(Res.drawable.arrow_left_icon),
                    contentDescription = stringResource(Res.string.go_back),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.extended.textSecondary
                )
            }
        }

        if (chatUi != null) {
            val isGroupChat = chatUi.otherParticipants.size > 1
            ChatItemHeaderRow(
                chat = chatUi,
                isGroupChat = isGroupChat,
                modifier = Modifier.weight(1f).clickable { onManageChatClick() }
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Box {
            ChirpIconButton(onClick = onChatOptionsClick) {
                Icon(
                    imageVector = vectorResource(Res.drawable.dots_icon),
                    contentDescription = stringResource(Res.string.open_chat_options_menu),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.extended.textSecondary
                )
            }

            ChirpDropDownMenu(
                isOpen = isChatOptionsDropDownOpen,
                onDismiss = onDismissChatOptions,
                items = listOf(
                    DropDownItem(
                        title = stringResource(Res.string.chat_members),
                        icon = vectorResource(Res.drawable.users_icon),
                        contentColor = MaterialTheme.colorScheme.extended.textSecondary,
                        onClick = onManageChatClick
                    ),
                    DropDownItem(
                        title = stringResource(Res.string.leave_chat),
                        icon = vectorResource(Res.drawable.log_out_icon),
                        contentColor = MaterialTheme.colorScheme.extended.destructiveHover,
                        onClick = onLeaveChatClick
                    ),
                )
            )
        }
    }
}