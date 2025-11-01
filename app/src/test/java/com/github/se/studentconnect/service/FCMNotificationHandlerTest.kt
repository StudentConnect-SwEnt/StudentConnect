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
  fun processMessage_withNoType_returnsNull() {
    val data = emptyMap<String, String>()

    val result = handler.processMessage(data)

    assertNull(result)
    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processMessage_withNoUserId_returnsNull() {
    val handlerNoUser = FCMNotificationHandler(mockRepository) { null }
    val data = mapOf("type" to NotificationType.FRIEND_REQUEST.name)

    val result = handlerNoUser.processMessage(data)

    assertNull(result)
    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processMessage_withUnknownType_returnsNull() {
    val data = mapOf("type" to "UNKNOWN_TYPE")

    val result = handler.processMessage(data)

    assertNull(result)
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

    val result = handler.processMessage(data)

    assertNotNull(result)
    assertEquals("New Friend Request", result?.title)
    assertEquals(NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID, result?.channelId)
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

    val result = handler.processMessage(data)

    assertNotNull(result)
    assertEquals("Event Starting Soon", result?.title)
    assertEquals(NotificationChannelManager.EVENT_STARTING_CHANNEL_ID, result?.channelId)
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

    val result = handler.processFriendRequest(data, testUserId)

    assertNotNull(result)
    assertEquals("New Friend Request", result?.title)
    assertEquals(NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID, result?.channelId)

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
  fun processFriendRequest_withNoFromUserId_returnsNull() {
    val data = mapOf("fromUserName" to "John Doe", "notificationId" to "notif-abc")

    val result = handler.processFriendRequest(data, testUserId)

    assertNull(result)
    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processFriendRequest_withNoFromUserName_usesDefault() {
    val data = mapOf("fromUserId" to "sender-123", "notificationId" to "notif-abc")

    val result = handler.processFriendRequest(data, testUserId)

    assertNotNull(result)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.FriendRequest
    assertEquals("Someone", notification.fromUserName)
  }

  @Test
  fun processFriendRequest_withNoNotificationId_usesEmptyString() {
    val data = mapOf("fromUserId" to "sender-123", "fromUserName" to "John Doe")

    val result = handler.processFriendRequest(data, testUserId)

    assertNotNull(result)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.FriendRequest
    assertEquals("", notification.id)
  }

  @Test
  fun processFriendRequest_generatesCorrectNotificationId() {
    val data =
        mapOf(
            "fromUserId" to "sender-123",
            "fromUserName" to "John Doe",
            "notificationId" to "notif-abc")

    val result = handler.processFriendRequest(data, testUserId)

    assertNotNull(result)
    assertEquals("notif-abc".hashCode(), result?.notificationId)
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

    val result = handler.processEventStarting(data, testUserId)

    assertNotNull(result)
    assertEquals("Event Starting Soon", result?.title)
    assertEquals(NotificationChannelManager.EVENT_STARTING_CHANNEL_ID, result?.channelId)

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
  fun processEventStarting_withNoEventId_returnsNull() {
    val data =
        mapOf(
            "eventTitle" to "Team Meeting",
            "notificationId" to "notif-def",
            "eventStart" to "1704067200000")

    val result = handler.processEventStarting(data, testUserId)

    assertNull(result)
    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processEventStarting_withNoEventStart_returnsNull() {
    val data =
        mapOf(
            "eventId" to "event-456", "eventTitle" to "Team Meeting", "notificationId" to "notif-def")

    val result = handler.processEventStarting(data, testUserId)

    assertNull(result)
    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processEventStarting_withInvalidEventStart_returnsNull() {
    val data =
        mapOf(
            "eventId" to "event-456",
            "eventTitle" to "Team Meeting",
            "notificationId" to "notif-def",
            "eventStart" to "invalid-timestamp")

    val result = handler.processEventStarting(data, testUserId)

    assertNull(result)
    verify(mockRepository, never()).createNotification(any(), any(), any())
  }

  @Test
  fun processEventStarting_withNoEventTitle_usesDefault() {
    val data =
        mapOf("eventId" to "event-456", "notificationId" to "notif-def", "eventStart" to "1704067200000")

    val result = handler.processEventStarting(data, testUserId)

    assertNotNull(result)

    val captor = argumentCaptor<Notification>()
    verify(mockRepository, times(1)).createNotification(captor.capture(), any(), any())

    val notification = captor.firstValue as Notification.EventStarting
    assertEquals("Event", notification.eventTitle)
  }

  @Test
  fun processEventStarting_withNoNotificationId_usesEmptyString() {
    val data =
        mapOf("eventId" to "event-456", "eventTitle" to "Team Meeting", "eventStart" to "1704067200000")

    val result = handler.processEventStarting(data, testUserId)

    assertNotNull(result)

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

    val result = handler.processEventStarting(data, testUserId)

    assertNotNull(result)

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
    val result = handlerWithBadRepo.processFriendRequest(data, testUserId)

    // Result should still be created even though storing failed
    assertNotNull(result)
  }

  // ==================== NotificationInfo Tests ====================

  @Test
  fun notificationInfo_containsCorrectData() {
    val info =
        NotificationInfo(
            title = "Test Title",
            message = "Test Message",
            channelId = "test-channel",
            notificationId = 12345)

    assertEquals("Test Title", info.title)
    assertEquals("Test Message", info.message)
    assertEquals("test-channel", info.channelId)
    assertEquals(12345, info.notificationId)
  }

  @Test
  fun notificationInfo_dataClass_hasCorrectEquality() {
    val info1 =
        NotificationInfo(
            title = "Title", message = "Message", channelId = "channel", notificationId = 123)
    val info2 =
        NotificationInfo(
            title = "Title", message = "Message", channelId = "channel", notificationId = 123)
    val info3 =
        NotificationInfo(
            title = "Different", message = "Message", channelId = "channel", notificationId = 123)

    assertEquals(info1, info2)
    assertNotEquals(info1, info3)
  }
}
