package com.bavly.chirp.core.di

import com.bavly.chirp.core.data.firebase.FirebaseClients
import com.bavly.chirp.core.data.logging.PrintlnChirpLogger
import com.bavly.chirp.core.domain.util.ChirpLogger
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val coreModule = module {
    single<ChirpLogger> { PrintlnChirpLogger }

    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

    single<FirebaseAuth> { FirebaseClients.auth }
    single<FirebaseFirestore> { FirebaseClients.firestore }
    single<FirebaseStorage> { FirebaseClients.storage }
}