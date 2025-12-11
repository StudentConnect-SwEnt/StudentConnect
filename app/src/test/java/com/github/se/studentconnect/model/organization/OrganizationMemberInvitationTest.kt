package com.github.se.studentconnect.model.organization

import org.junit.Assert.assertEquals
import org.junit.Test

class OrganizationMemberInvitationTest {

  @Test
  fun organizationMemberInvitation_createsCorrectly() {
    val invitation =
        OrganizationMemberInvitation(
            organizationId = "org-1", userId = "user-1", role = "Member", invitedBy = "user-2")

    assertEquals("org-1", invitation.organizationId)
    assertEquals("user-1", invitation.userId)
    assertEquals("Member", invitation.role)
    assertEquals("user-2", invitation.invitedBy)
  }

  @Test
  fun organizationMemberInvitation_handlesDefaultValues() {
    val invitation = OrganizationMemberInvitation()

    assertEquals("", invitation.organizationId)
    assertEquals("", invitation.userId)
    assertEquals("", invitation.role)
    assertEquals("", invitation.invitedBy)
  }

  @Test
  fun organizationMemberInvitation_toMapCreatesCorrectMap() {
    val invitation =
        OrganizationMemberInvitation(
            organizationId = "org-1", userId = "user-1", role = "Admin", invitedBy = "user-2")

    val map = invitation.toMap()

    assertEquals("org-1", map["organizationId"])
    assertEquals("user-1", map["userId"])
    assertEquals("Admin", map["role"])
    assertEquals("user-2", map["invitedBy"])
    assertEquals(4, map.size)
  }

  @Test
  fun fromMap_createsOrganizationMemberInvitationCorrectly() {
    val map =
        mapOf(
            "organizationId" to "org-1",
            "userId" to "user-1",
            "role" to "Member",
            "invitedBy" to "user-2")

    val invitation = OrganizationMemberInvitation.fromMap(map)

    assertEquals("org-1", invitation.organizationId)
    assertEquals("user-1", invitation.userId)
    assertEquals("Member", invitation.role)
    assertEquals("user-2", invitation.invitedBy)
  }

  @Test
  fun fromMap_handlesDefaultValuesForMissingFields() {
    val map = mapOf<String, Any?>()

    val invitation = OrganizationMemberInvitation.fromMap(map)

    assertEquals("", invitation.organizationId)
    assertEquals("", invitation.userId)
    assertEquals("", invitation.role)
    assertEquals("", invitation.invitedBy)
  }

  @Test
  fun fromMap_handlesNullValues() {
    val map = mapOf("organizationId" to null, "userId" to null, "role" to null, "invitedBy" to null)

    val invitation = OrganizationMemberInvitation.fromMap(map)

    assertEquals("", invitation.organizationId)
    assertEquals("", invitation.userId)
    assertEquals("", invitation.role)
    assertEquals("", invitation.invitedBy)
  }

  @Test
  fun fromMap_handlesPartialData() {
    val map = mapOf("organizationId" to "org-1", "role" to "Member")

    val invitation = OrganizationMemberInvitation.fromMap(map)

    assertEquals("org-1", invitation.organizationId)
    assertEquals("", invitation.userId)
    assertEquals("Member", invitation.role)
    assertEquals("", invitation.invitedBy)
  }

  @Test
  fun organizationMemberInvitation_equality_worksCorrectly() {
    val invitation1 =
        OrganizationMemberInvitation(
            organizationId = "org-1", userId = "user-1", role = "Member", invitedBy = "user-2")

    val invitation2 =
        OrganizationMemberInvitation(
            organizationId = "org-1", userId = "user-1", role = "Member", invitedBy = "user-2")

    assertEquals(invitation1, invitation2)
  }

  @Test
  fun organizationMemberInvitation_copy_worksCorrectly() {
    val original =
        OrganizationMemberInvitation(
            organizationId = "org-1", userId = "user-1", role = "Member", invitedBy = "user-2")

    val copy = original.copy(role = "Admin")

    assertEquals("org-1", copy.organizationId)
    assertEquals("user-1", copy.userId)
    assertEquals("Admin", copy.role)
    assertEquals("user-2", copy.invitedBy)
  }

  @Test
  fun toMap_andFromMap_roundTripWorks() {
    val original =
        OrganizationMemberInvitation(
            organizationId = "org-123",
            userId = "user-456",
            role = "Moderator",
            invitedBy = "user-789")

    val map = original.toMap()
    val reconstructed = OrganizationMemberInvitation.fromMap(map)

    assertEquals(original, reconstructed)
  }
}
