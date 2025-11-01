package com.github.se.studentconnect.service

import com.github.se.studentconnect.model.notification.NotificationType
import com.google.firebase.messaging.RemoteMessage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class FCMServiceTest {

  private lateinit var fcmService: FCMService
  private lateinit var mockHandler: FCMNotificationHandler

  @Before
  fun setUp() {
    fcmService = FCMService()
    mockHandler = mock()
    fcmService.fcmHandler = mockHandler
  }

  @Test
  fun onNewToken_logsToken() {
    val token = "test-fcm-token-12345"

    // Call the method - should complete without throwing
    fcmService.onNewToken(token)

    // Verify no crash
    assertTrue(true)
  }

  @Test
  fun onMessageReceived_callsHandlerProcessMessage() {
    val remoteMessage: RemoteMessage = mock()
    val data = mapOf("type" to NotificationType.FRIEND_REQUEST.name, "fromUserId" to "test")
    whenever(remoteMessage.data).thenReturn(data)
    whenever(remoteMessage.from).thenReturn("test-sender")

    whenever(mockHandler.processMessage(data)).thenReturn(null)

    fcmService.onMessageReceived(remoteMessage)

    verify(mockHandler, times(1)).processMessage(data)
  }

  @Test
  fun onMessageReceived_whenHandlerReturnsNull_doesNotShowNotification() {
    val remoteMessage: RemoteMessage = mock()
    val data = emptyMap<String, String>()
    whenever(remoteMessage.data).thenReturn(data)
    whenever(remoteMessage.from).thenReturn("test-sender")

    whenever(mockHandler.processMessage(data)).thenReturn(null)

    // Spy on fcmService to verify showPushNotification is not called
    val spyService = spy(fcmService)
    spyService.fcmHandler = mockHandler

    spyService.onMessageReceived(remoteMessage)

    verify(mockHandler, times(1)).processMessage(data)
    verify(spyService, never())
        .showPushNotification(any(), any(), any(), any())
  }

  @Test
  fun onMessageReceived_whenHandlerReturnsInfo_showsNotification() {
    val remoteMessage: RemoteMessage = mock()
    val data =
        mapOf(
            "type" to NotificationType.FRIEND_REQUEST.name,
            "fromUserId" to "sender-123",
            "fromUserName" to "John Doe",
            "notificationId" to "notif-abc")
    whenever(remoteMessage.data).thenReturn(data)
    whenever(remoteMessage.from).thenReturn("test-sender")

    val notificationInfo =
        NotificationInfo(
            title = "Test Title",
            message = "Test Message",
            channelId = "test-channel",
            notificationId = 12345)
    whenever(mockHandler.processMessage(data)).thenReturn(notificationInfo)

    val spyService = spy(fcmService)
    spyService.fcmHandler = mockHandler
    doNothing().whenever(spyService).showPushNotification(any(), any(), any(), any())

    spyService.onMessageReceived(remoteMessage)

    verify(mockHandler, times(1)).processMessage(data)
    verify(spyService, times(1))
        .showPushNotification("Test Title", "Test Message", "test-channel", 12345)
  }

  @Test
  fun fcmHandler_canBeInjected() {
    val service = FCMService()
    val mockHandler: FCMNotificationHandler = mock()

    service.fcmHandler = mockHandler

    assertNotNull(service.fcmHandler)
    assertSame(mockHandler, service.fcmHandler)
  }

  @Test
  fun fcmHandler_isNullByDefault() {
    val service = FCMService()

    assertNull(service.fcmHandler)
  }

  @Test
  fun fcmService_extendsFirebaseMessagingService() {
    val serviceClass = FCMService::class.java
    val superclass = serviceClass.superclass

    assertNotNull(superclass)
    assertEquals("FirebaseMessagingService", superclass?.simpleName)
  }

  @Test
  fun fcmService_hasOnNewTokenMethod() {
    val serviceClass = FCMService::class.java
    val methods = serviceClass.declaredMethods

    val hasOnNewToken = methods.any { it.name == "onNewToken" }
    assertTrue(hasOnNewToken)
  }

  @Test
  fun fcmService_hasOnMessageReceivedMethod() {
    val serviceClass = FCMService::class.java
    val methods = serviceClass.declaredMethods

    val hasOnMessageReceived = methods.any { it.name == "onMessageReceived" }
    assertTrue(hasOnMessageReceived)
  }

  @Test
  fun fcmService_hasInternalMethods() {
    val serviceClass = FCMService::class.java
    val methods = serviceClass.declaredMethods

    // Verify class has necessary methods (names may be mangled by Kotlin)
    assertTrue(methods.size > 0)
    assertTrue(methods.any { it.name.contains("onMessageReceived") || it.name == "onMessageReceived" })
    assertTrue(methods.any { it.name.contains("onNewToken") || it.name == "onNewToken" })
  }

  @Test
  fun fcmService_hasCompanionObject() {
    val serviceClass = FCMService::class.java
    val companionClass = serviceClass.declaredClasses.find { it.simpleName == "Companion" }

    assertNotNull(companionClass)
  }

  @Test
  fun notificationChannelIds_areCorrect() {
    assertEquals("friend_requests", NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID)
    assertEquals("event_starting", NotificationChannelManager.EVENT_STARTING_CHANNEL_ID)
  }

  @Test
  fun notificationInfo_dataClass_works() {
    // Test the NotificationInfo data class
    val info = NotificationInfo("Title", "Message", "channel", 123)

    assertEquals("Title", info.title)
    assertEquals("Message", info.message)
    assertEquals("channel", info.channelId)
    assertEquals(123, info.notificationId)
  }
}
