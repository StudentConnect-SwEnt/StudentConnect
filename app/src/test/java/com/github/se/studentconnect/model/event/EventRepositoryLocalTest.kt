package com.github.se.studentconnect.model.event

import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepositoryLocalTest {

  private lateinit var repository: EventRepositoryLocal

  private fun createTestPublicEvent(uid: String, ownerId: String = "owner1"): Event.Public {
    return Event.Public(
        uid = uid,
        ownerId = ownerId,
        title = "Test Event $uid",
        subtitle = "Subtitle",
        description = "Description",
        start = Timestamp.now(),
        end = Timestamp.now(),
        isFlash = false)
  }

  private fun createTestPrivateEvent(uid: String, ownerId: String = "owner1"): Event.Private {
    return Event.Private(
        uid = uid,
        ownerId = ownerId,
        title = "Private Event $uid",
        description = "Description",
        start = Timestamp.now(),
        end = Timestamp.now(),
        isFlash = false)
  }

  @Before
  fun setUp() {
    repository = EventRepositoryLocal()
  }

  @Test
  fun `getNewUid returns unique identifiers`() {
    val uid1 = repository.getNewUid()
    val uid2 = repository.getNewUid()

    assertNotNull(uid1)
    assertNotNull(uid2)
    assertTrue(uid1 != uid2)
  }

  @Test
  fun `addEvent adds event to repository`() = runTest {
    val event = createTestPublicEvent("event1")

    repository.addEvent(event)

    val retrievedEvent = repository.getEvent("event1")
    assertEquals(event.uid, retrievedEvent.uid)
    assertEquals(event.title, retrievedEvent.title)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `addEvent throws exception for duplicate uid`() = runTest {
    val event1 = createTestPublicEvent("event1")
    val event2 = createTestPublicEvent("event1")

    repository.addEvent(event1)
    repository.addEvent(event2)
  }

  @Test
  fun `getAllVisibleEvents returns all events`() = runTest {
    val event1 = createTestPublicEvent("event1")
    val event2 = createTestPublicEvent("event2")

    repository.addEvent(event1)
    repository.addEvent(event2)

    val events = repository.getAllVisibleEvents()
    assertEquals(2, events.size)
  }

  @Test
  fun `getAllVisibleEventsSatisfying returns filtered events`() = runTest {
    val event1 = createTestPublicEvent("event1", "owner1")
    val event2 = createTestPublicEvent("event2", "owner2")

    repository.addEvent(event1)
    repository.addEvent(event2)

    val filteredEvents = repository.getAllVisibleEventsSatisfying { it.ownerId == "owner1" }
    assertEquals(1, filteredEvents.size)
    assertEquals("event1", filteredEvents[0].uid)
  }

  @Test
  fun `getEventsByOrganization returns events by organization`() = runTest {
    val event1 = createTestPublicEvent("event1", "owner1").copy(organizationId = "org1")
    val event2 = createTestPublicEvent("event2", "owner2").copy(organizationId = "org2")
    val event3 = createTestPublicEvent("event3", "owner3").copy(organizationId = "org1")

    repository.addEvent(event1)
    repository.addEvent(event2)
    repository.addEvent(event3)

    val orgEvents = repository.getEventsByOrganization("org1")
    assertEquals(2, orgEvents.size)
    assertTrue(orgEvents.any { it.uid == "event1" })
    assertTrue(orgEvents.any { it.uid == "event3" })
  }

  @Test
  fun `getEventsByOwner returns events by owner`() = runTest {
    val event1 = createTestPublicEvent("event1", "user1")
    val event2 = createTestPublicEvent("event2", "user2")

    repository.addEvent(event1)
    repository.addEvent(event2)

    val userEvents = repository.getEventsByOwner("user1")
    assertEquals(1, userEvents.size)
    assertEquals("event1", userEvents[0].uid)
  }

  @Test
  fun `getEvent returns correct event`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    val retrieved = repository.getEvent("event1")
    assertEquals(event.uid, retrieved.uid)
    assertEquals(event.title, retrieved.title)
  }

  @Test(expected = NoSuchElementException::class)
  fun `getEvent throws exception for non-existent event`() = runTest {
    repository.getEvent("non-existent")
  }

  @Test
  fun `editEvent updates existing event`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    val updatedEvent =
        Event.Public(
            uid = "event1",
            ownerId = "owner1",
            title = "Updated Title",
            subtitle = "Updated Subtitle",
            description = "Updated Description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            isFlash = true)

    repository.editEvent("event1", updatedEvent)

    val retrieved = repository.getEvent("event1") as Event.Public
    assertEquals("Updated Title", retrieved.title)
    assertEquals("Updated Subtitle", retrieved.subtitle)
    assertTrue(retrieved.isFlash)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `editEvent throws exception for uid mismatch`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    val updatedEvent = createTestPublicEvent("event2")
    repository.editEvent("event1", updatedEvent)
  }

  @Test(expected = NoSuchElementException::class)
  fun `editEvent throws exception for non-existent event`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.editEvent("event1", event)
  }

  @Test
  fun `deleteEvent removes event from repository`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    repository.deleteEvent("event1")

    val events = repository.getAllVisibleEvents()
    assertTrue(events.isEmpty())
  }

  @Test(expected = NoSuchElementException::class)
  fun `deleteEvent throws exception for non-existent event`() = runTest {
    repository.deleteEvent("non-existent")
  }

  @Test
  fun `addParticipantToEvent adds participant`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    val participant = EventParticipant(uid = "user1", joinedAt = Timestamp.now())
    repository.addParticipantToEvent("event1", participant)

    val participants = repository.getEventParticipants("event1")
    assertEquals(1, participants.size)
    assertEquals("user1", participants[0].uid)
  }

  @Test(expected = IllegalStateException::class)
  fun `addParticipantToEvent throws exception for duplicate participant`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    val participant = EventParticipant(uid = "user1", joinedAt = Timestamp.now())
    repository.addParticipantToEvent("event1", participant)
    repository.addParticipantToEvent("event1", participant)
  }

  @Test
  fun `getEventParticipants returns empty list for event without participants`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    val participants = repository.getEventParticipants("event1")
    assertTrue(participants.isEmpty())
  }

  @Test
  fun `removeParticipantFromEvent removes participant`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    val participant = EventParticipant(uid = "user1", joinedAt = Timestamp.now())
    repository.addParticipantToEvent("event1", participant)
    repository.removeParticipantFromEvent("event1", "user1")

    val participants = repository.getEventParticipants("event1")
    assertTrue(participants.isEmpty())
  }

  @Test(expected = NoSuchElementException::class)
  fun `removeParticipantFromEvent throws exception for non-existent participant`() = runTest {
    val event = createTestPublicEvent("event1")
    repository.addEvent(event)

    repository.removeParticipantFromEvent("event1", "non-existent-user")
  }

  @Test
  fun `private event can be added and retrieved`() = runTest {
    val event = createTestPrivateEvent("private1")
    repository.addEvent(event)

    val retrieved = repository.getEvent("private1")
    assertTrue(retrieved is Event.Private)
    assertEquals("Private Event private1", retrieved.title)
  }
}
