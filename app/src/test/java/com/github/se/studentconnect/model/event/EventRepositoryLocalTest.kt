package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class EventRepositoryLocalTest {

  private lateinit var eventRepository: EventRepositoryLocal

  private val testEvent =
      Event.Public(
          uid = "event1",
          ownerId = "owner1",
          title = "Test Event",
          description = "This is a test event.",
          location = Location(46.5191, 6.5668, "EPFL"),
          start = Timestamp.now(),
          isFlash = false,
          subtitle = "A subtitle for testing")

  private val testParticipant = EventParticipant(uid = "user1", joinedAt = Timestamp.now())

  @Before
  fun setUp() {
    eventRepository = EventRepositoryLocal()
  }

  @Test
  fun getNewUid_generatesUniqueNonEmptyIds() {
    val uid1 = eventRepository.getNewUid()
    assertTrue(uid1.isNotEmpty())
    val uid2 = eventRepository.getNewUid()
    assertTrue(uid2.isNotEmpty())
    assert(uid1 != uid2)
  }

  @Test
  fun addAndGetEvent_succeeds() = runTest {
    eventRepository.addEvent(testEvent)
    val allEvents = eventRepository.getAllVisibleEvents()
    assertEquals(1, allEvents.size)
    assertTrue(allEvents.contains(testEvent))
    val retrievedEvent = eventRepository.getEvent("event1")
    assertEquals(testEvent, retrievedEvent)
  }

  @Test
  fun getEvent_throwsErrorWhenNotFound() {
    assertThrows(NoSuchElementException::class.java) {
      runTest { eventRepository.getEvent("non-existent-id") }
    }
  }

  @Test
  fun editEvent_succeeds() = runTest {
    eventRepository.addEvent(testEvent)
    val updatedEvent = testEvent.copy(title = "Updated Event Title")
    eventRepository.editEvent("event1", updatedEvent)
    val retrievedEvent = eventRepository.getEvent("event1")
    assertEquals("Updated Event Title", retrievedEvent.title)
    val allEvents = eventRepository.getAllVisibleEvents()
    assertEquals(1, allEvents.size)
    assertFalse(allEvents.contains(testEvent))
    assertTrue(allEvents.contains(updatedEvent))
  }

  @Test
  fun deleteEvent_succeeds() = runTest {
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent("event1", testParticipant)
    assertEquals(1, eventRepository.getAllVisibleEvents().size)
    assertEquals(1, eventRepository.getEventParticipants("event1").size)
    eventRepository.deleteEvent("event1")
    assertEquals(0, eventRepository.getAllVisibleEvents().size)
    assertEquals(0, eventRepository.getEventParticipants("event1").size)
  }

  @Test
  fun deleteEvent_deletesTheCorrectEvent() = runTest {
    val event2 = testEvent.copy(uid = "event2", title = "Second Event")
    eventRepository.addEvent(testEvent)
    eventRepository.addEvent(event2)
    eventRepository.deleteEvent("event1")
    val allEvents = eventRepository.getAllVisibleEvents()
    assertEquals(1, allEvents.size)
    assertFalse(allEvents.any { it.uid == "event1" })
    assertTrue(allEvents.any { it.uid == "event2" })
  }

  @Test
  fun deleteEvent_throwsErrorWhenNotFound() {
    assertThrows(NoSuchElementException::class.java) {
      runTest { eventRepository.deleteEvent("non-existent-id") }
    }
  }

  @Test
  fun getAllVisibleEventsSatisfying_returnsCorrectlyFilteredEvents() = runTest {
    val flashEvent = testEvent.copy(uid = "event2", isFlash = true)
    eventRepository.addEvent(testEvent)
    eventRepository.addEvent(flashEvent)
    val flashEvents = eventRepository.getAllVisibleEventsSatisfying { it.isFlash }
    assertEquals(1, flashEvents.size)
    assertEquals("event2", flashEvents[0].uid)
    val nonFlashEvents = eventRepository.getAllVisibleEventsSatisfying { !it.isFlash }
    assertEquals(1, nonFlashEvents.size)
    assertEquals("event1", nonFlashEvents[0].uid)
  }

  @Test
  fun addAndGetParticipants_succeeds() = runTest {
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent("event1", testParticipant)
    val participants = eventRepository.getEventParticipants("event1")
    assertEquals(1, participants.size)
    assertEquals("user1", participants[0].uid)
  }

  @Test
  fun addParticipant_throwsErrorWhenEventNotFound() {
    assertThrows(IllegalArgumentException::class.java) {
      runTest { eventRepository.addParticipantToEvent("non-existent-event", testParticipant) }
    }
  }

  @Test
  fun addParticipant_throwsErrorOnDuplicate() = runTest {
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent("event1", testParticipant)
    assertThrows(IllegalStateException::class.java) {
      runTest { eventRepository.addParticipantToEvent("event1", testParticipant) }
    }
  }

  @Test
  fun removeParticipant_succeeds() = runTest {
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent("event1", testParticipant)
    assertEquals(1, eventRepository.getEventParticipants("event1").size)
    eventRepository.removeParticipantFromEvent("event1", "user1")
    assertEquals(0, eventRepository.getEventParticipants("event1").size)
  }

  @Test
  fun getEventsAttendedByUser_returnsAllEvents_asPerCurrentImplementation() = runTest {
    val event2 = testEvent.copy(uid = "event2")
    eventRepository.addEvent(testEvent)
    eventRepository.addEvent(event2)
    eventRepository.addParticipantToEvent("event1", testParticipant)
    val eventsForUser = eventRepository.getEventsAttendedByUser(testParticipant.uid)
    assertEquals(2, eventsForUser.size)
    assertTrue(eventsForUser.contains(testEvent))
    assertTrue(eventsForUser.contains(event2))
  }
}
