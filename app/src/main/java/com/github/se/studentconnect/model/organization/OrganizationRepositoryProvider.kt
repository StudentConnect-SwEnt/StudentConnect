package com.github.se.studentconnect.model.organization

import com.github.se.studentconnect.model.BaseRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides instances of OrganizationRepository.
 *
 * Currently only supports Firestore implementation. Can be extended to support local repository for
 * testing similar to UserRepositoryProvider.
 */
object OrganizationRepositoryProvider : BaseRepositoryProvider<OrganizationRepository>() {
  override fun getCurrentRepository(): OrganizationRepository =
      OrganizationRepositoryFirestore(Firebase.firestore)
}
