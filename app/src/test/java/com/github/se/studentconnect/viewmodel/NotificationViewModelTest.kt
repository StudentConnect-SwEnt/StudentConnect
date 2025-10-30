package com.github.se.studentconnect.viewmodel

import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class NotificationViewModelTest {

  @Mock private lateinit var mockRepository: NotificationRepository

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Setup default mock behavior
    `when`(mockRepository.listenToNotifications(any(), any())).thenAnswer { {} }
  }

  @Test
  fun notificationUiState_hasCorrectDefaults() {
    val uiState = NotificationUiState()

    assert(uiState.notifications.isEmpty()) { "Default notifications should be empty" }
    assert(uiState.unreadCount == 0) { "Default unread count should be 0" }
    assert(uiState.isLoading) { "Default loading state should be true" }
  }

  @Test
  fun notificationUiState_copyWorks() {
    val notification =
        Notification.FriendRequest(
            id = "1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John",
            timestamp = Timestamp.now(),
            isRead = false)

    val original = NotificationUiState()
    val updated =
        original.copy(notifications = listOf(notification), unreadCount = 1, isLoading = false)

    assert(updated.notifications.size == 1) { "Updated should have 1 notification" }
    assert(updated.unreadCount == 1) { "Updated should have unread count 1" }
    assert(!updated.isLoading) { "Updated should not be loading" }
    assert(original.notifications.isEmpty()) { "Original should remain unchanged" }
  }

  @Test
  fun notificationUiState_calculatesUnreadCountCorrectly() {
    val notifications =
        listOf(
            Notification.FriendRequest(
                id = "1",
                userId = "user-1",
                fromUserId = "user-2",
                fromUserName = "John",
                timestamp = Timestamp.now(),
                isRead = false),
            Notification.FriendRequest(
                id = "2",
                userId = "user-1",
                fromUserId = "user-3",
                fromUserName = "Jane",
                timestamp = Timestamp.now(),
                isRead = true),
            Notification.EventStarting(
                id = "3",
                userId = "user-1",
                eventId = "event-1",
                eventTitle = "Test Event",
                eventStart = Timestamp.now(),
                timestamp = Timestamp.now(),
                isRead = false))

    val unreadCount = notifications.count { !it.isRead }

    assert(unreadCount == 2) { "Should have 2 unread notifications" }
  }

  @Test
  fun notificationUiState_withAllRead_hasZeroUnreadCount() {
    val notifications =
        listOf(
            Notification.FriendRequest(
                id = "1",
                userId = "user-1",
                fromUserId = "user-2",
                fromUserName = "John",
                timestamp = Timestamp.now(),
                isRead = true),
            Notification.EventStarting(
                id = "2",
                userId = "user-1",
                eventId = "event-1",
                eventTitle = "Test Event",
                eventStart = Timestamp.now(),
                timestamp = Timestamp.now(),
                isRead = true))

    val unreadCount = notifications.count { !it.isRead }

    assert(unreadCount == 0) { "Should have 0 unread notifications when all are read" }
  }

  @Test
  fun notificationUiState_withMixedNotifications_tracksCorrectly() {
    val friendRequest =
        Notification.FriendRequest(
            id = "1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John",
            timestamp = Timestamp.now(),
            isRead = false)

    val eventStarting =
        Notification.EventStarting(
            id = "2",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Basketball",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    val notifications = listOf(friendRequest, eventStarting)
    val uiState =
        NotificationUiState(notifications = notifications, unreadCount = 2, isLoading = false)

    assert(uiState.notifications.size == 2) { "Should have 2 notifications" }
    assert(uiState.notifications[0] is Notification.FriendRequest) {
      "First should be FriendRequest"
    }
    assert(uiState.notifications[1] is Notification.EventStarting) {
      "Second should be EventStarting"
    }
  }
}
