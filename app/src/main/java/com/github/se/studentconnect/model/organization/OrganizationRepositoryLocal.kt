package com.github.se.studentconnect.model.organization

/**
 * Local in-memory implementation of OrganizationRepository for testing.
 *
 * This implementation stores organizations in a simple in-memory map and is intended for use in
 * unit tests and local development.
 */
class OrganizationRepositoryLocal : OrganizationRepository {
  private val organizations = mutableMapOf<String, Organization>()
  private val invitations = mutableMapOf<String, OrganizationMemberInvitation>()
  private var idCounter = 0

  override suspend fun saveOrganization(organization: Organization) {
    organizations[organization.id] = organization
  }

  override suspend fun getOrganizationById(organizationId: String): Organization? {
    return organizations[organizationId]
  }

  override suspend fun getAllOrganizations(): List<Organization> {
    return organizations.values.toList()
  }

  override suspend fun getNewOrganizationId(): String {
    return "org_local_${idCounter++}"
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
    invitations[invitationId] = invitation
  }

  override suspend fun acceptMemberInvitation(organizationId: String, userId: String) {
    val org = organizations[organizationId]
    val invitationId = "${organizationId}_${userId}"
    val invitation = invitations[invitationId]

    if (org != null) {
      val updatedMemberUids = org.memberUids.toMutableList()
      val updatedMemberRoles = org.memberRoles.toMutableMap()

      if (!updatedMemberUids.contains(userId)) {
        updatedMemberUids.add(userId)
        // Store the role from the invitation, default to "Member" if not found
        updatedMemberRoles[userId] = invitation?.role ?: "Member"
        organizations[organizationId] =
            org.copy(memberUids = updatedMemberUids, memberRoles = updatedMemberRoles)
      }
    }

    // Delete the invitation
    invitations.remove(invitationId)
  }

  override suspend fun rejectMemberInvitation(organizationId: String, userId: String) {
    val invitationId = "${organizationId}_${userId}"
    invitations.remove(invitationId)
  }

  override suspend fun getPendingInvitations(
      organizationId: String
  ): List<OrganizationMemberInvitation> {
    return invitations.values.filter { it.organizationId == organizationId }
  }

  override suspend fun getUserPendingInvitations(
      userId: String
  ): List<OrganizationMemberInvitation> {
    return invitations.values.filter { it.userId == userId }
  }

  override suspend fun addMemberToOrganization(organizationId: String, userId: String) {
    val org = organizations[organizationId]
    if (org != null) {
      val updatedMemberUids = org.memberUids.toMutableList()
      if (!updatedMemberUids.contains(userId)) {
        updatedMemberUids.add(userId)
        organizations[organizationId] = org.copy(memberUids = updatedMemberUids)
      }
    }
  }

  /** Clears all organizations from the local repository. Useful for test setup/teardown. */
  fun clear() {
    organizations.clear()
    invitations.clear()
    idCounter = 0
  }
}
