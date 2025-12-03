package com.github.se.studentconnect.model.user

import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.activities.InvitationStatus
import java.util.UUID

/**
 * Implementation of UserRepository using local in-memory collections. This is useful for testing
 * and offline development.
 */
class UserRepositoryLocal : UserRepository {
  private val users = mutableListOf<User>()

  private val joinedEvents = mutableMapOf<String, MutableList<String>>()
  private val invitations = mutableMapOf<String, MutableList<Invitation>>()
  private val favoriteEvents = mutableMapOf<String, MutableList<String>>()
  private val followedOrganizations = mutableMapOf<String, MutableList<String>>()

  override suspend fun getUserById(userId: String): User? {
    return users.find { it.userId == userId }
  }

  override suspend fun getUserByEmail(email: String): User? {
    return users.find { it.email == email }
  }

  override suspend fun getAllUsers(): List<User> {
    return users.toList()
  }

  override suspend fun getUsersPaginated(
      limit: Int,
      lastUserId: String?
  ): Pair<List<User>, Boolean> {
    val sortedUsers = users.sortedBy { it.userId }
    val startIndex =
        if (lastUserId != null) {
          val index = sortedUsers.indexOfFirst { it.userId == lastUserId }
          if (index == -1) {
            // If lastUserId is not found, return empty result
            return emptyList<User>() to false
          }
          index + 1
        } else {
          0
        }

    if (startIndex >= sortedUsers.size) {
      return emptyList<User>() to false
    }

    val paginatedUsers = sortedUsers.drop(startIndex).take(limit + 1)
    val hasMore = paginatedUsers.size > limit

    return paginatedUsers.take(limit) to hasMore
  }

  override suspend fun saveUser(user: User) {
    users.removeAll { it.userId == user.userId }
    users.add(user)
  }

  override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
    val user = getUserById(userId) ?: return

    val userMap = user.toMap().toMutableMap()
    userMap.putAll(updates)
    userMap["updatedAt"] = System.currentTimeMillis()

    User.fromMap(userMap)?.let { updatedUser -> saveUser(updatedUser) }
  }

  override suspend fun deleteUser(userId: String) {
    users.removeAll { it.userId == userId }
    joinedEvents.remove(userId)
    invitations.remove(userId)
    favoriteEvents.remove(userId)
    followedOrganizations.remove(userId)
  }

  override suspend fun getUsersByUniversity(university: String): List<User> {
    return users.filter { it.university == university }
  }

  override suspend fun getUsersByHobby(hobby: String): List<User> {
    return users.filter { it.hobbies.contains(hobby) }
  }

  override suspend fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getJoinedEvents(userId: String): List<String> {
    return joinedEvents[userId] ?: emptyList()
  }

  override suspend fun addEventToUser(eventId: String, userId: String) {
    val userEvents = joinedEvents.getOrPut(userId) { mutableListOf() }
    if (!userEvents.contains(eventId)) {
      userEvents.add(eventId)
    }
  }

  override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {
    val userInvitations = invitations.getOrPut(userId) { mutableListOf() }
    if (userInvitations.none { it.eventId == eventId }) {
      userInvitations.add(Invitation(eventId = eventId, from = fromUserId))
    }
  }

  override suspend fun getInvitations(userId: String): List<Invitation> {
    return invitations[userId] ?: emptyList()
  }

  override suspend fun acceptInvitation(eventId: String, userId: String) {
    invitations[userId]?.removeAll { it.eventId == eventId }
    addEventToUser(eventId, userId)
  }

  override suspend fun declineInvitation(eventId: String, userId: String) {
    val userInvitations =
        invitations[userId]
            ?: throw NoSuchElementException("No Invitations found for user $userId.")

    val invitationIndex = userInvitations.indexOfFirst { it.eventId == eventId }
    if (invitationIndex == -1) {
      throw NoSuchElementException("No invitation for event $eventId for user $userId.")
    }

    val originalInvitation = userInvitations[invitationIndex]

    val updatedInvitation = originalInvitation.copy(status = InvitationStatus.Declined)
    userInvitations[invitationIndex] = updatedInvitation
  }

  override suspend fun removeInvitation(eventId: String, userId: String) {
    invitations[userId]?.removeAll { it.eventId == eventId }
  }

  override suspend fun joinEvent(eventId: String, userId: String) {
    invitations[userId]?.removeAll { it.eventId == eventId }
    addEventToUser(eventId, userId)
  }

  override suspend fun leaveEvent(eventId: String, userId: String) {
    joinedEvents[userId]?.remove(eventId)
  }

  override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {
    addInvitationToUser(eventId, toUserId, fromUserId)
  }

  override suspend fun addFavoriteEvent(userId: String, eventId: String) {
    val userFavorites = favoriteEvents.getOrPut(userId) { mutableListOf() }
    if (!userFavorites.contains(eventId)) {
      userFavorites.add(eventId)
    }
  }

  override suspend fun removeFavoriteEvent(userId: String, eventId: String) {
    favoriteEvents[userId]?.remove(eventId)
  }

  override suspend fun getFavoriteEvents(userId: String): List<String> {
    return favoriteEvents[userId] ?: emptyList()
  }

  override suspend fun checkUsernameAvailability(username: String): Boolean {
    // Case-insensitive check: normalize to lowercase for comparison
    val normalizedUsername = username.lowercase()
    return users.none { it.username.lowercase() == normalizedUsername }
  }

  override suspend fun followOrganization(userId: String, organizationId: String) {
    val userOrganizations = followedOrganizations.getOrPut(userId) { mutableListOf() }
    if (!userOrganizations.contains(organizationId)) {
      userOrganizations.add(organizationId)
    }
  }

  override suspend fun unfollowOrganization(userId: String, organizationId: String) {
    followedOrganizations[userId]?.remove(organizationId)
  }

  override suspend fun getFollowedOrganizations(userId: String): List<String> {
    return followedOrganizations[userId] ?: emptyList()
  }
}
