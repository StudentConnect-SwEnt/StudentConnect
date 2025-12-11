package com.github.se.studentconnect.model.notification

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/** Represents the type of notification */
enum class NotificationType {
  FRIEND_REQUEST,
  EVENT_STARTING,
  ORGANIZATION_MEMBER_INVITATION
}

/** Sealed class representing different types of notifications */
sealed class Notification {
  abstract val id: String
  abstract val userId: String
  abstract val timestamp: Timestamp?
  abstract val isRead: Boolean
  abstract val type: NotificationType

  /**
   * Friend request notification - sent when someone sends a friend request
   *
   * @param id Unique identifier for the notification
   * @param userId The user who should receive this notification
   * @param fromUserId The user who sent the friend request
   * @param fromUserName The name of the user who sent the request
   * @param timestamp When the notification was created
   * @param isRead Whether the notification has been read
   */
  data class FriendRequest(
      override val id: String = "",
      override val userId: String = "",
      val fromUserId: String = "",
      val fromUserName: String = "",
      @ServerTimestamp override val timestamp: Timestamp? = null,
      override val isRead: Boolean = false
  ) : Notification() {
    override val type = NotificationType.FRIEND_REQUEST

    /** Returns a human-readable message for the notification */
    fun getMessage(): String = "$fromUserName sent you a friend request"
  }

  /**
   * Event starting notification - sent when an event the user is participating in is about to start
   *
   * @param id Unique identifier for the notification
   * @param userId The user who should receive this notification
   * @param eventId The ID of the event that is starting
   * @param eventTitle The title of the event
   * @param eventStart The start time of the event
   * @param timestamp When the notification was created
   * @param isRead Whether the notification has been read
   */
  data class EventStarting(
      override val id: String = "",
      override val userId: String = "",
      val eventId: String = "",
      val eventTitle: String = "",
      val eventStart: Timestamp? = null,
      @ServerTimestamp override val timestamp: Timestamp? = null,
      override val isRead: Boolean = false
  ) : Notification() {
    override val type = NotificationType.EVENT_STARTING

    /** Returns a human-readable message for the notification */
    fun getMessage(): String = "Event \"$eventTitle\" is starting soon"
  }

  /**
   * Organization member invitation notification - sent when someone invites a user to join an
   * organization
   *
   * @param id Unique identifier for the notification
   * @param userId The user who should receive this notification
   * @param organizationId The ID of the organization
   * @param organizationName The name of the organization
   * @param role The role being offered in the organization
   * @param invitedBy The ID of the user who sent the invitation
   * @param invitedByName The name of the user who sent the invitation
   * @param timestamp When the notification was created
   * @param isRead Whether the notification has been read
   */
  data class OrganizationMemberInvitation(
      override val id: String = "",
      override val userId: String = "",
      val organizationId: String = "",
      val organizationName: String = "",
      val role: String = "",
      val invitedBy: String = "",
      val invitedByName: String = "",
      @ServerTimestamp override val timestamp: Timestamp? = null,
      override val isRead: Boolean = false
  ) : Notification() {
    override val type = NotificationType.ORGANIZATION_MEMBER_INVITATION

    /** Returns a human-readable message for the notification */
    fun getMessage(): String = "$invitedByName invited you to join \"$organizationName\" as $role"
  }

  /** Converts the notification to a map for Firestore storage */
  fun toMap(): Map<String, Any?> {
    return when (this) {
      is FriendRequest ->
          mapOf(
              "id" to id,
              "userId" to userId,
              "type" to type.name,
              "fromUserId" to fromUserId,
              "fromUserName" to fromUserName,
              "timestamp" to timestamp,
              "isRead" to isRead)
      is EventStarting ->
          mapOf(
              "id" to id,
              "userId" to userId,
              "type" to type.name,
              "eventId" to eventId,
              "eventTitle" to eventTitle,
              "eventStart" to eventStart,
              "timestamp" to timestamp,
              "isRead" to isRead)
      is OrganizationMemberInvitation ->
          mapOf(
              "id" to id,
              "userId" to userId,
              "type" to type.name,
              "organizationId" to organizationId,
              "organizationName" to organizationName,
              "role" to role,
              "invitedBy" to invitedBy,
              "invitedByName" to invitedByName,
              "timestamp" to timestamp,
              "isRead" to isRead)
    }
  }

  companion object {
    /**
     * Creates a Notification from a Firestore document map
     *
     * @param map The map from Firestore
     * @return The corresponding Notification object, or null if the type is unknown
     */
    fun fromMap(map: Map<String, Any?>): Notification? {
      val typeString = map["type"] as? String ?: return null
      val type =
          try {
            NotificationType.valueOf(typeString)
          } catch (e: IllegalArgumentException) {
            return null
          }

      return when (type) {
        NotificationType.FRIEND_REQUEST ->
            FriendRequest(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                fromUserId = map["fromUserId"] as? String ?: "",
                fromUserName = map["fromUserName"] as? String ?: "",
                timestamp = map["timestamp"] as? Timestamp,
                isRead = map["isRead"] as? Boolean ?: false)
        NotificationType.EVENT_STARTING ->
            EventStarting(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                eventId = map["eventId"] as? String ?: "",
                eventTitle = map["eventTitle"] as? String ?: "",
                eventStart = map["eventStart"] as? Timestamp,
                timestamp = map["timestamp"] as? Timestamp,
                isRead = map["isRead"] as? Boolean ?: false)
        NotificationType.ORGANIZATION_MEMBER_INVITATION ->
            OrganizationMemberInvitation(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                organizationId = map["organizationId"] as? String ?: "",
                organizationName = map["organizationName"] as? String ?: "",
                role = map["role"] as? String ?: "",
                invitedBy = map["invitedBy"] as? String ?: "",
                invitedByName = map["invitedByName"] as? String ?: "",
                timestamp = map["timestamp"] as? Timestamp,
                isRead = map["isRead"] as? Boolean ?: false)
      }
    }
  }
}
