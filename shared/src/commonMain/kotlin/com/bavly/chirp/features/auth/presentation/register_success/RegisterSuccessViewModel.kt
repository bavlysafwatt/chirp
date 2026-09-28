package com.bavly.chirp.features.auth.presentation.register_success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bavly.chirp.core.domain.util.onFailure
import com.bavly.chirp.core.domain.util.onSuccess
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.auth.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterSuccessViewModel(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<RegisterSuccessEvent>()
    val events = eventChannel.receiveAsFlow()

    private val email = savedStateHandle.get<String>("email")
        ?: throw IllegalStateException("No email passed to register success screen")

    private val _state = MutableStateFlow(RegisterSuccessState(registeredEmail = email))
    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value
        )

    fun onAction(action: RegisterSuccessAction) {
        when (action) {
            RegisterSuccessAction.OnResendVerificationEmailClick -> resendVerification()
            RegisterSuccessAction.OnLoginClick -> Unit
        }
    }

    private fun resendVerification() {
        if (_state.value.isResendingVerificationEmail) return

        viewModelScope.launch {
            _state.update {
                it.copy(isResendingVerificationEmail = true, resendVerificationError = null)
            }

            authRepository
                .resendVerificationEmail()
                .onSuccess {
                    _state.update { it.copy(isResendingVerificationEmail = false) }
                    eventChannel.send(RegisterSuccessEvent.ResendVerificationEmailSuccess)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isResendingVerificationEmail = false,
                            resendVerificationError = error.toUiText()
                        )
                    }
                }
        }
    }
}