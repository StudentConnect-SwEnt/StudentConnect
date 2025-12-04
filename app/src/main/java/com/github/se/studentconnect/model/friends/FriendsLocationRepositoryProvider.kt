package com.github.se.studentconnect.model.friends

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.database.database

/** Provides a single instance of the FriendsLocationRepository in the app. */
object FriendsLocationRepositoryProvider : BaseRepositoryProvider<FriendsLocationRepository>() {
  override fun getRepository(): FriendsLocationRepository =
      FriendsLocationRepositoryFirebase(Firebase.database)
}
