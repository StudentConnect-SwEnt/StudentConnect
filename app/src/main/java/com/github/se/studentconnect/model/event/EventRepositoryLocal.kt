// Portions of this code were generated with the help of Gemini
package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.activities.InvitationStatus
import java.util.UUID

/**
 * Represents a repository that manages a local list of events. This class is intended for testing
 * and development purposes.
 */
class EventRepositoryLocal : EventRepository {
  private val events = mutableListOf<Event>()
  private val participantsByEvent = mutableMapOf<String, MutableList<EventParticipant>>()
  // Data structure to hold invitations. Key is the invited user's UID.
  private val invitationsByUser = mutableMapOf<String, MutableList<Invitation>>()

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getAllVisibleEvents(): List<Event> {
    return events.toList()
  }

  override suspend fun getAllVisibleEventsSatisfying(predicate: (Event) -> Boolean): List<Event> {
    return events.filter(predicate)
  }

  override suspend fun getEventsByOrganization(organizationId: String): List<Event> {
    return events.filter { it.ownerId == organizationId }
  }

  override suspend fun getEventsByOwner(userId: String): List<Event> {
    return events.filter { it.ownerId == userId }
  }

  override suspend fun getEvent(eventUid: String): Event {
    return events.toList().find { it.uid == eventUid }
        ?: throw NoSuchElementException("Event with UID $eventUid not found.")
  }

  override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> {
    return participantsByEvent[eventUid]?.toList() ?: emptyList()
  }

  override suspend fun addEvent(event: Event) {
    require(!events.any { it.uid == event.uid }) { "Event with UID ${event.uid} already exists." }
    events.add(event)
    participantsByEvent[event.uid] = mutableListOf()
  }

  override suspend fun editEvent(eventUid: String, newEvent: Event) {
    require(eventUid == newEvent.uid) { "Event UID mismatch" }
    val index = events.indexOfFirst { it.uid == eventUid }
    if (index != -1) {
      events[index] = newEvent
    } else {
      throw NoSuchElementException("Cannot edit. Event with UID $eventUid not found.")
    }
  }

  override suspend fun deleteEvent(eventUid: String) {
    val removed = events.removeIf { it.uid == eventUid }
    if (!removed) {
      throw NoSuchElementException("Cannot delete. Event with UID $eventUid not found.")
    }
    participantsByEvent.remove(eventUid)
  }

  override suspend fun addParticipantToEvent(eventUid: String, participant: EventParticipant) {
    require(!events.none { it.uid == eventUid }) { "Event $eventUid does not exist." }

    val participants = participantsByEvent.getOrPut(eventUid) { mutableListOf() }

    check(!participants.any { it.uid == participant.uid }) {
      "Participant ${participant.uid} is already in event $eventUid."
    }

    participants.add(participant)
  }

  override suspend fun addInvitationToEvent(
      eventUid: String,
      invitedUserUid: String,
      currentUserId: String
  ) {
    if (events.none { it.uid == eventUid }) {
      throw NoSuchElementException(
          "Cannot invite to a non-existent event. Event with UID $eventUid not found.")
    }
    val participants = participantsByEvent[eventUid] ?: emptyList()
    check(!participants.any { it.uid == invitedUserUid }) {
      "User $invitedUserUid is already a participant in event $eventUid."
    }

    val userInvitations = invitationsByUser.getOrPut(invitedUserUid) { mutableListOf() }
    check(!userInvitations.any { it.eventId == eventUid }) {
      "An invitation for event $eventUid has already been sent to user $invitedUserUid."
    }

    val newInvitation =
        Invitation(
            eventId = eventUid,
            from = currentUserId,
            status = InvitationStatus.Pending,
            timestamp = null)
    userInvitations.add(newInvitation)
  }

  override suspend fun getEventInvitations(eventUid: String): List<String> {
    return invitationsByUser
        .filterValues { invites -> invites.any { it.eventId == eventUid } }
        .keys
        .toList()
  }

  override suspend fun removeInvitationFromEvent(
      eventUid: String,
      invitedUser: String,
      currentUserId: String
  ) {
    val userInvitations = invitationsByUser[invitedUser] ?: return
    userInvitations.removeIf { it.eventId == eventUid }
  }

  override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) {
    val participants =
        participantsByEvent[eventUid]
            ?: throw IllegalArgumentException(
                "Event $eventUid does not have any participants or does not exist.")

    val removed = participants.removeIf { it.uid == participantUid }
    if (!removed) {
      throw NoSuchElementException(
          "Participant with UID $participantUid not found in event $eventUid.")
    }
  }

  override suspend fun getEventStatistics(eventUid: String, followerCount: Int): EventStatistics {
    val participants = getEventParticipants(eventUid)
    val totalAttendees = participants.size

    // For local testing, return simplified statistics
    // Age and campus distributions are empty since we don't have user data in local repo
    val ageDistribution = emptyList<AgeGroupData>()
    val campusDistribution = emptyList<CampusData>()

    // Calculate join rate over time from participants
    val joinRateOverTime = calculateJoinRateOverTime(participants)

    // Calculate attendees/followers rate
    val attendeesFollowersRate = calculateAttendeesFollowersRate(totalAttendees, followerCount)

    return EventStatistics(
        eventId = eventUid,
        totalAttendees = totalAttendees,
        ageDistribution = ageDistribution,
        campusDistribution = campusDistribution,
        joinRateOverTime = joinRateOverTime,
        followerCount = followerCount,
        attendeesFollowersRate = attendeesFollowersRate)
  }
}
