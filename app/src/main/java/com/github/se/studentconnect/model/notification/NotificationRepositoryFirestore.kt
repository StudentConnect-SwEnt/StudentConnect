package com.github.se.studentconnect.model.notification

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Firestore implementation of NotificationRepository
 *
 * Stores notifications in: notifications/{notificationId}
 *
 * Each notification document contains: - id: Notification ID - userId: User who receives the
 * notification - type: Type of notification (FRIEND_REQUEST, EVENT_STARTING) - timestamp: When the
 * notification was created - isRead: Whether the notification has been read - Type-specific fields
 * (fromUserId, eventId, etc.)
 *
 * @property db The Firestore database instance
 */
class NotificationRepositoryFirestore(private val db: FirebaseFirestore) : NotificationRepository {

  companion object {
    private const val NOTIFICATIONS_COLLECTION = "notifications"
  }

  /** Gets the currently authenticated user ID */
  private fun getCurrentUserId(): String? {
    return Firebase.auth.currentUser?.uid
  }

  override fun getNotifications(
      userId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(NOTIFICATIONS_COLLECTION)
        .whereEqualTo("userId", userId)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { snapshot ->
          val notifications =
              snapshot.documents.mapNotNull { doc ->
                try {
                  Notification.fromMap(doc.data ?: emptyMap())
                } catch (e: Exception) {
                  null // Skip malformed notifications
                }
              }
          onSuccess(notifications)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getUnreadNotifications(
      userId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(NOTIFICATIONS_COLLECTION)
        .whereEqualTo("userId", userId)
        .whereEqualTo("isRead", false)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { snapshot ->
          val notifications =
              snapshot.documents.mapNotNull { doc ->
                try {
                  Notification.fromMap(doc.data ?: emptyMap())
                } catch (e: Exception) {
                  null
                }
              }
          onSuccess(notifications)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun createNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Generate ID if not provided
    val notificationId =
        notification.id.ifEmpty { db.collection(NOTIFICATIONS_COLLECTION).document().id }

    val notificationWithId =
        when (notification) {
          is Notification.FriendRequest -> notification.copy(id = notificationId)
          is Notification.EventStarting -> notification.copy(id = notificationId)
        }

    db.collection(NOTIFICATIONS_COLLECTION)
        .document(notificationId)
        .set(notificationWithId.toMap())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun markAsRead(
      notificationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(NOTIFICATIONS_COLLECTION)
        .document(notificationId)
        .update("isRead", true)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun markAllAsRead(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(NOTIFICATIONS_COLLECTION)
        .whereEqualTo("userId", userId)
        .whereEqualTo("isRead", false)
        .get()
        .addOnSuccessListener { snapshot ->
          val batch = db.batch()
          snapshot.documents.forEach { doc -> batch.update(doc.reference, "isRead", true) }
          batch
              .commit()
              .addOnSuccessListener { onSuccess() }
              .addOnFailureListener { exception -> onFailure(exception) }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun deleteNotification(
      notificationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(NOTIFICATIONS_COLLECTION)
        .document(notificationId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun deleteAllNotifications(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(NOTIFICATIONS_COLLECTION)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { snapshot ->
          val batch = db.batch()
          snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
          batch
              .commit()
              .addOnSuccessListener { onSuccess() }
              .addOnFailureListener { exception -> onFailure(exception) }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun listenToNotifications(
      userId: String,
      onNotificationsChanged: (List<Notification>) -> Unit
  ): () -> Unit {
    val listenerRegistration: ListenerRegistration =
        db.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                // In case of error, return empty list
                onNotificationsChanged(emptyList())
                return@addSnapshotListener
              }

              if (snapshot != null) {
                val notifications =
                    snapshot.documents.mapNotNull { doc ->
                      try {
                        Notification.fromMap(doc.data ?: emptyMap())
                      } catch (e: Exception) {
                        null
                      }
                    }
                onNotificationsChanged(notifications)
              } else {
                onNotificationsChanged(emptyList())
              }
            }

    // Return a function that removes the listener
    return { listenerRegistration.remove() }
  }
}
