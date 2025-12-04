package com.github.se.studentconnect.model.notification

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/** Singleton provider for NotificationRepository */
object NotificationRepositoryProvider : BaseRepositoryProvider<NotificationRepository>() {
  override fun createRepository(): NotificationRepository =
      NotificationRepositoryFirestore(Firebase.firestore)
}
