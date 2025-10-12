package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
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
  }

    override suspend fun getUserById(userId: String): User? {
        val document = db.collection(COLLECTION_NAME)
            .document(userId)
            .get()
            .await()

        return if (document.exists()) {
            User.fromMap(document.data ?: emptyMap())
        } else {
            null
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        val querySnapshot = db.collection(COLLECTION_NAME)
            .whereEqualTo("email", email)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            val document = querySnapshot.documents.first()
            User.fromMap(document.data ?: emptyMap())
        } else {
            null
        }
    }

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?): Pair<List<User>, Boolean> {
        var query = db.collection(COLLECTION_NAME)
            .orderBy("userId")
            .limit((limit + 1).toLong())

        lastUserId?.let { query = query.startAfter(it) }

        val querySnapshot = query.get().await()

        val allUsers = querySnapshot.documents.mapNotNull { document ->
            User.fromMap(document.data ?: emptyMap())
        }

        val hasMore = allUsers.size > limit
        val users = if (hasMore) allUsers.take(limit) else allUsers

        return users to hasMore
    }

    override suspend fun getAllUsers(): List<User> {
        val querySnapshot = db.collection(COLLECTION_NAME)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            User.fromMap(document.data ?: emptyMap())
        }
    }

    override suspend fun saveUser(user: User) {
        db.collection(COLLECTION_NAME)
            .document(user.userId)
            .set(user.toMap())
            .await()
    }

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
        val updatesWithTimestamp = updates.toMutableMap()
        updatesWithTimestamp["updatedAt"] = FieldValue.serverTimestamp()

        db.collection(COLLECTION_NAME)
            .document(userId)
            .update(updatesWithTimestamp)
            .await()
    }

    override suspend fun deleteUser(userId: String) {
        db.collection(COLLECTION_NAME)
            .document(userId)
            .delete()
            .await()
    }

    override suspend fun getUsersByUniversity(university: String): List<User> {
        val querySnapshot = db.collection(COLLECTION_NAME)
            .whereEqualTo("university", university)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            User.fromMap(document.data ?: emptyMap())
        }
    }

    override suspend fun getUsersByHobby(hobby: String): List<User> {
        val querySnapshot = db.collection(COLLECTION_NAME)
            .whereArrayContains("hobbies", hobby)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            User.fromMap(document.data ?: emptyMap())
        }
    }

}
