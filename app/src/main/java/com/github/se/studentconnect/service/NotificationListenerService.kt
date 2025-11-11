package com.github.se.studentconnect.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.se.studentconnect.MainActivity
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth

/**
 * Background service that listens for new notifications in Firestore and displays them as Android
 * push notifications
 *
 * This service runs in the background and monitors the user's notifications collection. When a new
 * unread notification is detected, it displays an Android system notification.
 */
class NotificationListenerService : Service() {

  companion object {
    private const val TAG = "NotificationListener"
    private const val NOTIFICATION_ID_BASE = 10000

    /** Starts the notification listener service */
    fun start(context: Context) {
      val intent = Intent(context, NotificationListenerService::class.java)
      context.startService(intent)
    }

    /** Stops the notification listener service */
    fun stop(context: Context) {
      val intent = Intent(context, NotificationListenerService::class.java)
      context.stopService(intent)
    }
  }

  private var stopListening: (() -> Unit)? = null
  private val displayedNotifications = mutableSetOf<String>()

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "NotificationListenerService created")
    startListening()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(TAG, "NotificationListenerService started")
    return START_STICKY // Restart service if killed
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d(TAG, "NotificationListenerService destroyed")
    stopListening?.invoke()
    stopListening = null
  }

  /** Starts listening to Firestore for new notifications */
  private fun startListening() {
    val userId = Firebase.auth.currentUser?.uid
    if (userId == null) {
      Log.w(TAG, "No user logged in, cannot listen to notifications")
      stopSelf()
      return
    }

    Log.d(TAG, "Starting to listen for notifications for user: $userId")

    val repository = NotificationRepositoryProvider.repository
    stopListening =
        repository.listenToNotifications(userId) { notifications ->
          Log.d(TAG, "Received ${notifications.size} notifications")

          // Filter for unread notifications
          val unreadNotifications = notifications.filter { !it.isRead }

          // Display each unread notification that we haven't displayed yet
          unreadNotifications.forEach { notification ->
            if (!displayedNotifications.contains(notification.id)) {
              Log.d(TAG, "Displaying new notification: ${notification.id}")
              showPushNotification(notification)
              displayedNotifications.add(notification.id)
            }
          }

          // Clean up displayed notifications that are now read or deleted
          displayedNotifications.retainAll(notifications.map { it.id }.toSet())
        }
  }

  /**
   * Displays an Android push notification for the given notification
   *
   * @param notification The notification to display
   */
  private fun showPushNotification(notification: Notification) {
    val (title, message, channelId) = getNotificationContent(notification)

    // Create intent to open the app when notification is tapped
    val intent =
        Intent(this, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
          putExtra("notification_id", notification.id)
        }

    val pendingIntent =
        PendingIntent.getActivity(
            this, notification.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)

    // Get username and time info
    val (username, timeAgo) = getUsernameAndTime(notification)

    // Build the notification with BigTextStyle to show username and time
    val bigTextStyle =
        NotificationCompat.BigTextStyle().bigText(message).setSummaryText("$timeAgo • $username")

    val notificationBuilder =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(bigTextStyle)
            .setSubText("$timeAgo • $username")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

    // Show the notification
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = NOTIFICATION_ID_BASE + notification.id.hashCode()
    notificationManager.notify(notificationId, notificationBuilder.build())

    Log.d(TAG, "Push notification displayed: $title")
  }

  /**
   * Extracts the title, message, and channel ID from a notification
   *
   * @param notification The notification
   * @return Triple of (title, message, channelId)
   */
  private fun getNotificationContent(notification: Notification): Triple<String, String, String> {
    return when (notification) {
      is Notification.FriendRequest -> {
        Triple(
            "Friend Request",
            "${notification.fromUserName} sent you a friend request",
            NotificationChannelManager.FRIEND_REQUEST_CHANNEL_ID)
      }
      is Notification.EventStarting -> {
        Triple(
            "Event Starting Soon",
            "${notification.eventTitle} is starting soon!",
            NotificationChannelManager.EVENT_STARTING_CHANNEL_ID)
      }
    }
  }

  /**
   * Extracts username and time difference from a notification
   *
   * @param notification The notification
   * @return Pair of (username, timeAgo)
   */
  private fun getUsernameAndTime(notification: Notification): Pair<String, String> {
    val username =
        when (notification) {
          is Notification.FriendRequest -> notification.fromUserId
          is Notification.EventStarting -> notification.userId
        }

    val timeAgo =
        notification.timestamp?.let {
          val now = System.currentTimeMillis()
          val notificationTime = it.toDate().time
          val diffMillis = now - notificationTime

          when {
            diffMillis < 60_000 -> "just now"
            diffMillis < 3600_000 -> "${diffMillis / 60_000}m ago"
            diffMillis < 86400_000 -> "${diffMillis / 3600_000}h ago"
            else -> "${diffMillis / 86400_000}d ago"
          }
        } ?: "just now"

    return Pair(username, timeAgo)
  }
}
