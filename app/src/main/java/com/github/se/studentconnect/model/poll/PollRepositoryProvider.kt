package com.github.se.studentconnect.model.poll

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/** Singleton object that provides access to the [PollRepository] instance. */
object PollRepositoryProvider : BaseRepositoryProvider<PollRepository>() {
  override fun createRepository(): PollRepository = PollRepositoryFirestore(Firebase.firestore)
}
