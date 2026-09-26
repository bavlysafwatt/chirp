package com.bavly.chirp

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.bavly.chirp.di.initKoin

fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Chirp",
            icon = painterResource("logo.png")
        ) {
            App()
        }
    }
}