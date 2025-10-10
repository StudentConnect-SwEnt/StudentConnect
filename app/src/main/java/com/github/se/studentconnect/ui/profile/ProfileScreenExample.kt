package com.github.se.studentconnect.ui.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
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
class MockUserRepository : UserRepository {
  private val mockUser =
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

  override fun leaveEvent(eventId: String, userId: String) {}

  override fun getUserById(
      userId: String,
      onSuccess: (User?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(mockUser)
  }

  override fun getUserByEmail(
      email: String,
      onSuccess: (User?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(mockUser)
  }

  override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    onSuccess(listOf(mockUser))
  }

  override fun getUsersPaginated(
      limit: Int,
      lastUserId: String?,
      onSuccess: (List<User>, Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(listOf(mockUser), false)
  }

  override fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    onSuccess()
  }

  override fun updateUser(
      userId: String,
      updates: Map<String, Any?>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }

  override fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    onSuccess()
  }

  override fun getUsersByUniversity(
      university: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(listOf(mockUser))
  }

  override fun getUsersByHobby(
      hobby: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(listOf(mockUser))
  }

  override fun getNewUid(): String = "new_mock_uid"
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
      ProfileScreen(
          currentUserId = "minimal_user_456",
          userRepository =
              object : UserRepository {
                private val minimalUser =
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

                override fun leaveEvent(eventId: String, userId: String) {}

                override fun getUserById(
                    userId: String,
                    onSuccess: (User?) -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess(minimalUser)
                }

                override fun getUserByEmail(
                    email: String,
                    onSuccess: (User?) -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess(minimalUser)
                }

                override fun getAllUsers(
                    onSuccess: (List<User>) -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess(listOf(minimalUser))
                }

                override fun getUsersPaginated(
                    limit: Int,
                    lastUserId: String?,
                    onSuccess: (List<User>, Boolean) -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess(listOf(minimalUser), false)
                }

                override fun saveUser(
                    user: User,
                    onSuccess: () -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess()
                }

                override fun updateUser(
                    userId: String,
                    updates: Map<String, Any?>,
                    onSuccess: () -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess()
                }

                override fun deleteUser(
                    userId: String,
                    onSuccess: () -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess()
                }

                override fun getUsersByUniversity(
                    university: String,
                    onSuccess: (List<User>) -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess(listOf(minimalUser))
                }

                override fun getUsersByHobby(
                    hobby: String,
                    onSuccess: (List<User>) -> Unit,
                    onFailure: (Exception) -> Unit
                ) {
                  onSuccess(listOf(minimalUser))
                }

                override fun getNewUid(): String = "new_minimal_uid"
              },
          modifier = Modifier.fillMaxSize())
    }
  }
}
