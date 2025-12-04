package com.github.se.studentconnect.model.friends

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/** Provides a single instance of the FriendsRepository in the app. */
object FriendsRepositoryProvider : BaseRepositoryProvider<FriendsRepository>() {
  override fun createRepository(): FriendsRepository =
      FriendsRepositoryFirestore(Firebase.firestore)
}
