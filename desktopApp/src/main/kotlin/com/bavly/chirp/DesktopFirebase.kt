package com.bavly.chirp

import android.app.Application
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import java.io.File
import java.util.Properties

private const val API_KEY = "AIzaSyAa9aKQyBcFGIc-erqRJlCelZL792da_E8"
private const val APP_ID = "1:255068244290:web:93f4b580a5522439b3e0db"
private const val PROJECT_ID = "chirp-b3a7b"

/** Desktop has no google-services plugin, so Firebase is initialized manually. */
fun initializeFirebaseDesktop() {
    val storeFile = File(System.getProperty("user.home"), ".chirp/firebase-session.properties")
    storeFile.parentFile.mkdirs()

    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
        private val props = Properties().apply {
            if (storeFile.exists()) storeFile.inputStream().use { load(it) }
        }

        private fun persist() = storeFile.outputStream().use { props.store(it, null) }

        override fun store(key: String, value: String) {
            props.setProperty(key, value)
            persist()
        }

        override fun retrieve(key: String): String? = props.getProperty(key)

        override fun clear(key: String) {
            props.remove(key)
            persist()
        }

        override fun log(msg: String) = println(msg)
    })

    Firebase.initialize(
        Application(),
        FirebaseOptions(
            applicationId = APP_ID,
            apiKey = API_KEY,
            projectId = PROJECT_ID
        )
    )
}