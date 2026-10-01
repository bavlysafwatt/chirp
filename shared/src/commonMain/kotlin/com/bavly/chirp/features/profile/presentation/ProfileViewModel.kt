package com.bavly.chirp.features.profile.presentation

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bavly.chirp.core.domain.util.Result
import com.bavly.chirp.core.domain.util.onFailure
import com.bavly.chirp.core.domain.util.onSuccess
import com.bavly.chirp.core.domain.validation.PasswordValidator
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.auth.presentation.util.toUiText
import com.bavly.chirp.features.chat.domain.repository.ParticipantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val participantRepository: ParticipantRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadProfile()
                observeCanChangePassword()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value
        )

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnChangePasswordClick -> changePassword()
            ProfileAction.OnToggleCurrentPasswordVisibility -> {
                _state.update { it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible) }
            }

            ProfileAction.OnToggleNewPasswordVisibility -> {
                _state.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
            }

            ProfileAction.OnDismiss -> Unit
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val authUser = authRepository.observeSession().firstOrNull()
            val participant = (participantRepository.getLocalParticipant() as? Result.Success)?.data

            authUser?.email?.let { email ->
                _state.value.emailTextState.setTextAndPlaceCursorAtEnd(email)
            }
            _state.update { it.copy(username = participant?.username.orEmpty()) }
        }
    }

    private fun observeCanChangePassword() {
        val isCurrentPasswordValid = snapshotFlow {
            _state.value.currentPasswordTextState.text.toString()
        }.map { it.isNotBlank() }.distinctUntilChanged()

        val isNewPasswordValid = snapshotFlow {
            _state.value.newPasswordTextState.text.toString()
        }.map { PasswordValidator.validate(it).isValidPassword }.distinctUntilChanged()

        combine(isCurrentPasswordValid, isNewPasswordValid) { currentValid, newValid ->
            _state.update { it.copy(canChangePassword = currentValid && newValid) }
        }.launchIn(viewModelScope)
    }

    private fun changePassword() {
        if (!_state.value.canChangePassword || _state.value.isChangingPassword) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isChangingPassword = true,
                    isPasswordChangeSuccessful = false,
                    newPasswordError = null
                )
            }

            val current = _state.value.currentPasswordTextState.text.toString()
            val newPassword = _state.value.newPasswordTextState.text.toString()

            authRepository
                .changePassword(current, newPassword)
                .onSuccess {
                    _state.value.currentPasswordTextState.clearText()
                    _state.value.newPasswordTextState.clearText()
                    _state.update {
                        it.copy(
                            isChangingPassword = false,
                            isPasswordChangeSuccessful = true,
                            isCurrentPasswordVisible = false,
                            isNewPasswordVisible = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isChangingPassword = false,
                            newPasswordError = error.toUiText()
                        )
                    }
                }
        }
    }
}