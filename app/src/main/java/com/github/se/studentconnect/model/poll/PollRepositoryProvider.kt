package com.github.se.studentconnect.model.poll

import com.google.firebase.firestore.FirebaseFirestore

/** Singleton object that provides access to the [PollRepository] instance. */
object PollRepositoryProvider {
  private var instance: PollRepository? = null

  val repository: PollRepository
    get() {
      if (instance == null) {
        instance = PollRepositoryFirestore(FirebaseFirestore.getInstance())
      }
      return instance!!
    }

  fun setRepository(repository: PollRepository) {
    instance = repository
  }
}
