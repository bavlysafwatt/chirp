package com.bavly.chirp.di

import com.bavly.chirp.core.di.coreModule
import com.bavly.chirp.features.auth.di.authModule
import com.bavly.chirp.features.chat.di.chatModule
import com.bavly.chirp.features.profile.di.profileModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreModule,
            appModule,
            authModule,
            chatModule,
            profileModule
        )
    }
}