package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/** Provides instances of EventRepository. */
object EventRepositoryProvider : BaseRepositoryProvider<EventRepository>() {
  override fun createRepository(): EventRepository = EventRepositoryFirestore(Firebase.firestore)
}
