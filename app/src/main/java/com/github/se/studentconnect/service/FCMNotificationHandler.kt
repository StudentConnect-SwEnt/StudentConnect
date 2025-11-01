package com.github.se.studentconnect.service

import android.util.Log
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationType
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Handler for FCM notification business logic
 *
 * This class is separated from FCMService to make it testable without Android dependencies
 */
class FCMNotificationHandler(
    private val notificationRepository: NotificationRepository,
    private val getCurrentUserId: () -> String?
) {

  companion object {
    private const val TAG = "FCMNotificationHandler"
  }

  /**
   * Processes a received FCM message and returns the notification to display
   *
   * @param data The FCM message data
   * @return Pair of (title, message, channelId, notificationId) or null if message should be
   *   ignored
   */
  fun processMessage(data: Map<String, String>): NotificationInfo? {
    val type = data["type"] ?: return null
    val userId = getCurrentUserId() ?: return null

    return when (type) {
      NotificationType.FRIEND_REQUEST.name -> processFriendRequest(data, userId)
      NotificationType.EVENT_STARTING.name -> processEventStarting(data, userId)
      else -> {
        Log.w(TAG, "Unknown notification type: $type")
        null
      }
    }
  }

  /**
   * Processes a friend request notification
   *
   * @param data The notification data
   * @param userId The user ID
   * @return NotificationInfo or null if invalid data
   */
  fun processFriendRequest(data: Map<String, String>, userId: String): NotificationInfo? {
    val fromUserId = data["fromUserId"] ?: return null
    val fromUserName = data["fromUserName"] ?: "Someone"
    val notificationId = data["notificationId"] ?: ""

    // Create notification object
    val notification =
        Notification.FriendRequest(
            id = notificationId,
            userId = userId,
            fromUserId = fromUserId,
            fromUserName = fromUserName,
            timestamp = Timestamp.now(),
            isRead = false)

    // Store in Firestore
    storeNotification(notification)

    return NotificationInfo(
        title = "New Friend Request",
        message = notification.getMessage(),
        channelId = NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID,
        notificationId = notificationId.hashCode())
  }

  /**
   * Processes an event starting notification
   *
   * @param data The notification data
   * @param userId The user ID
   * @return NotificationInfo or null if invalid data
   */
  fun processEventStarting(data: Map<String, String>, userId: String): NotificationInfo? {
    val eventId = data["eventId"] ?: return null
    val eventTitle = data["eventTitle"] ?: "Event"
    val notificationId = data["notificationId"] ?: ""
    val eventStartMillis = data["eventStart"]?.toLongOrNull() ?: return null

    // Create notification object
    val notification =
        Notification.EventStarting(
            id = notificationId,
            userId = userId,
            eventId = eventId,
            eventTitle = eventTitle,
            eventStart = Timestamp(Date(eventStartMillis)),
            timestamp = Timestamp.now(),
            isRead = false)

    // Store in Firestore
    storeNotification(notification)

    return NotificationInfo(
        title = "Event Starting Soon",
        message = notification.getMessage(),
        channelId = NotificationChannelManager.EVENT_STARTING_CHANNEL_ID,
        notificationId = notificationId.hashCode())
  }

  /**
   * Stores a notification in Firestore
   *
   * @param notification The notification to store
   */
  fun storeNotification(notification: Notification) {
    try {
      notificationRepository.createNotification(
          notification,
          onSuccess = { Log.d(TAG, "Notification stored in Firestore") },
          onFailure = { e -> Log.e(TAG, "Failed to store notification in Firestore", e) })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to store notification", e)
    }
  }
}

/** Data class for notification display information */
data class NotificationInfo(
    val title: String,
    val message: String,
    val channelId: String,
    val notificationId: Int
)
