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

  // EventInvitation Tests
  @Test
  fun markAsRead_eventInvitation_marksCorrectly() {
    val eventInvitation =
        Notification.EventInvitation(
            id = "invite-1",
            userId = testUserId1,
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    repository.createNotification(eventInvitation, {}, {})

    // Mark as read
    var successCalled = false
    repository.markAsRead(
        notificationId = "invite-1", onSuccess = { successCalled = true }, onFailure = {})

    assertTrue(successCalled)

    // Verify notification is marked as read
    var notifications: List<Notification>? = null
    repository.getNotifications(testUserId1, { notifications = it }, {})

    assertEquals(1, notifications!!.size)
    val updated = notifications!![0] as Notification.EventInvitation
    assertTrue(updated.isRead)
    assertEquals("Private Party", updated.eventTitle)
    assertEquals("Alice Smith", updated.invitedByName)
  }

  @Test
  fun markAllAsRead_eventInvitation_marksCorrectly() {
    val eventInvitation1 =
        Notification.EventInvitation(
            id = "invite-1",
            userId = testUserId1,
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    val eventInvitation2 =
        Notification.EventInvitation(
            id = "invite-2",
            userId = testUserId1,
            eventId = "event-2",
            eventTitle = "Secret Meeting",
            invitedBy = "user-3",
            invitedByName = "Bob Johnson",
            timestamp = Timestamp.now(),
            isRead = false)

    repository.createNotification(eventInvitation1, {}, {})
    repository.createNotification(eventInvitation2, {}, {})
    repository.createNotification(friendRequestNotification, {}, {})

    // Mark all as read for testUserId1
    var successCalled = false
    repository.markAllAsRead(testUserId1, { successCalled = true }, {})

    assertTrue(successCalled)

    // Verify all notifications are marked as read
    var notifications: List<Notification>? = null
    repository.getNotifications(testUserId1, { notifications = it }, {})

    assertEquals(3, notifications!!.size)
    notifications!!.forEach { assertTrue(it.isRead) }
  }

  @Test
  fun createNotification_eventInvitation_storesCorrectly() {
    val eventInvitation =
        Notification.EventInvitation(
            id = "invite-1",
            userId = testUserId1,
            eventId = "event-1",
            eventTitle = "Birthday Bash",
            invitedBy = "user-2",
            invitedByName = "Charlie Brown",
            timestamp = Timestamp.now(),
            isRead = false)

    var successCalled = false
    repository.createNotification(eventInvitation, { successCalled = true }, {})

    assertTrue(successCalled)

    // Verify notification was stored correctly
    var notifications: List<Notification>? = null
    repository.getNotifications(testUserId1, { notifications = it }, {})

    assertEquals(1, notifications!!.size)
    val stored = notifications!![0] as Notification.EventInvitation
    assertEquals("invite-1", stored.id)
    assertEquals(testUserId1, stored.userId)
    assertEquals("event-1", stored.eventId)
    assertEquals("Birthday Bash", stored.eventTitle)
    assertEquals("user-2", stored.invitedBy)
    assertEquals("Charlie Brown", stored.invitedByName)
    assertFalse(stored.isRead)
  }

  @Test
  fun getUnreadNotifications_eventInvitation_filtersCorrectly() {
    val unreadInvitation =
        Notification.EventInvitation(
            id = "invite-1",
            userId = testUserId1,
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    val readInvitation =
        Notification.EventInvitation(
            id = "invite-2",
            userId = testUserId1,
            eventId = "event-2",
            eventTitle = "Secret Meeting",
            invitedBy = "user-3",
            invitedByName = "Bob Johnson",
            timestamp = Timestamp.now(),
            isRead = true)

    repository.createNotification(unreadInvitation, {}, {})
    repository.createNotification(readInvitation, {}, {})

    // Get unread notifications
    var unreadNotifications: List<Notification>? = null
    repository.getUnreadNotifications(testUserId1, { unreadNotifications = it }, {})

    assertEquals(1, unreadNotifications!!.size)
    val unread = unreadNotifications!![0] as Notification.EventInvitation
    assertEquals("invite-1", unread.id)
    assertFalse(unread.isRead)
  }

  @Test
  fun deleteNotification_eventInvitation_deletesCorrectly() {
    val eventInvitation =
        Notification.EventInvitation(
            id = "invite-1",
            userId = testUserId1,
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    repository.createNotification(eventInvitation, {}, {})
    repository.createNotification(friendRequestNotification, {}, {})

    // Delete event invitation
    var successCalled = false
    repository.deleteNotification("invite-1", { successCalled = true }, {})

    assertTrue(successCalled)

    // Verify only friend request remains
    var notifications: List<Notification>? = null
    repository.getNotifications(testUserId1, { notifications = it }, {})

    assertEquals(1, notifications!!.size)
    assertTrue(notifications!![0] is Notification.FriendRequest)
  }
}
