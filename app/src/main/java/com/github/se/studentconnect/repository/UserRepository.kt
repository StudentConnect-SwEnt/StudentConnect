package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.ui.activities.Invitation

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

  /** Retrieves all users from the database. */
  suspend fun getAllUsers(): List<User>

  /**
   * Retrieves users from the database with pagination.
   *
   * @param limit Maximum number of users to retrieve.
   * @param lastUserId The ID of the last user from the previous page (for pagination).
   */
  suspend fun getUsersPaginated(limit: Int, lastUserId: String? = null): Pair<List<User>, Boolean>

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
  suspend fun updateUser(userId: String, updates: Map<String, Any?>)

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
  suspend fun getUsersByUniversity(university: String): List<User>

  /**
   * Searches for users by hobby.
   *
   * @param hobby The hobby to search for.
   */
  suspend fun getUsersByHobby(hobby: String): List<User>

  /** Returns a unique ID for a new user document. */
  suspend fun getNewUid(): String

  /**
   * Retrieves all events that a given user is participating in.
   *
   * @param userId The unique identifier
   */
  suspend fun getJoinedEvents(userId: String): List<String>

  /**
   * adds an event to a user's list of joined events.
   *
   * @param eventId The unique identifier of the event to add.
   * @param userId The unique identifier of the user to whom the event should be added
   */
  suspend fun addEventToUser(eventId: String, userId: String)

  /**
   * adds an invitation to a user's list of invitations.
   *
   * @param eventId The unique identifier of the event for which the invitation is sent.
   * @param userId The unique identifier of the user to whom the invitation should be added
   * @param fromUserId The unique identifier of the user sending the invitation.
   */
  suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String)

  /**
   * Retrieves all event invitations for a given user.
   *
   * @param userId The unique identifier of the user whose invitations should be retrieved.
   * @return A list of event IDs that the user is invited to.
   */
  suspend fun getInvitations(userId: String): List<Invitation>

  /**
   * Accepts an event invitation for a user, adding the event to their joined events and removing it
   * from their invitations.
   *
   * @param eventId The unique identifier of the event being accepted.
   * @param userId The unique identifier of the user accepting the invitation.
   */
  suspend fun acceptInvitation(eventId: String, userId: String)

  /**
   * join an event (adds the event to the user's joined events and removes any invitation)à
   *
   * @param eventId The unique identifier of the event to join.
   * @param userId The unique identifier of the user joining the event.
   */
  suspend fun joinEvent(eventId: String, userId: String)

  /**
   * Sends an event invitation from one user to another.
   *
   * @param eventId The unique identifier of the event for which the invitation is sent.
   * @param fromUserId The unique identifier of the user sending the invitation.
   * @param toUserId The unique identifier of the user receiving the invitation.
   */
  suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String)
}
