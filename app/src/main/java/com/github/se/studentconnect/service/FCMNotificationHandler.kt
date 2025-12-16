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
   * Processes a received FCM message and stores it in Firestore
   *
   * @param data The FCM message data
   */
  fun processMessage(data: Map<String, String>) {
    val type = data["type"] ?: return
    val userId = getCurrentUserId() ?: return

    when (type) {
      NotificationType.FRIEND_REQUEST.name -> processFriendRequest(data, userId)
      NotificationType.EVENT_STARTING.name -> processEventStarting(data, userId)
      NotificationType.EVENT_INVITATION.name -> processEventInvitation(data, userId)
      NotificationType.ORGANIZATION_MEMBER_INVITATION.name ->
          processOrganizationMemberInvitation(data, userId)
      else -> {
        Log.w(TAG, "Unknown notification type: $type")
      }
    }
  }

  /**
   * Processes a friend request notification
   *
   * @param data The notification data
   * @param userId The user ID
   */
  fun processFriendRequest(data: Map<String, String>, userId: String) {
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
  }

  /**
   * Processes an event starting notification
   *
   * @param data The notification data
   * @param userId The user ID
   */
  fun processEventStarting(data: Map<String, String>, userId: String) {
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
  }

  /**
   * Processes an event invitation notification
   *
   * @param data The notification data
   * @param userId The user ID
   */
  fun processEventInvitation(data: Map<String, String>, userId: String) {
    val eventId = data["eventId"] ?: return
    val eventTitle = data["eventTitle"] ?: "Event"
    val invitedBy = data["invitedBy"] ?: return
    val invitedByName = data["invitedByName"] ?: "Someone"
    val notificationId = data["notificationId"] ?: ""

    // Create notification object
    val notification =
        Notification.EventInvitation(
            id = notificationId,
            userId = userId,
            eventId = eventId,
            eventTitle = eventTitle,
            invitedBy = invitedBy,
            invitedByName = invitedByName,
            timestamp = Timestamp.now(),
            isRead = false)

    // Store in Firestore
    storeNotification(notification)
  }

  /**
   * Processes an organization member invitation notification
   *
   * @param data The notification data
   * @param userId The user ID
   */
  fun processOrganizationMemberInvitation(data: Map<String, String>, userId: String) {
    val organizationId = data["organizationId"] ?: return
    val organizationName = data["organizationName"] ?: "Organization"
    val role = data["role"] ?: "Member"
    val invitedBy = data["invitedBy"] ?: return
    val invitedByName = data["invitedByName"] ?: "Someone"
    val notificationId = data["notificationId"] ?: ""

    // Create notification object
    val notification =
        Notification.OrganizationMemberInvitation(
            id = notificationId,
            userId = userId,
            organizationId = organizationId,
            organizationName = organizationName,
            role = role,
            invitedBy = invitedBy,
            invitedByName = invitedByName,
            timestamp = Timestamp.now(),
            isRead = false)

    // Store in Firestore
    storeNotification(notification)
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
