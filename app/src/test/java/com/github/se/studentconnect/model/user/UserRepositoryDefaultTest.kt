package com.github.se.studentconnect.model.user

import com.github.se.studentconnect.model.activities.Invitation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the default implementations in [UserRepository] to avoid uncovered new code warnings. The
 * fake repository intentionally relies on the interface defaults for organization follow helpers
 * while stubbing the rest with no-op/throwing implementations.
 */
class UserRepositoryDefaultTest {

  private val repo =
      object : UserRepository {
        override suspend fun leaveEvent(eventId: String, userId: String) {}

        override suspend fun getUserById(userId: String): User? = null

        override suspend fun getUserByEmail(email: String): User? = null

        override suspend fun getAllUsers(): List<User> = emptyList()

        override suspend fun getUsersPaginated(
            limit: Int,
            lastUserId: String?
        ): Pair<List<User>, Boolean> = Pair(emptyList(), false)

        override suspend fun saveUser(user: User) {}

        override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

        override suspend fun deleteUser(userId: String) {}

        override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

        override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

        override suspend fun getNewUid(): String = "new-uid"

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

        override suspend fun removeInvitation(eventId: String, userId: String) {}

        override suspend fun joinEvent(eventId: String, userId: String) {}

        override suspend fun sendInvitation(
            eventId: String,
            fromUserId: String,
            toUserId: String
        ) {}

        override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

        override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

        override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

        override suspend fun addPinnedEvent(userId: String, eventId: String) {}

        override suspend fun removePinnedEvent(userId: String, eventId: String) {}

        override suspend fun getPinnedEvents(userId: String): List<String> = emptyList()

        override suspend fun checkUsernameAvailability(username: String): Boolean = true
        // followOrganization, unfollowOrganization, getFollowedOrganizations,
        // getOrganizationFollowers
        // rely on the interface default implementations.
      }

  @Test
  fun `default getOrganizationFollowers returns empty list`() = runTest {
    val followers = repo.getOrganizationFollowers("org-1")
    assertTrue(followers.isEmpty())
  }

  @Test
  fun `default getFollowedOrganizations returns empty list`() = runTest {
    val followed = repo.getFollowedOrganizations("user-1")
    assertTrue(followed.isEmpty())
  }
}
