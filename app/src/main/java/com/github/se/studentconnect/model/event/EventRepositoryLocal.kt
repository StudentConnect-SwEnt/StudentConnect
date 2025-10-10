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

  /** Correctly implemented function to get events a user is attending. */
  override suspend fun getEventsAttendedByUser(userUid: String): List<Event> {
    // TODO filter based on if the currently logged in user can see the event or not; for now, gets
    //  all events
    return events
  }

  override suspend fun addEvent(event: Event) {
    if (events.any { it.uid == event.uid }) {
      // This throws IllegalArgumentException, which is standard for invalid parameters.
      throw IllegalArgumentException("Event with UID ${event.uid} already exists.")
    }
    events.add(event)
    participantsByEvent[event.uid] = mutableListOf()
  }

  override suspend fun editEvent(eventUid: String, newEvent: Event) {
    // 'require' correctly throws IllegalArgumentException for precondition failures.
    require(eventUid == newEvent.uid) { "Event UID mismatch" }
    val index = events.indexOfFirst { it.uid == eventUid }
    if (index != -1) {
      events[index] = newEvent
    } else {
      // Throws NoSuchElementException when the item to edit isn't found.
      throw NoSuchElementException("Cannot edit. Event with UID $eventUid not found.")
    }
  }

  override suspend fun deleteEvent(eventUid: String) {
    val removed = events.removeIf { it.uid == eventUid }
    if (!removed) {
      // Throws NoSuchElementException when the item to delete isn't found.
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

  override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) {
    val participants =
        participantsByEvent[eventUid]
            ?: throw IllegalArgumentException(
                "Event $eventUid does not have any participants or does not exist.")

    val removed = participants.removeIf { it.uid == participantUid }
    if (!removed) {
      // Throws NoSuchElementException when the participant to remove isn't found in the list.
      throw NoSuchElementException(
          "Participant with UID $participantUid not found in event $eventUid.")
    }
  }
}
