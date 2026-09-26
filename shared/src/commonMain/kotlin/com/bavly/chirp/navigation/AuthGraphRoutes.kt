package com.bavly.chirp.navigation

import kotlinx.serialization.Serializable

sealed interface AuthGraphRoutes {
    @Serializable
    data object Graph : AuthGraphRoutes
}