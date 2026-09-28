package com.bavly.chirp.features.auth.presentation.forgot_password

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bavly.chirp.core.domain.util.onFailure
import com.bavly.chirp.core.domain.util.onSuccess
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.auth.domain.validation.EmailValidator
import com.bavly.chirp.features.auth.presentation.util.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeValidationState()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value
        )

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            ForgotPasswordAction.OnSubmitClick -> submit()
        }
    }

    private fun observeValidationState() {
        snapshotFlow { _state.value.emailTextFieldState.text.toString() }
            .map { EmailValidator.validate(it.trim()) }
            .distinctUntilChanged()
            .onEach { isValid -> _state.update { it.copy(canSubmit = isValid) } }
            .launchIn(viewModelScope)
    }

    private fun submit() {
        if (_state.value.isLoading || !_state.value.canSubmit) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, isEmailSentSuccessfully = false, errorText = null)
            }

            val email = _state.value.emailTextFieldState.text.toString().trim()
            authRepository
                .sendPasswordResetEmail(email)
                .onSuccess {
                    _state.update { it.copy(isEmailSentSuccessfully = true, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorText = error.toUiText(), isLoading = false) }
                }
        }
    }
}