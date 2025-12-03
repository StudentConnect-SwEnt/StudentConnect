package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class UserCardViewModelTest {

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var testScope: TestScope
  private lateinit var userRepository: TestUserRepository
  private lateinit var viewModel: UserCardViewModel

  private val testUser =
      User(
          userId = "test_user_123",
          username = "testuser",
          firstName = "Test",
          lastName = "User",
          email = "test@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthdate = "01/01/2000",
          hobbies = listOf("Reading", "Coding"),
          bio = "Test bio")

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    testScope = TestScope(testDispatcher)
    Dispatchers.setMain(testDispatcher)

    userRepository = TestUserRepository(testUser)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `viewModel initializes and loads user data`() =
      testScope.runTest {
        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(testUser, viewModel.user.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `viewModel shows loading state during initialization`() =
      testScope.runTest {
        userRepository.delay = 100L

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel handles user fetch error`() =
      testScope.runTest {
        userRepository.shouldThrowError = true

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()

        assertNull(viewModel.user.value)
        assertEquals("User fetch failed", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel loads correct user by id`() =
      testScope.runTest {
        val specificUser =
            User(
                userId = "specific_user_456",
                username = "specificuser",
                firstName = "Specific",
                lastName = "User",
                email = "specific@example.com",
                university = "ETH",
                bio = "Specific bio")

        userRepository.user = specificUser

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = "specific_user_456")

        advanceUntilIdle()

        assertEquals(specificUser, viewModel.user.value)
        assertEquals("Specific", viewModel.user.value?.firstName)
        assertEquals("User", viewModel.user.value?.lastName)
      }

  @Test
  fun `viewModel reload refreshes user data`() =
      testScope.runTest {
        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals("Test", viewModel.user.value?.firstName)

        // Update repository data
        val updatedUser = testUser.copy(firstName = "Updated")
        userRepository.user = updatedUser

        viewModel.reload()
        advanceUntilIdle()

        assertEquals("Updated", viewModel.user.value?.firstName)
        assertEquals(updatedUser, viewModel.user.value)
      }

  @Test
  fun `viewModel reload shows loading state`() =
      testScope.runTest {
        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)

        userRepository.delay = 100L
        viewModel.reload()

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel reload clears previous error`() =
      testScope.runTest {
        userRepository.shouldThrowError = true
        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals("User fetch failed", viewModel.error.value)

        // Fix the error and reload
        userRepository.shouldThrowError = false
        viewModel.reload()
        advanceUntilIdle()

        assertNull(viewModel.error.value)
        assertEquals(testUser, viewModel.user.value)
      }

  @Test
  fun `viewModel handles null user from repository`() =
      testScope.runTest {
        userRepository.user = null

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = "non_existent")

        advanceUntilIdle()

        assertNull(viewModel.user.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `viewModel maintains user data after multiple reloads`() =
      testScope.runTest {
        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals(testUser, viewModel.user.value)

        // Reload multiple times
        viewModel.reload()
        advanceUntilIdle()
        assertEquals(testUser, viewModel.user.value)

        viewModel.reload()
        advanceUntilIdle()
        assertEquals(testUser, viewModel.user.value)
      }

  @Test
  fun `viewModel handles delayed repository response`() =
      testScope.runTest {
        userRepository.delay = 500L

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        assertTrue(viewModel.isLoading.value)
        assertNull(viewModel.user.value)

        advanceTimeBy(250L)
        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
        assertEquals(testUser, viewModel.user.value)
      }

  @Test
  fun `viewModel handles user with all fields populated`() =
      testScope.runTest {
        val completeUser =
            User(
                userId = "complete_user",
                username = "completeuser",
                firstName = "Complete",
                lastName = "User",
                email = "complete@example.com",
                university = "EPFL",
                country = "Switzerland",
                birthdate = "15/03/1995",
                hobbies = listOf("Reading", "Swimming", "Coding"),
                bio = "A complete user profile",
                profilePictureUrl = "https://example.com/picture.jpg")

        userRepository.user = completeUser

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = "complete_user")

        advanceUntilIdle()

        assertEquals(completeUser, viewModel.user.value)
        assertEquals("Complete", viewModel.user.value?.firstName)
        assertEquals("Switzerland", viewModel.user.value?.country)
        assertEquals("15/03/1995", viewModel.user.value?.birthdate)
        assertEquals(3, viewModel.user.value?.hobbies?.size)
      }

  @Test
  fun `viewModel handles user with minimal fields`() =
      testScope.runTest {
        val minimalUser =
            User(
                userId = "minimal_user",
                username = "minimaluser",
                firstName = "Min",
                lastName = "User",
                email = "min@example.com",
                university = "EPFL",
                hobbies = emptyList())

        userRepository.user = minimalUser

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = "minimal_user")

        advanceUntilIdle()

        assertEquals(minimalUser, viewModel.user.value)
        assertNull(viewModel.user.value?.country)
        assertNull(viewModel.user.value?.birthdate)
        assertNull(viewModel.user.value?.bio)
        assertTrue(viewModel.user.value?.hobbies?.isEmpty() ?: false)
      }

  @Test
  fun `viewModel handles exception with custom message`() =
      testScope.runTest {
        userRepository.shouldThrowError = true
        userRepository.errorMessage = "Custom error message"

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals("Custom error message", viewModel.error.value)
      }

  @Test
  fun `viewModel handles exception without message`() =
      testScope.runTest {
        userRepository.shouldThrowError = true
        userRepository.errorMessage = null

        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals("Failed to load user", viewModel.error.value)
      }

  @Test
  fun `viewModel handles rapid reload calls`() =
      testScope.runTest {
        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        advanceUntilIdle()

        // Call reload multiple times rapidly
        viewModel.reload()
        viewModel.reload()
        viewModel.reload()

        advanceUntilIdle()

        // Should still have correct data
        assertEquals(testUser, viewModel.user.value)
        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel state flows emit correct values`() =
      testScope.runTest {
        viewModel =
            UserCardViewModel(userRepository = userRepository, currentUserId = testUser.userId)

        // Initial state
        assertTrue(viewModel.isLoading.value)
        assertNull(viewModel.user.value)

        advanceUntilIdle()

        // Final state
        assertFalse(viewModel.isLoading.value)
        assertEquals(testUser, viewModel.user.value)
        assertNull(viewModel.error.value)
      }

  // Test helper class
  private class TestUserRepository(
      var user: User?,
      var delay: Long = 0L,
      var shouldThrowError: Boolean = false,
      var errorMessage: String? = "User fetch failed"
  ) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
      if (delay > 0) delay(delay)
      if (shouldThrowError) {
        throw Exception(errorMessage)
      }
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) = Unit

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

    override suspend fun addFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun getFavoriteEvents(userId: String) = emptyList<String>()

    override suspend fun checkUsernameAvailability(username: String) = true
  }
}
