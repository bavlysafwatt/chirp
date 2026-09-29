package com.bavly.chirp

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(15.dp)
                        .alpha(alpha = 1f),
                    strokeWidth = 1.5.dp,
                    color = Color.Black
                )
            }
        }
    }
}