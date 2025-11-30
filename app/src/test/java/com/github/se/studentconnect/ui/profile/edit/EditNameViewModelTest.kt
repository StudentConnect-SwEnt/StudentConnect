package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditNameViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestUserRepository
  private lateinit var viewModel: EditNameViewModel
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
          profilePictureUrl = null)

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    viewModel = EditNameViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial state loads user names correctly`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals("John", viewModel.firstName.value)
    assertEquals("Doe", viewModel.lastName.value)
    assertNull(viewModel.firstNameError.value)
    assertNull(viewModel.lastNameError.value)
  }

  @Test
  fun `initial state handles user not found`() = runTest {
    repository = TestUserRepository(null)
    val errorViewModel = EditNameViewModel(repository, "non_existent_user")

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals("", errorViewModel.firstName.value)
    assertEquals("", errorViewModel.lastName.value)
  }

  @Test
  fun `updateFirstName updates first name and clears error`() {
    viewModel.updateFirstName("NewName")
    assertEquals("NewName", viewModel.firstName.value)
    assertNull(viewModel.firstNameError.value)
  }

  @Test
  fun `updateLastName updates last name and clears error`() {
    viewModel.updateLastName("NewLastName")
    assertEquals("NewLastName", viewModel.lastName.value)
    assertNull(viewModel.lastNameError.value)
  }

  @Test
  fun `saveName validates empty first name`() = runTest {
    viewModel.updateFirstName("")
    viewModel.updateLastName("Doe")
    viewModel.saveName()

    // Wait for validation to complete
    kotlinx.coroutines.delay(200)

    assertTrue(
        viewModel.firstNameError.value?.contains(R.string.error_first_name_empty.toString()) ==
            true)
    assertNull(viewModel.lastNameError.value)
    assertTrue(repository.savedUsers.isEmpty())
  }

  @Test
  fun `saveName validates empty last name`() = runTest {
    viewModel.updateFirstName("John")
    viewModel.updateLastName("")
    viewModel.saveName()

    // Wait for validation to complete
    kotlinx.coroutines.delay(200)

    assertNull(viewModel.firstNameError.value)
    assertTrue(
        viewModel.lastNameError.value?.contains(R.string.error_last_name_empty.toString()) == true)
    assertTrue(repository.savedUsers.isEmpty())
  }

  @Test
  fun `saveName validates both empty names`() = runTest {
    viewModel.updateFirstName("")
    viewModel.updateLastName("")
    viewModel.saveName()

    // Wait for validation to complete
    kotlinx.coroutines.delay(200)

    assertTrue(
        viewModel.firstNameError.value?.contains(R.string.error_first_name_empty.toString()) ==
            true)
    assertTrue(
        viewModel.lastNameError.value?.contains(R.string.error_last_name_empty.toString()) == true)
    assertTrue(repository.savedUsers.isEmpty())
  }

  @Test
  fun `saveName validates whitespace only names`() = runTest {
    viewModel.updateFirstName("   ")
    viewModel.updateLastName("   ")
    viewModel.saveName()

    // Wait for validation to complete
    kotlinx.coroutines.delay(200)

    assertTrue(
        viewModel.firstNameError.value?.contains(R.string.error_first_name_empty.toString()) ==
            true)
    assertTrue(
        viewModel.lastNameError.value?.contains(R.string.error_last_name_empty.toString()) == true)
    assertTrue(repository.savedUsers.isEmpty())
  }

  @Test
  fun `saveName saves valid names successfully`() = runTest {
    val newFirstName = "Alice"
    val newLastName = "Smith"
    viewModel.updateFirstName(newFirstName)
    viewModel.updateLastName(newLastName)

    viewModel.saveName()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(newFirstName, savedUser.firstName)
    assertEquals(newLastName, savedUser.lastName)
    assertNull(viewModel.firstNameError.value)
    assertNull(viewModel.lastNameError.value)
  }

  @Test
  fun `saveName trims whitespace from names`() = runTest {
    val firstNameWithWhitespace = "  Bob "
    val lastNameWithWhitespace = "  Johnson  "
    viewModel.updateFirstName(firstNameWithWhitespace)
    viewModel.updateLastName(lastNameWithWhitespace)

    viewModel.saveName()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals("Bob", savedUser.firstName)
    assertEquals("Johnson", savedUser.lastName)
  }

  @Test
  fun `saveName handles user not found error`() = runTest {
    repository = TestUserRepository(null)
    val errorViewModel = EditNameViewModel(repository, "non_existent_user")
    errorViewModel.updateFirstName("John")
    errorViewModel.updateLastName("Doe")

    errorViewModel.saveName()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(errorViewModel.uiState.value is BaseEditViewModel.UiState.Error)
  }

  @Test
  fun `saveName handles repository error`() = runTest {
    repository.shouldThrowOnSave = RuntimeException("Save failed")
    viewModel.updateFirstName("John")
    viewModel.updateLastName("Doe")

    viewModel.saveName()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is BaseEditViewModel.UiState.Error)
  }

  @Test
  fun `clearErrors clears all validation errors`() {
    // Given: there are validation errors
    viewModel.updateFirstName("")
    viewModel.updateLastName("")
    viewModel.saveName()

    // When: clearErrors is called
    viewModel.clearErrors()

    // Then: errors are cleared
    assertNull(viewModel.firstNameError.value)
    assertNull(viewModel.lastNameError.value)
  }

  @Test
  fun `saveName with special characters in names`() = runTest {
    val specialFirstName = "José-María"
    val specialLastName = "O'Connor-Smith"
    viewModel.updateFirstName(specialFirstName)
    viewModel.updateLastName(specialLastName)

    viewModel.saveName()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(specialFirstName, savedUser.firstName)
    assertEquals(specialLastName, savedUser.lastName)
  }

  @Test
  fun `saveName with very long names`() = runTest {
    val longFirstName = "A".repeat(100)
    val longLastName = "B".repeat(100)
    viewModel.updateFirstName(longFirstName)
    viewModel.updateLastName(longLastName)

    viewModel.saveName()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(repository.savedUsers.isNotEmpty())
    val savedUser = repository.savedUsers.last()
    assertEquals(longFirstName, savedUser.firstName)
    assertEquals(longLastName, savedUser.lastName)
  }

  @Test
  fun `multiple save operations work correctly`() = runTest {
    // First save
    viewModel.updateFirstName("Alice")
    viewModel.updateLastName("Smith")
    viewModel.saveName()

    // Wait for first save to complete
    kotlinx.coroutines.delay(300)

    // Second save
    viewModel.updateFirstName("Bob")
    viewModel.updateLastName("Johnson")
    viewModel.saveName()

    // Wait for second save to complete
    kotlinx.coroutines.delay(300)

    assertEquals(2, repository.savedUsers.size)
    assertEquals("Bob", repository.savedUsers.last().firstName)
    assertEquals("Johnson", repository.savedUsers.last().lastName)
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnSave: Throwable? = null
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) {
      shouldThrowOnSave?.let { throw it }
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
  }
}
