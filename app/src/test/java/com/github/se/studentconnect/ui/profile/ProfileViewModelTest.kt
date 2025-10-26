package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.profile.EditingField
import com.github.se.studentconnect.ui.screen.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileViewModelTest {

  private val dispatcher = StandardTestDispatcher()
  private val scope = TestScope(dispatcher)

  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: ProfileViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    repository = FakeUserRepository()
    viewModel = ProfileViewModel(repository, DEFAULT_USER.userId)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `loadUserProfile populates state with repository user`() =
      scope.runTest {
        advanceUntilIdle()

        assertSame(DEFAULT_USER, viewModel.user.value)
        assertEquals(EditingField.None, viewModel.editingField.value)
        assertTrue(viewModel.loadingFields.value.isEmpty())
        assertTrue(viewModel.fieldErrors.value.isEmpty())
        assertNull(viewModel.successMessage.value)
      }

  @Test
  fun `loadUserProfile failure exposes error`() =
      scope.runTest {
        val failure = IllegalStateException("User not found")
        repository.shouldThrowOnGet = failure

        viewModel.loadUserProfile()
        advanceUntilIdle()

        val errors = viewModel.fieldErrors.value
        assertEquals("User not found", errors[EditingField.None])
      }

  @Test
  fun `startEditing updates editing field and clears existing error`() =
      scope.runTest {
        val failureMessage = "Invalid university"
        repository.shouldThrowOnSave = IllegalStateException(failureMessage)

        viewModel.startEditing(EditingField.University)
        viewModel.updateUniversity("EPFL")
        advanceUntilIdle()
        assertEquals(failureMessage, viewModel.fieldErrors.value[EditingField.University])

        viewModel.startEditing(EditingField.University)
        assertFalse(viewModel.fieldErrors.value.containsKey(EditingField.University))
        assertEquals(EditingField.University, viewModel.editingField.value)
      }

  @Test
  fun `cancelEditing clears editing state`() =
      scope.runTest {
        viewModel.startEditing(EditingField.Country)
        viewModel.cancelEditing()

        assertEquals(EditingField.None, viewModel.editingField.value)
      }

  @Test
  fun `updateName validates input`() =
      scope.runTest {
        viewModel.updateName("", "Doe")
        assertEquals(
            "First name and last name cannot be empty",
            viewModel.fieldErrors.value[EditingField.Name])
        assertTrue(repository.savedUsers.isEmpty())
      }

  @Test
  fun `updateName persists trimmed data and emits success`() =
      scope.runTest {
        viewModel.updateName("  Alice ", " Smith ")
        assertTrue(viewModel.loadingFields.value.contains(EditingField.Name))

        advanceUntilIdle()

        assertFalse(viewModel.loadingFields.value.contains(EditingField.Name))
        val savedUser = repository.savedUsers.single()
        assertEquals("Alice", savedUser.firstName)
        assertEquals("Smith", savedUser.lastName)
        assertEquals("Name updated successfully", viewModel.successMessage.value)
        assertEquals(EditingField.None, viewModel.editingField.value)
      }

  @Test
  fun `updateUniversity rejects blank value`() =
      scope.runTest {
        viewModel.updateUniversity("   ")
        assertEquals(
            "University cannot be empty", viewModel.fieldErrors.value[EditingField.University])
        assertTrue(repository.savedUsers.isEmpty())
      }

  @Test
  fun `updateUniversity saves trimmed input`() =
      scope.runTest {
        viewModel.updateUniversity(" University of Geneva  ")
        advanceUntilIdle()

        val savedUser = repository.savedUsers.single()
        assertEquals("University of Geneva", savedUser.university)
        assertEquals("University updated successfully", viewModel.successMessage.value)
      }

  @Test
  fun `updateCountry allows nullable value`() =
      scope.runTest {
        viewModel.updateCountry("  Switzerland ")
        advanceUntilIdle()

        assertEquals("Switzerland", repository.savedUsers.last().country)

        viewModel.updateCountry("   ")
        advanceUntilIdle()

        assertNull(repository.savedUsers.last().country)
      }

  @Test
  fun `updateBirthday rejects invalid format`() =
      scope.runTest {
        viewModel.updateBirthday("13-01-2020")
        assertEquals(
            "Please use DD/MM/YYYY format", viewModel.fieldErrors.value[EditingField.Birthday])
        assertEquals(0, repository.savedUsers.size)
      }

  @Test
  fun `updateBirthday accepts valid date`() =
      scope.runTest {
        viewModel.updateBirthday("13/01/2020")
        advanceUntilIdle()

        assertEquals("13/01/2020", repository.savedUsers.last().birthday)
        assertEquals("Birthday updated successfully", viewModel.successMessage.value)
      }

  @Test
  fun `updateActivities normalizes comma separated list`() =
      scope.runTest {
        viewModel.updateActivities(" running, hiking , ,coding ")
        advanceUntilIdle()

        val hobbies = repository.savedUsers.last().hobbies
        assertEquals(listOf("running", "hiking", "coding"), hobbies)
        assertEquals("Activities updated successfully", viewModel.successMessage.value)
      }

  @Test
  fun `updateBio trims and allows clearing`() =
      scope.runTest {
        viewModel.updateBio("  Enthusiastic student ")
        advanceUntilIdle()
        assertEquals("Enthusiastic student", repository.savedUsers.last().bio)

        viewModel.updateBio("  ")
        advanceUntilIdle()
        assertNull(repository.savedUsers.last().bio)
        assertEquals("Bio updated successfully", viewModel.successMessage.value)
      }

  @Test
  fun `updateProfilePicture forwards nullable value`() =
      scope.runTest {
        viewModel.updateProfilePicture("https://example.com/avatar.png")
        advanceUntilIdle()
        assertEquals(
            "https://example.com/avatar.png", repository.savedUsers.last().profilePictureUrl)

        viewModel.updateProfilePicture(null)
        advanceUntilIdle()
        assertNull(repository.savedUsers.last().profilePictureUrl)
        assertEquals("Profile picture updated successfully", viewModel.successMessage.value)
      }

  @Test
  fun `clearSuccessMessage resets value`() =
      scope.runTest {
        viewModel.updateCountry("France")
        advanceUntilIdle()
        assertTrue(viewModel.successMessage.value?.isNotBlank() == true)

        viewModel.clearSuccessMessage()
        assertNull(viewModel.successMessage.value)
      }

  @Test
  fun `clearFieldErrors removes all errors`() =
      scope.runTest {
        viewModel.updateName("", "")
        assertFalse(viewModel.fieldErrors.value.isEmpty())

        viewModel.clearFieldErrors()
        assertTrue(viewModel.fieldErrors.value.isEmpty())
      }

  @Test
  fun `update failure surfaces field error and clears loading`() =
      scope.runTest {
        val failure = IllegalStateException("Firestore down")
        repository.shouldThrowOnSave = failure

        viewModel.updateCountry("France")
        assertTrue(viewModel.loadingFields.value.contains(EditingField.Country))

        advanceUntilIdle()

        assertFalse(viewModel.loadingFields.value.contains(EditingField.Country))
        assertEquals("Firestore down", viewModel.fieldErrors.value[EditingField.Country])
        assertTrue(repository.savedUsers.isEmpty())
      }

  @Test
  fun `editingField displayName returns correct values`() {
    assertEquals("Name", EditingField.Name.displayName)
    assertEquals("University", EditingField.University.displayName)
    assertEquals("Country", EditingField.Country.displayName)
    assertEquals("Birthday", EditingField.Birthday.displayName)
    assertEquals("Activities", EditingField.Activities.displayName)
    assertEquals("Bio", EditingField.Bio.displayName)
    assertEquals("Profile Picture", EditingField.ProfilePicture.displayName)
    assertEquals("Profile", EditingField.None.displayName)
  }

  @Test
  fun `updateBirthday accepts empty string`() =
      scope.runTest {
        viewModel.updateBirthday("")
        advanceUntilIdle()

        assertNull(repository.savedUsers.last().birthday)
        assertEquals("Birthday updated successfully", viewModel.successMessage.value)
      }

  @Test
  fun `updateCountry accepts empty string`() =
      scope.runTest {
        viewModel.updateCountry("")
        advanceUntilIdle()

        assertNull(repository.savedUsers.last().country)
        assertEquals("Country updated successfully", viewModel.successMessage.value)
      }

  @Test
  fun `updateName rejects both blank names`() =
      scope.runTest {
        viewModel.updateName("   ", "   ")
        assertEquals(
            "First name and last name cannot be empty",
            viewModel.fieldErrors.value[EditingField.Name])
        assertTrue(repository.savedUsers.isEmpty())
      }

  @Test
  fun `loadUserProfile is called in init`() =
      scope.runTest {
        advanceUntilIdle()

        // User should be loaded in init
        assertSame(DEFAULT_USER, viewModel.user.value)
      }

  @Test
  fun `multiple field updates preserve independent loading states`() =
      scope.runTest {
        // Start updating multiple fields
        viewModel.updateCountry("France")
        viewModel.updateBio("New bio")

        assertTrue(viewModel.loadingFields.value.contains(EditingField.Country))
        assertTrue(viewModel.loadingFields.value.contains(EditingField.Bio))

        advanceUntilIdle()

        // Both should complete
        assertFalse(viewModel.loadingFields.value.contains(EditingField.Country))
        assertFalse(viewModel.loadingFields.value.contains(EditingField.Bio))
        assertEquals("France", repository.savedUsers[0].country)
        assertEquals("New bio", repository.savedUsers[1].bio)
      }

  @Test
  fun `updateActivities handles single item`() =
      scope.runTest {
        viewModel.updateActivities("running")
        advanceUntilIdle()

        val hobbies = repository.savedUsers.last().hobbies
        assertEquals(listOf("running"), hobbies)
      }

  @Test
  fun `updateActivities handles empty string`() =
      scope.runTest {
        viewModel.updateActivities("")
        advanceUntilIdle()

        val hobbies = repository.savedUsers.last().hobbies
        assertTrue(hobbies.isEmpty())
      }

  @Test
  fun `updateBirthday rejects invalid date with wrong separator`() =
      scope.runTest {
        viewModel.updateBirthday("13.01.2020")
        assertEquals(
            "Please use DD/MM/YYYY format", viewModel.fieldErrors.value[EditingField.Birthday])
        assertEquals(0, repository.savedUsers.size)
      }

  @Test
  fun `updateBirthday rejects invalid day`() =
      scope.runTest {
        viewModel.updateBirthday("32/01/2020")
        assertEquals(
            "Please use DD/MM/YYYY format", viewModel.fieldErrors.value[EditingField.Birthday])
        assertEquals(0, repository.savedUsers.size)
      }

  @Test
  fun `updateBirthday rejects invalid month`() =
      scope.runTest {
        viewModel.updateBirthday("15/13/2020")
        assertEquals(
            "Please use DD/MM/YYYY format", viewModel.fieldErrors.value[EditingField.Birthday])
        assertEquals(0, repository.savedUsers.size)
      }

  private class FakeUserRepository(var user: User = DEFAULT_USER) : UserRepository {

    var shouldThrowOnGet: Throwable? = null
    var shouldThrowOnSave: Throwable? = null
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      shouldThrowOnGet?.let { throw it }
      return user
    }

    override suspend fun saveUser(user: User) {
      shouldThrowOnSave?.let { throw it }
      savedUsers += user
      this.user = user
    }

    override suspend fun leaveEvent(eventId: String, userId: String) = unsupported()

    override suspend fun getUserByEmail(email: String) = unsupported()

    override suspend fun getAllUsers(): List<User> = unsupported()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) = unsupported()

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = unsupported()

    override suspend fun deleteUser(userId: String) = unsupported()

    override suspend fun getUsersByUniversity(university: String): List<User> = unsupported()

    override suspend fun getUsersByHobby(hobby: String): List<User> = unsupported()

    override suspend fun getNewUid(): String = unsupported()

    override suspend fun getJoinedEvents(userId: String): List<String> = unsupported()

    override suspend fun addEventToUser(eventId: String, userId: String) = unsupported()

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        unsupported()

    override suspend fun getInvitations(userId: String): List<Invitation> = unsupported()

    override suspend fun acceptInvitation(eventId: String, userId: String) = unsupported()

    override suspend fun declineInvitation(eventId: String, userId: String) = unsupported()

    override suspend fun joinEvent(eventId: String, userId: String) = unsupported()

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

      private fun unsupported(): Nothing =
        throw UnsupportedOperationException("Not required for test")
  }

  companion object {
    private val DEFAULT_USER =
        User(
            userId = "user-123",
            email = "john.doe@epfl.ch",
            firstName = "John",
            lastName = "Doe",
            university = "EPFL",
            hobbies = listOf("running"),
            profilePictureUrl = "https://example.com/profile.png",
            bio = "Student at EPFL",
            country = "Switzerland",
            birthday = "01/01/2000",
            createdAt = 1_000L,
            updatedAt = 2_000L)
  }
}
