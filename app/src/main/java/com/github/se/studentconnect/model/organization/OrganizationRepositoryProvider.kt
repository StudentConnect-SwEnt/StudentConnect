package com.github.se.studentconnect.model.organization

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides instances of OrganizationRepository.
 *
 * Currently only supports Firestore implementation. Can be extended to support local repository for
 * testing similar to UserRepositoryProvider.
 */
object OrganizationRepositoryProvider {
  private val firestoreRepository: OrganizationRepository by lazy {
    OrganizationRepositoryFirestore(Firebase.firestore)
  }

  private var _repository: OrganizationRepository? = null

  var repository: OrganizationRepository
    get() = _repository ?: firestoreRepository
    set(value) {
      _repository = value
    }
}
