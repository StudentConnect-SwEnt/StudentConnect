package com.github.se.studentconnect.model

import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.ui.screen.home.OrganizationData

/**
 * Converts a backend Organization model to the UI OrganizationProfile model.
 *
 * @param isFollowing Whether the current user is following this organization
 * @param events Optional list of events to include in the profile
 * @param members Optional list of members to include in the profile
 * @return OrganizationProfile for UI display
 */
fun Organization.toOrganizationProfile(
    isFollowing: Boolean = false,
    events: List<OrganizationEvent> = emptyList(),
    members: List<OrganizationMember> = emptyList()
): OrganizationProfile {
  return OrganizationProfile(
      organizationId = this.id,
      name = this.name,
      description = this.description ?: "",
      logoUrl = this.logoUrl,
      isFollowing = isFollowing,
      events = events,
      members = members)
}

/**
 * Converts a backend Organization model to the simplified OrganizationData model used for
 * suggestions.
 *
 * @return OrganizationData for suggestion cards
 */
fun Organization.toOrganizationData(): OrganizationData {
  // Generate a handle from the organization name if not available
  val handle = "@${this.name.lowercase().replace(" ", "")}"
  return OrganizationData(id = this.id, name = this.name, handle = handle)
}

/**
 * Converts a list of Organizations to OrganizationData for suggestions.
 *
 * @return List of OrganizationData
 */
fun List<Organization>.toOrganizationDataList(): List<OrganizationData> {
  return this.map { it.toOrganizationData() }
}
