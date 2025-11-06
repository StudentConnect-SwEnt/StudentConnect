package com.github.se.studentconnect.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.se.studentconnect.MainActivity
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

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

  // Notification handler for business logic (injected for testability)
  internal var fcmHandler: FCMNotificationHandler? = null

  internal fun getFCMHandler(): FCMNotificationHandler {
    if (fcmHandler == null) {
      fcmHandler =
          FCMNotificationHandler(
              notificationRepository = NotificationRepositoryProvider.repository,
              getCurrentUserId = { Firebase.auth.currentUser?.uid })
    }
    return fcmHandler!!
  }

  /**
   * Called when a new FCM token is generated
   *
   * @param token The new FCM token
   */
  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM token: $token")
    // maybe if needed: Send token to server if needed for targeted push notifications
  }

  /**
   * Called when a message is received from FCM
   *
   * @param remoteMessage The message received from FCM
   */
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "Message received from: ${remoteMessage.from}")

    // Process message using handler
    val notificationInfo = getFCMHandler().processMessage(remoteMessage.data)

    // Show push notification if valid
    notificationInfo?.let {
      showPushNotification(it.title, it.message, it.channelId, it.notificationId)
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
  internal fun showPushNotification(
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
