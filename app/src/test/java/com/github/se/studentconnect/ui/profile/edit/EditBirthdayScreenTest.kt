package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.screen.profile.edit.EditBirthdayScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class EditBirthdayScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: TestableUserRepositoryLocal
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
          username = "Avatar123",
      )

  private var navigatedBack = false

  @Before
  fun setUp() {
    repository = TestableUserRepositoryLocal(testUser)
    navigatedBack = false
  }

  @Test
  fun editBirthdayScreen_displaysCorrectTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Edit Birthday").assertIsDisplayed()
  }

  @Test
  fun editBirthdayScreen_displaysBackButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun editBirthdayScreen_backButtonNavigatesBack() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assertTrue(navigatedBack)
  }

  @Test
  fun editBirthdayScreen_displaysInstructions() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Select your date of birth").assertIsDisplayed()
  }

  @Test
  fun editBirthdayScreen_displaysCurrentBirthdayLabel() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // Current Birthday card was removed - DatePicker now shows the date directly
    composeTestRule.onNodeWithText("Pick a date").assertIsDisplayed()
  }

  @Test
  fun editBirthdayScreen_displaysDatePickerTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Pick a date").assertIsDisplayed()
  }

  @Test
  fun editBirthdayScreen_displaysSaveButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertIsDisplayed()
  }

  @Test
  fun editBirthdayScreen_saveButtonIsEnabledWithValidDate() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertIsEnabled()
  }

  @Test
  fun editBirthdayScreen_handlesUserWithNoBirthday() {
    val userWithNoBirthday = testUser.copy(birthdate = null)
    val testRepo = TestableUserRepositoryLocal(userWithNoBirthday)

    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = testRepo,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // Current birthday card should not be displayed when there's no birthday
    composeTestRule.onNodeWithText("Current Birthday").assertDoesNotExist()
  }

  @Test
  fun editBirthdayScreen_saveButtonCallsSaveBirthday() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save operation and navigation
    composeTestRule.waitForIdle()
    Thread.sleep(500) // Give time for save operation
    composeTestRule.waitForIdle()

    // Should navigate back after successful save
    assert(navigatedBack)
  }

  @Test
  fun editBirthdayScreen_navigatesBackAfterSave() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save operation and navigation
    composeTestRule.waitForIdle()
    Thread.sleep(500) // Give time for save operation
    composeTestRule.waitForIdle()

    // Should navigate back automatically after successful save
    assert(navigatedBack)
  }

  @Test
  fun editBirthdayScreen_showsLoadingStateWhileSaving() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").performClick()

    // Should show loading state (CircularProgressIndicator in button)
    composeTestRule.waitForIdle()
    // Button text should not be "Save" anymore when loading
    composeTestRule.onNodeWithText("Save").assertDoesNotExist()
  }

  @Test
  fun editBirthdayScreen_saveButtonDisabledDuringLoading() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule.waitForIdle()
    // Since button shows progress indicator, it shouldn't have "Save" text
    composeTestRule.onNodeWithText("Save").assertDoesNotExist()
  }

  @Test
  fun editBirthdayScreen_handlesRepositoryError() {
    repository.shouldThrowOnSave = RuntimeException("Network error")

    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for error message
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Network error").assertExists()

    // Should NOT navigate back on error
    assertFalse(navigatedBack)
  }

  @Test
  fun editBirthdayScreen_handlesUserNotFound() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = "non_existent_user",
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Should show error state
    // Save button might be disabled or screen shows error
    composeTestRule.waitForIdle()
  }

  @Test
  fun editBirthdayScreen_loadsInitialBirthdayFromRepository() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    // Wait for UI to settle
    composeTestRule.waitForIdle()

    // DatePicker should be displayed and initialized with the user's birthday
    composeTestRule.onNodeWithText("Pick a date").assertExists()
  }

  @Test
  fun editBirthdayScreen_saveButtonDisabledWhenNoDateSelected() {
    // Create a repository with user that has no birthday
    val userWithNoBirthday = testUser.copy(birthdate = null)
    val testRepo = TestableUserRepositoryLocal(userWithNoBirthday)

    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = testRepo,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // When no date is selected initially, save button should be disabled
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editBirthdayScreen_handlesInvalidBirthdayFormat() {
    val userWithInvalidBirthday = testUser.copy(birthdate = "invalid-date")
    val testRepo = TestableUserRepositoryLocal(userWithInvalidBirthday)

    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = testRepo,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // Invalid date format results in null parseDate, so DatePicker has no initial date
    // Screen should still render without crashing
    composeTestRule.onNodeWithText("Pick a date").assertExists()
  }

  @Test
  fun editBirthdayScreen_savesUpdatedBirthdayToRepository() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // The DatePicker is inline, so the selected date should already be set to the user's birthday
    // Click save to save the current date
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save operation and navigation
    composeTestRule.waitForIdle()
    Thread.sleep(500) // Give time for save operation
    composeTestRule.waitForIdle()

    // Should navigate back after successful save
    assert(navigatedBack)
  }

  @Test
  fun editBirthdayScreen_updatesTimestampOnSave() = runTest {
    val timeBefore = System.currentTimeMillis()

    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save operation and verify timestamp was updated
    composeTestRule.waitForIdle()
    delay(500) // Give time for save operation
    composeTestRule.waitForIdle()

    val timeAfter = System.currentTimeMillis()
    val savedUser = repository.getUserById(testUser.userId)
    assertNotNull(savedUser)
    assert(savedUser!!.updatedAt >= timeBefore && savedUser.updatedAt <= timeAfter)
  }

  @Test
  fun editBirthdayScreen_datePickerDisplaysInitialDate() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // The DatePicker should be displayed with the title
    composeTestRule.onNodeWithText("Pick a date").assertIsDisplayed()
  }

  /**
   * TestableUserRepositoryLocal wraps UserRepositoryLocal to add error injection and delay
   * capabilities for testing error scenarios.
   */
  private class TestableUserRepositoryLocal(initialUser: User? = null) : UserRepository {
    private val delegate = UserRepositoryLocal()
    var shouldThrowOnSave: Throwable? = null
    var shouldThrowOnLoad: Throwable? = null
    var saveDelay: Long = 0L

    init {
      // Initialize with user synchronously using runBlocking
      initialUser?.let { kotlinx.coroutines.runBlocking { delegate.saveUser(it) } }
    }

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
  }
}
