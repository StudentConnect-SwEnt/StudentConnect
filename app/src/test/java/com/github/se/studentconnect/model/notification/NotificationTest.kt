package com.github.se.studentconnect.model.notification

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTest {

  @Test
  fun friendRequestNotification_hasCorrectType() {
    val notification =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    assertEquals(NotificationType.FRIEND_REQUEST, notification.type)
  }

  @Test
  fun friendRequestNotification_getMessageReturnsCorrectFormat() {
    val notification =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    assertEquals("John Doe sent you a friend request", notification.getMessage())
  }

  @Test
  fun eventStartingNotification_hasCorrectType() {
    val notification =
        Notification.EventStarting(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Study Session",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    assertEquals(NotificationType.EVENT_STARTING, notification.type)
  }

  @Test
  fun eventStartingNotification_getMessageReturnsCorrectFormat() {
    val notification =
        Notification.EventStarting(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Study Session",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    assertEquals("Event \"Study Session\" is starting soon", notification.getMessage())
  }

  @Test
  fun friendRequestNotification_toMapCreatesCorrectMap() {
    val timestamp = Timestamp.now()
    val notification =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = timestamp,
            isRead = false)

    val map = notification.toMap()

    assertEquals("test-id", map["id"])
    assertEquals("user-1", map["userId"])
    assertEquals("FRIEND_REQUEST", map["type"])
    assertEquals("user-2", map["fromUserId"])
    assertEquals("John Doe", map["fromUserName"])
    assertEquals(timestamp, map["timestamp"])
    assertEquals(false, map["isRead"])
  }

  @Test
  fun eventStartingNotification_toMapCreatesCorrectMap() {
    val timestamp = Timestamp.now()
    val eventStart = Timestamp.now()
    val notification =
        Notification.EventStarting(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Study Session",
            eventStart = eventStart,
            timestamp = timestamp,
            isRead = true)

    val map = notification.toMap()

    assertEquals("test-id", map["id"])
    assertEquals("user-1", map["userId"])
    assertEquals("EVENT_STARTING", map["type"])
    assertEquals("event-1", map["eventId"])
    assertEquals("Study Session", map["eventTitle"])
    assertEquals(eventStart, map["eventStart"])
    assertEquals(timestamp, map["timestamp"])
    assertEquals(true, map["isRead"])
  }

  @Test
  fun fromMap_createsFriendRequestNotificationCorrectly() {
    val timestamp = Timestamp.now()
    val map =
        mapOf(
            "id" to "test-id",
            "userId" to "user-1",
            "type" to "FRIEND_REQUEST",
            "fromUserId" to "user-2",
            "fromUserName" to "Jane Doe",
            "timestamp" to timestamp,
            "isRead" to false)

    val notification = Notification.fromMap(map) as? Notification.FriendRequest

    assertNotNull(notification)
    assertEquals("test-id", notification!!.id)
    assertEquals("user-1", notification.userId)
    assertEquals("user-2", notification.fromUserId)
    assertEquals("Jane Doe", notification.fromUserName)
    assertEquals(timestamp, notification.timestamp)
    assertFalse(notification.isRead)
  }

  @Test
  fun fromMap_createsEventStartingNotificationCorrectly() {
    val timestamp = Timestamp.now()
    val eventStart = Timestamp.now()
    val map =
        mapOf(
            "id" to "test-id",
            "userId" to "user-1",
            "type" to "EVENT_STARTING",
            "eventId" to "event-1",
            "eventTitle" to "Basketball Game",
            "eventStart" to eventStart,
            "timestamp" to timestamp,
            "isRead" to true)

    val notification = Notification.fromMap(map) as? Notification.EventStarting

    assertNotNull(notification)
    assertEquals("test-id", notification!!.id)
    assertEquals("user-1", notification.userId)
    assertEquals("event-1", notification.eventId)
    assertEquals("Basketball Game", notification.eventTitle)
    assertEquals(eventStart, notification.eventStart)
    assertEquals(timestamp, notification.timestamp)
    assertTrue(notification.isRead)
  }

  @Test
  fun fromMap_returnsNullForUnknownType() {
    val map =
        mapOf("id" to "test-id", "userId" to "user-1", "type" to "UNKNOWN_TYPE", "isRead" to false)

    val notification = Notification.fromMap(map)

    assertNull(notification)
  }

  @Test
  fun fromMap_returnsNullForMissingType() {
    val map = mapOf("id" to "test-id", "userId" to "user-1", "isRead" to false)

    val notification = Notification.fromMap(map)

    assertNull(notification)
  }

  @Test
  fun fromMap_handlesDefaultValues() {
    val map = mapOf("type" to "FRIEND_REQUEST")

    val notification = Notification.fromMap(map) as? Notification.FriendRequest

    assertNotNull(notification)
    assertEquals("", notification!!.id)
    assertEquals("", notification.userId)
    assertEquals("", notification.fromUserId)
    assertEquals("", notification.fromUserName)
    assertNull(notification.timestamp)
    assertFalse(notification.isRead)
  }

  @Test
  fun notificationEquality_worksCorrectly() {
    val notification1 =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = null,
            isRead = false)

    val notification2 =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = null,
            isRead = false)

    assertEquals(notification1, notification2)
  }

  @Test
  fun notificationCopy_worksCorrectly() {
    val original =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = null,
            isRead = false)

    val copy = original.copy(isRead = true)

    assertEquals("test-id", copy.id)
    assertEquals("user-1", copy.userId)
    assertEquals("user-2", copy.fromUserId)
    assertEquals("John Doe", copy.fromUserName)
    assertTrue(copy.isRead)
  }
}
