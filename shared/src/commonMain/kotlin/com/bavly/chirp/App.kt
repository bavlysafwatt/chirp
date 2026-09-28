package com.bavly.chirp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.bavly.chirp.core.designsystem.theme.ChirpTheme
import com.bavly.chirp.navigation.NavigationRoot


@Composable
fun App() {
    ChirpTheme {
        val navController = rememberNavController()
        NavigationRoot(navController = navController)
    }
}