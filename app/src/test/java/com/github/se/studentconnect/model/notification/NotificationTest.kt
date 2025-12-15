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

  @Test
  fun organizationMemberInvitationNotification_hasCorrectType() {
    val notification =
        Notification.OrganizationMemberInvitation(
            id = "test-id",
            userId = "user-1",
            organizationId = "org-1",
            organizationName = "Test Org",
            role = "Member",
            invitedBy = "user-2",
            invitedByName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    assertEquals(NotificationType.ORGANIZATION_MEMBER_INVITATION, notification.type)
  }

  @Test
  fun organizationMemberInvitationNotification_getMessageReturnsCorrectFormat() {
    val notification =
        Notification.OrganizationMemberInvitation(
            id = "test-id",
            userId = "user-1",
            organizationId = "org-1",
            organizationName = "Test Org",
            role = "Member",
            invitedBy = "user-2",
            invitedByName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    assertEquals("John Doe invited you to join \"Test Org\" as Member", notification.getMessage())
  }

  @Test
  fun organizationMemberInvitationNotification_toMapCreatesCorrectMap() {
    val timestamp = Timestamp.now()
    val notification =
        Notification.OrganizationMemberInvitation(
            id = "test-id",
            userId = "user-1",
            organizationId = "org-1",
            organizationName = "Test Org",
            role = "Member",
            invitedBy = "user-2",
            invitedByName = "John Doe",
            timestamp = timestamp,
            isRead = false)

    val map = notification.toMap()

    assertEquals("test-id", map["id"])
    assertEquals("user-1", map["userId"])
    assertEquals("ORGANIZATION_MEMBER_INVITATION", map["type"])
    assertEquals("org-1", map["organizationId"])
    assertEquals("Test Org", map["organizationName"])
    assertEquals("Member", map["role"])
    assertEquals("user-2", map["invitedBy"])
    assertEquals("John Doe", map["invitedByName"])
    assertEquals(timestamp, map["timestamp"])
    assertEquals(false, map["isRead"])
  }

  @Test
  fun fromMap_createsOrganizationMemberInvitationCorrectly() {
    val timestamp = Timestamp.now()
    val map =
        mapOf(
            "id" to "test-id",
            "userId" to "user-1",
            "type" to "ORGANIZATION_MEMBER_INVITATION",
            "organizationId" to "org-1",
            "organizationName" to "Test Org",
            "role" to "Admin",
            "invitedBy" to "user-2",
            "invitedByName" to "Jane Doe",
            "timestamp" to timestamp,
            "isRead" to true)

    val notification = Notification.fromMap(map) as? Notification.OrganizationMemberInvitation

    assertNotNull(notification)
    assertEquals("test-id", notification!!.id)
    assertEquals("user-1", notification.userId)
    assertEquals("org-1", notification.organizationId)
    assertEquals("Test Org", notification.organizationName)
    assertEquals("Admin", notification.role)
    assertEquals("user-2", notification.invitedBy)
    assertEquals("Jane Doe", notification.invitedByName)
    assertEquals(timestamp, notification.timestamp)
    assertTrue(notification.isRead)
  }

  @Test
  fun organizationMemberInvitationNotification_handlesDefaultValues() {
    val notification = Notification.OrganizationMemberInvitation()

    assertEquals("", notification.id)
    assertEquals("", notification.userId)
    assertEquals("", notification.organizationId)
    assertEquals("", notification.organizationName)
    assertEquals("", notification.role)
    assertEquals("", notification.invitedBy)
    assertEquals("", notification.invitedByName)
    assertNull(notification.timestamp)
    assertFalse(notification.isRead)
  }

  @Test
  fun organizationMemberInvitationNotification_copyWorksCorrectly() {
    val original =
        Notification.OrganizationMemberInvitation(
            id = "test-id",
            userId = "user-1",
            organizationId = "org-1",
            organizationName = "Test Org",
            role = "Member",
            invitedBy = "user-2",
            invitedByName = "John Doe",
            timestamp = null,
            isRead = false)

    val copy = original.copy(isRead = true)

    assertEquals("test-id", copy.id)
    assertEquals("user-1", copy.userId)
    assertEquals("org-1", copy.organizationId)
    assertEquals("Test Org", copy.organizationName)
    assertEquals("Member", copy.role)
    assertEquals("user-2", copy.invitedBy)
    assertEquals("John Doe", copy.invitedByName)
    assertTrue(copy.isRead)
  }

  @Test
  fun fromMap_organizationMemberInvitation_handlesDefaultValues() {
    val map = mapOf("type" to "ORGANIZATION_MEMBER_INVITATION")

    val notification = Notification.fromMap(map) as? Notification.OrganizationMemberInvitation

    assertNotNull(notification)
    assertEquals("", notification!!.id)
    assertEquals("", notification.userId)
    assertEquals("", notification.organizationId)
    assertEquals("", notification.organizationName)
    assertEquals("", notification.role)
    assertEquals("", notification.invitedBy)
    assertEquals("", notification.invitedByName)
    assertNull(notification.timestamp)
    assertFalse(notification.isRead)
  }

  // EventInvitation Notification Tests
  @Test
  fun eventInvitationNotification_hasCorrectType() {
    val notification =
        Notification.EventInvitation(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    assertEquals(NotificationType.EVENT_INVITATION, notification.type)
  }

  @Test
  fun eventInvitationNotification_getMessageReturnsCorrectFormat() {
    val notification =
        Notification.EventInvitation(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    assertEquals("Alice Smith invited you to \"Private Party\"", notification.getMessage())
  }

  @Test
  fun eventInvitationNotification_toMapCreatesCorrectMap() {
    val timestamp = Timestamp.now()
    val notification =
        Notification.EventInvitation(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = timestamp,
            isRead = false)

    val map = notification.toMap()

    assertEquals("test-id", map["id"])
    assertEquals("user-1", map["userId"])
    assertEquals("EVENT_INVITATION", map["type"])
    assertEquals("event-1", map["eventId"])
    assertEquals("Private Party", map["eventTitle"])
    assertEquals("user-2", map["invitedBy"])
    assertEquals("Alice Smith", map["invitedByName"])
    assertEquals(timestamp, map["timestamp"])
    assertEquals(false, map["isRead"])
  }

  @Test
  fun eventInvitationNotification_toMapWithNullTimestampUsesFieldValue() {
    val notification =
        Notification.EventInvitation(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = null,
            isRead = false)

    val map = notification.toMap()

    assertNotNull(map["timestamp"]) // Should have FieldValue.serverTimestamp()
    // Note: We can't directly test FieldValue.serverTimestamp() equality,
    // but we verify it's not null
  }

  @Test
  fun fromMap_createsEventInvitationNotificationCorrectly() {
    val timestamp = Timestamp.now()
    val map =
        mapOf(
            "id" to "test-id",
            "userId" to "user-1",
            "type" to "EVENT_INVITATION",
            "eventId" to "event-1",
            "eventTitle" to "Birthday Celebration",
            "invitedBy" to "user-2",
            "invitedByName" to "Bob Johnson",
            "timestamp" to timestamp,
            "isRead" to true)

    val notification = Notification.fromMap(map) as? Notification.EventInvitation

    assertNotNull(notification)
    assertEquals("test-id", notification!!.id)
    assertEquals("user-1", notification.userId)
    assertEquals("event-1", notification.eventId)
    assertEquals("Birthday Celebration", notification.eventTitle)
    assertEquals("user-2", notification.invitedBy)
    assertEquals("Bob Johnson", notification.invitedByName)
    assertEquals(timestamp, notification.timestamp)
    assertTrue(notification.isRead)
  }

  @Test
  fun eventInvitationNotification_handlesDefaultValues() {
    val notification = Notification.EventInvitation()

    assertEquals("", notification.id)
    assertEquals("", notification.userId)
    assertEquals("", notification.eventId)
    assertEquals("", notification.eventTitle)
    assertEquals("", notification.invitedBy)
    assertEquals("", notification.invitedByName)
    assertNull(notification.timestamp)
    assertFalse(notification.isRead)
  }

  @Test
  fun eventInvitationNotification_copyWorksCorrectly() {
    val original =
        Notification.EventInvitation(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = null,
            isRead = false)

    val copy = original.copy(isRead = true)

    assertEquals("test-id", copy.id)
    assertEquals("user-1", copy.userId)
    assertEquals("event-1", copy.eventId)
    assertEquals("Private Party", copy.eventTitle)
    assertEquals("user-2", copy.invitedBy)
    assertEquals("Alice Smith", copy.invitedByName)
    assertTrue(copy.isRead)
  }

  @Test
  fun fromMap_eventInvitation_handlesDefaultValues() {
    val map = mapOf("type" to "EVENT_INVITATION")

    val notification = Notification.fromMap(map) as? Notification.EventInvitation

    assertNotNull(notification)
    assertEquals("", notification!!.id)
    assertEquals("", notification.userId)
    assertEquals("", notification.eventId)
    assertEquals("", notification.eventTitle)
    assertEquals("", notification.invitedBy)
    assertEquals("", notification.invitedByName)
    assertNull(notification.timestamp)
    assertFalse(notification.isRead)
  }

  @Test
  fun eventInvitationNotification_equalityWorksCorrectly() {
    val notification1 =
        Notification.EventInvitation(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = null,
            isRead = false)

    val notification2 =
        Notification.EventInvitation(
            id = "test-id",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = null,
            isRead = false)

    assertEquals(notification1, notification2)
  }
}
