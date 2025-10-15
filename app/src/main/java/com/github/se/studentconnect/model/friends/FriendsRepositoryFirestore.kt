package com.github.se.studentconnect.model.friends

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

/**
 * Implementation of FriendsRepository using Firebase Firestore.
 *
 * This implementation stores friends data in subcollections under each user document:
 * - users/{userId}/friends/{friendId} - Contains confirmed friendships
 * - users/{userId}/friendRequests/{requesterId} - Contains pending incoming requests
 * - users/{userId}/sentRequests/{recipientId} - Contains pending outgoing requests
 *
 * All operations are centered around the currently authenticated user for security.
 *
 * @property db The Firestore database instance.
 */
class FriendsRepositoryFirestore(private val db: FirebaseFirestore) : FriendsRepository {

  companion object {
    private const val USERS_COLLECTION = "users"
    private const val FRIENDS_SUBCOLLECTION = "friends"
    private const val FRIEND_REQUESTS_SUBCOLLECTION = "friendRequests"
    private const val SENT_REQUESTS_SUBCOLLECTION = "sentRequests"
  }

  /**
   * Gets the currently authenticated user ID.
   *
   * @throws IllegalAccessException if no user is currently authenticated.
   */
  private fun getCurrentUserId(): String {
    return Firebase.auth.currentUser?.uid
        ?: throw IllegalAccessException("User must be logged in for this action")
  }

  /**
   * Ensures that the requested userId matches the currently authenticated user.
   *
   * @param userId The user ID to verify.
   * @throws IllegalAccessException if the userId doesn't match the current user.
   */
  private fun ensureCurrentUser(userId: String) {
    val currentUserId = getCurrentUserId()
    if (userId != currentUserId) {
      throw IllegalAccessException("Users can only access their own friend data")
    }
  }

  override suspend fun getFriends(userId: String): List<String> {
    ensureCurrentUser(userId)
    val snapshot =
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FRIENDS_SUBCOLLECTION)
            .get()
            .await()

