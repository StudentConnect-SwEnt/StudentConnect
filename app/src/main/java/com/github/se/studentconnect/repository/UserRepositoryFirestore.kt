package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.EventRepositoryFirestore.Companion.EVENTS_COLLECTION_PATH
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.activities.InvitationStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Implementation of UserRepository using Firebase Firestore.
 *
 * @property db The Firestore database instance.
 */
class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  companion object {
    private const val COLLECTION_NAME = "users"
    private const val JOINED_EVENT = "joinedEvents"
    private const val INVITATIONS = "invitations"
    private const val FAVORITE_EVENTS = "favoriteEvents"
  }

  override suspend fun getUserById(userId: String): User? {
    val document = db.collection(COLLECTION_NAME).document(userId).get().await()

    return if (document.exists()) {
      User.fromMap(document.data ?: emptyMap())
    } else {
      null // Return null for non-existent users (e.g., first-time users during onboarding)
    }
  }

  override suspend fun getUserByEmail(email: String): User? {
    val querySnapshot = db.collection(COLLECTION_NAME).whereEqualTo("email", email).get().await()

    return if (!querySnapshot.isEmpty) {
      val document = querySnapshot.documents.first()
      User.fromMap(document.data ?: emptyMap())
    } else {
      null // Return null for non-existent users
    }
  }

  override suspend fun getUsersPaginated(
      limit: Int,
      lastUserId: String?
  ): Pair<List<User>, Boolean> {
    var query = db.collection(COLLECTION_NAME).orderBy("userId").limit((limit + 1).toLong())

    lastUserId?.let { query = query.startAfter(it) }

    val querySnapshot = query.get().await()

    val allUsers =
        querySnapshot.documents.mapNotNull { document -> User.fromMap(document.data ?: emptyMap()) }

    val hasMore = allUsers.size > limit
    val users = if (hasMore) allUsers.take(limit) else allUsers

    return users to hasMore
  }

  override suspend fun getAllUsers(): List<User> {
    val querySnapshot = db.collection(COLLECTION_NAME).get().await()

    return querySnapshot.documents.mapNotNull { document ->
      User.fromMap(document.data ?: emptyMap())
    }
  }

  override suspend fun saveUser(user: User) {
    db.collection(COLLECTION_NAME).document(user.userId).set(user.toMap()).await()
  }

  override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
    val updatesWithTimestamp = updates.toMutableMap()
    updatesWithTimestamp["updatedAt"] = FieldValue.serverTimestamp()

    db.collection(COLLECTION_NAME).document(userId).update(updatesWithTimestamp).await()
  }

  override suspend fun deleteUser(userId: String) {
    db.collection(COLLECTION_NAME).document(userId).delete().await()
  }

  override suspend fun getUsersByUniversity(university: String): List<User> {
    val querySnapshot =
        db.collection(COLLECTION_NAME).whereEqualTo("university", university).get().await()

    return querySnapshot.documents.mapNotNull { document ->
      User.fromMap(document.data ?: emptyMap())
    }
  }

  override suspend fun getUsersByHobby(hobby: String): List<User> {
    val querySnapshot =
        db.collection(COLLECTION_NAME).whereArrayContains("hobbies", hobby).get().await()

    return querySnapshot.documents.mapNotNull { document ->
      User.fromMap(document.data ?: emptyMap())
    }
  }

  override suspend fun joinEvent(eventId: String, userId: String) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .collection(JOINED_EVENT)
        .document(eventId)
        .set(mapOf("eventId" to eventId))
        .await()
  }

  override suspend fun getNewUid(): String {
    return db.collection(COLLECTION_NAME).document().id
  }

  override suspend fun getJoinedEvents(userId: String): List<String> {
    val document =
        db.collection(COLLECTION_NAME).document(userId).collection(JOINED_EVENT).get().await()
    return document.documents.map { it.getString("eventId")!! }
  }

  override suspend fun addEventToUser(eventId: String, userId: String) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .collection(JOINED_EVENT)
        .document(eventId)
        .set(mapOf("eventId" to eventId))
        .await()
  }

  override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .collection(INVITATIONS)
        .document(eventId)
        .set(
            mapOf(
                "from" to fromUserId,
                "eventId" to eventId,
                "timestamp" to FieldValue.serverTimestamp()))
        .await()
  }

  override suspend fun getInvitations(userId: String): List<Invitation> {
    val document =
        db.collection(COLLECTION_NAME).document(userId).collection(INVITATIONS).get().await()
    return document.documents.mapNotNull { doc ->
      Invitation(
          eventId = doc.getString("eventId")!!,
          from = doc.getString("from")!!,
          status =
              InvitationStatus.valueOf(doc.getString("status") ?: InvitationStatus.Pending.name),
          timestamp = doc.getTimestamp("timestamp"))
    }
  }

  override suspend fun acceptInvitation(eventId: String, userId: String) {
    val userDoc = db.collection(COLLECTION_NAME).document(userId)
    val invitationRef = userDoc.collection(INVITATIONS).document(eventId)
    val joinedRef = userDoc.collection(JOINED_EVENT).document(eventId)

    val snapshot = invitationRef.get().await()
    require(snapshot.exists()) {
      "No invitation " + "found to event with ID : ${eventId} for user ID: $userId"
    }
    joinedRef.set(mapOf("eventId" to eventId, "timestamp" to FieldValue.serverTimestamp())).await()
    invitationRef.delete().await()
  }

  override suspend fun declineInvitation(eventId: String, userId: String) {
    val invitationRef =
        db.collection(COLLECTION_NAME).document(userId).collection(INVITATIONS).document(eventId)

    invitationRef.update("status", InvitationStatus.Declined.name).await()
  }

  override suspend fun leaveEvent(eventId: String, userId: String) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .collection(JOINED_EVENT)
        .document(eventId)
        .delete()
        .await()
  }

  override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {
    val event = db.collection(EVENTS_COLLECTION_PATH).document(eventId).get().await()
    require(event.getString("ownerId") == fromUserId) {
      "User $fromUserId is not the owner of event $eventId"
    }
    val invitationData =
        Invitation(eventId, fromUserId, InvitationStatus.Pending, timestamp = Timestamp.now())
    db.collection(COLLECTION_NAME)
        .document(toUserId)
        .collection(INVITATIONS)
        .document(eventId)
        .set(invitationData)
        .await()
  }

  override suspend fun addFavoriteEvent(userId: String, eventId: String) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .collection(FAVORITE_EVENTS)
        .document(eventId)
        .set(mapOf("eventId" to eventId, "addedAt" to FieldValue.serverTimestamp()))
        .await()
  }

  override suspend fun removeFavoriteEvent(userId: String, eventId: String) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .collection(FAVORITE_EVENTS)
        .document(eventId)
        .delete()
        .await()
  }

  override suspend fun getFavoriteEvents(userId: String): List<String> {
    val document =
        db.collection(COLLECTION_NAME).document(userId).collection(FAVORITE_EVENTS).get().await()
    return document.documents.map { it.getString("eventId")!! }
  }

  override suspend fun checkUsernameAvailability(username: String): Boolean {
    // Username is already normalized to lowercase by SignUpViewModel
    // Query Firestore for username (stored in lowercase)
    val querySnapshot =
        db.collection(COLLECTION_NAME)
            .whereEqualTo("username", username.lowercase())
            .limit(1)
            .get()
            .await()

    // Username is available if no documents are found
    return querySnapshot.isEmpty
  }
}
