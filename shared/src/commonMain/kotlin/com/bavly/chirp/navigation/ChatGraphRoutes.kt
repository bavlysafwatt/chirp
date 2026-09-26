package com.bavly.chirp.navigation

import kotlinx.serialization.Serializable

sealed interface ChatGraphRoutes {
    @Serializable
    data object Graph : ChatGraphRoutes
}