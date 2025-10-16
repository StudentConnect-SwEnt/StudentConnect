package com.github.se.studentconnect.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object UserRepositoryProvider {
  private val _repository: UserRepository = UserRepositoryFirestore(Firebase.firestore)

  var repository: UserRepository = _repository
}
