package com.github.se.studentconnect.ui.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.activities.Invitation
import com.github.se.studentconnect.ui.theme.AppTheme

@Composable
fun ProfileScreenExample(
    currentUserId: String = "example_user_id", // Replace with actual user ID from Firebase Auth
    modifier: Modifier = Modifier
) {
  AppTheme {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      ProfileScreen(currentUserId = currentUserId, modifier = Modifier.fillMaxSize())
    }
  }
}

/**
 * Mock UserRepository for preview purposes. This simulates a user repository that returns mock
 * data.
 */
class MockUserRepository(
    private val previewUser: User = DEFAULT_USER,
    private val generatedUid: String = "new_mock_uid"
) : UserRepository {

  companion object {
    private val DEFAULT_USER =
        User(
            userId = "mock_user_123",
            email = "forest.gump@epfl.ch",
            firstName = "Forest",
            lastName = "Gump",
            university = "EPFL",
            hobbies = listOf("Sports", "Hiking", "Coding", "Running"),
            profilePictureUrl = null,
            bio =
                "I love running in the wilderness and meeting new people. Life is like a box of chocolates!",
            country = "Tunisia",
            birthday = "31/12/1980",
            createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
            updatedAt = System.currentTimeMillis())
  }

  override suspend fun leaveEvent(eventId: String, userId: String) {
    // Preview data does not track events; no-op.
  }

  override suspend fun getUserById(userId: String): User? = previewUser

  override suspend fun getUserByEmail(email: String): User? = previewUser

  override suspend fun getAllUsers(): List<User> = listOf(previewUser)

  override suspend fun getUsersPaginated(
      limit: Int,
      lastUserId: String?
  ): Pair<List<User>, Boolean> = listOf(previewUser) to false

  override suspend fun saveUser(user: User) {
    // Persisting is unnecessary for preview data.
  }

  override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
    // Preview data is immutable; no-op.
  }

  override suspend fun deleteUser(userId: String) {
    // Preview data is immutable; no-op.
  }

  override suspend fun getUsersByUniversity(university: String): List<User> = listOf(previewUser)

  override suspend fun getUsersByHobby(hobby: String): List<User> = listOf(previewUser)

  override suspend fun getNewUid(): String = generatedUid

  override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

  override suspend fun addEventToUser(eventId: String, userId: String) {
    // Preview data does not track events; no-op.
  }

  override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {
    // Preview data does not track invitations; no-op.
  }

  override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

  override suspend fun acceptInvitation(eventId: String, userId: String) {
    // Preview data does not track invitations; no-op.
  }

  override suspend fun joinEvent(eventId: String, userId: String) {
    // Preview data does not track events; no-op.
  }

  override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {
    // Preview data does not track invitations; no-op.
  }
}

/**
 * Preview of the ProfileScreen with mock data. This shows how the screen looks with a complete user
 * profile.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
  AppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      ProfileScreen(
          currentUserId = "mock_user_123",
          userRepository = MockUserRepository(),
          modifier = Modifier.fillMaxSize())
    }
  }
}

/**
 * Preview of the ProfileScreen with minimal data. This shows how the screen looks with mostly empty
 * fields.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenMinimalPreview() {
  AppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      val minimalUser =
          User(
              userId = "minimal_user_456",
              email = "john.doe@epfl.ch",
              firstName = "John",
              lastName = "Doe",
              university = "EPFL",
              hobbies = emptyList(),
              profilePictureUrl = null,
              bio = null,
              country = null,
              birthday = null,
              createdAt = System.currentTimeMillis(),
              updatedAt = System.currentTimeMillis())

      ProfileScreen(
          currentUserId = minimalUser.userId,
          userRepository =
              MockUserRepository(previewUser = minimalUser, generatedUid = "new_minimal_uid"),
          modifier = Modifier.fillMaxSize())
    }
  }
}
