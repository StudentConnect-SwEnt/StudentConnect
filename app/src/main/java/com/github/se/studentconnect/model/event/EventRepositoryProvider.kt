// Fake events were created by Gemini
package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides instances of EventRepository. Allows switching between Firestore and a local, in-memory
 * repository for testing.
 *
 * The repository mode is controlled by `AuthenticationProvider.local`:
 * - local = true: Uses in-memory repository for testing
 * - local = false: Uses Firestore for production
 */
object EventRepositoryProvider : BaseRepositoryProvider<EventRepository>() {
  override fun getCurrentRepository(): EventRepository =
      EventRepositoryFirestore(Firebase.firestore)
}
