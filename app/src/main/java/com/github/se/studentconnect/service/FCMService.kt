package com.github.se.studentconnect.service

import android.util.Log
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for handling push notifications
 *
 * This service receives push notifications from FCM and stores them in Firestore.
 * NotificationListenerService monitors Firestore and displays notifications to avoid duplicates.
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

    // Process and store message using handler
    // NotificationListenerService will detect the Firestore change and display the notification
    getFCMHandler().processMessage(remoteMessage.data)
  }
}
