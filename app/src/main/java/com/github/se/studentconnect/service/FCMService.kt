package com.github.se.studentconnect.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.se.studentconnect.MainActivity
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.notification.NotificationType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Date

/**
 * Firebase Cloud Messaging service for handling push notifications
 *
 * This service receives push notifications from FCM and displays them to the user It also stores
 * notifications in Firestore for in-app display
 */
class FCMService : FirebaseMessagingService() {

  companion object {
    private const val TAG = "FCMService"
  }

  /**
   * Called when a new FCM token is generated
   *
   * @param token The new FCM token
   */
  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM token: $token")
    // TODO: Send token to server if needed for targeted push notifications
  }

  /**
   * Called when a message is received from FCM
   *
   * @param remoteMessage The message received from FCM
   */
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "Message received from: ${remoteMessage.from}")

    // Extract notification data
    val data = remoteMessage.data
    val type = data["type"] ?: return
    val userId = Firebase.auth.currentUser?.uid ?: return

    when (type) {
      NotificationType.FRIEND_REQUEST.name -> handleFriendRequestNotification(data, userId)
      NotificationType.EVENT_STARTING.name -> handleEventStartingNotification(data, userId)
      else -> Log.w(TAG, "Unknown notification type: $type")
    }
  }

  /**
   * Handles friend request notifications
   *
   * @param data The notification data
   * @param userId The user ID
   */
  private fun handleFriendRequestNotification(data: Map<String, String>, userId: String) {
    val fromUserId = data["fromUserId"] ?: return
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

    // Show push notification
    showPushNotification(
        title = "New Friend Request",
        message = notification.getMessage(),
        channelId = NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID,
        notificationId = notificationId.hashCode())
  }

  /**
   * Handles event starting notifications
   *
   * @param data The notification data
   * @param userId The user ID
   */
  private fun handleEventStartingNotification(data: Map<String, String>, userId: String) {
    val eventId = data["eventId"] ?: return
    val eventTitle = data["eventTitle"] ?: "Event"
    val notificationId = data["notificationId"] ?: ""
    val eventStartMillis = data["eventStart"]?.toLongOrNull() ?: return

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

    // Show push notification
    showPushNotification(
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
  private fun storeNotification(notification: Notification) {
    try {
      val repository = NotificationRepositoryProvider.repository
      repository.createNotification(
          notification,
          onSuccess = { Log.d(TAG, "Notification stored in Firestore") },
          onFailure = { e -> Log.e(TAG, "Failed to store notification in Firestore", e) })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to get notification repository", e)
    }
  }

  /**
   * Shows a push notification to the user
   *
   * @param title The notification title
   * @param message The notification message
   * @param channelId The notification channel ID
   * @param notificationId The notification ID (for managing multiple notifications)
   */
  private fun showPushNotification(
      title: String,
      message: String,
      channelId: String,
      notificationId: Int
  ) {
    // Create intent to open the app when notification is tapped
    val intent =
        Intent(this, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    val pendingIntent =
        PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    // Build notification
    val notification =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

    // Show notification
    try {
      NotificationManagerCompat.from(this).notify(notificationId, notification)
    } catch (e: SecurityException) {
      Log.e(TAG, "Failed to show notification: missing permission", e)
    }
  }
}
