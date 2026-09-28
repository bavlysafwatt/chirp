package com.bavly.chirp

sealed interface MainEvent {
    data object OnSessionEnded : MainEvent
}