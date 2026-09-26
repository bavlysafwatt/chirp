package com.bavly.chirp.core.data.logging

import com.bavly.chirp.core.domain.util.ChirpLogger

object PrintlnChirpLogger : ChirpLogger {
    override fun debug(message: String) = println("D/Chirp: $message")
    override fun info(message: String) = println("I/Chirp: $message")
    override fun warn(message: String) = println("W/Chirp: $message")
    override fun error(message: String, throwable: Throwable?) {
        println("E/Chirp: $message${throwable?.let { " - ${it.message}" } ?: ""}")
    }
}