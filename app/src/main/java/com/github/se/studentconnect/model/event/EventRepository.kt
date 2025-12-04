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
   * Retrieves all events that are visible and satisfy the given predicate.
   *
   * @param predicate The predicate to filter the events by.
   * @return A list of [Event] objects.
   * @throws Exception if the retrieval fails.
   */
  suspend fun getAllVisibleEventsSatisfying(predicate: (Event) -> Boolean): List<Event>

  /**
   * Retrieves all events owned by a specific organization.
   *
   * @param organizationId The unique identifier of the organization.
   * @return A list of [Event] objects owned by the organization.
   * @throws Exception if the retrieval fails.
   */
  suspend fun getEventsByOrganization(organizationId: String): List<Event> {
    // Default implementation: return empty list
    return emptyList()
  }

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
   * @throws Exception if the event is not found or the participant already joined the event.
   */
  suspend fun addParticipantToEvent(eventUid: String, participant: EventParticipant)

  /** Adds an invitation (a participant with pending status) to a given event. */
  suspend fun addInvitationToEvent(eventUid: String, invitedUser: String, currentUserId: String)

  /**
   * Returns the list of user IDs invited to the event.
   *
   * @param eventUid The unique identifier of the event.
   */
  suspend fun getEventInvitations(eventUid: String): List<String>

  /**
   * Removes an invitation from a given event (owner only).
   *
   * @param eventUid The event identifier.
   * @param invitedUser The user whose invitation should be revoked.
   * @param currentUserId The UID of the caller (must match owner).
   */
  suspend fun removeInvitationFromEvent(
      eventUid: String,
      invitedUser: String,
      currentUserId: String
  )

  /**
   * Removes a participant from a given event.
   *
   * @param eventUid The unique identifier of the event from which the participant should be
   *   removed.
   * @param participantUid The unique identifier of the participant to remove.
   * @throws Exception if the participant is not found.
   */
  suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String)

  /**
   * Retrieves comprehensive statistics for a specific event.
   *
   * @param eventUid The unique identifier of the event.
   * @param followerCount Number of followers for the organization hosting the event.
   * @return [EventStatistics] containing all metrics for the event.
   * @throws Exception if the event is not found or statistics cannot be computed.
   */
  suspend fun getEventStatistics(eventUid: String, followerCount: Int): EventStatistics {
    // Default implementation returns empty statistics
    return EventStatistics.empty(eventUid)
  }
}
