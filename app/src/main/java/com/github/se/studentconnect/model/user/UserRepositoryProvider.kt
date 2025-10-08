package com.github.se.studentconnect.model.user

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/** Provides a single instance of the repository in the app. */
class UserRepositoryProvider {
    private val _repository: UserRepository = UserRepositoryFirestore(Firebase.firestore)
    val repository: UserRepository = _repository
}
