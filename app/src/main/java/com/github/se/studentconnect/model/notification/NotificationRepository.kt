package com.github.se.studentconnect.model.notification

import com.github.se.studentconnect.model.Repository

/** Repository interface for managing notifications */
interface NotificationRepository : Repository {

  /**
   * Gets all notifications for a user
   *
   * @param userId The ID of the user
   * @param onSuccess Callback with the list of notifications (sorted by timestamp, newest first)
   * @param onFailure Callback with the exception if the operation fails
   */
  fun getNotifications(
      userId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Gets unread notifications for a user
   *
   * @param userId The ID of the user
   * @param onSuccess Callback with the list of unread notifications
   * @param onFailure Callback with the exception if the operation fails
   */
  fun getUnreadNotifications(
      userId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Creates a new notification
   *
   * @param notification The notification to create
   * @param onSuccess Callback when the operation succeeds
   * @param onFailure Callback with the exception if the operation fails
   */
  fun createNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Marks a notification as read
   *
   * @param notificationId The ID of the notification
   * @param onSuccess Callback when the operation succeeds
   * @param onFailure Callback with the exception if the operation fails
   */
  fun markAsRead(notificationId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Marks all notifications for a user as read
   *
   * @param userId The ID of the user
   * @param onSuccess Callback when the operation succeeds
   * @param onFailure Callback with the exception if the operation fails
   */
  fun markAllAsRead(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a notification
   *
   * @param notificationId The ID of the notification
   * @param onSuccess Callback when the operation succeeds
   * @param onFailure Callback with the exception if the operation fails
   */
  fun deleteNotification(
      notificationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes all notifications for a user
   *
   * @param userId The ID of the user
   * @param onSuccess Callback when the operation succeeds
   * @param onFailure Callback with the exception if the operation fails
   */
  fun deleteAllNotifications(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Listens for real-time updates to notifications for a user
   *
   * @param userId The ID of the user
   * @param onNotificationsChanged Callback with the updated list of notifications
   * @return A function that can be called to stop listening
   */
  fun listenToNotifications(
      userId: String,
      onNotificationsChanged: (List<Notification>) -> Unit
  ): () -> Unit
}
