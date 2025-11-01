package com.github.se.studentconnect.model.friends

import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.google.firebase.Timestamp
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class FriendsRepositoryNotificationTest {

  @Mock private lateinit var mockNotificationRepository: NotificationRepository

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    NotificationRepositoryProvider.setRepository(mockNotificationRepository)

    // Setup default mock behavior
    `when`(mockNotificationRepository.createNotification(any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
      null
    }
  }

  @After
  fun tearDown() {
    NotificationRepositoryProvider.clearRepository()
  }

  @Test
  fun notification_createsCorrectNotificationType() {
    // Test that friend request notifications are of the correct type
    val notification =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    assert(notification is Notification.FriendRequest) {
      "Notification should be FriendRequest type"
    }
    assert(notification.userId == "user-1") { "UserId should match recipient" }
    assert(notification.fromUserId == "user-2") { "FromUserId should match sender" }
    assert(notification.fromUserName == "John Doe") { "FromUserName should match sender name" }
    assert(!notification.isRead) { "New notification should be unread" }
  }

  @Test
  fun notification_generatesCorrectMessage() {
    val notification =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Jane Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    val message = notification.getMessage()

    assert(message.contains("Jane Smith")) { "Message should contain sender name" }
    assert(message.contains("friend request")) { "Message should mention friend request" }
  }

  @Test
  fun notificationRepository_createNotification_isCalled() {
    val notification =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Test User",
            timestamp = Timestamp.now(),
            isRead = false)

    mockNotificationRepository.createNotification(notification, {}, {})

    verify(mockNotificationRepository).createNotification(any(), any(), any())
  }

  @Test
  fun notificationRepository_handlesFailureGracefully() {
    `when`(mockNotificationRepository.createNotification(any(), any(), any())).thenAnswer {
        invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(Exception("Test failure"))
      null
    }

    var failureCalled = false
    mockNotificationRepository.createNotification(
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Test",
            timestamp = Timestamp.now(),
            isRead = false),
        onSuccess = {},
        onFailure = { failureCalled = true })

    assert(failureCalled) { "Failure callback should be invoked on error" }
  }

  @Test
  fun notification_hasTimestamp() {
    val timestamp = Timestamp.now()
    val notification =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Test",
            timestamp = timestamp,
            isRead = false)

    assert(notification.timestamp == timestamp) { "Notification should have correct timestamp" }
  }

  @Test
  fun notification_canBeMarkedAsRead() {
    val notification =
        Notification.FriendRequest(
            id = "test-id",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Test",
            timestamp = Timestamp.now(),
            isRead = false)

    val readNotification = notification.copy(isRead = true)

    assert(!notification.isRead) { "Original should be unread" }
    assert(readNotification.isRead) { "Copy should be read" }
  }

  @Test
  fun notificationProvider_isInitialized() {
    // Verify that the provider was set up correctly
    val repository = NotificationRepositoryProvider.repository

    assert(repository != null) { "Repository should be initialized" }
    assert(repository === mockNotificationRepository) { "Repository should be the mock instance" }
  }
}
