package com.github.se.studentconnect.model.calendar

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Firestore implementation of PersonalCalendarRepository. */
class PersonalCalendarRepositoryFirestore(private val firestore: FirebaseFirestore) :
    PersonalCalendarRepository {

  companion object {
    private const val COLLECTION_NAME = "personal_calendar_events"
  }

  private val collection
    get() = firestore.collection(COLLECTION_NAME)

  override suspend fun getEventsForUser(userId: String): List<PersonalCalendarEvent> {
    return try {
      collection
          .whereEqualTo("userId", userId)
          .get()
          .await()
          .documents
          .mapNotNull { doc -> doc.data?.let { PersonalCalendarEvent.fromMap(it) } }
          .sortedBy { it.start }
    } catch (e: Exception) {
      android.util.Log.e("PersonalCalendarRepo", "Error getting events for user: $userId", e)
      emptyList()
    }
  }

  override suspend fun getEventsInRange(
      userId: String,
      startTime: Timestamp,
      endTime: Timestamp
  ): List<PersonalCalendarEvent> {
    return try {
      collection
          .whereEqualTo("userId", userId)
          .get()
          .await()
          .documents
          .mapNotNull { doc -> doc.data?.let { PersonalCalendarEvent.fromMap(it) } }
          .filter { it.start >= startTime && it.start <= endTime }
          .sortedBy { it.start }
    } catch (e: Exception) {
      android.util.Log.e("PersonalCalendarRepo", "Error getting events in range", e)
      emptyList()
    }
  }

  override suspend fun addEvent(event: PersonalCalendarEvent) {
    try {
      collection.document(event.uid).set(event.toMap()).await()
    } catch (e: Exception) {
      android.util.Log.e("PersonalCalendarRepo", "Error adding event: ${event.uid}", e)
      throw e
    }
  }

  override suspend fun addEvents(events: List<PersonalCalendarEvent>) {
    try {
      val batch = firestore.batch()
      events.forEach { event -> batch.set(collection.document(event.uid), event.toMap()) }
      batch.commit().await()
    } catch (e: Exception) {
      android.util.Log.e("PersonalCalendarRepo", "Error adding events batch", e)
      throw e
    }
  }

  override suspend fun updateEvent(event: PersonalCalendarEvent) {
    try {
      collection.document(event.uid).set(event.toMap()).await()
    } catch (e: Exception) {
      android.util.Log.e("PersonalCalendarRepo", "Error updating event: ${event.uid}", e)
      throw e
    }
  }

  override suspend fun deleteEvent(eventId: String, userId: String) {
    try {
      collection.document(eventId).delete().await()
    } catch (e: Exception) {
      android.util.Log.e("PersonalCalendarRepo", "Error deleting event: $eventId", e)
      throw e
    }
  }

  override suspend fun deleteEventsBySource(userId: String, sourceCalendar: String) {
    try {
      val eventsToDelete =
          collection
              .whereEqualTo("userId", userId)
              .whereEqualTo("sourceCalendar", sourceCalendar)
              .get()
              .await()
              .documents

      val batch = firestore.batch()
      eventsToDelete.forEach { doc -> batch.delete(doc.reference) }
      batch.commit().await()
    } catch (e: Exception) {
      android.util.Log.e("PersonalCalendarRepo", "Error deleting events by source", e)
      throw e
    }
  }

  override suspend fun getNewUid(): String {
    return collection.document().id
  }

  override suspend fun getConflictingEvents(
      userId: String,
      startTime: Timestamp,
      endTime: Timestamp?
  ): List<PersonalCalendarEvent> {
    val effectiveEndTime = endTime ?: Timestamp(startTime.seconds + 3600, 0) // Default 1 hour

    return try {
      // Get all events for the user that might overlap
      val allEvents = getEventsForUser(userId)

      allEvents.filter { event ->
        val eventEnd = event.end ?: Timestamp(event.start.seconds + 3600, 0)

        // Check for overlap: event starts before our end AND event ends after our start
        event.start.seconds < effectiveEndTime.seconds && eventEnd.seconds > startTime.seconds
      }
    } catch (e: Exception) {
      android.util.Log.e("PersonalCalendarRepo", "Error getting conflicting events", e)
      emptyList()
    }
  }
}
