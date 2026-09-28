package com.bavly.chirp.features.auth.domain.validation

object EmailValidator {
    private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

    fun validate(email: String): Boolean = EMAIL_REGEX.matches(email)
}