    return snapshot.documents.mapNotNull { it.id }
  }

  override suspend fun getPendingRequests(userId: String): List<String> {
    ensureCurrentUser(userId)
    val snapshot =
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FRIEND_REQUESTS_SUBCOLLECTION)
            .get()
            .await()

    return snapshot.documents.mapNotNull { it.id }
  }

  override suspend fun getSentRequests(userId: String): List<String> {
    ensureCurrentUser(userId)
    val snapshot =
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(SENT_REQUESTS_SUBCOLLECTION)
            .get()
            .await()

    return snapshot.documents.mapNotNull { it.id }
  }

  override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) {
    ensureCurrentUser(fromUserId)

    // Validate that recipient user exists
    val toUserDoc = db.collection(USERS_COLLECTION).document(toUserId).get().await()

    if (!toUserDoc.exists()) {
      throw IllegalArgumentException("Recipient user not found: $toUserId")
    }

    // Check if users are the same
    if (fromUserId == toUserId) {
      throw IllegalArgumentException("Cannot send friend request to yourself")
    }

    // Check if already friends (check from sender's perspective)
    val friendDoc =
        db.collection(USERS_COLLECTION)
            .document(fromUserId)
            .collection(FRIENDS_SUBCOLLECTION)
            .document(toUserId)
            .get()
            .await()

    if (friendDoc.exists()) {
      throw IllegalArgumentException("Users are already friends")
    }

    // Check if request already exists (check from sender's sent requests)
    val sentRequestDoc =
        db.collection(USERS_COLLECTION)
            .document(fromUserId)
            .collection(SENT_REQUESTS_SUBCOLLECTION)
            .document(toUserId)
            .get()
            .await()

    if (sentRequestDoc.exists()) {
      throw IllegalArgumentException("Friend request already sent")
    }

    // Check if reverse request exists (check from sender's pending requests)
    val reverseRequestDoc =
        db.collection(USERS_COLLECTION)
            .document(fromUserId)
            .collection(FRIEND_REQUESTS_SUBCOLLECTION)
            .document(toUserId)
            .get()
            .await()

    if (reverseRequestDoc.exists()) {
      throw IllegalArgumentException(
          "A friend request from the recipient already exists. Accept their request instead.")
    }

    val timestamp = FieldValue.serverTimestamp()
    val requestData = mapOf("timestamp" to timestamp)

    try {
      // Add to recipient's pending requests
      db.collection(USERS_COLLECTION)
          .document(toUserId)
          .collection(FRIEND_REQUESTS_SUBCOLLECTION)
          .document(fromUserId)
          .set(requestData)
          .await()

      // Add to sender's sent requests
      db.collection(USERS_COLLECTION)
          .document(fromUserId)
          .collection(SENT_REQUESTS_SUBCOLLECTION)
          .document(toUserId)
          .set(requestData)
          .await()
    } catch (e: FirebaseFirestoreException) {
      if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        throw IllegalAccessException("Permission denied: Cannot send friend request")
      }
      throw e
    }
  }

  override suspend fun acceptFriendRequest(userId: String, fromUserId: String) {
    ensureCurrentUser(userId)

    // Verify the request exists
    val requestDoc =
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FRIEND_REQUESTS_SUBCOLLECTION)
            .document(fromUserId)
            .get()
            .await()

    if (!requestDoc.exists()) {
      throw IllegalArgumentException("No pending friend request from user: $fromUserId")
    }

    val timestamp = FieldValue.serverTimestamp()
    val friendData = mapOf("timestamp" to timestamp)

    try {
      // Add to recipient's friends list (current user can write to their own data)
      db.collection(USERS_COLLECTION)
          .document(userId)
          .collection(FRIENDS_SUBCOLLECTION)
          .document(fromUserId)
          .set(friendData)
          .await()

      // Remove from recipient's pending requests (current user can delete their own data)
      db.collection(USERS_COLLECTION)
          .document(userId)
          .collection(FRIEND_REQUESTS_SUBCOLLECTION)
          .document(fromUserId)
          .delete()
          .await()

      // Add to sender's friends list (Firestore rules should allow this write)
      db.collection(USERS_COLLECTION)
          .document(fromUserId)
          .collection(FRIENDS_SUBCOLLECTION)
          .document(userId)
          .set(friendData)
          .await()

      // Remove from sender's sent requests (Firestore rules should allow this delete)
      db.collection(USERS_COLLECTION)
          .document(fromUserId)
          .collection(SENT_REQUESTS_SUBCOLLECTION)
          .document(userId)
          .delete()
          .await()
    } catch (e: FirebaseFirestoreException) {
      if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        throw IllegalAccessException("Permission denied: Cannot accept friend request")
      }
      throw e
    }
  }

  override suspend fun rejectFriendRequest(userId: String, fromUserId: String) {
    ensureCurrentUser(userId)

    // Verify the request exists
    val requestDoc =
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FRIEND_REQUESTS_SUBCOLLECTION)
            .document(fromUserId)
            .get()
            .await()

    if (!requestDoc.exists()) {
      throw IllegalArgumentException("No pending friend request from user: $fromUserId")
    }

    try {
      // Remove from recipient's pending requests (current user can delete their own data)
      db.collection(USERS_COLLECTION)
          .document(userId)
          .collection(FRIEND_REQUESTS_SUBCOLLECTION)
          .document(fromUserId)
          .delete()
          .await()

      // Remove from sender's sent requests (Firestore rules should allow this delete)
      db.collection(USERS_COLLECTION)
          .document(fromUserId)
          .collection(SENT_REQUESTS_SUBCOLLECTION)
          .document(userId)
          .delete()
          .await()
    } catch (e: FirebaseFirestoreException) {
      if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        throw IllegalAccessException("Permission denied: Cannot reject friend request")
      }
      throw e
    }
  }

  override suspend fun cancelFriendRequest(userId: String, toUserId: String) {
    ensureCurrentUser(userId)

    // Verify the sent request exists
    val requestDoc =
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(SENT_REQUESTS_SUBCOLLECTION)
            .document(toUserId)
            .get()
            .await()

    if (!requestDoc.exists()) {
      throw IllegalArgumentException("No sent friend request to user: $toUserId")
    }

    try {
      // Remove from sender's sent requests
      db.collection(USERS_COLLECTION)
          .document(userId)
          .collection(SENT_REQUESTS_SUBCOLLECTION)
          .document(toUserId)
          .delete()
          .await()

      // Remove from recipient's pending requests
      db.collection(USERS_COLLECTION)
          .document(toUserId)
          .collection(FRIEND_REQUESTS_SUBCOLLECTION)
          .document(userId)
          .delete()
          .await()
    } catch (e: FirebaseFirestoreException) {
      if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        throw IllegalAccessException("Permission denied: Cannot cancel friend request")
      }
      throw e
    }
  }

  override suspend fun removeFriend(userId: String, friendId: String) {
    ensureCurrentUser(userId)

    // Verify friendship exists
    val friendDoc =
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FRIENDS_SUBCOLLECTION)
            .document(friendId)
            .get()
            .await()

    if (!friendDoc.exists()) {
      throw IllegalArgumentException("Users are not friends")
    }

    try {
      // Remove from both users' friends lists
      db.collection(USERS_COLLECTION)
          .document(userId)
          .collection(FRIENDS_SUBCOLLECTION)
          .document(friendId)
          .delete()
          .await()

      db.collection(USERS_COLLECTION)
          .document(friendId)
          .collection(FRIENDS_SUBCOLLECTION)
          .document(userId)
          .delete()
          .await()
    } catch (e: FirebaseFirestoreException) {
      if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        throw IllegalAccessException("Permission denied: Cannot remove friend")
      }
      throw e
    }
  }

  override suspend fun areFriends(userId: String, otherUserId: String): Boolean {
    ensureCurrentUser(userId)

    val friendDoc =
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FRIENDS_SUBCOLLECTION)
            .document(otherUserId)
            .get()
            .await()

    return friendDoc.exists()
  }

  override suspend fun hasPendingRequest(fromUserId: String, toUserId: String): Boolean {
    val currentUserId = getCurrentUserId()

    // User can only check pending requests where they are involved (sender or receiver)
    if (currentUserId != fromUserId && currentUserId != toUserId) {
      throw IllegalAccessException(
          "Users can only check pending requests where they are the sender or receiver")
    }

    // Check from the current user's perspective
    val requestDoc =
        if (currentUserId == toUserId) {
          // Check if current user has a pending request from fromUserId
          db.collection(USERS_COLLECTION)
              .document(toUserId)
              .collection(FRIEND_REQUESTS_SUBCOLLECTION)
              .document(fromUserId)
              .get()
              .await()
        } else {
          // Check if current user has sent a request to toUserId
          db.collection(USERS_COLLECTION)
              .document(fromUserId)
              .collection(SENT_REQUESTS_SUBCOLLECTION)
              .document(toUserId)
              .get()
              .await()
        }

    return requestDoc.exists()
  }
}
