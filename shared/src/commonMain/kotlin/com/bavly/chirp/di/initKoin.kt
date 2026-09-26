package com.bavly.chirp.di

import com.bavly.chirp.core.di.coreModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreModule
            // authModule,     -- Phase 2
            // chatModule,     -- Phase 4
            // profileModule,  -- Phase 5
        )
    }
}