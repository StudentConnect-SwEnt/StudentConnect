package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User

/**
 * Repository interface for User operations.
 *
 * This interface defines the contract for user data operations, allowing for different
 * implementations (e.g., Firestore, local database, mock for testing).
 */
interface UserRepository {

  suspend fun leaveEvent(eventId: String, userId: String)
  /**
   * Retrieves a user by their unique identifier.
   *
   * @param userId The unique identifier of the user.
   */
  suspend fun getUserById(userId: String): User?

    /**
   * Retrieves a user by their email address.
   *
   * @param email The email address of the user.
   */
  suspend fun getUserByEmail(email: String): User?

    /**
   * Retrieves all users from the database.
   */
  suspend fun getAllUsers() : List<User>

  /**
   * Retrieves users from the database with pagination.
   *
   * @param limit Maximum number of users to retrieve.
   * @param lastUserId The ID of the last user from the previous page (for pagination).
   * */
  suspend fun getUsersPaginated(
      limit: Int,
      lastUserId: String? = null
  ): Pair<List<User>, Boolean>

    /**
   * Creates or updates a user in the database.
   *
   * @param user The User to save.
   */
  suspend fun saveUser(user: User)

  /**
   * Updates specific fields of a user.
   *
   * @param userId The unique identifier of the user to update.
   * @param updates A map of field names to new values.
   */
  suspend fun updateUser(
      userId: String,
      updates: Map<String, Any?>
  )

  /**
   * Deletes a user from the database.
   *
   * @param userId The unique identifier of the user to delete.
   */
  suspend fun deleteUser(userId: String)

  /**
   * Searches for users by university.
   *
   * @param university The university to search for.
   */
  suspend fun getUsersByUniversity(
      university: String
  ): List<User>

    /**
   * Searches for users by hobby.
   *
   * @param hobby The hobby to search for.
   */
  suspend fun getUsersByHobby(
      hobby: String
  ): List<User>

  /** Returns a unique ID for a new user document. */
  fun getNewUid(): String
}
