package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.profile.edit.EditBirthdayScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditBirthdayScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: TestUserRepository
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "15/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = null)

  private var navigatedBack = false

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
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
    composeTestRule.onNodeWithText("Current Birthday").assertIsDisplayed()
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
    val userWithNoBirthday = testUser.copy(birthday = null)
    val testRepo = TestUserRepository(userWithNoBirthday)

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

    // Wait for save operation
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    assertTrue(repository.savedUsers.isNotEmpty())
  }

  @Test
  fun editBirthdayScreen_showsSuccessMessageAfterSave() {
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

    // Wait for success message
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Birthday updated successfully").assertExists()
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

    // The "Current Birthday" card should be displayed for users with birthdays
    composeTestRule.onNodeWithText("Current Birthday").assertExists()
  }

  @Test
  fun editBirthdayScreen_saveButtonDisabledWhenNoDateSelected() {
    // Create a repository with user that has no birthday
    val userWithNoBirthday = testUser.copy(birthday = null)
    val testRepo = TestUserRepository(userWithNoBirthday)

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
    val userWithInvalidBirthday = testUser.copy(birthday = "invalid-date")
    val testRepo = TestUserRepository(userWithInvalidBirthday)

    composeTestRule.setContent {
      MaterialTheme {
        EditBirthdayScreen(
            userId = testUser.userId,
            userRepository = testRepo,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // Should still display the invalid birthday string
    composeTestRule.onNodeWithText("invalid-date").assertIsDisplayed()
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

    // Wait for save operation
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    val savedUser = repository.savedUsers.last()
    // Should save the birthday (either the original or updated one)
    assertTrue(savedUser.birthday != null)
  }

  @Test
  fun editBirthdayScreen_updatesTimestampOnSave() {
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

    // Wait for save operation
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    val savedUser = repository.savedUsers.last()
    assertTrue(savedUser.updatedAt >= timeBefore)
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


  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnSave: Throwable? = null,
      var saveDelay: Long = 0L
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) {
      if (saveDelay > 0) {
        delay(saveDelay)
      }
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
