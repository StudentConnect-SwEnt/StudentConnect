package com.github.se.studentconnect.model.notification

/** Local in-memory implementation of NotificationRepository for testing */
class NotificationRepositoryLocal : NotificationRepository {

  private val notifications = mutableListOf<Notification>()
  private val listeners =
      mutableListOf<Pair<String, (List<Notification>) -> Unit>>() // (userId, callback)

  override fun getNotifications(
      userId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(notifications.filter { getNotificationUserId(it) == userId })
  }

  override fun getUnreadNotifications(
      userId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(notifications.filter { getNotificationUserId(it) == userId && !it.isRead })
  }

  override fun createNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    notifications.add(notification)
    onSuccess()
    // Notify all listeners for this user
    notifyListeners(getNotificationUserId(notification))
  }

  override fun markAsRead(
      notificationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val notification = notifications.find { it.id == notificationId }
    if (notification != null) {
      val index = notifications.indexOf(notification)
      val updated =
          when (notification) {
            is Notification.FriendRequest -> notification.copy(isRead = true)
            is Notification.EventStarting -> notification.copy(isRead = true)
          }
      notifications[index] = updated
      notifyListeners(getNotificationUserId(updated))
      onSuccess()
    } else {
      onFailure(Exception("Notification with id $notificationId not found"))
    }
  }

  override fun markAllAsRead(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    for (i in notifications.indices) {
      val notification = notifications[i]
      if (getNotificationUserId(notification) == userId && !notification.isRead) {
        notifications[i] =
            when (notification) {
              is Notification.FriendRequest -> notification.copy(isRead = true)
              is Notification.EventStarting -> notification.copy(isRead = true)
            }
      }
    }
    onSuccess()
    notifyListeners(userId)
  }

  override fun deleteNotification(
      notificationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val notification = notifications.find { it.id == notificationId }
    if (notification != null) {
      val userId = getNotificationUserId(notification)
      notifications.removeIf { it.id == notificationId }
      onSuccess()
      notifyListeners(userId)
    } else {
      onFailure(Exception("Notification with id $notificationId not found"))
    }
  }

  override fun deleteAllNotifications(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    notifications.removeIf { getNotificationUserId(it) == userId }
    onSuccess()
    notifyListeners(userId)
  }

  override fun listenToNotifications(
      userId: String,
      onNotificationsChanged: (List<Notification>) -> Unit
  ): () -> Unit {
    // Add listener
    val listener = Pair(userId, onNotificationsChanged)
    listeners.add(listener)

    // Immediately return current notifications for the user
    onNotificationsChanged(notifications.filter { getNotificationUserId(it) == userId })

    // Return cleanup function that removes this listener
    return { listeners.remove(listener) }
  }

  private fun notifyListeners(userId: String) {
    val userNotifications = notifications.filter { getNotificationUserId(it) == userId }
    listeners.filter { it.first == userId }.forEach { it.second(userNotifications) }
  }

  private fun getNotificationUserId(notification: Notification): String {
    return notification.userId
  }
}
