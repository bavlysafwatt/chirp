package com.bavly.chirp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.bavly.chirp.core.designsystem.theme.ChirpTheme
import com.bavly.chirp.core.presentation.util.ObserveAsEvents
import com.bavly.chirp.navigation.AuthGraphRoutes
import com.bavly.chirp.navigation.ChatGraphRoutes
import com.bavly.chirp.navigation.NavigationRoot
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onAuthenticationChecked: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isCheckingAuth) {
        if (!state.isCheckingAuth) onAuthenticationChecked()
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MainEvent.OnSessionEnded -> {
                navController.navigate(AuthGraphRoutes.Graph) {
                    popUpTo(ChatGraphRoutes.Graph) { inclusive = true }
                }
            }
        }
    }

    ChirpTheme(darkTheme = isDarkTheme) {
        if (!state.isCheckingAuth) {
            val startDestination: Any = remember {
                if (state.isLoggedIn) ChatGraphRoutes.Graph else AuthGraphRoutes.Graph
            }
            NavigationRoot(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}