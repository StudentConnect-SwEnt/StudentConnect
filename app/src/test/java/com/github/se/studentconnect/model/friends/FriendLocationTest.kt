package com.github.se.studentconnect.model.friends

import org.junit.Assert.*
import org.junit.Test

class FriendLocationTest {

  @Test
  fun constructor_setsValuesCorrectly() {
    val location = FriendLocation("user123", 46.5191, 6.5668, 1000L)
    assertEquals("user123", location.userId)
    assertEquals(46.5191, location.latitude, 0.0)
    assertEquals(6.5668, location.longitude, 0.0)
    assertEquals(1000L, location.timestamp)
  }

  @Test
  fun isFresh_returnsTrueForRecentTimestamps() {
    val currentTime = System.currentTimeMillis()
    assertTrue(FriendLocation("u", 0.0, 0.0, currentTime - 60_000L).isFresh(currentTime))
    assertTrue(
        FriendLocation("u", 0.0, 0.0, currentTime - FriendLocation.MAX_LOCATION_AGE_MS)
            .isFresh(currentTime))
  }

  @Test
  fun isFresh_returnsFalseForStaleTimestamps() {
    val currentTime = System.currentTimeMillis()
    assertFalse(FriendLocation("u", 0.0, 0.0, currentTime - 6 * 60 * 1000L).isFresh(currentTime))
    assertFalse(
        FriendLocation("u", 0.0, 0.0, currentTime - FriendLocation.MAX_LOCATION_AGE_MS - 1L)
            .isFresh(currentTime))
  }

  @Test
  fun dataClassFunctions_workCorrectly() {
    val loc1 = FriendLocation("user1", 46.5191, 6.5668, 1000L)
    val loc2 = FriendLocation("user1", 46.5191, 6.5668, 1000L)
    assertEquals(loc1, loc2)
    assertEquals(loc1.hashCode(), loc2.hashCode())
    assertEquals(46.5200, loc1.copy(latitude = 46.5200).latitude, 0.0)
  }
}
