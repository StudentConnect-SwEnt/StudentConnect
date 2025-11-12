// Portions of this code were generated with the help of Gemini
package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.activities.InvitationStatus
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

  override suspend fun getEvent(eventUid: String): Event {
    return events.find { it.uid == eventUid }
        ?: throw NoSuchElementException("Event with UID $eventUid not found.")
  }

  override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> {
    return participantsByEvent[eventUid]?.toList() ?: emptyList()
  }

  override suspend fun addEvent(event: Event) {
    require(!(events.any { it.uid == event.uid })) { "Event with UID ${event.uid} already exists." }
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
    require(!(events.none { it.uid == eventUid })) { "Event $eventUid does not exist." }

    val participants = participantsByEvent.getOrPut(eventUid) { mutableListOf() }

    check(!(participants.any { it.uid == participant.uid })) {
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
    check(!(participants.any { it.uid == invitedUserUid })) {
      "User $invitedUserUid is already a participant in event $eventUid."
    }

    val userInvitations = invitationsByUser.getOrPut(invitedUserUid) { mutableListOf() }
    check(!(userInvitations.any { it.eventId == eventUid })) {
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
}
