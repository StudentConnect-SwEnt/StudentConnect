package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var fakeRepository: FakeUserRepository
  private lateinit var viewModel: ProfileViewModel

  private val baseUser =
      User(
          userId = "user-123",
          email = "user@example.com",
          firstName = "Alex",
          lastName = "Doe",
          university = "EPFL",
          hobbies = listOf("Climbing", "Chess"),
          profilePictureUrl = "https://example.com/pic.jpg",
          bio = "Bio",
          country = "Switzerland",
          birthday = "24/12/1994",
          createdAt = 1L,
          updatedAt = 1L)

  @Before
  fun setUp() {
    fakeRepository = FakeUserRepository(storedUser = baseUser)
    viewModel = ProfileViewModel(fakeRepository, baseUser.userId)
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
  }

  @Test
  fun loadUserProfile_success_updatesState() {
    assertEquals(baseUser, viewModel.user.value)
    assertTrue(fakeRepository.getUserRequests > 0)
  }

  @Test
  fun loadUserProfile_failure_populatesFieldError() {
    val failingRepo = FakeUserRepository(getUserException = IllegalStateException("boom"))
    val failingViewModel = ProfileViewModel(failingRepo, baseUser.userId)

    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    val errors = failingViewModel.fieldErrors.value
    assertEquals("boom", errors[EditingField.None])
  }

  @Test
  fun startEditing_setsFieldAndClearsError() {
    viewModel.updateName("", "Doe")
    assertTrue(viewModel.fieldErrors.value.containsKey(EditingField.Name))

    viewModel.startEditing(EditingField.Name)

    assertEquals(EditingField.Name, viewModel.editingField.value)
    assertFalse(viewModel.fieldErrors.value.containsKey(EditingField.Name))
  }

  @Test
  fun cancelEditing_resetsEditingField() {
    viewModel.startEditing(EditingField.Country)
    viewModel.cancelEditing()

    assertEquals(EditingField.None, viewModel.editingField.value)
  }

  @Test
  fun updateName_blankInput_setsErrorAndSkipsRepository() {
    viewModel.updateName("", "Doe")

    assertTrue(viewModel.fieldErrors.value.containsKey(EditingField.Name))
    assertEquals(0, fakeRepository.saveUserCalls)
  }

  @Test
  fun updateName_withoutLoadedUser_doesNotSave() {
    val repoWithNullUser = FakeUserRepository(storedUser = null)
    val vm = ProfileViewModel(repoWithNullUser, baseUser.userId)
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    vm.updateName("New", "Name")

    assertEquals(0, repoWithNullUser.saveUserCalls)
  }

  @Test
  fun updateName_success_updatesStateAndClearsLoading() {
    viewModel.startEditing(EditingField.Name)

    viewModel.updateName("Jordan", "Smith")

    assertTrue(viewModel.loadingFields.value.contains(EditingField.Name))

    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    val updatedUser = viewModel.user.value!!
    assertEquals("Jordan", updatedUser.firstName)
    assertEquals("Smith", updatedUser.lastName)
    assertEquals(EditingField.None, viewModel.editingField.value)
    assertFalse(viewModel.loadingFields.value.contains(EditingField.Name))
    assertEquals("Name updated successfully", viewModel.successMessage.value)
    assertEquals(updatedUser, fakeRepository.lastSavedUser)
  }

  @Test
  fun updateUniversity_blank_setsError() {
    viewModel.updateUniversity("")

    assertEquals("University cannot be empty", viewModel.fieldErrors.value[EditingField.University])
    assertEquals(0, fakeRepository.saveUserCalls)
  }

  @Test
  fun updateUniversity_success_updatesUser() {
    viewModel.updateUniversity("ETHZ")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    val updated = viewModel.user.value!!
    assertEquals("ETHZ", updated.university)
    assertEquals("University updated successfully", viewModel.successMessage.value)
  }

  @Test
  fun updateCountry_updatesAndTrimsValue() {
    viewModel.updateCountry("  Germany  ")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertEquals("Germany", viewModel.user.value!!.country)
  }

  @Test
  fun updateCountry_blank_setsNull() {
    viewModel.updateCountry("   ")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.user.value!!.country)
  }

  @Test
  fun updateBirthday_invalidFormat_setsError() {
    viewModel.updateBirthday("2020-01-01")

    assertEquals("Please use DD/MM/YYYY format", viewModel.fieldErrors.value[EditingField.Birthday])
    assertEquals(0, fakeRepository.saveUserCalls)
  }

  @Test
  fun updateBirthday_validFormat_updatesUser() {
    viewModel.updateBirthday("01/01/2000")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertEquals("01/01/2000", viewModel.user.value!!.birthday)
    assertEquals("Birthday updated successfully", viewModel.successMessage.value)
  }

  @Test
  fun updateBirthday_blank_clearsBirthday() {
    viewModel.updateBirthday("")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.user.value!!.birthday)
  }

  @Test
  fun updateActivities_filtersBlankEntries() {
    viewModel.updateActivities(" climbing , , chess ,,   hiking ")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertEquals(listOf("climbing", "chess", "hiking"), viewModel.user.value!!.hobbies)
  }

  @Test
  fun updateBio_blank_clearsBio() {
    viewModel.updateBio("   ")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.user.value!!.bio)
  }

  @Test
  fun updateBio_nonBlank_trimsAndSaves() {
    viewModel.updateBio("  Hello world  ")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertEquals("Hello world", viewModel.user.value!!.bio)
  }

  @Test
  fun updateProfilePicture_handlesNull() {
    viewModel.updateProfilePicture(null)
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.user.value!!.profilePictureUrl)
  }

  @Test
  fun updateProfilePicture_withUrl_updatesState() {
    viewModel.updateProfilePicture("https://example.com/new.jpg")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertEquals("https://example.com/new.jpg", viewModel.user.value!!.profilePictureUrl)
  }

  @Test
  fun clearSuccessMessage_resetsValue() {
    viewModel.updateName("Jordan", "Smith")
    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    assertEquals("Name updated successfully", viewModel.successMessage.value)

    viewModel.clearSuccessMessage()

    assertNull(viewModel.successMessage.value)
  }

  @Test
  fun clearFieldErrors_resetsMap() {
    viewModel.updateName("", "Smith")
    assertTrue(viewModel.fieldErrors.value.isNotEmpty())

    viewModel.clearFieldErrors()
    assertTrue(viewModel.fieldErrors.value.isEmpty())
  }

  @Test
  fun updateUserInFirebase_failure_setsErrorAndStopsLoading() {
    fakeRepository.saveUserException = IllegalArgumentException("cannot save")

    viewModel.startEditing(EditingField.Bio)
    viewModel.updateBio("New Bio")

    assertTrue(viewModel.loadingFields.value.contains(EditingField.Bio))

    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

    assertEquals("cannot save", viewModel.fieldErrors.value[EditingField.Bio])
    assertFalse(viewModel.loadingFields.value.contains(EditingField.Bio))
  }

  private class FakeUserRepository(
      var storedUser: User? = null,
      val getUserException: Exception? = null
  ) : UserRepository {

    var saveUserException: Exception? = null
    var saveUserCalls: Int = 0
    var lastSavedUser: User? = null
    var getUserRequests: Int = 0

    override fun getUserById(
        userId: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      getUserRequests++
      val exception = getUserException
      if (exception != null) {
        onFailure(exception)
      } else {
        onSuccess(storedUser)
      }
    }

    override fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
      saveUserCalls++
      val exception = saveUserException
      if (exception != null) {
        onFailure(exception)
      } else {
        storedUser = user
        lastSavedUser = user
        onSuccess()
      }
    }

    override fun leaveEvent(eventId: String, userId: String) = error("Not required for tests")

    override fun getUserByEmail(
        email: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) =
        error("Not required for tests")

    override fun getUsersPaginated(
        limit: Int,
        lastUserId: String?,
        onSuccess: (List<User>, Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun updateUser(
        userId: String,
        updates: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) =
        error("Not required for tests")

    override fun getUsersByUniversity(
        university: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun getUsersByHobby(
        hobby: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = error("Not required for tests")

    override fun getNewUid(): String = "unused"
  }
}
