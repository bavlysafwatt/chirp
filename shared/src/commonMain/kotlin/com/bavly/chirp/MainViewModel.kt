package com.bavly.chirp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class MainViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val eventChannel = Channel<MainEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        authRepository
            .observeSession()
            .onEach { user ->
                val isLoggedIn = user != null
                val previous = _state.value

                when {
                    previous.isCheckingAuth -> {
                        _state.update { it.copy(isCheckingAuth = false, isLoggedIn = isLoggedIn) }
                    }

                    previous.isLoggedIn && !isLoggedIn -> {
                        _state.update { it.copy(isLoggedIn = false) }
                        eventChannel.send(MainEvent.OnSessionEnded)
                    }

                    else -> _state.update { it.copy(isLoggedIn = isLoggedIn) }
                }
            }
            .launchIn(viewModelScope)
    }
}