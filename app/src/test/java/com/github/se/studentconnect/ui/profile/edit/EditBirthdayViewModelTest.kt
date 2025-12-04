package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.util.MainDispatcherRule
import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditBirthdayViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: TestableUserRepositoryLocal
  private lateinit var viewModel: EditBirthdayViewModel
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthdate = "15/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = null,
          username = "johndoe",
      )

  @Before
  fun setUp() = runTest {
    repository = TestableUserRepositoryLocal()
    repository.saveUser(testUser)
    viewModel = EditBirthdayViewModel(repository, testUser.userId)
  }

  @Test
  fun `initial state loads birthday correctly`() = runTest {
    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals("15/01/2000", viewModel.birthdayString.value)
    assertNotNull(viewModel.selectedDateMillis.value)
    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Idle)
  }

  @Test
  fun `initial state handles user with no birthday`() = runTest {
    val userWithNoBirthday = testUser.copy(birthdate = null)
    val testRepository = TestableUserRepositoryLocal()
    testRepository.saveUser(userWithNoBirthday)
    val noBirthdayViewModel = EditBirthdayViewModel(testRepository, userWithNoBirthday.userId)

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertNull(noBirthdayViewModel.birthdayString.value)
    assertNull(noBirthdayViewModel.selectedDateMillis.value)
    assertTrue(noBirthdayViewModel.uiState.value is EditBirthdayViewModel.UiState.Idle)
  }

  @Test
  fun `initial state handles user not found`() = runTest {
    val testRepository = TestableUserRepositoryLocal()
    val errorViewModel = EditBirthdayViewModel(testRepository, "non_existent_user")

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertNull(errorViewModel.birthdayString.value)
    assertNull(errorViewModel.selectedDateMillis.value)
    // When user is not found, it's treated same as user with no birthday (Idle state)
    assertTrue(errorViewModel.uiState.value is EditBirthdayViewModel.UiState.Idle)
  }

  @Test
  fun `initial state handles invalid birthday format`() = runTest {
    val userWithInvalidBirthday = testUser.copy(birthdate = "invalid-date")
    val testRepository = TestableUserRepositoryLocal()
    testRepository.saveUser(userWithInvalidBirthday)
    val invalidBirthdayViewModel =
        EditBirthdayViewModel(testRepository, userWithInvalidBirthday.userId)

    // Wait for initial load to complete
    kotlinx.coroutines.delay(200)

    assertEquals("invalid-date", invalidBirthdayViewModel.birthdayString.value)
    assertNull(
        invalidBirthdayViewModel.selectedDateMillis.value) // Should be null due to parse error
    assertTrue(invalidBirthdayViewModel.uiState.value is EditBirthdayViewModel.UiState.Idle)
  }

  @Test
  fun `updateSelectedDate updates date in milliseconds and formatted string`() {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.MARCH, 25, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    viewModel.updateSelectedDate(millis)

    assertEquals(millis, viewModel.selectedDateMillis.value)
    assertEquals("25/03/2000", viewModel.birthdayString.value)
  }

  @Test
  fun `updateSelectedDate with null clears both date and string`() {
    viewModel.updateSelectedDate(null)

    assertNull(viewModel.selectedDateMillis.value)
    assertNull(viewModel.birthdayString.value)
  }

  @Test
  fun `updateSelectedDate multiple times updates correctly`() {
    val calendar1 = Calendar.getInstance(TimeZone.getDefault())
    calendar1.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar1.set(Calendar.MILLISECOND, 0)
    val millis1 = calendar1.timeInMillis

    viewModel.updateSelectedDate(millis1)
    assertEquals("15/01/2000", viewModel.birthdayString.value)

    val calendar2 = Calendar.getInstance(TimeZone.getDefault())
    calendar2.set(1995, Calendar.DECEMBER, 25, 0, 0, 0)
    calendar2.set(Calendar.MILLISECOND, 0)
    val millis2 = calendar2.timeInMillis

    viewModel.updateSelectedDate(millis2)
    assertEquals("25/12/1995", viewModel.birthdayString.value)
  }

  @Test
  fun `saveBirthday saves valid birthday successfully`() = runTest {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(1995, Calendar.JUNE, 10, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    viewModel.updateSelectedDate(millis)
    viewModel.saveBirthday()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertNotNull(savedUser)
    assertEquals("10/06/1995", savedUser!!.birthdate)
    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Success)
  }

  @Test
  fun `saveBirthday handles user not found error`() = runTest {
    val testRepository = TestableUserRepositoryLocal()
    val errorViewModel = EditBirthdayViewModel(testRepository, "non_existent_user")

    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    errorViewModel.updateSelectedDate(millis)
    errorViewModel.saveBirthday()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(errorViewModel.uiState.value is EditBirthdayViewModel.UiState.Error)
    val errorState = errorViewModel.uiState.value as EditBirthdayViewModel.UiState.Error
    assertTrue(errorState.message.contains(R.string.error_user_not_found.toString()))
  }

  @Test
  fun `saveBirthday handles repository error`() = runTest {
    repository.shouldThrowOnSave = RuntimeException("Network error")

    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    viewModel.updateSelectedDate(millis)
    viewModel.saveBirthday()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Error)
    val errorState = viewModel.uiState.value as EditBirthdayViewModel.UiState.Error
    assertTrue(errorState.message.contains("Network error"))
  }

  @Test
  fun `saveBirthday updates updatedAt timestamp`() = runTest {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    val timeBefore = System.currentTimeMillis()
    viewModel.updateSelectedDate(millis)
    viewModel.saveBirthday()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertNotNull(savedUser)
    assertTrue(savedUser!!.updatedAt >= timeBefore)
  }

  @Test
  fun `removeBirthday sets birthday to null`() = runTest {
    viewModel.removeBirthday()

    // Wait for removal to complete
    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertNotNull(savedUser)
    assertNull(savedUser!!.birthdate)
    assertNull(viewModel.birthdayString.value)
    assertNull(viewModel.selectedDateMillis.value)
    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Success)
  }

  @Test
  fun `removeBirthday handles user not found error`() = runTest {
    val testRepository = TestableUserRepositoryLocal()
    val errorViewModel = EditBirthdayViewModel(testRepository, "non_existent_user")

    errorViewModel.removeBirthday()

    // Wait for removal to complete
    kotlinx.coroutines.delay(200)

    assertTrue(errorViewModel.uiState.value is EditBirthdayViewModel.UiState.Error)
    val errorState = errorViewModel.uiState.value as EditBirthdayViewModel.UiState.Error
    assertTrue(errorState.message.contains(R.string.error_user_not_found.toString()))
  }

  @Test
  fun `removeBirthday handles repository error`() = runTest {
    repository.shouldThrowOnSave = RuntimeException("Network error")

    viewModel.removeBirthday()

    // Wait for removal to complete
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Error)
    val errorState = viewModel.uiState.value as EditBirthdayViewModel.UiState.Error
    assertTrue(errorState.message.contains("Network error"))
  }

  @Test
  fun `removeBirthday updates updatedAt timestamp`() = runTest {
    val timeBefore = System.currentTimeMillis()
    viewModel.removeBirthday()

    // Wait for removal to complete
    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertNotNull(savedUser)
    assertTrue(savedUser!!.updatedAt >= timeBefore)
  }

  @Test
  fun `resetState resets UI state to Idle`() = runTest {
    // Trigger an error
    repository.shouldThrowOnSave = RuntimeException("Error")
    viewModel.saveBirthday()

    // Wait for error
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Error)

    // Reset state
    viewModel.resetState()

    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Idle)
  }

  @Test
  fun `saveBirthday shows loading state`() = runTest {
    repository.saveDelay = 1000L

    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    viewModel.updateSelectedDate(millis)
    viewModel.saveBirthday()

    // Check loading state immediately
    kotlinx.coroutines.delay(50)
    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Loading)
  }

  @Test
  fun `removeBirthday shows loading state`() = runTest {
    repository.saveDelay = 1000L

    viewModel.removeBirthday()

    // Check loading state immediately
    kotlinx.coroutines.delay(50)
    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Loading)
  }

  @Test
  fun `saveBirthday with leap year date`() = runTest {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.FEBRUARY, 29, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    viewModel.updateSelectedDate(millis)
    viewModel.saveBirthday()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    val savedUser = repository.getUserById(testUser.userId)
    assertNotNull(savedUser)
    assertEquals("29/02/2000", savedUser!!.birthdate)
  }

  @Test
  fun `multiple save operations work correctly`() = runTest {
    // First save
    val calendar1 = Calendar.getInstance(TimeZone.getDefault())
    calendar1.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar1.set(Calendar.MILLISECOND, 0)
    viewModel.updateSelectedDate(calendar1.timeInMillis)
    viewModel.saveBirthday()

    // Wait for first save to complete
    kotlinx.coroutines.delay(300)

    // Second save
    val calendar2 = Calendar.getInstance(TimeZone.getDefault())
    calendar2.set(1995, Calendar.DECEMBER, 25, 0, 0, 0)
    calendar2.set(Calendar.MILLISECOND, 0)
    viewModel.updateSelectedDate(calendar2.timeInMillis)
    viewModel.saveBirthday()

    // Wait for second save to complete
    kotlinx.coroutines.delay(300)

    val savedUser = repository.getUserById(testUser.userId)
    assertNotNull(savedUser)
    assertEquals("25/12/1995", savedUser!!.birthdate)
  }

  @Test
  fun `loading user birthday with different date formats`() = runTest {
    val testDates =
        listOf(
            "01/01/2000", "31/12/1995", "15/03/2024", "29/02/2000" // Leap year
            )

    testDates.forEach { dateString ->
      val userWithDate = testUser.copy(birthdate = dateString)
      val testRepository = TestableUserRepositoryLocal()
      testRepository.saveUser(userWithDate)
      val vm = EditBirthdayViewModel(testRepository, userWithDate.userId)

      // Wait for initial load to complete
      kotlinx.coroutines.delay(200)

      assertEquals("Failed for date: $dateString", dateString, vm.birthdayString.value)
      assertNotNull("Failed to parse: $dateString", vm.selectedDateMillis.value)
    }
  }

  @Test
  fun `saveBirthday success message is correct`() = runTest {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(2000, Calendar.JANUARY, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val millis = calendar.timeInMillis

    viewModel.updateSelectedDate(millis)
    viewModel.saveBirthday()

    // Wait for save to complete
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Success)
    val successState = viewModel.uiState.value as EditBirthdayViewModel.UiState.Success
    assertEquals(R.string.success_birthday_updated.toString(), successState.message)
  }

  @Test
  fun `removeBirthday success message is correct`() = runTest {
    viewModel.removeBirthday()

    // Wait for removal to complete
    kotlinx.coroutines.delay(200)

    assertTrue(viewModel.uiState.value is EditBirthdayViewModel.UiState.Success)
    val successState = viewModel.uiState.value as EditBirthdayViewModel.UiState.Success
    assertEquals(R.string.success_birthday_removed.toString(), successState.message)
  }

  /**
   * TestableUserRepositoryLocal wraps UserRepositoryLocal to add error injection capabilities for
   * testing error scenarios.
   */
  private class TestableUserRepositoryLocal : UserRepository {
    private val delegate = UserRepositoryLocal()
    var shouldThrowOnSave: Throwable? = null
    var shouldThrowOnLoad: Throwable? = null
    var saveDelay: Long = 0L

    override suspend fun getUserById(userId: String): User? {
      shouldThrowOnLoad?.let { throw it }
      return delegate.getUserById(userId)
    }

    override suspend fun saveUser(user: User) {
      if (saveDelay > 0) {
        delay(saveDelay)
      }
      shouldThrowOnSave?.let { throw it }
      delegate.saveUser(user)
    }

    override suspend fun getUserByEmail(email: String) = delegate.getUserByEmail(email)

    override suspend fun getAllUsers() = delegate.getAllUsers()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        delegate.getUsersPaginated(limit, lastUserId)

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {
      if (saveDelay > 0) {
        delay(saveDelay)
      }
      shouldThrowOnSave?.let { throw it }
      delegate.updateUser(userId, updates)
    }

    override suspend fun deleteUser(userId: String) = delegate.deleteUser(userId)

    override suspend fun getUsersByUniversity(university: String) =
        delegate.getUsersByUniversity(university)

    override suspend fun getUsersByHobby(hobby: String) = delegate.getUsersByHobby(hobby)

    override suspend fun getNewUid() = delegate.getNewUid()

    override suspend fun getJoinedEvents(userId: String) = delegate.getJoinedEvents(userId)

    override suspend fun addEventToUser(eventId: String, userId: String) =
        delegate.addEventToUser(eventId, userId)

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        delegate.addInvitationToUser(eventId, userId, fromUserId)

    override suspend fun getInvitations(userId: String) = delegate.getInvitations(userId)

    override suspend fun acceptInvitation(eventId: String, userId: String) =
        delegate.acceptInvitation(eventId, userId)

    override suspend fun declineInvitation(eventId: String, userId: String) =
        delegate.declineInvitation(eventId, userId)

    override suspend fun removeInvitation(eventId: String, userId: String) =
        delegate.removeInvitation(eventId, userId)

    override suspend fun joinEvent(eventId: String, userId: String) =
        delegate.joinEvent(eventId, userId)

    override suspend fun leaveEvent(eventId: String, userId: String) =
        delegate.leaveEvent(eventId, userId)

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        delegate.sendInvitation(eventId, fromUserId, toUserId)

    override suspend fun addFavoriteEvent(userId: String, eventId: String) =
        delegate.addFavoriteEvent(userId, eventId)

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) =
        delegate.removeFavoriteEvent(userId, eventId)

    override suspend fun getFavoriteEvents(userId: String) = delegate.getFavoriteEvents(userId)

    override suspend fun checkUsernameAvailability(username: String): Boolean {
      TODO("Not yet implemented")
    }
    override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun getPinnedEvents(userId: String) = emptyList<String>()
  }
}
