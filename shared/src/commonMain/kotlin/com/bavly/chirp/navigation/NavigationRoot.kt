package com.bavly.chirp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationRoot(
    navController: NavHostController,
    startDestination: Any = AuthGraphRoutes.Graph
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<AuthGraphRoutes.Graph> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chirp",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}