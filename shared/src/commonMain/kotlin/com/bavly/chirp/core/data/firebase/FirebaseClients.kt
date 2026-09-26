package com.bavly.chirp.core.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.storage

object FirebaseClients {
    val auth: FirebaseAuth get() = Firebase.auth
    val firestore: FirebaseFirestore get() = Firebase.firestore
    val storage: FirebaseStorage get() = Firebase.storage
}