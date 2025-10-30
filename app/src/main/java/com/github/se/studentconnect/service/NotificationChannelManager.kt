package com.github.se.studentconnect.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Manages Android notification channels for the app
 *
 * Notification channels are required for Android O (API 26+) to show notifications
 */
object NotificationChannelManager {

  /** Channel ID for friend request notifications */
  const val FRIEND_REQUEST_CHANNEL_ID = "friend_requests"

  /** Channel ID for event starting notifications */
  const val EVENT_STARTING_CHANNEL_ID = "event_starting"

  /** Channel name for friend requests */
  private const val FRIEND_REQUEST_CHANNEL_NAME = "Friend Requests"

  /** Channel name for event starting */
  private const val EVENT_STARTING_CHANNEL_NAME = "Event Reminders"

  /**
   * Creates all notification channels for the app
   *
   * @param context The application context
   */
  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
          context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      // Friend Request Channel
      val friendRequestChannel =
          NotificationChannel(
                  FRIEND_REQUEST_CHANNEL_ID,
                  FRIEND_REQUEST_CHANNEL_NAME,
                  NotificationManager.IMPORTANCE_HIGH)
              .apply {
                description = "Notifications for new friend requests"
                enableLights(true)
                enableVibration(true)
              }

      // Event Starting Channel
      val eventStartingChannel =
          NotificationChannel(
                  EVENT_STARTING_CHANNEL_ID,
                  EVENT_STARTING_CHANNEL_NAME,
                  NotificationManager.IMPORTANCE_HIGH)
              .apply {
                description = "Notifications for events that are starting soon"
                enableLights(true)
                enableVibration(true)
              }

      notificationManager.createNotificationChannel(friendRequestChannel)
      notificationManager.createNotificationChannel(eventStartingChannel)
    }
  }
}
