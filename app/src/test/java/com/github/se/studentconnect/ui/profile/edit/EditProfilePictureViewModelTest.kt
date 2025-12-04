package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfilePictureViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestUserRepository
  private lateinit var viewModel: EditProfilePictureViewModel
  private val testUser =
      User(
          userId = "test_user",
          username = "johndoe",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthdate = "01/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = "http://example.com/old-pic.jpg")

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    viewModel = EditProfilePictureViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial state loads user profile correctly`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals(testUser, viewModel.user.value)
    assertFalse(viewModel.isLoading.value)
    assertNull(viewModel.errorMessage.value)
    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun `initial state handles user not found`() = runTest {
    repository = TestUserRepository(null)
    val errorViewModel = EditProfilePictureViewModel(repository, "non_existent_user")

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertNull(errorViewModel.user.value)
    assertFalse(errorViewModel.isLoading.value)
  }

  @Test
  fun `initial state handles repository error`() = runTest {
    repository.shouldThrowOnGet = RuntimeException("Repository error")
    val errorViewModel = EditProfilePictureViewModel(repository, testUser.userId)

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertNull(errorViewModel.user.value)
    assertFalse(errorViewModel.isLoading.value)
    assertEquals("Repository error", errorViewModel.errorMessage.value)
  }

  @Test
  fun `updateProfilePicture saves new URL successfully`() = runTest {
    val newUrl = "http://example.com/new-pic.jpg"

    viewModel.updateProfilePicture(newUrl)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(newUrl, savedUser.profilePictureUrl)
    assertEquals(newUrl, viewModel.user.value?.profilePictureUrl)
    assertEquals("Profile picture updated successfully", viewModel.successMessage.value)
    assertFalse(viewModel.isLoading.value)
  }

  @Test
  fun `updateProfilePicture clears profile picture`() = runTest {
    viewModel.updateProfilePicture(null)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertNull(savedUser.profilePictureUrl)
    assertNull(viewModel.user.value?.profilePictureUrl)
    assertEquals("Profile picture updated successfully", viewModel.successMessage.value)
  }

  @Test
  fun `updateProfilePicture handles repository error`() = runTest {
    repository.shouldThrowOnSave = RuntimeException("Save failed")

    viewModel.updateProfilePicture("http://example.com/new-pic.jpg")

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    assertEquals("Save failed", viewModel.errorMessage.value)
    assertFalse(viewModel.isLoading.value)
    // User should remain unchanged
    assertEquals(testUser.profilePictureUrl, viewModel.user.value?.profilePictureUrl)
  }

  @Test
  fun `updateProfilePicture handles user not found`() = runTest {
    repository = TestUserRepository(null)
    val errorViewModel = EditProfilePictureViewModel(repository, "non_existent_user")

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    errorViewModel.updateProfilePicture("http://example.com/new-pic.jpg")

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    // Should handle gracefully without crashing
    assertFalse(errorViewModel.isLoading.value)
  }

  @Test
  fun `clearErrorMessage clears error message`() {
    // Given: there's an error message
    viewModel.clearErrorMessage()

    // Then: error message is cleared
    assertNull(viewModel.errorMessage.value)
  }

  @Test
  fun `clearSuccessMessage clears success message`() {
    // Given: there's a success message
    viewModel.clearSuccessMessage()

    // Then: success message is cleared
    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun `updateProfilePicture with invalid URL`() = runTest {
    val invalidUrl = "not-a-valid-url"

    viewModel.updateProfilePicture(invalidUrl)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(invalidUrl, savedUser.profilePictureUrl)
  }

  @Test
  fun `updateProfilePicture with empty URL`() = runTest {
    viewModel.updateProfilePicture("")

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals("", savedUser.profilePictureUrl)
  }

  @Test
  fun `updateProfilePicture preserves other user fields`() = runTest {
    val newUrl = "http://example.com/new-pic.jpg"

    viewModel.updateProfilePicture(newUrl)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    val savedUser = repository.savedUsers.last()
    assertEquals(testUser.firstName, savedUser.firstName)
    assertEquals(testUser.lastName, savedUser.lastName)
    assertEquals(testUser.email, savedUser.email)
    assertEquals(testUser.university, savedUser.university)
    assertEquals(testUser.country, savedUser.country)
    assertEquals(testUser.birthdate, savedUser.birthdate)
    assertEquals(testUser.hobbies, savedUser.hobbies)
    assertEquals(testUser.bio, savedUser.bio)
    assertEquals(newUrl, savedUser.profilePictureUrl)
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnGet: Throwable? = null,
      var shouldThrowOnSave: Throwable? = null
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      shouldThrowOnGet?.let { throw it }
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) {
      shouldThrowOnSave?.let { throw it }
      savedUsers.add(user)
      this.user = user
    }

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<User>()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<User>() to false

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String) = emptyList<User>()

    override suspend fun getUsersByHobby(hobby: String) = emptyList<User>()

    override suspend fun getNewUid() = "new_uid"

    override suspend fun getJoinedEvents(userId: String) = emptyList<String>()

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(userId: String) = emptyList<Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun removeInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit

    override suspend fun addFavoriteEvent(userId: String, eventId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun getFavoriteEvents(userId: String): List<String> {
      TODO("Not yet implemented")
    }

    override suspend fun checkUsernameAvailability(username: String): Boolean {
      TODO("Not yet implemented")
    }
    override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun getPinnedEvents(userId: String) = emptyList<String>()
  }
}
