package com.github.se.studentconnect.model.calendar

import com.google.firebase.Timestamp

/** Repository interface for personal calendar operations. */
interface PersonalCalendarRepository {

  /** Gets all personal calendar events for a user. */
  suspend fun getEventsForUser(userId: String): List<PersonalCalendarEvent>

  /** Gets personal calendar events for a user within a date range. */
  suspend fun getEventsInRange(
      userId: String,
      startTime: Timestamp,
      endTime: Timestamp
  ): List<PersonalCalendarEvent>

  /** Adds a personal calendar event. */
  suspend fun addEvent(event: PersonalCalendarEvent)

  /** Adds multiple personal calendar events (for ICS import). */
  suspend fun addEvents(events: List<PersonalCalendarEvent>)

  /** Updates a personal calendar event. */
  suspend fun updateEvent(event: PersonalCalendarEvent)

  /** Deletes a personal calendar event. */
  suspend fun deleteEvent(eventId: String, userId: String)

  /** Deletes all events from a specific source calendar. */
  suspend fun deleteEventsBySource(userId: String, sourceCalendar: String)

  /** Generates a new unique ID for a personal calendar event. */
  suspend fun getNewUid(): String

  /** Gets events that conflict with a given time range. */
  suspend fun getConflictingEvents(
      userId: String,
      startTime: Timestamp,
      endTime: Timestamp?
  ): List<PersonalCalendarEvent>
}
