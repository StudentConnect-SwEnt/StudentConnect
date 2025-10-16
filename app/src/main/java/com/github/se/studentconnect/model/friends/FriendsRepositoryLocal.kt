package com.github.se.studentconnect.model.friends

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Local in-memory implementation of FriendsRepository.
 *
 * This implementation stores friends data in memory using maps: - friends: Map<userId,
 * Set<friendId>> - Contains confirmed friendships - friendRequests: Map<userId, Set<requesterId>>
 * - Contains pending incoming requests - sentRequests: Map<userId, Set<recipientId>> - Contains
 *   pending outgoing requests
 *
 * This is useful for testing and offline scenarios.
 */
class FriendsRepositoryLocal : FriendsRepository {

  // Store friendships (bidirectional)
  private val friends = mutableMapOf<String, MutableSet<String>>()

  // Store pending friend requests (recipient -> set of requester IDs)
  private val friendRequests = mutableMapOf<String, MutableSet<String>>()

  // Store sent friend requests (sender -> set of recipient IDs)
  private val sentRequests = mutableMapOf<String, MutableSet<String>>()

  // Mutex for thread-safe operations
  private val mutex = Mutex()

  override suspend fun getFriends(userId: String): List<String> =
      mutex.withLock { friends[userId]?.toList() ?: emptyList() }

  override suspend fun getPendingRequests(userId: String): List<String> =
      mutex.withLock { friendRequests[userId]?.toList() ?: emptyList() }

  override suspend fun getSentRequests(userId: String): List<String> =
      mutex.withLock { sentRequests[userId]?.toList() ?: emptyList() }

  override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) {
    mutex.withLock {
      // Check if users are the same
      if (fromUserId == toUserId) {
        throw IllegalArgumentException("Cannot send friend request to yourself")
      }

      // Check if already friends
      if (friends[fromUserId]?.contains(toUserId) == true) {
        throw IllegalArgumentException("Users are already friends")
      }

      // Check if request already exists
      if (sentRequests[fromUserId]?.contains(toUserId) == true) {
        throw IllegalArgumentException("Friend request already sent")
      }

      // Check if reverse request exists
      if (friendRequests[fromUserId]?.contains(toUserId) == true) {
        throw IllegalArgumentException(
            "A friend request from the recipient already exists. Accept their request instead.")
      }

      // Add to recipient's pending requests
      friendRequests.getOrPut(toUserId) { mutableSetOf() }.add(fromUserId)

      // Add to sender's sent requests
      sentRequests.getOrPut(fromUserId) { mutableSetOf() }.add(toUserId)
    }
  }

  override suspend fun acceptFriendRequest(userId: String, fromUserId: String) {
    mutex.withLock {
      // Verify the request exists
      if (friendRequests[userId]?.contains(fromUserId) != true) {
        throw IllegalArgumentException("No pending friend request from user: $fromUserId")
      }

      // Add to both users' friends lists
      friends.getOrPut(userId) { mutableSetOf() }.add(fromUserId)
      friends.getOrPut(fromUserId) { mutableSetOf() }.add(userId)

      // Remove from pending requests
      friendRequests[userId]?.remove(fromUserId)
      if (friendRequests[userId]?.isEmpty() == true) {
        friendRequests.remove(userId)
      }

      // Remove from sent requests
      sentRequests[fromUserId]?.remove(userId)
      if (sentRequests[fromUserId]?.isEmpty() == true) {
        sentRequests.remove(fromUserId)
      }
    }
  }

  override suspend fun rejectFriendRequest(userId: String, fromUserId: String) {
    mutex.withLock {
      // Verify the request exists
      if (friendRequests[userId]?.contains(fromUserId) != true) {
        throw IllegalArgumentException("No pending friend request from user: $fromUserId")
      }

      // Remove from pending requests
      friendRequests[userId]?.remove(fromUserId)
      if (friendRequests[userId]?.isEmpty() == true) {
        friendRequests.remove(userId)
      }

      // Remove from sent requests
      sentRequests[fromUserId]?.remove(userId)
      if (sentRequests[fromUserId]?.isEmpty() == true) {
        sentRequests.remove(fromUserId)
      }
    }
  }

  override suspend fun cancelFriendRequest(userId: String, toUserId: String) {
    mutex.withLock {
      // Verify the sent request exists
      if (sentRequests[userId]?.contains(toUserId) != true) {
        throw IllegalArgumentException("No sent friend request to user: $toUserId")
      }

      // Remove from sent requests
      sentRequests[userId]?.remove(toUserId)
      if (sentRequests[userId]?.isEmpty() == true) {
        sentRequests.remove(userId)
      }

      // Remove from pending requests
      friendRequests[toUserId]?.remove(userId)
      if (friendRequests[toUserId]?.isEmpty() == true) {
        friendRequests.remove(toUserId)
      }
    }
  }

  override suspend fun removeFriend(userId: String, friendId: String) {
    mutex.withLock {
      // Verify friendship exists
      if (friends[userId]?.contains(friendId) != true) {
        throw IllegalArgumentException("Users are not friends")
      }

      // Remove from both users' friends lists
      friends[userId]?.remove(friendId)
      if (friends[userId]?.isEmpty() == true) {
        friends.remove(userId)
      }

      friends[friendId]?.remove(userId)
      if (friends[friendId]?.isEmpty() == true) {
        friends.remove(friendId)
      }
    }
  }

  override suspend fun areFriends(userId: String, otherUserId: String): Boolean =
      mutex.withLock { friends[userId]?.contains(otherUserId) ?: false }

  override suspend fun hasPendingRequest(fromUserId: String, toUserId: String): Boolean =
      mutex.withLock { sentRequests[fromUserId]?.contains(toUserId) ?: false }

  /** Clears all data from the repository. Useful for testing. */
  suspend fun clear() =
      mutex.withLock {
        friends.clear()
        friendRequests.clear()
        sentRequests.clear()
      }
}
