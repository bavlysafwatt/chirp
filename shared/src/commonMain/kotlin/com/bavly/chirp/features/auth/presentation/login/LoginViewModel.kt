package com.bavly.chirp.features.auth.presentation.login

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bavly.chirp.core.domain.util.onFailure
import com.bavly.chirp.core.domain.util.onSuccess
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.auth.domain.validation.EmailValidator
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

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeTextStates()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value
        )

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnLoginClick -> login()
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            else -> Unit
        }
    }

    private fun observeTextStates() {
        val isEmailValid = snapshotFlow { _state.value.emailTextFieldState.text.toString() }
            .map { EmailValidator.validate(it.trim()) }
            .distinctUntilChanged()
        val isPasswordNotBlank =
            snapshotFlow { _state.value.passwordTextFieldState.text.toString() }
                .map { it.isNotBlank() }
                .distinctUntilChanged()
        val isLoggingIn = _state
            .map { it.isLoggingIn }
            .distinctUntilChanged()

        combine(
            isEmailValid,
            isPasswordNotBlank,
            isLoggingIn
        ) { emailValid, passwordNotBlank, loggingIn ->
            _state.update { it.copy(canLogin = !loggingIn && emailValid && passwordNotBlank) }
        }.launchIn(viewModelScope)
    }

    private fun login() {
        if (!_state.value.canLogin) return

        viewModelScope.launch {
            _state.update { it.copy(isLoggingIn = true, error = null) }

            val email = _state.value.emailTextFieldState.text.toString().trim()
            val password = _state.value.passwordTextFieldState.text.toString()

            authRepository
                .login(email = email, password = password)
                .onSuccess {
                    _state.update { it.copy(isLoggingIn = false) }
                    eventChannel.send(LoginEvent.Success)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoggingIn = false, error = error.toUiText()) }
                }
        }
    }
}