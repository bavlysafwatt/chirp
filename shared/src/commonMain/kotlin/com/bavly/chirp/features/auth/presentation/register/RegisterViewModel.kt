package com.bavly.chirp.features.auth.presentation.register

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.shared.generated.resources.Res
import chirp.shared.generated.resources.error_invalid_email
import chirp.shared.generated.resources.error_invalid_password
import chirp.shared.generated.resources.error_invalid_username
import com.bavly.chirp.core.domain.util.onFailure
import com.bavly.chirp.core.domain.util.onSuccess
import com.bavly.chirp.core.domain.validation.PasswordValidator
import com.bavly.chirp.core.presentation.util.UiText
import com.bavly.chirp.features.auth.domain.model.AuthError
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.auth.domain.validation.EmailValidator
import com.bavly.chirp.features.auth.domain.validation.UsernameValidator
import com.bavly.chirp.features.auth.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeValidationStates()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value
        )

    fun onAction(action: RegisterAction) {
        when (action) {
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnInputTextFocusGain -> clearAllErrors()
            RegisterAction.OnTogglePasswordVisibilityClick -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            RegisterAction.OnLoginClick -> Unit
        }
    }

    private fun observeValidationStates() {
        val isEmailValid = snapshotFlow { _state.value.emailTextState.text.toString() }
            .map { EmailValidator.validate(it.trim()) }
            .distinctUntilChanged()
        val isUsernameValid = snapshotFlow { _state.value.usernameTextState.text.toString() }
            .map { UsernameValidator.validate(it.trim()) }
            .distinctUntilChanged()
        val isPasswordValid = snapshotFlow { _state.value.passwordTextState.text.toString() }
            .map { PasswordValidator.validate(it).isValidPassword }
            .distinctUntilChanged()
        val isRegistering = _state
            .map { it.isRegistering }
            .distinctUntilChanged()

        combine(
            isEmailValid,
            isUsernameValid,
            isPasswordValid,
            isRegistering
        ) { emailValid, usernameValid, passwordValid, registering ->
            _state.update {
                it.copy(canRegister = !registering && emailValid && usernameValid && passwordValid)
            }
        }.launchIn(viewModelScope)
    }

    private fun register() {
        if (!validateFormInputs()) return

        viewModelScope.launch {
            _state.update { it.copy(isRegistering = true) }

            val email = _state.value.emailTextState.text.toString().trim()
            val username = _state.value.usernameTextState.text.toString().trim()
            val password = _state.value.passwordTextState.text.toString()

            authRepository
                .register(email = email, username = username, password = password)
                .onSuccess {
                    _state.update { it.copy(isRegistering = false) }
                    eventChannel.send(RegisterEvent.Success(email))
                }
                .onFailure { error ->
                    val uiError = error.toUiText()
                    _state.update {
                        when (error) {
                            AuthError.USERNAME_TAKEN ->
                                it.copy(isRegistering = false, usernameError = uiError)

                            AuthError.EMAIL_ALREADY_IN_USE ->
                                it.copy(isRegistering = false, emailError = uiError)

                            else ->
                                it.copy(isRegistering = false, registrationError = uiError)
                        }
                    }
                }
        }
    }

    private fun clearAllErrors() {
        _state.update {
            it.copy(
                emailError = null,
                usernameError = null,
                passwordError = null,
                registrationError = null
            )
        }
    }

    private fun validateFormInputs(): Boolean {
        clearAllErrors()

        val email = _state.value.emailTextState.text.toString().trim()
        val username = _state.value.usernameTextState.text.toString().trim()
        val password = _state.value.passwordTextState.text.toString()

        val isEmailValid = EmailValidator.validate(email)
        val isUsernameValid = UsernameValidator.validate(username)
        val isPasswordValid = PasswordValidator.validate(password).isValidPassword

        _state.update {
            it.copy(
                emailError = if (!isEmailValid) UiText.Resource(Res.string.error_invalid_email) else null,
                usernameError = if (!isUsernameValid) UiText.Resource(Res.string.error_invalid_username) else null,
                passwordError = if (!isPasswordValid) UiText.Resource(Res.string.error_invalid_password) else null
            )
        }

        return isEmailValid && isUsernameValid && isPasswordValid
    }
}