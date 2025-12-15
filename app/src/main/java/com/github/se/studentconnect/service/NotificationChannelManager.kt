package com.github.se.studentconnect.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

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

  /** Channel ID for event invitation notifications */
  const val EVENT_INVITATION_CHANNEL_ID = "event_invitations"

  /** Channel name for friend requests */
  private const val FRIEND_REQUEST_CHANNEL_NAME = "Friend Requests"

  /** Channel name for event starting */
  private const val EVENT_STARTING_CHANNEL_NAME = "Event Reminders"

  /** Channel name for event invitations */
  private const val EVENT_INVITATION_CHANNEL_NAME = "Event Invitations"

  /**
   * Creates all notification channels for the app
   *
   * @param context The application context
   */
  fun createNotificationChannels(context: Context) {
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

    // Event Invitation Channel
    val eventInvitationChannel =
        NotificationChannel(
                EVENT_INVITATION_CHANNEL_ID,
                EVENT_INVITATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
            .apply {
              description = "Notifications for private event invitations"
              enableLights(true)
              enableVibration(true)
            }

    notificationManager.createNotificationChannel(friendRequestChannel)
    notificationManager.createNotificationChannel(eventStartingChannel)
    notificationManager.createNotificationChannel(eventInvitationChannel)
  }
}
