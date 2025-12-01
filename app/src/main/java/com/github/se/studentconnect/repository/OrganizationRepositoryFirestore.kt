package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.organization.Organization
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Implementation of OrganizationRepository using Firebase Firestore.
 *
 * @property db The Firestore database instance.
 */
class OrganizationRepositoryFirestore(private val db: FirebaseFirestore) : OrganizationRepository {

  companion object {
    private const val COLLECTION_NAME = "organizations"
  }

  override suspend fun saveOrganization(organization: Organization) {
    db.collection(COLLECTION_NAME).document(organization.id).set(organization.toMap()).await()
  }

  override suspend fun getOrganizationById(organizationId: String): Organization? {
    val document = db.collection(COLLECTION_NAME).document(organizationId).get().await()

    return if (document.exists()) {
      Organization.fromMap(document.data ?: emptyMap())
    } else {
      null
    }
  }

  override suspend fun getNewOrganizationId(): String {
    val docRef = db.collection(COLLECTION_NAME).document()
    return docRef.id
  }

  override suspend fun getAllOrganizations(): List<Organization> {
    return try {
      val snapshot = db.collection(COLLECTION_NAME).get().await()
      snapshot.documents.mapNotNull { document ->
        Organization.fromMap(document.data ?: emptyMap())
      }
    } catch (e: Exception) {
      android.util.Log.e("OrganizationRepo", "Failed to get all organizations", e)
      emptyList()
    }
  }
}
