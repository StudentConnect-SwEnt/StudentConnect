package com.github.se.studentconnect.model.user

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/** Provides instances of UserRepository. */
object UserRepositoryProvider : BaseRepositoryProvider<UserRepository>() {
  override fun createRepository(): UserRepository = UserRepositoryFirestore(Firebase.firestore)
}
