package com.github.se.studentconnect.model.notification

import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NotificationRepositoryLocalTest {

  private lateinit var repository: NotificationRepositoryLocal

  private val testUserId1 = "user123"
  private val testUserId2 = "user456"

  private val friendRequestNotification =
      Notification.FriendRequest(
          id = "notif1",
          userId = testUserId1,
          fromUserId = "sender1",
          fromUserName = "John Doe",
          timestamp = Timestamp.now(),
          isRead = false)

  private val eventStartingNotification =
      Notification.EventStarting(
          id = "notif2",
          userId = testUserId1,
          eventId = "event123",
          eventTitle = "Tech Conference",
          eventStart = Timestamp.now(),
          timestamp = Timestamp.now(),
          isRead = true)

  private val anotherUserNotification =
      Notification.FriendRequest(
          id = "notif3",
          userId = testUserId2,
          fromUserId = "sender2",
          fromUserName = "Jane Smith",
          timestamp = Timestamp.now(),
          isRead = false)

  @Before
  fun setup() {
    repository = NotificationRepositoryLocal()
  }

  @Test
  fun getNotifications_emptyRepository_returnsEmptyList() {
    var result: List<Notification>? = null
    var error: Exception? = null

    repository.getNotifications(
        userId = testUserId1, onSuccess = { result = it }, onFailure = { error = it })

    assertNotNull(result)
    assertTrue(result!!.isEmpty())
    assertNull(error)
  }

  @Test
  fun createNotification_addsNotificationSuccessfully() {
    var successCalled = false
    var error: Exception? = null

    repository.createNotification(
        notification = friendRequestNotification,
        onSuccess = { successCalled = true },
        onFailure = { error = it })

    assertTrue(successCalled)
    assertNull(error)

    // Verify notification was added
    var notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId1, onSuccess = { notifications = it }, onFailure = {})

    assertEquals(1, notifications!!.size)
    assertEquals(friendRequestNotification.id, notifications!![0].id)
  }

  @Test
  fun getNotifications_filtersNotificationsByUserId() {
    // Add notifications for different users
    repository.createNotification(friendRequestNotification, {}, {})
    repository.createNotification(eventStartingNotification, {}, {})
    repository.createNotification(anotherUserNotification, {}, {})

    // Get notifications for user1
    var user1Notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId1, onSuccess = { user1Notifications = it }, onFailure = {})

    assertEquals(2, user1Notifications!!.size)
    assertTrue(user1Notifications!!.all { it.userId == testUserId1 })

    // Get notifications for user2
    var user2Notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId2, onSuccess = { user2Notifications = it }, onFailure = {})

    assertEquals(1, user2Notifications!!.size)
    assertEquals(testUserId2, user2Notifications!![0].userId)
  }

  @Test
  fun getUnreadNotifications_filtersUnreadNotificationsOnly() {
    repository.createNotification(friendRequestNotification, {}, {}) // isRead = false
    repository.createNotification(eventStartingNotification, {}, {}) // isRead = true

    var unreadNotifications: List<Notification>? = null
    repository.getUnreadNotifications(
        userId = testUserId1, onSuccess = { unreadNotifications = it }, onFailure = {})

    assertEquals(1, unreadNotifications!!.size)
    assertEquals(friendRequestNotification.id, unreadNotifications!![0].id)
    assertFalse(unreadNotifications!![0].isRead)
  }

  @Test
  fun markAsRead_callsOnSuccess() {
    repository.createNotification(friendRequestNotification, {}, {})

    var successCalled = false
    var error: Exception? = null

    repository.markAsRead(
        notificationId = friendRequestNotification.id,
        onSuccess = { successCalled = true },
        onFailure = { error = it })

    assertTrue(successCalled)
    assertNull(error)
  }

  @Test
  fun markAsRead_nonExistentId_callsOnFailure() {
    var successCalled = false
    var failureCalled = false
    var error: Exception? = null

    repository.markAsRead(
        notificationId = "nonexistent",
        onSuccess = { successCalled = true },
        onFailure = {
          failureCalled = true
          error = it
        })

    assertFalse(successCalled)
    assertTrue(failureCalled)
    assertNotNull(error)
    assertTrue(error!!.message!!.contains("not found"))
  }

  @Test
  fun markAllAsRead_callsOnSuccess() {
    repository.createNotification(friendRequestNotification, {}, {})
    repository.createNotification(eventStartingNotification, {}, {})

    var successCalled = false
    var error: Exception? = null

    repository.markAllAsRead(
        userId = testUserId1, onSuccess = { successCalled = true }, onFailure = { error = it })

    assertTrue(successCalled)
    assertNull(error)
  }

  @Test
  fun deleteNotification_removesNotificationById() {
    repository.createNotification(friendRequestNotification, {}, {})
    repository.createNotification(eventStartingNotification, {}, {})

    var successCalled = false
    repository.deleteNotification(
        notificationId = friendRequestNotification.id,
        onSuccess = { successCalled = true },
        onFailure = {})

    assertTrue(successCalled)

    // Verify notification was deleted
    var notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId1, onSuccess = { notifications = it }, onFailure = {})

    assertEquals(1, notifications!!.size)
    assertEquals(eventStartingNotification.id, notifications!![0].id)
  }

  @Test
  fun deleteNotification_nonExistentId_callsOnFailure() {
    repository.createNotification(friendRequestNotification, {}, {})

    var successCalled = false
    var failureCalled = false
    var error: Exception? = null
    repository.deleteNotification(
        notificationId = "nonexistent",
        onSuccess = { successCalled = true },
        onFailure = {
          failureCalled = true
          error = it
        })

    assertFalse(successCalled)
    assertTrue(failureCalled)
    assertNotNull(error)
    assertTrue(error!!.message!!.contains("not found"))

    // Original notification should still exist
    var notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId1, onSuccess = { notifications = it }, onFailure = {})

    assertEquals(1, notifications!!.size)
  }

  @Test
  fun deleteAllNotifications_removesAllNotificationsForUser() {
    repository.createNotification(friendRequestNotification, {}, {})
    repository.createNotification(eventStartingNotification, {}, {})
    repository.createNotification(anotherUserNotification, {}, {})

    var successCalled = false
    repository.deleteAllNotifications(
        userId = testUserId1, onSuccess = { successCalled = true }, onFailure = {})

    assertTrue(successCalled)

    // Verify user1 notifications were deleted
    var user1Notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId1, onSuccess = { user1Notifications = it }, onFailure = {})

    assertTrue(user1Notifications!!.isEmpty())

    // Verify user2 notification still exists
    var user2Notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId2, onSuccess = { user2Notifications = it }, onFailure = {})

    assertEquals(1, user2Notifications!!.size)
  }

  @Test
  fun listenToNotifications_returnsEmptyListImmediately() {
    var receivedNotifications: List<Notification>? = null

    val stopListening =
        repository.listenToNotifications(
            userId = testUserId1, onNotificationsChanged = { receivedNotifications = it })

    assertNotNull(receivedNotifications)
    assertTrue(receivedNotifications!!.isEmpty())

    // Verify cleanup function is not null
    assertNotNull(stopListening)
    stopListening() // Should not crash
  }

  @Test
  fun getNotificationUserId_friendRequest_returnsCorrectUserId() {
    repository.createNotification(friendRequestNotification, {}, {})

    var notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId1, onSuccess = { notifications = it }, onFailure = {})

    assertEquals(1, notifications!!.size)
    assertEquals(testUserId1, notifications!![0].userId)
  }

  @Test
  fun getNotificationUserId_eventStarting_returnsCorrectUserId() {
    repository.createNotification(eventStartingNotification, {}, {})

    var notifications: List<Notification>? = null
    repository.getNotifications(
        userId = testUserId1, onSuccess = { notifications = it }, onFailure = {})

    assertEquals(1, notifications!!.size)
    assertEquals(testUserId1, notifications!![0].userId)
  }

  @Test
  fun multipleOperations_workCorrectly() {
    // Create multiple notifications
    repository.createNotification(friendRequestNotification, {}, {})
    repository.createNotification(eventStartingNotification, {}, {})
    repository.createNotification(anotherUserNotification, {}, {})

    // Get all notifications for user1
    var allNotifications: List<Notification>? = null
    repository.getNotifications(testUserId1, { allNotifications = it }, {})
    assertEquals(2, allNotifications!!.size)

    // Get unread notifications
    var unreadNotifications: List<Notification>? = null
    repository.getUnreadNotifications(testUserId1, { unreadNotifications = it }, {})
    assertEquals(1, unreadNotifications!!.size)

    // Delete one notification
    repository.deleteNotification(friendRequestNotification.id, {}, {})

    // Verify deletion
    repository.getNotifications(testUserId1, { allNotifications = it }, {})
    assertEquals(1, allNotifications!!.size)
    assertEquals(eventStartingNotification.id, allNotifications!![0].id)
  }
}
