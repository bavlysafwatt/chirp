package com.bavly.chirp.features.auth.domain.validation

object UsernameValidator {
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]{3,20}$")

    fun validate(username: String): Boolean = USERNAME_REGEX.matches(username)

    fun toKey(username: String): String = username.lowercase()
}