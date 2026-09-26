package com.bavly.chirp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.bavly.chirp.navigation.NavigationRoot


@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavigationRoot(navController = navController)
    }
}