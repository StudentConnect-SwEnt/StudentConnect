package com.github.se.studentconnect.model.notification

/** Local in-memory implementation of NotificationRepository for testing */
class NotificationRepositoryLocal : NotificationRepository {

  private val notifications = mutableListOf<Notification>()

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
  }

  override fun markAsRead(
      notificationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }

  override fun markAllAsRead(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }

  override fun deleteNotification(
      notificationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    notifications.removeIf { it.id == notificationId }
    onSuccess()
  }

  override fun deleteAllNotifications(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    notifications.removeIf { getNotificationUserId(it) == userId }
    onSuccess()
  }

  override fun listenToNotifications(
      userId: String,
      onNotificationsChanged: (List<Notification>) -> Unit
  ): () -> Unit {
    // Immediately return empty list for tests
    onNotificationsChanged(emptyList())
    // Return no-op cleanup function
    return {}
  }

  private fun getNotificationUserId(notification: Notification): String {
    return notification.userId
  }
}
