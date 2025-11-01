package com.github.se.studentconnect.model.friends

import com.google.firebase.Firebase
import com.google.firebase.database.database

/**
 * Provides a single instance of the FriendsLocationRepository in the app. `repository` is mutable
 * for testing purposes.
 */
object FriendsLocationRepositoryProvider {
  private val _repository: FriendsLocationRepository =
      FriendsLocationRepositoryFirebase(Firebase.database)

  var repository: FriendsLocationRepository = _repository
}
