package com.github.se.studentconnect.model.friends

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Provides a single instance of the FriendsRepository in the app. `repository` is mutable for
 * testing purposes.
 */
object FriendsRepositoryProvider {
  private val _repository: FriendsRepository = FriendsRepositoryFirestore(Firebase.firestore)

  var repository: FriendsRepository = _repository
}
