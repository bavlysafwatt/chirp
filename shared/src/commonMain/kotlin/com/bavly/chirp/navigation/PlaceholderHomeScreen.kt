package com.bavly.chirp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButton
import com.bavly.chirp.core.designsystem.components.buttons.ChirpButtonStyle
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PlaceholderHomeScreen(
    authRepository: AuthRepository = koinInject()
) {
    val user by authRepository.observeSession().collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Logged in as ${user?.email.orEmpty()}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        ChirpButton(
            text = "Logout",
            onClick = { scope.launch { authRepository.logout() } },
            style = ChirpButtonStyle.DESTRUCTIVE_SECONDARY
        )
    }
}