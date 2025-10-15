package com.github.se.studentconnect.model.friends

/**
 * Repository interface for Friends operations.
 *
 * This interface defines the contract for friend management operations, allowing for different
 * implementations (e.g., Firestore, local database, mock for testing).
 */
interface FriendsRepository {

  /**
   * Retrieves the list of friend IDs for a given user.
   *
   * @param userId The unique identifier of the user.
   * @return A list of user IDs representing the user's friends.
   */
  suspend fun getFriends(userId: String): List<String>

  /**
   * Retrieves the list of pending friend request IDs sent to a given user.
   *
   * @param userId The unique identifier of the user.
   * @return A list of user IDs representing pending friend requests.
   */
  suspend fun getPendingRequests(userId: String): List<String>

  /**
   * Retrieves the list of friend request IDs sent by a given user that are pending.
   *
   * @param userId The unique identifier of the user.
   * @return A list of user IDs representing sent friend requests.
   */
  suspend fun getSentRequests(userId: String): List<String>

  /**
   * Sends a friend request from one user to another.
   *
   * @param fromUserId The unique identifier of the user sending the request.
   * @param toUserId The unique identifier of the user receiving the request.
   * @throws IllegalArgumentException if the users are already friends or a request already exists.
   */
  suspend fun sendFriendRequest(fromUserId: String, toUserId: String)

  /**
   * Accepts a friend request, creating a bidirectional friendship.
   *
   * @param userId The unique identifier of the user accepting the request.
   * @param fromUserId The unique identifier of the user who sent the request.
   * @throws IllegalArgumentException if no pending request exists.
   */
  suspend fun acceptFriendRequest(userId: String, fromUserId: String)

  /**
   * Rejects a friend request, removing it from pending requests.
   *
   * @param userId The unique identifier of the user rejecting the request.
   * @param fromUserId The unique identifier of the user who sent the request.
   * @throws IllegalArgumentException if no pending request exists.
   */
  suspend fun rejectFriendRequest(userId: String, fromUserId: String)

  /**
   * Cancels a sent friend request.
   *
   * @param userId The unique identifier of the user who sent the request.
   * @param toUserId The unique identifier of the user who was to receive the request.
   * @throws IllegalArgumentException if no sent request exists.
   */
  suspend fun cancelFriendRequest(userId: String, toUserId: String)

  /**
   * Removes a friend from a user's friends list (bidirectional removal).
   *
   * @param userId The unique identifier of the user.
   * @param friendId The unique identifier of the friend to remove.
   * @throws IllegalArgumentException if the users are not friends.
   */
  suspend fun removeFriend(userId: String, friendId: String)

  /**
   * Checks if two users are friends.
   *
   * @param userId The unique identifier of the first user.
   * @param otherUserId The unique identifier of the second user.
   * @return True if the users are friends, false otherwise.
   */
  suspend fun areFriends(userId: String, otherUserId: String): Boolean

  /**
   * Checks if a friend request exists from one user to another.
   *
   * @param fromUserId The unique identifier of the sender.
   * @param toUserId The unique identifier of the receiver.
   * @return True if a pending request exists, false otherwise.
   */
  suspend fun hasPendingRequest(fromUserId: String, toUserId: String): Boolean
}
