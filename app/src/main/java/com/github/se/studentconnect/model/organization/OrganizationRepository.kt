package com.github.se.studentconnect.model.organization

import com.github.se.studentconnect.model.Repository

/**
 * Repository interface for Organization operations.
 *
 * This interface defines the contract for organization data operations, allowing for different
 * implementations (e.g., Firestore, local database, mock for testing).
 */
interface OrganizationRepository : Repository {
  /**
   * Creates or updates an organization in the database.
   *
   * @param organization The OrganizationModel to save.
   */
  suspend fun saveOrganization(organization: Organization)

  /**
   * Retrieves an organization by its unique identifier.
   *
   * @param organizationId The unique identifier of the organization.
   * @return The OrganizationModel, or null if not found.
   */
  suspend fun getOrganizationById(organizationId: String): Organization?

  /**
   * Retrieves all organizations from the database.
   *
   * @return A list of all organizations.
   */
  suspend fun getAllOrganizations(): List<Organization>

  /** Returns a unique ID for a new organization document. */
  suspend fun getNewOrganizationId(): String

  /**
   * Sends a member invitation to join an organization with a specific role.
   *
   * @param organizationId The ID of the organization.
   * @param userId The ID of the user being invited.
   * @param role The role being offered.
   * @param invitedBy The ID of the user sending the invitation.
   */
  suspend fun sendMemberInvitation(
      organizationId: String,
      userId: String,
      role: String,
      invitedBy: String
  )

  /**
   * Accepts a member invitation and adds the user to the organization.
   *
   * @param organizationId The ID of the organization.
   * @param userId The ID of the user accepting the invitation.
   */
  suspend fun acceptMemberInvitation(organizationId: String, userId: String)

  /**
   * Rejects a member invitation.
   *
   * @param organizationId The ID of the organization.
   * @param userId The ID of the user rejecting the invitation.
   */
  suspend fun rejectMemberInvitation(organizationId: String, userId: String)

  /**
   * Gets all pending invitations for an organization.
   *
   * @param organizationId The ID of the organization.
   * @return List of pending invitations.
   */
  suspend fun getPendingInvitations(organizationId: String): List<OrganizationMemberInvitation>

  /**
   * Gets all pending invitations for a user.
   *
   * @param userId The ID of the user.
   * @return List of pending invitations.
   */
  suspend fun getUserPendingInvitations(userId: String): List<OrganizationMemberInvitation>

  /**
   * Adds a member directly to the organization (used by invitation acceptance).
   *
   * @param organizationId The ID of the organization.
   * @param userId The ID of the user to add.
   */
  suspend fun addMemberToOrganization(organizationId: String, userId: String)
}

/**
 * Data class representing a pending organization member invitation.
 *
 * @property organizationId The ID of the organization
 * @property userId The ID of the invited user
 * @property role The role being offered
 * @property invitedBy The ID of the user who sent the invitation
 */
data class OrganizationMemberInvitation(
    val organizationId: String = "",
    val userId: String = "",
    val role: String = "",
    val invitedBy: String = ""
) {
  /** Converts the invitation to a map for Firestore storage */
  fun toMap(): Map<String, Any> {
    return mapOf(
        "organizationId" to organizationId,
        "userId" to userId,
        "role" to role,
        "invitedBy" to invitedBy)
  }

  companion object {
    /** Creates an invitation from a Firestore document map */
    fun fromMap(map: Map<String, Any?>): OrganizationMemberInvitation {
      return OrganizationMemberInvitation(
          organizationId = map["organizationId"] as? String ?: "",
          userId = map["userId"] as? String ?: "",
          role = map["role"] as? String ?: "",
          invitedBy = map["invitedBy"] as? String ?: "")
    }
  }
}
