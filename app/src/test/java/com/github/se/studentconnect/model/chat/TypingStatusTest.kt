package com.github.se.studentconnect.model.chat

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TypingStatusTest {

  @Test
  fun typingStatus_creation_withValidData_succeeds() {
    val timestamp = Timestamp.now()
    val status =
        TypingStatus(
            userId = "user-123",
            userName = "Jane Doe",
            eventId = "event-456",
            isTyping = true,
            lastUpdate = timestamp)

    assertEquals("user-123", status.userId)
    assertEquals("Jane Doe", status.userName)
    assertEquals("event-456", status.eventId)
    assertTrue(status.isTyping)
    assertEquals(timestamp, status.lastUpdate)
  }

  @Test
  fun typingStatus_creation_withDefaultTimestamp_usesCurrentTime() {
    val status =
        TypingStatus(
            userId = "user-123", userName = "Jane Doe", eventId = "event-456", isTyping = true)

    assertNotNull(status.lastUpdate)
  }

  @Test
  fun typingStatus_creation_withIsTypingFalse_succeeds() {
    val status =
        TypingStatus(
            userId = "user-123", userName = "Jane Doe", eventId = "event-456", isTyping = false)

    assertFalse(status.isTyping)
  }

  @Test
  fun typingStatus_toMap_convertsAllFieldsCorrectly() {
    val timestamp = Timestamp.now()
    val status =
        TypingStatus(
            userId = "user-123",
            userName = "Jane Doe",
            eventId = "event-456",
            isTyping = true,
            lastUpdate = timestamp)

    val map = status.toMap()

    assertEquals("user-123", map["userId"])
    assertEquals("Jane Doe", map["userName"])
    assertEquals("event-456", map["eventId"])
    assertEquals(true, map["isTyping"])
    assertEquals(timestamp, map["lastUpdate"])
  }

  @Test
  fun typingStatus_toMap_withIsTypingFalse_convertsCorrectly() {
    val timestamp = Timestamp.now()
    val status =
        TypingStatus(
            userId = "user-123",
            userName = "Jane Doe",
            eventId = "event-456",
            isTyping = false,
            lastUpdate = timestamp)

    val map = status.toMap()

    assertEquals(false, map["isTyping"])
  }

  @Test
  fun typingStatus_fromMap_withValidData_createsStatus() {
    val timestamp = Timestamp.now()
    val map =
        mapOf(
            "userId" to "user-123",
            "userName" to "Jane Doe",
            "eventId" to "event-456",
            "isTyping" to true,
            "lastUpdate" to timestamp)

    val status = TypingStatus.fromMap(map)

    assertNotNull(status)
    assertEquals("user-123", status?.userId)
    assertEquals("Jane Doe", status?.userName)
    assertEquals("event-456", status?.eventId)
    assertTrue(status?.isTyping == true)
    assertEquals(timestamp, status?.lastUpdate)
  }

  @Test
  fun typingStatus_fromMap_withMissingIsTyping_defaultsToFalse() {
    val timestamp = Timestamp.now()
    val map =
        mapOf(
            "userId" to "user-123",
            "userName" to "Jane Doe",
            "eventId" to "event-456",
            "lastUpdate" to timestamp)

    val status = TypingStatus.fromMap(map)

    assertNotNull(status)
    assertFalse(status?.isTyping == true)
  }

  @Test
  fun typingStatus_fromMap_withMissingLastUpdate_usesDefaultTimestamp() {
    val map =
        mapOf(
            "userId" to "user-123",
            "userName" to "Jane Doe",
            "eventId" to "event-456",
            "isTyping" to true)

    val status = TypingStatus.fromMap(map)

    assertNotNull(status)
    assertNotNull(status?.lastUpdate)
  }

  @Test
  fun typingStatus_fromMap_withMissingUserId_returnsNull() {
    val map = mapOf("userName" to "Jane Doe", "eventId" to "event-456", "isTyping" to true)

    val status = TypingStatus.fromMap(map)

    assertNull(status)
  }

  @Test
  fun typingStatus_fromMap_withMissingUserName_returnsNull() {
    val map = mapOf("userId" to "user-123", "eventId" to "event-456", "isTyping" to true)

    val status = TypingStatus.fromMap(map)

    assertNull(status)
  }

  @Test
  fun typingStatus_fromMap_withMissingEventId_returnsNull() {
    val map = mapOf("userId" to "user-123", "userName" to "Jane Doe", "isTyping" to true)

    val status = TypingStatus.fromMap(map)

    assertNull(status)
  }

  @Test
  fun typingStatus_fromMap_withInvalidDataType_returnsNull() {
    val map =
        mapOf(
            "userId" to 123, // Should be String
            "userName" to "Jane Doe",
            "eventId" to "event-456",
            "isTyping" to true)

    val status = TypingStatus.fromMap(map)

    assertNull(status)
  }

  @Test
  fun typingStatus_fromMap_withInvalidIsTypingType_returnsNull() {
    val map =
        mapOf(
            "userId" to "user-123",
            "userName" to "Jane Doe",
            "eventId" to "event-456",
            "isTyping" to "yes") // Should be Boolean

    val status = TypingStatus.fromMap(map)

    // Should still create the status with isTyping defaulting to false
    // because of the safe cast "as? Boolean ?: false"
    assertNotNull(status)
    assertFalse(status?.isTyping == true)
  }

  @Test
  fun typingStatus_roundTrip_toMapAndFromMap_preservesData() {
    val timestamp = Timestamp.now()
    val original =
        TypingStatus(
            userId = "user-123",
            userName = "Jane Doe",
            eventId = "event-456",
            isTyping = true,
            lastUpdate = timestamp)

    val map = original.toMap()
    val reconstructed = TypingStatus.fromMap(map)

    assertNotNull(reconstructed)
    assertEquals(original.userId, reconstructed?.userId)
    assertEquals(original.userName, reconstructed?.userName)
    assertEquals(original.eventId, reconstructed?.eventId)
    assertEquals(original.isTyping, reconstructed?.isTyping)
    assertEquals(original.lastUpdate, reconstructed?.lastUpdate)
  }

  @Test
  fun typingStatus_fromMap_withEmptyMap_returnsNull() {
    val status = TypingStatus.fromMap(emptyMap())

    assertNull(status)
  }

  @Test
  fun typingStatus_fromMap_withNullValues_returnsNull() {
    val map =
        mapOf(
            "userId" to null,
            "userName" to "Jane Doe",
            "eventId" to "event-456",
            "isTyping" to true)

    val status = TypingStatus.fromMap(map)

    assertNull(status)
  }

  @Test
  fun typingStatus_fromMap_withInvalidTimestamp_usesDefaultTimestamp() {
    val map =
        mapOf(
            "userId" to "user-123",
            "userName" to "Jane Doe",
            "eventId" to "event-456",
            "isTyping" to true,
            "lastUpdate" to "not-a-timestamp") // Invalid timestamp

    val status = TypingStatus.fromMap(map)

    // Should use default timestamp when cast fails
    assertNotNull(status)
    assertNotNull(status?.lastUpdate)
  }
}
