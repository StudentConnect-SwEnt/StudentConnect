package com.github.se.studentconnect.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides instances of OrganizationRepository.
 *
 * Currently only supports Firestore implementation. Can be extended to support local repository
 * for testing similar to UserRepositoryProvider.
 */
object OrganizationRepositoryProvider {
  val repository: OrganizationRepository = OrganizationRepositoryFirestore(Firebase.firestore)
}

