package com.github.se.studentconnect.model.notification

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
          val documents = snapshot.documents
          if (documents.isEmpty()) {
            onSuccess()
            return@addOnSuccessListener
          }

          // Firestore batch limit is 500 operations, so chunk the documents
          val chunks = documents.chunked(500)
          var completedChunks = 0

          chunks.forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { doc -> batch.update(doc.reference, "isRead", true) }
            batch
                .commit()
                .addOnSuccessListener {
                  completedChunks++
                  if (completedChunks == chunks.size) {
                    onSuccess()
                  }
                }
                .addOnFailureListener { exception -> onFailure(exception) }
          }
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
          val documents = snapshot.documents
          if (documents.isEmpty()) {
            onSuccess()
            return@addOnSuccessListener
          }

          // Firestore batch limit is 500 operations, so chunk the documents
          val chunks = documents.chunked(500)
          var completedChunks = 0

          chunks.forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { doc -> batch.delete(doc.reference) }
            batch
                .commit()
                .addOnSuccessListener {
                  completedChunks++
                  if (completedChunks == chunks.size) {
                    onSuccess()
                  }
                }
                .addOnFailureListener { exception -> onFailure(exception) }
          }
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
                // Log error for debugging while gracefully handling by returning empty list
                android.util.Log.e(
                    "NotificationRepository", "Error listening to notifications", error)
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
