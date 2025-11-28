package com.github.se.studentconnect.model.event

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Tests for the default implementations in the EventRepository interface.
 *
 * These tests verify that the default implementation of getEventsByOrganization returns an empty
 * list.
 */
class EventRepositoryInterfaceTest {

  @Test
  fun `default getEventsByOrganization returns empty list`() = runTest {
    // Create a minimal implementation that uses the default getEventsByOrganization
    val repository =
        object : EventRepository {
          override fun getNewUid(): String = "test-uid"

          override suspend fun getAllVisibleEvents(): List<Event> = emptyList()

          override suspend fun getAllVisibleEventsSatisfying(
              predicate: (Event) -> Boolean
          ): List<Event> = emptyList()

          // Using default implementation for getEventsByOrganization

          override suspend fun getEvent(eventUid: String): Event {
            throw NotImplementedError("Not needed for this test")
          }

          override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> =
              emptyList()

          override suspend fun addEvent(event: Event) {}

          override suspend fun editEvent(eventUid: String, newEvent: Event) {}

          override suspend fun deleteEvent(eventUid: String) {}

          override suspend fun addParticipantToEvent(
              eventUid: String,
              participant: EventParticipant
          ) {}

          override suspend fun addInvitationToEvent(
              eventUid: String,
              invitedUser: String,
              currentUserId: String
          ) {}

          override suspend fun removeParticipantFromEvent(
              eventUid: String,
              participantUid: String
          ) {}
        }

    val result = repository.getEventsByOrganization("org123")
    assertTrue(result.isEmpty())
    assertEquals(0, result.size)
  }

  @Test
  fun `default getEventsByOrganization works with different organization ids`() = runTest {
    val repository =
        object : EventRepository {
          override fun getNewUid(): String = "test-uid"

          override suspend fun getAllVisibleEvents(): List<Event> = emptyList()

          override suspend fun getAllVisibleEventsSatisfying(
              predicate: (Event) -> Boolean
          ): List<Event> = emptyList()

          // Using default implementation for getEventsByOrganization

          override suspend fun getEvent(eventUid: String): Event {
            throw NotImplementedError("Not needed for this test")
          }

          override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> =
              emptyList()

          override suspend fun addEvent(event: Event) {}

          override suspend fun editEvent(eventUid: String, newEvent: Event) {}

          override suspend fun deleteEvent(eventUid: String) {}

          override suspend fun addParticipantToEvent(
              eventUid: String,
              participant: EventParticipant
          ) {}

          override suspend fun addInvitationToEvent(
              eventUid: String,
              invitedUser: String,
              currentUserId: String
          ) {}

          override suspend fun removeParticipantFromEvent(
              eventUid: String,
              participantUid: String
          ) {}
        }

    // Test with different organization IDs - all should return empty list
    assertTrue(repository.getEventsByOrganization("org1").isEmpty())
    assertTrue(repository.getEventsByOrganization("org2").isEmpty())
    assertTrue(repository.getEventsByOrganization("org_xyz").isEmpty())
    assertTrue(repository.getEventsByOrganization("").isEmpty())
  }

  @Test
  fun `default getEventsByOrganization does not throw exceptions`() = runTest {
    val repository =
        object : EventRepository {
          override fun getNewUid(): String = "test-uid"

          override suspend fun getAllVisibleEvents(): List<Event> = emptyList()

          override suspend fun getAllVisibleEventsSatisfying(
              predicate: (Event) -> Boolean
          ): List<Event> = emptyList()

          // Using default implementation for getEventsByOrganization

          override suspend fun getEvent(eventUid: String): Event {
            throw NotImplementedError("Not needed for this test")
          }

          override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> =
              emptyList()

          override suspend fun addEvent(event: Event) {}

          override suspend fun editEvent(eventUid: String, newEvent: Event) {}

          override suspend fun deleteEvent(eventUid: String) {}

          override suspend fun addParticipantToEvent(
              eventUid: String,
              participant: EventParticipant
          ) {}

          override suspend fun addInvitationToEvent(
              eventUid: String,
              invitedUser: String,
              currentUserId: String
          ) {}

          override suspend fun removeParticipantFromEvent(
              eventUid: String,
              participantUid: String
          ) {}
        }

    // Should not throw any exceptions
    var result = repository.getEventsByOrganization("org1")
    assertTrue(result.isEmpty())

    result = repository.getEventsByOrganization("org2")
    assertTrue(result.isEmpty())
  }
}
