package com.bavly.chirp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bavly.chirp.features.auth.presentation.forgot_password.ForgotPasswordRoot
import com.bavly.chirp.features.auth.presentation.login.LoginRoot
import com.bavly.chirp.features.auth.presentation.register.RegisterRoot
import com.bavly.chirp.features.auth.presentation.register_success.RegisterSuccessRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Login
    ) {
        composable<AuthGraphRoutes.Login> {
            LoginRoot(
                onLoginSuccess = onLoginSuccess,
                onForgotPasswordClick = {
                    navController.navigate(AuthGraphRoutes.ForgotPassword)
                },
                onCreateAccountClick = {
                    navController.navigate(AuthGraphRoutes.Register)
                }
            )
        }
        composable<AuthGraphRoutes.Register> {
            RegisterRoot(
                onRegisterSuccess = { email ->
                    navController.navigate(AuthGraphRoutes.RegisterSuccess(email))
                },
                onLoginClick = {
                    navController.popBackStack(AuthGraphRoutes.Login, inclusive = false)
                }
            )
        }
        composable<AuthGraphRoutes.RegisterSuccess> {
            RegisterSuccessRoot(
                onLoginClick = {
                    navController.popBackStack(AuthGraphRoutes.Login, inclusive = false)
                }
            )
        }
        composable<AuthGraphRoutes.ForgotPassword> {
            ForgotPasswordRoot()
        }
    }
}