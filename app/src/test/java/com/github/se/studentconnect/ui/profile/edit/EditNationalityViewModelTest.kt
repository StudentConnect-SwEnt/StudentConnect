package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditNationalityViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestUserRepository
  private lateinit var viewModel: EditNationalityViewModel
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "01/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = null)

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    viewModel = EditNationalityViewModel(repository, testUser.userId)
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
    val errorViewModel = EditNationalityViewModel(repository, "non_existent_user")

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertNull(errorViewModel.user.value)
    assertFalse(errorViewModel.isLoading.value)
    // When getUserById returns null, no exception is thrown, so no error message
    assertNull(errorViewModel.errorMessage.value)
  }

  @Test
  fun `initial state handles repository error`() = runTest {
    repository = TestUserRepository(testUser, shouldThrowOnGet = RuntimeException("Load failed"))
    val errorViewModel = EditNationalityViewModel(repository, testUser.userId)

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertNull(errorViewModel.user.value)
    assertFalse(errorViewModel.isLoading.value)
    assertEquals("Load failed", errorViewModel.errorMessage.value)
  }

  @Test
  fun `updateNationality updates country successfully`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    val newCountry = "France"
    viewModel.updateNationality(newCountry)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    // Verify the user was updated in the repository
    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(newCountry, savedUser.country)

    // Verify the viewModel state is updated
    assertEquals(newCountry, viewModel.user.value?.country)
    assertFalse(viewModel.isLoading.value)
    assertEquals("Nationality updated successfully", viewModel.successMessage.value)
    assertNull(viewModel.errorMessage.value)
  }

  @Test
  fun `updateNationality handles null user gracefully`() = runTest {
    repository = TestUserRepository(null)
    val errorViewModel = EditNationalityViewModel(repository, "non_existent_user")

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    errorViewModel.updateNationality("France")

    // Wait for update attempt
    kotlinx.coroutines.delay(200)

    // Should not save anything when user is null
    assertTrue(repository.savedUsers.isEmpty())
    assertFalse(errorViewModel.isLoading.value)
  }

  @Test
  fun `updateNationality handles save error`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    repository.shouldThrowOnSave = RuntimeException("Save failed")
    viewModel.updateNationality("Germany")

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    assertFalse(viewModel.isLoading.value)
    assertEquals("Save failed", viewModel.errorMessage.value)
    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun `updateNationality with empty country name`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    val emptyCountry = ""
    viewModel.updateNationality(emptyCountry)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    // Should still save (validation happens on UI level)
    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(emptyCountry, savedUser.country)
  }

  @Test
  fun `updateNationality with country containing special characters`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    val specialCountry = "Côte d'Ivoire"
    viewModel.updateNationality(specialCountry)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(specialCountry, savedUser.country)
    assertEquals("Nationality updated successfully", viewModel.successMessage.value)
  }

  @Test
  fun `updateNationality with very long country name`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    val longCountry = "United Kingdom of Great Britain and Northern Ireland and Other Territories"
    viewModel.updateNationality(longCountry)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(longCountry, savedUser.country)
  }

  @Test
  fun `multiple updateNationality calls work correctly`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    // First update
    viewModel.updateNationality("France")
    kotlinx.coroutines.delay(200)

    // Second update
    viewModel.updateNationality("Germany")
    kotlinx.coroutines.delay(200)

    // Third update
    viewModel.updateNationality("Italy")
    kotlinx.coroutines.delay(200)

    // Should have 3 saves
    assertEquals(3, repository.savedUsers.size)
    assertEquals("Italy", repository.savedUsers.last().country)
    assertEquals("Italy", viewModel.user.value?.country)
  }

  @Test
  fun `clearErrorMessage clears error state`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    // Create an error
    repository.shouldThrowOnSave = RuntimeException("Save failed")
    viewModel.updateNationality("Spain")
    kotlinx.coroutines.delay(200)

    assertNotNull(viewModel.errorMessage.value)

    // Clear the error
    viewModel.clearErrorMessage()

    assertNull(viewModel.errorMessage.value)
  }

  @Test
  fun `clearSuccessMessage clears success state`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    // Create a success message
    viewModel.updateNationality("Portugal")
    kotlinx.coroutines.delay(200)

    assertNotNull(viewModel.successMessage.value)

    // Clear the success message
    viewModel.clearSuccessMessage()

    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun `isLoading is true during initial load`() = runTest {
    // Create a new viewModel with slow repository
    repository = TestUserRepository(testUser, getDelay = 500L)
    val slowViewModel = EditNationalityViewModel(repository, testUser.userId)

    // Give a small delay to let the coroutine start
    kotlinx.coroutines.delay(50)

    // Check loading state
    assertTrue(slowViewModel.isLoading.value)

    // Wait for load to complete
    kotlinx.coroutines.delay(600)

    assertFalse(slowViewModel.isLoading.value)
  }

  @Test
  fun `isLoading is true during save operation`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    // Set up slow save
    repository.saveDelay = 500L
    viewModel.updateNationality("Netherlands")

    // Check loading state immediately after calling update
    kotlinx.coroutines.delay(50)
    assertTrue(viewModel.isLoading.value)

    // Wait for save to complete
    kotlinx.coroutines.delay(600)
    assertFalse(viewModel.isLoading.value)
  }

  @Test
  fun `updateNationality preserves other user fields`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    val newCountry = "Belgium"
    viewModel.updateNationality(newCountry)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    val savedUser = repository.savedUsers.last()
    assertEquals(testUser.userId, savedUser.userId)
    assertEquals(testUser.firstName, savedUser.firstName)
    assertEquals(testUser.lastName, savedUser.lastName)
    assertEquals(testUser.email, savedUser.email)
    assertEquals(testUser.university, savedUser.university)
    assertEquals(testUser.birthday, savedUser.birthday)
    assertEquals(testUser.hobbies, savedUser.hobbies)
    assertEquals(testUser.bio, savedUser.bio)
    assertEquals(testUser.profilePictureUrl, savedUser.profilePictureUrl)
    assertEquals(newCountry, savedUser.country)
  }

  @Test
  fun `updateNationality from null country to valid country`() = runTest {
    // Create user with null country
    val userWithNullCountry = testUser.copy(country = null)
    repository = TestUserRepository(userWithNullCountry)
    viewModel = EditNationalityViewModel(repository, userWithNullCountry.userId)

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertNull(viewModel.user.value?.country)

    viewModel.updateNationality("Austria")
    kotlinx.coroutines.delay(200)

    assertEquals("Austria", viewModel.user.value?.country)
    assertTrue(repository.savedUsers.isNotEmpty())
  }

  @Test
  fun `updateNationality to same country still triggers save`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    val originalCountry = testUser.country
    viewModel.updateNationality(originalCountry!!)

    // Wait for update to complete
    kotlinx.coroutines.delay(200)

    // Should still save even if country is the same
    assertTrue(repository.savedUsers.isNotEmpty())
    assertEquals(originalCountry, repository.savedUsers.last().country)
    assertEquals("Nationality updated successfully", viewModel.successMessage.value)
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnSave: Throwable? = null,
      var shouldThrowOnGet: Throwable? = null,
      var saveDelay: Long = 0L,
      var getDelay: Long = 0L
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      if (getDelay > 0) kotlinx.coroutines.delay(getDelay)
      shouldThrowOnGet?.let { throw it }
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) {
      if (saveDelay > 0) kotlinx.coroutines.delay(saveDelay)
      shouldThrowOnSave?.let { throw it }
      this.user = user
      savedUsers.add(user)
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

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.ui.screen.activities.Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

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
  }
}
