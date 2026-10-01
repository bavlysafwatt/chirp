package com.bavly.chirp.features.chat.presentation.util

import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.offline
import chirp.shared.generated.resources.online
import com.bavly.chirp.core.presentation.util.UiText
import com.bavly.chirp.features.chat.domain.model.ConnectionState

fun ConnectionState.toUiText(): UiText {
    val resource = when (this) {
        ConnectionState.CONNECTED -> Res.string.online
        ConnectionState.DISCONNECTED -> Res.string.offline
    }
    return UiText.Resource(resource)
}