package com.github.se.studentconnect.model.event

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object EventRepositoryProvider {
    private val _repository: EventRepository = EventRepositoryFirestore(Firebase.firestore)

    var repository: EventRepository = _repository
}