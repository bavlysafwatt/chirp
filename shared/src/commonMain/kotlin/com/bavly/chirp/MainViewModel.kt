package com.bavly.chirp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class MainViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        authRepository
            .observeSession()
            .onEach { user ->
                _state.update {
                    it.copy(
                        isCheckingAuth = false,
                        isLoggedIn = user != null
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}