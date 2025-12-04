package com.github.se.studentconnect.model.user

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UserRepositoryLocalPinnedEventsTest {

  private lateinit var repository: UserRepositoryLocal

  private val testUser =
      User(
          userId = "user1",
          username = "johndoe",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          hobbies = listOf("reading", "coding"))

  @Before
  fun setup() {
    repository = UserRepositoryLocal()
  }

  @Test
  fun getPinnedEvents_returnsEmptyList_initially() = runTest {
    val result = repository.getPinnedEvents("user1")
    assertTrue(result.isEmpty())
  }

  @Test
  fun addPinnedEvent_addsEventToPinnedList() = runTest {
    repository.addPinnedEvent("user1", "event1")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(1, pinnedEvents.size)
    assertEquals("event1", pinnedEvents[0])
  }

  @Test
  fun addPinnedEvent_addsTwoEvents() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(2, pinnedEvents.size)
    assertTrue(pinnedEvents.contains("event1"))
    assertTrue(pinnedEvents.contains("event2"))
  }

  @Test
  fun addPinnedEvent_addsThreeEvents() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")
    repository.addPinnedEvent("user1", "event3")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(3, pinnedEvents.size)
    assertTrue(pinnedEvents.contains("event1"))
    assertTrue(pinnedEvents.contains("event2"))
    assertTrue(pinnedEvents.contains("event3"))
  }

  @Test
  fun addPinnedEvent_doesNotExceedMaximumOfThree() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")
    repository.addPinnedEvent("user1", "event3")
    repository.addPinnedEvent("user1", "event4") // Should not be added

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(3, pinnedEvents.size)
    assertFalse(pinnedEvents.contains("event4"))
  }

  @Test
  fun addPinnedEvent_doesNotAddDuplicate() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event1") // Duplicate

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(1, pinnedEvents.size)
  }

  @Test
  fun removePinnedEvent_removesEvent() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")

    repository.removePinnedEvent("user1", "event1")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(1, pinnedEvents.size)
    assertFalse(pinnedEvents.contains("event1"))
    assertTrue(pinnedEvents.contains("event2"))
  }

  @Test
  fun removePinnedEvent_removesAllEvents() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")
    repository.addPinnedEvent("user1", "event3")

    repository.removePinnedEvent("user1", "event1")
    repository.removePinnedEvent("user1", "event2")
    repository.removePinnedEvent("user1", "event3")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertTrue(pinnedEvents.isEmpty())
  }

  @Test
  fun removePinnedEvent_fromEmptyList_doesNotThrow() = runTest {
    repository.removePinnedEvent("user1", "event1")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertTrue(pinnedEvents.isEmpty())
  }

  @Test
  fun removePinnedEvent_nonExistentEvent_doesNotAffectOtherPinnedEvents() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")

    repository.removePinnedEvent("user1", "event3") // Non-existent

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(2, pinnedEvents.size)
    assertTrue(pinnedEvents.contains("event1"))
    assertTrue(pinnedEvents.contains("event2"))
  }

  @Test
  fun multipleUsers_canHaveDifferentPinnedEvents() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")

    repository.addPinnedEvent("user2", "event3")

    val user1Pinned = repository.getPinnedEvents("user1")
    val user2Pinned = repository.getPinnedEvents("user2")

    assertEquals(2, user1Pinned.size)
    assertTrue(user1Pinned.contains("event1"))
    assertTrue(user1Pinned.contains("event2"))

    assertEquals(1, user2Pinned.size)
    assertTrue(user2Pinned.contains("event3"))
  }

  @Test
  fun addPinnedEvent_afterRemovingOne_allowsAddingUpToThree() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")
    repository.addPinnedEvent("user1", "event3")

    repository.removePinnedEvent("user1", "event2")

    repository.addPinnedEvent("user1", "event4")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(3, pinnedEvents.size)
    assertTrue(pinnedEvents.contains("event1"))
    assertTrue(pinnedEvents.contains("event3"))
    assertTrue(pinnedEvents.contains("event4"))
    assertFalse(pinnedEvents.contains("event2"))
  }

  @Test
  fun deleteUser_alsoClearsPinnedEvents() = runTest {
    repository.saveUser(testUser)
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")

    repository.deleteUser("user1")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertTrue(pinnedEvents.isEmpty())
  }

  @Test
  fun getPinnedEvents_forNonExistentUser_returnsEmptyList() = runTest {
    val pinnedEvents = repository.getPinnedEvents("non-existent-user")
    assertTrue(pinnedEvents.isEmpty())
  }

  @Test
  fun pinnedEvents_areIndependentFromJoinedEvents() = runTest {
    repository.addEventToUser("event1", "user1")
    repository.addPinnedEvent("user1", "event2")

    val joinedEvents = repository.getJoinedEvents("user1")
    val pinnedEvents = repository.getPinnedEvents("user1")

    assertEquals(1, joinedEvents.size)
    assertEquals("event1", joinedEvents[0])

    assertEquals(1, pinnedEvents.size)
    assertEquals("event2", pinnedEvents[0])
  }

  @Test
  fun pinnedEvents_areIndependentFromFavoriteEvents() = runTest {
    repository.addFavoriteEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")

    val favoriteEvents = repository.getFavoriteEvents("user1")
    val pinnedEvents = repository.getPinnedEvents("user1")

    assertEquals(1, favoriteEvents.size)
    assertEquals("event1", favoriteEvents[0])

    assertEquals(1, pinnedEvents.size)
    assertEquals("event2", pinnedEvents[0])
  }

  @Test
  fun addPinnedEvent_sameEventCanBePinnedAndFavorited() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addFavoriteEvent("user1", "event1")

    val pinnedEvents = repository.getPinnedEvents("user1")
    val favoriteEvents = repository.getFavoriteEvents("user1")

    assertTrue(pinnedEvents.contains("event1"))
    assertTrue(favoriteEvents.contains("event1"))
  }

  @Test
  fun removePinnedEvent_allowsAddingNewEventImmediately() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")
    repository.addPinnedEvent("user1", "event3")

    // Remove one
    repository.removePinnedEvent("user1", "event1")

    // Add new one immediately
    repository.addPinnedEvent("user1", "event4")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(3, pinnedEvents.size)
    assertTrue(pinnedEvents.contains("event2"))
    assertTrue(pinnedEvents.contains("event3"))
    assertTrue(pinnedEvents.contains("event4"))
  }

  @Test
  fun pinnedEvents_maintainInsertionOrder() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event2")
    repository.addPinnedEvent("user1", "event3")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals("event1", pinnedEvents[0])
    assertEquals("event2", pinnedEvents[1])
    assertEquals("event3", pinnedEvents[2])
  }

  @Test
  fun addPinnedEvent_multipleTimes_doesNotDuplicate() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event1")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(1, pinnedEvents.size)
    assertEquals("event1", pinnedEvents[0])
  }

  @Test
  fun pinnedEvents_canBeRemovedAndReadded() = runTest {
    repository.addPinnedEvent("user1", "event1")
    repository.removePinnedEvent("user1", "event1")
    repository.addPinnedEvent("user1", "event1")

    val pinnedEvents = repository.getPinnedEvents("user1")
    assertEquals(1, pinnedEvents.size)
    assertEquals("event1", pinnedEvents[0])
  }
}
