package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Implementation of UserRepository using Firebase Firestore.
 *
 * @property db The Firestore database instance.
 */
class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  companion object {
    private const val COLLECTION_NAME = "users"
  }

  override fun leaveEvent(eventId: String, userId: String) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .update("joinedEvents", FieldValue.arrayRemove(eventId))
  }

  override fun getUserById(
      userId: String,
      onSuccess: (User?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
          if (document.exists()) {
            val user = User.fromMap(document.data ?: emptyMap())
            onSuccess(user)
          } else {
            onSuccess(null)
          }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getUserByEmail(
      email: String,
      onSuccess: (User?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_NAME)
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener { querySnapshot ->
          if (!querySnapshot.isEmpty) {
            val document = querySnapshot.documents.first()
            val user = User.fromMap(document.data ?: emptyMap())
            onSuccess(user)
          } else {
            onSuccess(null)
          }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(COLLECTION_NAME)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val users =
              querySnapshot.documents.mapNotNull { document ->
                User.fromMap(document.data ?: emptyMap())
              }
          onSuccess(users)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getUsersPaginated(
      limit: Int,
      lastUserId: String?,
      onSuccess: (List<User>, Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    var query =
        db.collection(COLLECTION_NAME)
            .orderBy("userId")
            .limit((limit + 1).toLong()) // Request one extra to check if there are more pages

    // If we have a lastUserId, start after that document
    lastUserId?.let { userId -> query = query.startAfter(userId) }

    query
        .get()
        .addOnSuccessListener { querySnapshot ->
          val allUsers =
              querySnapshot.documents.mapNotNull { document ->
                User.fromMap(document.data ?: emptyMap())
              }

          val hasMore = allUsers.size > limit
          val users = if (hasMore) allUsers.take(limit) else allUsers

          onSuccess(users, hasMore)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(COLLECTION_NAME)
        .document(user.userId)
        .set(user.toMap())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun updateUser(
      userId: String,
      updates: Map<String, Any?>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Add server timestamp to the updates
    val updatesWithTimestamp = updates.toMutableMap()
    updatesWithTimestamp["updatedAt"] = FieldValue.serverTimestamp()

    db.collection(COLLECTION_NAME)
        .document(userId)
        .update(updatesWithTimestamp)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(COLLECTION_NAME)
        .document(userId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getUsersByUniversity(
      university: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_NAME)
        .whereEqualTo("university", university)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val users =
              querySnapshot.documents.mapNotNull { document ->
                User.fromMap(document.data ?: emptyMap())
              }
          onSuccess(users)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getUsersByHobby(
      hobby: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_NAME)
        .whereArrayContains("hobbies", hobby)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val users =
              querySnapshot.documents.mapNotNull { document ->
                User.fromMap(document.data ?: emptyMap())
              }
          onSuccess(users)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getNewUid(): String {
    return db.collection(COLLECTION_NAME).document().id
  }
}
