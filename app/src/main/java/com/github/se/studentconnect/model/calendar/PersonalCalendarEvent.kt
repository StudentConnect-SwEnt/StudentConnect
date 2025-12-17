package com.github.se.studentconnect.model.calendar

import com.google.firebase.Timestamp

/**
 * Represents a personal calendar event imported from an ICS file or manually added. These events
 * are separate from the app's Event system and represent the user's personal schedule (classes,
 * appointments, etc.)
 *
 * @property uid Unique identifier for the calendar event
 * @property userId The user this event belongs to
 * @property title The title/summary of the event
 * @property description Optional description of the event
 * @property location Optional location of the event
 * @property start Start time of the event
 * @property end End time of the event (optional for all-day events)
 * @property isAllDay Whether this is an all-day event
 * @property color Optional color for display (hex string)
 * @property sourceCalendar Name of the source calendar (e.g., "Google Calendar", "Outlook")
 * @property externalUid Original UID from the ICS file for deduplication
 */
data class PersonalCalendarEvent(
    val uid: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val start: Timestamp,
    val end: Timestamp? = null,
    val isAllDay: Boolean = false,
    val color: String? = null,
    val sourceCalendar: String? = null,
    val externalUid: String? = null
) {
  /** Converts the PersonalCalendarEvent to a Map for Firestore storage. */
  fun toMap(): Map<String, Any?> =
      mapOf(
          "uid" to uid,
          "userId" to userId,
          "title" to title,
          "description" to description,
          "location" to location,
          "start" to start,
          "end" to end,
          "isAllDay" to isAllDay,
          "color" to color,
          "sourceCalendar" to sourceCalendar,
          "externalUid" to externalUid)

  companion object {
    /** Creates a PersonalCalendarEvent from a Firestore document map. */
    fun fromMap(map: Map<String, Any?>): PersonalCalendarEvent {
      return PersonalCalendarEvent(
          uid = map["uid"] as? String ?: "",
          userId = map["userId"] as? String ?: "",
          title = map["title"] as? String ?: "",
          description = map["description"] as? String,
          location = map["location"] as? String,
          start = map["start"] as? Timestamp ?: Timestamp.now(),
          end = map["end"] as? Timestamp,
          isAllDay = map["isAllDay"] as? Boolean ?: false,
          color = map["color"] as? String,
          sourceCalendar = map["sourceCalendar"] as? String,
          externalUid = map["externalUid"] as? String)
    }
  }
}

/** Represents a conflict between a personal calendar event and an app event. */
data class CalendarConflict(
    val personalEvent: PersonalCalendarEvent,
    val conflictType: ConflictType
)

/** Types of calendar conflicts. */
enum class ConflictType {
  /** The events overlap in time */
  OVERLAP,
  /** The events are at the same time */
  EXACT_MATCH,
  /** The personal event starts during the app event */
  STARTS_DURING,
  /** The personal event ends during the app event */
  ENDS_DURING
}
