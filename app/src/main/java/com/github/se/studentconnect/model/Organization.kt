package com.github.se.studentconnect.model

/**
 * Represents an Organization in the StudentConnect application.
 *
 * @property organizationId The unique identifier for the organization.
 * @property name The organization's name.
 * @property description A description of the organization.
 * @property logoUrl URL to the organization's logo (optional).
 * @property isFollowing Whether the current user is following this organization.
 * @property events List of events associated with this organization.
 * @property members List of members in this organization.
 */
data class Organization(
    val organizationId: String,
    val name: String,
    val description: String,
    val logoUrl: String? = null,
    val isFollowing: Boolean = false,
    val events: List<OrganizationEvent> = emptyList(),
    val members: List<OrganizationMember> = emptyList()
) {
  init {
    require(organizationId.isNotBlank()) { "Organization ID cannot be blank" }
    require(name.isNotBlank()) { "Organization name cannot be blank" }
    require(name.length <= 200) { "Organization name cannot exceed 200 characters" }
    require(description.isNotBlank()) { "Organization description cannot be blank" }
    require(description.length <= 1000) { "Organization description cannot exceed 1000 characters" }
  }
}

/**
 * Represents an event organized by an organization.
 *
 * @property eventId The unique identifier for the event.
 * @property cardTitle The title displayed on the event card.
 * @property cardDate The date displayed on the event card.
 * @property title The main title of the event.
 * @property subtitle Additional information about the event (e.g., "Tomorrow").
 * @property location The location of the event (optional).
 */
data class OrganizationEvent(
    val eventId: String,
    val cardTitle: String,
    val cardDate: String,
    val title: String,
    val subtitle: String,
    val location: String? = null
) {
  init {
    require(eventId.isNotBlank()) { "Event ID cannot be blank" }
    require(cardTitle.isNotBlank()) { "Card title cannot be blank" }
    require(cardDate.isNotBlank()) { "Card date cannot be blank" }
    require(title.isNotBlank()) { "Event title cannot be blank" }
    require(subtitle.isNotBlank()) { "Event subtitle cannot be blank" }
  }
}

/**
 * Represents a member of an organization.
 *
 * @property memberId The unique identifier for the member.
 * @property name The member's display name.
 * @property role The member's role in the organization (e.g., "Owner", "Member").
 * @property avatarUrl URL to the member's avatar (optional).
 */
data class OrganizationMember(
    val memberId: String,
    val name: String,
    val role: String,
    val avatarUrl: String? = null
) {
  init {
    require(memberId.isNotBlank()) { "Member ID cannot be blank" }
    require(name.isNotBlank()) { "Member name cannot be blank" }
    require(name.length <= 100) { "Member name cannot exceed 100 characters" }
    require(role.isNotBlank()) { "Member role cannot be blank" }
    require(role.length <= 50) { "Member role cannot exceed 50 characters" }
  }
}
