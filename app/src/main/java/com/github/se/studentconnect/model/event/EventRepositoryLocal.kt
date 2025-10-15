// Portions of this code were generated with the help of Gemini
package com.github.se.studentconnect.model.event

import java.util.UUID

/**
 * Represents a repository that manages a local list of events. This class is intended for testing
 * and development purposes.
 */
class EventRepositoryLocal : EventRepository {
  private val events = mutableListOf<Event>()
  private val participantsByEvent = mutableMapOf<String, MutableList<EventParticipant>>()

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
    if (events.any { it.uid == event.uid }) {
      throw IllegalArgumentException("Event with UID ${event.uid} already exists.")
    }
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
    if (events.none { it.uid == eventUid }) {
      throw IllegalArgumentException("Event $eventUid does not exist.")
    }

    val participants = participantsByEvent.getOrPut(eventUid) { mutableListOf() }

    if (participants.any { it.uid == participant.uid }) {
      throw IllegalStateException("Participant ${participant.uid} is already in event $eventUid.")
    }

    participants.add(participant)
  }

  override suspend fun addInvitationToEvent(
      eventUid: String,
      invitedUser: String,
      currentUserId: String
  ) {
    TODO("Not yet implemented")
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
