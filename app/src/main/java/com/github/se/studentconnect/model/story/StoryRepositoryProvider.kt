package com.github.se.studentconnect.model.story

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides instances of StoryRepository.
 *
 * Uses Firestore implementation for production.
 */
object StoryRepositoryProvider : BaseRepositoryProvider<StoryRepository>() {
  override fun createRepository() =
      StoryRepositoryFirestore(
          db = Firebase.firestore,
          mediaRepository = MediaRepositoryProvider.repository,
          userRepository = UserRepositoryProvider.repository,
          eventRepository = EventRepositoryProvider.repository)
}
