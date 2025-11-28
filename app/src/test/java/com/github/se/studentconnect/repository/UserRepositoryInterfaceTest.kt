package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.ui.screen.activities.Invitation
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Tests for the default implementations in the UserRepository interface.
 *
 * These tests verify that the default implementations of optional methods work correctly.
 */
class UserRepositoryInterfaceTest {

  @Test
  fun `default followOrganization does nothing`() = runTest {
    // Create a minimal implementation that uses defaults
    val repository =
        object : UserRepository {
          override suspend fun leaveEvent(eventId: String, userId: String) {}

          override suspend fun getUserById(userId: String): User? = null

          override suspend fun getUserByEmail(email: String): User? = null

          override suspend fun getAllUsers(): List<User> = emptyList()

          override suspend fun getUsersPaginated(
              limit: Int,
              lastUserId: String?
          ): Pair<List<User>, Boolean> = emptyList<User>() to false

          override suspend fun saveUser(user: User) {}

          override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

          override suspend fun deleteUser(userId: String) {}

          override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

          override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

          override suspend fun getNewUid(): String = "test-uid"

          override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

          override suspend fun addEventToUser(eventId: String, userId: String) {}

          override suspend fun addInvitationToUser(
              eventId: String,
              userId: String,
              fromUserId: String
          ) {}

          override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

          override suspend fun acceptInvitation(eventId: String, userId: String) {}

          override suspend fun declineInvitation(eventId: String, userId: String) {}

          override suspend fun joinEvent(eventId: String, userId: String) {}

          override suspend fun sendInvitation(
              eventId: String,
              fromUserId: String,
              toUserId: String
          ) {}

          override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

          override suspend fun checkUsernameAvailability(username: String): Boolean = true

          // Using default implementations for organization methods
        }

    // Should not throw an exception - just does nothing
    repository.followOrganization("user1", "org1")
  }

  @Test
  fun `default unfollowOrganization does nothing`() = runTest {
    val repository =
        object : UserRepository {
          override suspend fun leaveEvent(eventId: String, userId: String) {}

          override suspend fun getUserById(userId: String): User? = null

          override suspend fun getUserByEmail(email: String): User? = null

          override suspend fun getAllUsers(): List<User> = emptyList()

          override suspend fun getUsersPaginated(
              limit: Int,
              lastUserId: String?
          ): Pair<List<User>, Boolean> = emptyList<User>() to false

          override suspend fun saveUser(user: User) {}

          override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

          override suspend fun deleteUser(userId: String) {}

          override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

          override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

          override suspend fun getNewUid(): String = "test-uid"

          override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

          override suspend fun addEventToUser(eventId: String, userId: String) {}

          override suspend fun addInvitationToUser(
              eventId: String,
              userId: String,
              fromUserId: String
          ) {}

          override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

          override suspend fun acceptInvitation(eventId: String, userId: String) {}

          override suspend fun declineInvitation(eventId: String, userId: String) {}

          override suspend fun joinEvent(eventId: String, userId: String) {}

          override suspend fun sendInvitation(
              eventId: String,
              fromUserId: String,
              toUserId: String
          ) {}

          override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

          override suspend fun checkUsernameAvailability(username: String): Boolean = true

          // Using default implementations for organization methods
        }

    // Should not throw an exception - just does nothing
    repository.unfollowOrganization("user1", "org1")
  }

  @Test
  fun `default getFollowedOrganizations returns empty list`() = runTest {
    val repository =
        object : UserRepository {
          override suspend fun leaveEvent(eventId: String, userId: String) {}

          override suspend fun getUserById(userId: String): User? = null

          override suspend fun getUserByEmail(email: String): User? = null

          override suspend fun getAllUsers(): List<User> = emptyList()

          override suspend fun getUsersPaginated(
              limit: Int,
              lastUserId: String?
          ): Pair<List<User>, Boolean> = emptyList<User>() to false

          override suspend fun saveUser(user: User) {}

          override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

          override suspend fun deleteUser(userId: String) {}

          override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

          override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

          override suspend fun getNewUid(): String = "test-uid"

          override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

          override suspend fun addEventToUser(eventId: String, userId: String) {}

          override suspend fun addInvitationToUser(
              eventId: String,
              userId: String,
              fromUserId: String
          ) {}

          override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

          override suspend fun acceptInvitation(eventId: String, userId: String) {}

          override suspend fun declineInvitation(eventId: String, userId: String) {}

          override suspend fun joinEvent(eventId: String, userId: String) {}

          override suspend fun sendInvitation(
              eventId: String,
              fromUserId: String,
              toUserId: String
          ) {}

          override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

          override suspend fun checkUsernameAvailability(username: String): Boolean = true

          // Using default implementations for organization methods
        }

    val result = repository.getFollowedOrganizations("user1")
    assertTrue(result.isEmpty())
    assertEquals(0, result.size)
  }

  @Test
  fun `all default organization methods work together`() = runTest {
    val repository =
        object : UserRepository {
          override suspend fun leaveEvent(eventId: String, userId: String) {}

          override suspend fun getUserById(userId: String): User? = null

          override suspend fun getUserByEmail(email: String): User? = null

          override suspend fun getAllUsers(): List<User> = emptyList()

          override suspend fun getUsersPaginated(
              limit: Int,
              lastUserId: String?
          ): Pair<List<User>, Boolean> = emptyList<User>() to false

          override suspend fun saveUser(user: User) {}

          override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

          override suspend fun deleteUser(userId: String) {}

          override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

          override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

          override suspend fun getNewUid(): String = "test-uid"

          override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

          override suspend fun addEventToUser(eventId: String, userId: String) {}

          override suspend fun addInvitationToUser(
              eventId: String,
              userId: String,
              fromUserId: String
          ) {}

          override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

          override suspend fun acceptInvitation(eventId: String, userId: String) {}

          override suspend fun declineInvitation(eventId: String, userId: String) {}

          override suspend fun joinEvent(eventId: String, userId: String) {}

          override suspend fun sendInvitation(
              eventId: String,
              fromUserId: String,
              toUserId: String
          ) {}

          override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

          override suspend fun checkUsernameAvailability(username: String): Boolean = true

          // Using default implementations for organization methods
        }

    // Test all default methods work together
    repository.followOrganization("user1", "org1")
    repository.followOrganization("user1", "org2")
    val followed = repository.getFollowedOrganizations("user1")
    assertTrue(followed.isEmpty()) // Default implementation doesn't store anything

    repository.unfollowOrganization("user1", "org1")
    val followedAfter = repository.getFollowedOrganizations("user1")
    assertTrue(followedAfter.isEmpty())
  }
}
