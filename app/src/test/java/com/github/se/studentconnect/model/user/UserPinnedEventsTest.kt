package com.github.se.studentconnect.model.user

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class UserPinnedEventsTest {

  private val baseUser =
      User(
          userId = "user123",
          email = "test@example.com",
          username = "testuser",
          firstName = "Test",
          lastName = "User",
          university = "EPFL",
          hobbies = listOf("coding", "reading"))

  @Test
  fun user_canBeCreatedWithEmptyPinnedEvents() {
    val user = baseUser.copy(pinnedEventIds = emptyList())
    assertEquals(0, user.pinnedEventIds.size)
  }

  @Test
  fun user_canBeCreatedWithOnePinnedEvent() {
    val user = baseUser.copy(pinnedEventIds = listOf("event1"))
    assertEquals(1, user.pinnedEventIds.size)
    assertEquals("event1", user.pinnedEventIds[0])
  }

  @Test
  fun user_canBeCreatedWithTwoPinnedEvents() {
    val user = baseUser.copy(pinnedEventIds = listOf("event1", "event2"))
    assertEquals(2, user.pinnedEventIds.size)
    assertTrue(user.pinnedEventIds.contains("event1"))
    assertTrue(user.pinnedEventIds.contains("event2"))
  }

  @Test
  fun user_canBeCreatedWithThreePinnedEvents() {
    val user = baseUser.copy(pinnedEventIds = listOf("event1", "event2", "event3"))
    assertEquals(3, user.pinnedEventIds.size)
    assertTrue(user.pinnedEventIds.contains("event1"))
    assertTrue(user.pinnedEventIds.contains("event2"))
    assertTrue(user.pinnedEventIds.contains("event3"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun user_throwsException_whenPinnedEventsExceedLimit() {
    baseUser.copy(pinnedEventIds = listOf("event1", "event2", "event3", "event4"))
  }

  @Test
  fun user_toMap_includesPinnedEventIds() {
    val user = baseUser.copy(pinnedEventIds = listOf("event1", "event2"))
    val map = user.toMap()

    assertNotNull(map["pinnedEventIds"])
    val pinnedIds = map["pinnedEventIds"] as? List<*>
    assertNotNull(pinnedIds)
    assertEquals(2, pinnedIds?.size)
    assertTrue(pinnedIds?.contains("event1") == true)
    assertTrue(pinnedIds?.contains("event2") == true)
  }

  @Test
  fun user_toMap_includesEmptyPinnedEventIds() {
    val user = baseUser.copy(pinnedEventIds = emptyList())
    val map = user.toMap()

    assertNotNull(map["pinnedEventIds"])
    val pinnedIds = map["pinnedEventIds"] as? List<*>
    assertNotNull(pinnedIds)
    assertTrue(pinnedIds?.isEmpty() == true)
  }

  @Test
  fun user_fromMap_deserializesPinnedEventIds() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@example.com",
            "username" to "testuser",
            "firstName" to "Test",
            "lastName" to "User",
            "university" to "EPFL",
            "hobbies" to listOf("coding"),
            "pinnedEventIds" to listOf("event1", "event2"),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis())

    val user = User.fromMap(map)

    assertNotNull(user)
    assertEquals(2, user?.pinnedEventIds?.size)
    assertTrue(user?.pinnedEventIds?.contains("event1") == true)
    assertTrue(user?.pinnedEventIds?.contains("event2") == true)
  }

  @Test
  fun user_fromMap_handlesEmptyPinnedEventIds() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@example.com",
            "username" to "testuser",
            "firstName" to "Test",
            "lastName" to "User",
            "university" to "EPFL",
            "hobbies" to listOf("coding"),
            "pinnedEventIds" to emptyList<String>(),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis())

    val user = User.fromMap(map)

    assertNotNull(user)
    assertTrue(user?.pinnedEventIds?.isEmpty() == true)
  }

  @Test
  fun user_fromMap_handlesMissingPinnedEventIds() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@example.com",
            "username" to "testuser",
            "firstName" to "Test",
            "lastName" to "User",
            "university" to "EPFL",
            "hobbies" to listOf("coding"),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis())

    val user = User.fromMap(map)

    assertNotNull(user)
    assertTrue(user?.pinnedEventIds?.isEmpty() == true)
  }

  @Test
  fun user_fromMap_filtersNonStringPinnedEventIds() {
    val map =
        mapOf(
            "userId" to "user123",
            "email" to "test@example.com",
            "username" to "testuser",
            "firstName" to "Test",
            "lastName" to "User",
            "university" to "EPFL",
            "hobbies" to listOf("coding"),
            "pinnedEventIds" to listOf("event1", 123, "event2", null, "event3"),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis())

    val user = User.fromMap(map)

    assertNotNull(user)
    assertEquals(3, user?.pinnedEventIds?.size)
    assertTrue(user?.pinnedEventIds?.contains("event1") == true)
    assertTrue(user?.pinnedEventIds?.contains("event2") == true)
    assertTrue(user?.pinnedEventIds?.contains("event3") == true)
  }

  @Test
  fun user_update_canUpdatePinnedEventIds() {
    val user = baseUser.copy(pinnedEventIds = listOf("event1"))

    val updatedUser =
        user.update(
            pinnedEventIds = User.UpdateValue.SetValue(listOf("event1", "event2", "event3")))

    assertEquals(3, updatedUser.pinnedEventIds.size)
    assertTrue(updatedUser.pinnedEventIds.contains("event1"))
    assertTrue(updatedUser.pinnedEventIds.contains("event2"))
    assertTrue(updatedUser.pinnedEventIds.contains("event3"))
  }

  @Test
  fun user_update_canClearPinnedEventIds() {
    val user = baseUser.copy(pinnedEventIds = listOf("event1", "event2"))

    val updatedUser = user.update(pinnedEventIds = User.UpdateValue.SetValue(emptyList()))

    assertTrue(updatedUser.pinnedEventIds.isEmpty())
  }

  @Test
  fun user_update_preservesPinnedEventIdsWithNoChange() {
    val user = baseUser.copy(pinnedEventIds = listOf("event1", "event2"))

    val updatedUser = user.update(firstName = User.UpdateValue.SetValue("Updated"))

    assertEquals(2, updatedUser.pinnedEventIds.size)
    assertTrue(updatedUser.pinnedEventIds.contains("event1"))
    assertTrue(updatedUser.pinnedEventIds.contains("event2"))
    assertEquals("Updated", updatedUser.firstName)
  }

  @Test(expected = IllegalArgumentException::class)
  fun user_update_throwsException_whenUpdatingToMoreThanThreePinnedEvents() {
    val user = baseUser.copy(pinnedEventIds = emptyList())

    user.update(
        pinnedEventIds =
            User.UpdateValue.SetValue(listOf("event1", "event2", "event3", "event4")))
  }

  @Test
  fun user_roundTripSerialization_preservesPinnedEventIds() {
    val originalUser = baseUser.copy(pinnedEventIds = listOf("event1", "event2", "event3"))

    val map = originalUser.toMap()
    val deserializedUser = User.fromMap(map)

    assertNotNull(deserializedUser)
    assertEquals(originalUser.pinnedEventIds, deserializedUser?.pinnedEventIds)
    assertEquals(3, deserializedUser?.pinnedEventIds?.size)
  }
}
