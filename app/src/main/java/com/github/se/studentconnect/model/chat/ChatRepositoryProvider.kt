package com.github.se.studentconnect.model.chat

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/** Singleton object that provides access to the [ChatRepository] instance. */
object ChatRepositoryProvider : BaseRepositoryProvider<ChatRepository>() {
  override fun createRepository(): ChatRepository = ChatRepositoryFirestore(Firebase.firestore)
}
