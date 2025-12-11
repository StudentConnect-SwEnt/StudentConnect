package com.github.se.studentconnect.model.chat

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ChatMessageTest {

  @Test
  fun chatMessage_creation_withValidData_succeeds() {
    val timestamp = Timestamp.now()
    val message =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = "Hello World!",
            timestamp = timestamp)

    assertEquals("msg-123", message.messageId)
    assertEquals("event-456", message.eventId)
    assertEquals("user-789", message.senderId)
    assertEquals("John Doe", message.senderName)
    assertEquals("Hello World!", message.content)
    assertEquals(timestamp, message.timestamp)
  }

  @Test
  fun chatMessage_creation_withDefaultTimestamp_usesCurrentTime() {
    val message =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = "Hello World!")

    assertNotNull(message.timestamp)
  }

  @Test(expected = IllegalArgumentException::class)
  fun chatMessage_creation_withBlankMessageId_throwsException() {
    ChatMessage(
        messageId = "",
        eventId = "event-456",
        senderId = "user-789",
        senderName = "John Doe",
        content = "Hello World!")
  }

  @Test(expected = IllegalArgumentException::class)
  fun chatMessage_creation_withBlankEventId_throwsException() {
    ChatMessage(
        messageId = "msg-123",
        eventId = "",
        senderId = "user-789",
        senderName = "John Doe",
        content = "Hello World!")
  }

  @Test(expected = IllegalArgumentException::class)
  fun chatMessage_creation_withBlankSenderId_throwsException() {
    ChatMessage(
        messageId = "msg-123",
        eventId = "event-456",
        senderId = "",
        senderName = "John Doe",
        content = "Hello World!")
  }

  @Test(expected = IllegalArgumentException::class)
  fun chatMessage_creation_withBlankSenderName_throwsException() {
    ChatMessage(
        messageId = "msg-123",
        eventId = "event-456",
        senderId = "user-789",
        senderName = "",
        content = "Hello World!")
  }

  @Test(expected = IllegalArgumentException::class)
  fun chatMessage_creation_withBlankContent_throwsException() {
    ChatMessage(
        messageId = "msg-123",
        eventId = "event-456",
        senderId = "user-789",
        senderName = "John Doe",
        content = "")
  }

  @Test(expected = IllegalArgumentException::class)
  fun chatMessage_creation_withContentExceeding1000Characters_throwsException() {
    val longContent = "a".repeat(1001)
    ChatMessage(
        messageId = "msg-123",
        eventId = "event-456",
        senderId = "user-789",
        senderName = "John Doe",
        content = longContent)
  }

  @Test
  fun chatMessage_creation_withContentExactly1000Characters_succeeds() {
    val content = "a".repeat(1000)
    val message =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = content)

    assertEquals(1000, message.content.length)
  }

  @Test
  fun chatMessage_toMap_convertsAllFieldsCorrectly() {
    val timestamp = Timestamp.now()
    val message =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = "Hello World!",
            timestamp = timestamp)

    val map = message.toMap()

    assertEquals("msg-123", map["messageId"])
    assertEquals("event-456", map["eventId"])
    assertEquals("user-789", map["senderId"])
    assertEquals("John Doe", map["senderName"])
    assertEquals("Hello World!", map["content"])
    assertEquals(timestamp, map["timestamp"])
  }

  @Test
  fun chatMessage_fromMap_withValidData_createsMessage() {
    val timestamp = Timestamp.now()
    val map =
        mapOf(
            "messageId" to "msg-123",
            "eventId" to "event-456",
            "senderId" to "user-789",
            "senderName" to "John Doe",
            "content" to "Hello World!",
            "timestamp" to timestamp)

    val message = ChatMessage.fromMap(map)

    assertNotNull(message)
    assertEquals("msg-123", message?.messageId)
    assertEquals("event-456", message?.eventId)
    assertEquals("user-789", message?.senderId)
    assertEquals("John Doe", message?.senderName)
    assertEquals("Hello World!", message?.content)
    assertEquals(timestamp, message?.timestamp)
  }

  @Test
  fun chatMessage_fromMap_withMissingTimestamp_usesDefaultTimestamp() {
    val map =
        mapOf(
            "messageId" to "msg-123",
            "eventId" to "event-456",
            "senderId" to "user-789",
            "senderName" to "John Doe",
            "content" to "Hello World!")

    val message = ChatMessage.fromMap(map)

    assertNotNull(message)
    assertNotNull(message?.timestamp)
  }

  @Test
  fun chatMessage_fromMap_withMissingMessageId_returnsNull() {
    val map =
        mapOf(
            "eventId" to "event-456",
            "senderId" to "user-789",
            "senderName" to "John Doe",
            "content" to "Hello World!")

    val message = ChatMessage.fromMap(map)

    assertNull(message)
  }

  @Test
  fun chatMessage_fromMap_withMissingEventId_returnsNull() {
    val map =
        mapOf(
            "messageId" to "msg-123",
            "senderId" to "user-789",
            "senderName" to "John Doe",
            "content" to "Hello World!")

    val message = ChatMessage.fromMap(map)

    assertNull(message)
  }

  @Test
  fun chatMessage_fromMap_withMissingSenderId_returnsNull() {
    val map =
        mapOf(
            "messageId" to "msg-123",
            "eventId" to "event-456",
            "senderName" to "John Doe",
            "content" to "Hello World!")

    val message = ChatMessage.fromMap(map)

    assertNull(message)
  }

  @Test
  fun chatMessage_fromMap_withMissingSenderName_returnsNull() {
    val map =
        mapOf(
            "messageId" to "msg-123",
            "eventId" to "event-456",
            "senderId" to "user-789",
            "content" to "Hello World!")

    val message = ChatMessage.fromMap(map)

    assertNull(message)
  }

  @Test
  fun chatMessage_fromMap_withMissingContent_returnsNull() {
    val map =
        mapOf(
            "messageId" to "msg-123",
            "eventId" to "event-456",
            "senderId" to "user-789",
            "senderName" to "John Doe")

    val message = ChatMessage.fromMap(map)

    assertNull(message)
  }

  @Test
  fun chatMessage_fromMap_withInvalidDataType_returnsNull() {
    val map =
        mapOf(
            "messageId" to 123, // Should be String
            "eventId" to "event-456",
            "senderId" to "user-789",
            "senderName" to "John Doe",
            "content" to "Hello World!")

    val message = ChatMessage.fromMap(map)

    assertNull(message)
  }

  @Test
  fun chatMessage_fromMap_withBlankContent_returnsNull() {
    val timestamp = Timestamp.now()
    val map =
        mapOf(
            "messageId" to "msg-123",
            "eventId" to "event-456",
            "senderId" to "user-789",
            "senderName" to "John Doe",
            "content" to "",
            "timestamp" to timestamp)

    val message = ChatMessage.fromMap(map)

    assertNull(message)
  }

  @Test
  fun chatMessage_fromMap_withContentExceeding1000Characters_returnsNull() {
    val longContent = "a".repeat(1001)
    val map =
        mapOf(
            "messageId" to "msg-123",
            "eventId" to "event-456",
            "senderId" to "user-789",
            "senderName" to "John Doe",
            "content" to longContent)

    val message = ChatMessage.fromMap(map)

    assertNull(message)
  }

  @Test
  fun chatMessage_roundTrip_toMapAndFromMap_preservesData() {
    val timestamp = Timestamp.now()
    val original =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = "Hello World!",
            timestamp = timestamp)

    val map = original.toMap()
    val reconstructed = ChatMessage.fromMap(map)

    assertNotNull(reconstructed)
    assertEquals(original.messageId, reconstructed?.messageId)
    assertEquals(original.eventId, reconstructed?.eventId)
    assertEquals(original.senderId, reconstructed?.senderId)
    assertEquals(original.senderName, reconstructed?.senderName)
    assertEquals(original.content, reconstructed?.content)
    assertEquals(original.timestamp, reconstructed?.timestamp)
  }
}
