package com.github.se.studentconnect.model.event

/** Represents a repository that manages events. */
interface EventRepository {

  /**
   * Generates and returns a new unique identifier for an event.
   *
   * @return A newly generated unique event identifier.
   */
  fun getNewUid(): String

  /**
   * Retrieves all events that are visible.
   *
   * @return A list of [Event] objects.
   * @throws Exception if the retrieval fails.
   */
  suspend fun getAllVisibleEvents(): List<Event>

  /**
   * Retrieves a specific event by its unique identifier.
   *
   * @param eventUid The unique identifier of the event to retrieve.
   * @return The [Event] with the specified identifier.
   * @throws Exception if the event is not found.
   */
  suspend fun getEvent(eventUid: String): Event

  /**
   * Retrieves the list of participants for a specific event.
   *
   * @param eventUid The unique identifier of the event.
   * @return A list of [EventParticipant] objects associated with the event.
   * @throws Exception if the event or its participants cannot be found.
   */
  suspend fun getEventParticipants(eventUid: String): List<EventParticipant>

  /**
   * Retrieves all events that a given user is participating in.
   *
   * @param userUid The unique identifier of the user whose attended events should be retrieved.
   * @return A list of [Event] objects that the user is participating in.
   * @throws Exception if the retrieval fails or if the user cannot be found.
   */
  suspend fun getEventsAttendedByUser(userUid: String): List<Event>
  // TODO: move getEventsAttendedByUser to UserRepository.getAttendingEvents

  /**
   * Adds a new event to the repository.
   *
   * @param event The [Event] object to add.
   */
  suspend fun addEvent(event: Event)

  /**
   * Edits an existing event in the repository.
   *
   * @param eventUid The unique identifier of the event to edit.
   * @param newEvent The new value for the event.
   * @throws Exception if the event is not found.
   */
  suspend fun editEvent(eventUid: String, newEvent: Event)

  /**
   * Deletes an event from the repository.
   *
   * @param eventUid The unique identifier of the event to delete.
   * @throws Exception if the event is not found.
   */
  suspend fun deleteEvent(eventUid: String)

  /**
   * Adds a participant to a given event.
   *
   * @param eventUid The unique identifier of the event to which the participant should be added.
   * @param participant The [EventParticipant] object representing the participant being added.
   * @throws Exception if the participant already joined the event.
   */
  suspend fun addParticipantToEvent(eventUid: String, participant: EventParticipant)

  /**
   * Removes a participant from a given event.
   *
   * @param eventUid The unique identifier of the event from which the participant should be
   *   removed.
   * @param participantUid The unique identifier of the participant to remove.
   * @throws Exception if the participant is not found.
   */
  suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String)
}
