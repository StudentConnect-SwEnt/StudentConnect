package com.github.se.studentconnect.service

import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class FCMNotificationHandlerTest {

  private lateinit var mockRepository: NotificationRepository
  private lateinit var handler: FCMNotificationHandler
  private val testUserId = "test-user-123"

  @Before
  fun setUp() {
    mockRepository = mock()
    handler = FCMNotificationHandler(mockRepository) { testUserId }
  }

  // ==================== processMessage Tests ====================

  @Test
  fun processMessage_withNoType_doesNothing() {
    val data = emptyMap<String, String>()

    handler.processMessage(data)

    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processMessage_withNoUserId_doesNothing() {
    val handlerNoUser = FCMNotificationHandler(mockRepository) { null }
    val data = mapOf("type" to NotificationType.FRIEND_REQUEST.name)

    handlerNoUser.processMessage(data)

    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processMessage_withUnknownType_doesNothing() {
    val data = mapOf("type" to "UNKNOWN_TYPE")

    handler.processMessage(data)

    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processMessage_withFriendRequest_callsProcessFriendRequest() {
    val data =
        mapOf(
            "type" to NotificationType.FRIEND_REQUEST.name,
            "fromUserId" to "sender-123",
            "fromUserName" to "John Doe",
            "notificationId" to "notif-abc")

    handler.processMessage(data)

    verify(mockRepository, times(1)).createNotification(any(), any(), any())
  }

  @Test
  fun processMessage_withEventStarting_callsProcessEventStarting() {
    val data =
        mapOf(
            "type" to NotificationType.EVENT_STARTING.name,
            "eventId" to "event-456",
            "eventTitle" to "Team Meeting",
            "notificationId" to "notif-def",
            "eventStart" to "1704067200000")

    handler.processMessage(data)

    verify(mockRepository, times(1)).createNotification(any(), any(), any())
  }

  // ==================== processFriendRequest Tests ====================

  @Test
  fun processFriendRequest_withValidData_createsNotification() {
    val data =
        mapOf(
            "fromUserId" to "sender-123",
            "fromUserName" to "John Doe",
            "notificationId" to "notif-abc")

    handler.processFriendRequest(data, testUserId)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.FriendRequest
    assertEquals(testUserId, notification.userId)
    assertEquals("sender-123", notification.fromUserId)
    assertEquals("John Doe", notification.fromUserName)
    assertEquals("notif-abc", notification.id)
    assertFalse(notification.isRead)
  }

  @Test
  fun processFriendRequest_withNoFromUserId_doesNothing() {
    val data = mapOf("fromUserName" to "John Doe", "notificationId" to "notif-abc")

    handler.processFriendRequest(data, testUserId)

    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processFriendRequest_withNoFromUserName_usesDefault() {
    val data = mapOf("fromUserId" to "sender-123", "notificationId" to "notif-abc")

    handler.processFriendRequest(data, testUserId)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.FriendRequest
    assertEquals("Someone", notification.fromUserName)
  }

  @Test
  fun processFriendRequest_withNoNotificationId_usesEmptyString() {
    val data = mapOf("fromUserId" to "sender-123", "fromUserName" to "John Doe")

    handler.processFriendRequest(data, testUserId)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.FriendRequest
    assertEquals("", notification.id)
  }

  // ==================== processEventStarting Tests ====================

  @Test
  fun processEventStarting_withValidData_createsNotification() {
    val data =
        mapOf(
            "eventId" to "event-456",
            "eventTitle" to "Team Meeting",
            "notificationId" to "notif-def",
            "eventStart" to "1704067200000")

    handler.processEventStarting(data, testUserId)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.EventStarting
    assertEquals(testUserId, notification.userId)
    assertEquals("event-456", notification.eventId)
    assertEquals("Team Meeting", notification.eventTitle)
    assertEquals("notif-def", notification.id)
    assertFalse(notification.isRead)
  }

  @Test
  fun processEventStarting_withNoEventId_doesNothing() {
    val data =
        mapOf(
            "eventTitle" to "Team Meeting",
            "notificationId" to "notif-def",
            "eventStart" to "1704067200000")

    handler.processEventStarting(data, testUserId)

    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processEventStarting_withNoEventStart_doesNothing() {
    val data =
        mapOf(
            "eventId" to "event-456",
            "eventTitle" to "Team Meeting",
            "notificationId" to "notif-def")

    handler.processEventStarting(data, testUserId)

    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processEventStarting_withInvalidEventStart_doesNothing() {
    val data =
        mapOf(
            "eventId" to "event-456",
            "eventTitle" to "Team Meeting",
            "notificationId" to "notif-def",
            "eventStart" to "invalid-timestamp")

    handler.processEventStarting(data, testUserId)

    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processEventStarting_withNoEventTitle_usesDefault() {
    val data =
        mapOf(
            "eventId" to "event-456",
            "notificationId" to "notif-def",
            "eventStart" to "1704067200000")

    handler.processEventStarting(data, testUserId)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.EventStarting
    assertEquals("Event", notification.eventTitle)
  }

  @Test
  fun processEventStarting_withNoNotificationId_usesEmptyString() {
    val data =
        mapOf(
            "eventId" to "event-456",
            "eventTitle" to "Team Meeting",
            "eventStart" to "1704067200000")

    handler.processEventStarting(data, testUserId)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.EventStarting
    assertEquals("", notification.id)
  }

  @Test
  fun processEventStarting_parsesTimestampCorrectly() {
    val data =
        mapOf(
            "eventId" to "event-456",
            "eventTitle" to "Team Meeting",
            "notificationId" to "notif-def",
            "eventStart" to "1704067200000")

    handler.processEventStarting(data, testUserId)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.EventStarting
    assertEquals(1704067200000L, notification.eventStart!!.toDate().time)
  }

  // ==================== storeNotification Tests ====================

  @Test
  fun storeNotification_callsRepositoryCreateNotification() {
    val data =
        mapOf(
            "fromUserId" to "sender-123",
            "fromUserName" to "John Doe",
            "notificationId" to "notif-abc")

    handler.processFriendRequest(data, testUserId)

    verify(mockRepository, times(1)).createNotification(any(), any(), any())
  }

  @Test
  fun storeNotification_onSuccess_logsSuccess() {
    val data =
        mapOf(
            "fromUserId" to "sender-123",
            "fromUserName" to "John Doe",
            "notificationId" to "notif-abc")

    val captor = argumentCaptor<() -> Unit>()
    doNothing().whenever(mockRepository).createNotification(any(), captor.capture(), any())

    handler.processFriendRequest(data, testUserId)

    // Invoke success callback
    captor.firstValue.invoke()

    // Should complete without throwing
    verify(mockRepository, times(1)).createNotification(any(), any(), any())
  }

  @Test
  fun storeNotification_onFailure_logsError() {
    val data =
        mapOf(
            "fromUserId" to "sender-123",
            "fromUserName" to "John Doe",
            "notificationId" to "notif-abc")

    val captor = argumentCaptor<(Exception) -> Unit>()
    doNothing().whenever(mockRepository).createNotification(any(), any(), captor.capture())

    handler.processFriendRequest(data, testUserId)

    // Invoke failure callback
    captor.firstValue.invoke(Exception("Test error"))

    // Should complete without throwing
    verify(mockRepository, times(1)).createNotification(any(), any(), any())
  }

  @Test
  fun storeNotification_whenRepositoryThrows_catchesException() {
    val mockRepoThatThrows: NotificationRepository = mock()
    whenever(mockRepoThatThrows.createNotification(any(), any(), any()))
        .thenThrow(RuntimeException("Repository error"))

    val handlerWithBadRepo = FCMNotificationHandler(mockRepoThatThrows) { testUserId }

    val data =
        mapOf(
            "fromUserId" to "sender-123",
            "fromUserName" to "John Doe",
            "notificationId" to "notif-abc")

    // Should not throw exception
    handlerWithBadRepo.processFriendRequest(data, testUserId)

    // Should complete without throwing
    assertTrue(true)
  }
}
