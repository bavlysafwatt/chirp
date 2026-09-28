package com.bavly.chirp.features.auth.di

import com.bavly.chirp.features.auth.data.repository.FirebaseAuthRepository
import com.bavly.chirp.features.auth.domain.repository.AuthRepository
import com.bavly.chirp.features.auth.presentation.forgot_password.ForgotPasswordViewModel
import com.bavly.chirp.features.auth.presentation.login.LoginViewModel
import com.bavly.chirp.features.auth.presentation.register.RegisterViewModel
import com.bavly.chirp.features.auth.presentation.register_success.RegisterSuccessViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    singleOf(::FirebaseAuthRepository) bind AuthRepository::class

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
    viewModelOf(::ForgotPasswordViewModel)
}