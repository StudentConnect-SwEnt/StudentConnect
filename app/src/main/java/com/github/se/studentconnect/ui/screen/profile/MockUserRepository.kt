package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.activities.Invitation
import kotlinx.coroutines.delay

/**
 * Mock implementation of UserRepository for demo purposes. This allows the app to work without
 * Firebase authentication.
 */
class MockUserRepository : UserRepository {

  // Mock user data for demo - using var to allow modifications
  private var mockUser =
      User(
          userId = "mock_user_123",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@epfl.ch",
          university = "EPFL",
          country = "Switzerland",
          birthday = "15/03/1998",
          hobbies = listOf("Football", "Photography", "Cooking", "Reading"),
          bio =
              "I'm a computer science student at EPFL. I love technology, sports, and meeting new people!",
          profilePictureUrl = null)

  override suspend fun getUserById(userId: String): User? {
    delay(500) // Simulate network delay
    return if (userId == "mock_user_123") mockUser else null
  }

  override suspend fun getUserByEmail(email: String): User? {
    delay(500)
    return if (email == "john.doe@epfl.ch") mockUser else null
  }

  override suspend fun getAllUsers(): List<User> {
    delay(500)
    return listOf(mockUser)
  }

  override suspend fun getUsersPaginated(
      limit: Int,
      lastUserId: String?
  ): Pair<List<User>, Boolean> {
    delay(500)
    return Pair(listOf(mockUser), false)
  }

  override suspend fun saveUser(user: User) {
    delay(500)
    // Mock save - actually update the mock user
    mockUser = user
  }

  override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
    delay(500)
    // Mock update - actually update the mock user
    if (userId == "mock_user_123") {
      // Create a new user with updated fields
      mockUser =
          mockUser.copy(
              firstName = updates["firstName"] as? String ?: mockUser.firstName,
              lastName = updates["lastName"] as? String ?: mockUser.lastName,
              university = updates["university"] as? String ?: mockUser.university,
              country = updates["country"] as? String ?: mockUser.country,
              birthday = updates["birthday"] as? String ?: mockUser.birthday,
              hobbies =
                  (updates["hobbies"] as? List<*>)?.filterIsInstance<String>() ?: mockUser.hobbies,
              bio = updates["bio"] as? String ?: mockUser.bio,
              profilePictureUrl =
                  updates["profilePictureUrl"] as? String ?: mockUser.profilePictureUrl)
    }
  }

  override suspend fun deleteUser(userId: String) {
    delay(500)
    // Mock delete - just simulate success
  }

  override suspend fun getUsersByUniversity(university: String): List<User> {
    delay(500)
    return if (university == "EPFL") listOf(mockUser) else emptyList()
  }

  override suspend fun getUsersByHobby(hobby: String): List<User> {
    delay(500)
    return if (mockUser.hobbies.contains(hobby)) listOf(mockUser) else emptyList()
  }

  override suspend fun getNewUid(): String {
    delay(100)
    return "mock_user_${System.currentTimeMillis()}"
  }

  override suspend fun getJoinedEvents(userId: String): List<String> {
    delay(500)
    return emptyList() // Mock implementation - return empty list
  }

  override suspend fun addEventToUser(eventId: String, userId: String) {
    delay(500)
    // Mock add event - just simulate success
  }

  override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {
    delay(500)
    // Mock add invitation - just simulate success
  }

  override suspend fun getInvitations(userId: String): List<Invitation> {
    delay(500)
    return emptyList()
  }

  override suspend fun acceptInvitation(eventId: String, userId: String) {
    delay(500)
    // Mock accept invitation - just simulate success
  }

  override suspend fun declineInvitation(eventId: String, userId: String) {
    delay(500)
    // Mock decline invitation - just simulate success
  }

  override suspend fun joinEvent(eventId: String, userId: String) {
    delay(500)
    // Mock join event - just simulate success
  }

  override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {
    delay(500)
    // Mock send invitation - just simulate success
  }

  override suspend fun leaveEvent(eventId: String, userId: String) {
    delay(500)
    // Mock leave event - just simulate success
  }
}
