package com.github.se.studentconnect.model

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.home.OrganizationData
import java.text.SimpleDateFormat
import java.util.Locale

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

/**
 * Converts a User to an OrganizationMember.
 *
 * @param role The member's role in the organization (default: "Member")
 * @return OrganizationMember for UI display
 */
fun User.toOrganizationMember(role: String = "Member"): OrganizationMember {
  return OrganizationMember(
      memberId = this.userId,
      name = this.firstName + " " + this.lastName,
      role = role,
      avatarUrl = this.profilePictureUrl)
}

/**
 * Fetches users by their IDs and converts them to OrganizationMembers.
 *
 * @param memberUids List of user IDs to fetch
 * @param userRepository Repository to fetch user data from
 * @param defaultRole Default role for members (default: "Member")
 * @return List of OrganizationMembers
 */
suspend fun fetchOrganizationMembers(
    memberUids: List<String>,
    userRepository: UserRepository,
    defaultRole: String = "Member"
): List<OrganizationMember> {
  return memberUids.mapNotNull { uid ->
    try {
      val user = userRepository.getUserById(uid)
      user?.toOrganizationMember(defaultRole)
    } catch (e: Exception) {
      null // Skip users that can't be fetched
    }
  }
}

/**
 * Converts an Event to an OrganizationEvent for display in the organization profile.
 *
 * @return OrganizationEvent for UI display
 */
fun Event.toOrganizationEvent(): OrganizationEvent {
  val dateFormat = SimpleDateFormat("d MMM, yyyy", Locale.ENGLISH)
  val cardDate = dateFormat.format(this.start.toDate())

  // Calculate subtitle (e.g., "Tomorrow", "Today", "In 3 days")
  val today = java.util.Calendar.getInstance()
  val eventDate =
      java.util.Calendar.getInstance().apply { time = this@toOrganizationEvent.start.toDate() }

  val daysDifference =
      ((eventDate.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

  val subtitle =
      when {
        daysDifference == 0 -> "Today"
        daysDifference == 1 -> "Tomorrow"
        daysDifference > 1 -> "In $daysDifference days"
        else -> cardDate
      }

  return OrganizationEvent(
      eventId = this.uid,
      cardTitle = this.title,
      cardDate = cardDate,
      title = this.title,
      subtitle = subtitle,
      location = this.location?.name)
}

/**
 * Converts a list of Events to OrganizationEvents.
 *
 * @return List of OrganizationEvents
 */
fun List<Event>.toOrganizationEvents(): List<OrganizationEvent> {
  return this.map { it.toOrganizationEvent() }
}
