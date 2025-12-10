package com.github.se.studentconnect.model.organization

import android.util.Log
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
    private const val INVITATIONS_COLLECTION = "organization_member_invitations"
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

  override suspend fun getAllOrganizations(): List<Organization> {
    return try {
      val snapshot = db.collection(COLLECTION_NAME).get().await()
      snapshot.documents.mapNotNull { document ->
        Organization.fromMap(document.data ?: emptyMap())
      }
    } catch (e: Exception) {
      Log.e("OrganizationRepo", "Failed to get all organizations", e)
      emptyList()
    }
  }

  override suspend fun getNewOrganizationId(): String {
    val docRef = db.collection(COLLECTION_NAME).document()
    return docRef.id
  }

  override suspend fun sendMemberInvitation(
      organizationId: String,
      userId: String,
      role: String,
      invitedBy: String
  ) {
    val invitation =
        OrganizationMemberInvitation(
            organizationId = organizationId, userId = userId, role = role, invitedBy = invitedBy)
    val invitationId = "${organizationId}_${userId}"
    db.collection(INVITATIONS_COLLECTION).document(invitationId).set(invitation.toMap()).await()
  }

  override suspend fun acceptMemberInvitation(organizationId: String, userId: String) {
    // Add user to organization's memberUids
    val orgRef = db.collection(COLLECTION_NAME).document(organizationId)
    val org = getOrganizationById(organizationId)
    if (org != null) {
      val updatedMemberUids = org.memberUids.toMutableList()
      if (!updatedMemberUids.contains(userId)) {
        updatedMemberUids.add(userId)
        orgRef.update("memberUids", updatedMemberUids).await()
      }
    }

    // Delete the invitation
    val invitationId = "${organizationId}_${userId}"
    db.collection(INVITATIONS_COLLECTION).document(invitationId).delete().await()
  }

  override suspend fun rejectMemberInvitation(organizationId: String, userId: String) {
    val invitationId = "${organizationId}_${userId}"
    db.collection(INVITATIONS_COLLECTION).document(invitationId).delete().await()
  }

  override suspend fun getPendingInvitations(
      organizationId: String
  ): List<OrganizationMemberInvitation> {
    return try {
      val snapshot =
          db.collection(INVITATIONS_COLLECTION)
              .whereEqualTo("organizationId", organizationId)
              .get()
              .await()
      snapshot.documents.mapNotNull { document ->
        OrganizationMemberInvitation.fromMap(document.data ?: emptyMap())
      }
    } catch (e: Exception) {
      Log.e("OrganizationRepo", "Failed to get pending invitations", e)
      emptyList()
    }
  }

  override suspend fun getUserPendingInvitations(
      userId: String
  ): List<OrganizationMemberInvitation> {
    return try {
      val snapshot =
          db.collection(INVITATIONS_COLLECTION).whereEqualTo("userId", userId).get().await()
      snapshot.documents.mapNotNull { document ->
        OrganizationMemberInvitation.fromMap(document.data ?: emptyMap())
      }
    } catch (e: Exception) {
      Log.e("OrganizationRepo", "Failed to get user pending invitations", e)
      emptyList()
    }
  }

  override suspend fun addMemberToOrganization(organizationId: String, userId: String) {
    val orgRef = db.collection(COLLECTION_NAME).document(organizationId)
    val org = getOrganizationById(organizationId)
    if (org != null) {
      val updatedMemberUids = org.memberUids.toMutableList()
      if (!updatedMemberUids.contains(userId)) {
        updatedMemberUids.add(userId)
        orgRef.update("memberUids", updatedMemberUids).await()
      }
    }
  }
}
