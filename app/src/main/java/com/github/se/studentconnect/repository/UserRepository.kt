package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User

/**
 * Repository interface for User operations.
 *
 * This interface defines the contract for user data operations, allowing for different
 * implementations (e.g., Firestore, local database, mock for testing).
 */
interface UserRepository {

  fun leaveEvent(eventId: String, userId: String)
  /**
   * Retrieves a user by their unique identifier.
   *
   * @param userId The unique identifier of the user.
   * @param onSuccess Callback invoked with the User if found.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun getUserById(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves a user by their email address.
   *
   * @param email The email address of the user.
   * @param onSuccess Callback invoked with the User if found.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun getUserByEmail(email: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all users from the database.
   *
   * @param onSuccess Callback invoked with a list of all Users.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves users from the database with pagination.
   *
   * @param limit Maximum number of users to retrieve.
   * @param lastUserId The ID of the last user from the previous page (for pagination).
   * @param onSuccess Callback invoked with a list of Users and a boolean indicating if there are
   *   more pages.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun getUsersPaginated(
      limit: Int,
      lastUserId: String? = null,
      onSuccess: (List<User>, hasMore: Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Creates or updates a user in the database.
   *
   * @param user The User to save.
   * @param onSuccess Callback invoked when the operation succeeds.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates specific fields of a user.
   *
   * @param userId The unique identifier of the user to update.
   * @param updates A map of field names to new values.
   * @param onSuccess Callback invoked when the operation succeeds.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun updateUser(
      userId: String,
      updates: Map<String, Any?>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a user from the database.
   *
   * @param userId The unique identifier of the user to delete.
   * @param onSuccess Callback invoked when the operation succeeds.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Searches for users by university.
   *
   * @param university The university to search for.
   * @param onSuccess Callback invoked with a list of Users from that university.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun getUsersByUniversity(
      university: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Searches for users by hobby.
   *
   * @param hobby The hobby to search for.
   * @param onSuccess Callback invoked with a list of Users who have that hobby.
   * @param onFailure Callback invoked with an Exception if the operation fails.
   */
  fun getUsersByHobby(
      hobby: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /** Returns a unique ID for a new user document. */
  fun getNewUid(): String
}
