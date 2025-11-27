package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestUserRepository
  private lateinit var viewModel: ProfileViewModel
  private val testUser =
      User(
          userId = "test_user",
          username = "johndoe",
          firstName = "John",
          lastName = "Doe",
          email = "john@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthdate = "01/01/2000",
          hobbies = listOf("Reading", "Coding"),
          bio = "Test bio")

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    viewModel = ProfileViewModel(repository, testUser.userId)
  }

  @Test
  fun `loadUserProfile loads user successfully`() = runTest {
    kotlinx.coroutines.delay(100)
    assertEquals(testUser, viewModel.user.value)
  }

  @Test
  fun `loadUserProfile handles error`() = runTest {
    repository.shouldThrowOnGet = RuntimeException("Load failed")
    viewModel.loadUserProfile()
    kotlinx.coroutines.delay(100)

    assertTrue(viewModel.fieldErrors.value.isNotEmpty())
  }

  @Test
  fun `startEditing sets editing field`() {
    viewModel.startEditing(EditingField.Name)
    assertEquals(EditingField.Name, viewModel.editingField.value)
  }

  @Test
  fun `startEditing clears field error`() = runTest {
    viewModel.updateName("", "Doe")
    kotlinx.coroutines.delay(100)

    viewModel.startEditing(EditingField.Name)
    assertNull(viewModel.fieldErrors.value[EditingField.Name])
  }

  @Test
  fun `cancelEditing resets editing field`() {
    viewModel.startEditing(EditingField.University)
    viewModel.cancelEditing()
    assertEquals(EditingField.None, viewModel.editingField.value)
  }

  @Test
  fun `updateName with valid names succeeds`() = runTest {
    viewModel.updateName("Alice", "Smith")
    kotlinx.coroutines.delay(200)

    val savedUser = repository.savedUsers.last()
    assertEquals("Alice", savedUser.firstName)
    assertEquals("Smith", savedUser.lastName)
    assertEquals(EditingField.None, viewModel.editingField.value)
  }

  @Test
  fun `updateName with blank firstName shows error`() = runTest {
    viewModel.updateName("", "Doe")
    kotlinx.coroutines.delay(100)

    assertEquals(R.string.error_name_empty, viewModel.fieldErrors.value[EditingField.Name])
  }

  @Test
  fun `updateName with blank lastName shows error`() = runTest {
    viewModel.updateName("John", "")
    kotlinx.coroutines.delay(100)

    assertEquals(R.string.error_name_empty, viewModel.fieldErrors.value[EditingField.Name])
  }

  @Test
  fun `updateName trims whitespace`() = runTest {
    viewModel.updateName("  Alice  ", "  Smith  ")
    kotlinx.coroutines.delay(200)

    val savedUser = repository.savedUsers.last()
    assertEquals("Alice", savedUser.firstName)
    assertEquals("Smith", savedUser.lastName)
  }

  @Test
  fun `updateUniversity with valid name succeeds`() = runTest {
    viewModel.updateUniversity("MIT")
    kotlinx.coroutines.delay(200)

    assertEquals("MIT", repository.savedUsers.last().university)
  }

  @Test
  fun `updateUniversity with blank name shows error`() = runTest {
    viewModel.updateUniversity("")
    kotlinx.coroutines.delay(100)

    assertNotNull(viewModel.fieldErrors.value[EditingField.University])
  }

  @Test
  fun `updateCountry succeeds`() = runTest {
    viewModel.updateCountry("France")
    kotlinx.coroutines.delay(200)

    assertEquals("France", repository.savedUsers.last().country)
  }

  @Test
  fun `updateCountry with blank value sets null`() = runTest {
    viewModel.updateCountry("")
    kotlinx.coroutines.delay(200)

    assertNull(repository.savedUsers.last().country)
  }

  @Test
  fun `updateBirthday with valid date succeeds`() = runTest {
    viewModel.updateBirthday("15/06/1995")
    kotlinx.coroutines.delay(200)

    assertEquals("15/06/1995", repository.savedUsers.last().birthdate)
  }

  @Test
  fun `updateBirthday with invalid format shows error`() = runTest {
    viewModel.updateBirthday("invalid")
    kotlinx.coroutines.delay(100)

    assertNotNull(viewModel.fieldErrors.value[EditingField.Birthday])
  }

  @Test
  fun `updateBirthday with blank value sets null`() = runTest {
    viewModel.updateBirthday("")
    kotlinx.coroutines.delay(200)

    assertNull(repository.savedUsers.last().birthdate)
  }

  @Test
  fun `updateActivities parses comma-separated list`() = runTest {
    viewModel.updateActivities("Reading, Gaming, Hiking")
    kotlinx.coroutines.delay(200)

    val savedUser = repository.savedUsers.last()
    assertEquals(listOf("Reading", "Gaming", "Hiking"), savedUser.hobbies)
  }

  @Test
  fun `updateActivities filters empty strings`() = runTest {
    viewModel.updateActivities("Reading, , Hiking")
    kotlinx.coroutines.delay(200)

    val savedUser = repository.savedUsers.last()
    assertEquals(listOf("Reading", "Hiking"), savedUser.hobbies)
  }

  @Test
  fun `updateBio succeeds`() = runTest {
    viewModel.updateBio("New bio")
    kotlinx.coroutines.delay(200)

    assertEquals("New bio", repository.savedUsers.last().bio)
  }

  @Test
  fun `updateBio with blank value sets null`() = runTest {
    viewModel.updateBio("")
    kotlinx.coroutines.delay(200)

    assertNull(repository.savedUsers.last().bio)
  }

  @Test
  fun `clearSuccessMessage clears message`() {
    viewModel.updateName("Alice", "Smith")
    viewModel.clearSuccessMessage()
    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun `clearFieldErrors clears all errors`() = runTest {
    viewModel.updateName("", "")
    kotlinx.coroutines.delay(100)

    viewModel.clearFieldErrors()
    assertTrue(viewModel.fieldErrors.value.isEmpty())
  }

  @Test
  fun `repository error is handled`() = runTest {
    repository.shouldThrowOnSave = RuntimeException("Save failed")
    viewModel.updateName("Alice", "Smith")
    kotlinx.coroutines.delay(200)

    assertNotNull(viewModel.fieldErrors.value[EditingField.Name])
  }

  @Test
  fun `loading state is set during update`() = runTest {
    repository.saveDelay = 500L
    viewModel.updateName("Alice", "Smith")
    kotlinx.coroutines.delay(100)

    assertTrue(viewModel.loadingFields.value.contains(EditingField.Name))
  }

  @Test
  fun `success message is set after update`() = runTest {
    viewModel.updateName("Alice", "Smith")
    kotlinx.coroutines.delay(200)

    assertNotNull(viewModel.successMessage.value)
  }

  @Test
  fun `EditingField displayNameResId returns correct resource IDs`() {
    assertEquals(R.string.label_name, EditingField.Name.displayNameResId)
    assertEquals(R.string.label_university, EditingField.University.displayNameResId)
    assertEquals(R.string.label_country, EditingField.Country.displayNameResId)
    assertEquals(R.string.label_birthday, EditingField.Birthday.displayNameResId)
    assertEquals(R.string.label_activities, EditingField.Activities.displayNameResId)
    assertEquals(R.string.label_bio, EditingField.Bio.displayNameResId)
    assertEquals(R.string.label_profile, EditingField.None.displayNameResId)
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnGet: Throwable? = null,
      var shouldThrowOnSave: Throwable? = null,
      var saveDelay: Long = 0L
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      shouldThrowOnGet?.let { throw it }
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) {
      if (saveDelay > 0) kotlinx.coroutines.delay(saveDelay)
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

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.ui.screen.activities.Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun removeInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        unsupported()

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

    private fun unsupported(): Nothing =
        throw UnsupportedOperationException("Not required for test")
  }

  companion object {
    private val DEFAULT_USER =
        User(
            userId = "user-123",
            email = "john.doe@epfl.ch",
            username = "johndoe",
            firstName = "John",
            lastName = "Doe",
            university = "EPFL",
            hobbies = listOf("running"),
            profilePictureUrl = "https://example.com/profile.png",
            bio = "Student at EPFL",
            country = "Switzerland",
            birthdate = "01/01/2000",
            createdAt = 1_000L,
            updatedAt = 2_000L)
  }
}
