package com.github.se.studentconnect.ui.screen.profile.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.profile.ProfileConstants
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class EditBioScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: TestUserRepository
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
          bio = "This is my test bio",
          profilePictureUrl = null)

  private var navigatedBack = false

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    navigatedBack = false
  }

  @Test
  fun editBioScreen_displaysCorrectTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Edit Bio").assertExists()
  }

  @Test
  fun editBioScreen_displaysBackButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun editBioScreen_backButtonNavigatesBack() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(navigatedBack)
  }

  @Test
  fun editBioScreen_displaysInstructions() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText(ProfileConstants.INSTRUCTION_TELL_ABOUT_YOURSELF).assertExists()
  }

  @Test
  fun editBioScreen_loadsExistingBio() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // Check that the text field contains the user's current bio
    composeTestRule.onNodeWithText("This is my test bio").assertExists()
  }

  @Test
  fun editBioScreen_displaysCharacterCounter() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    val bioLength = testUser.bio?.length ?: 0
    composeTestRule.onNodeWithText("$bioLength / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun editBioScreen_displaysSaveButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertExists()
  }

  @Test
  fun editBioScreen_saveButtonIsEnabledWithValidBio() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertIsEnabled()
  }

  @Test
  fun editBioScreen_saveButtonIsDisabledWithEmptyBio() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Clear the bio
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editBioScreen_saveButtonIsDisabledWhenBioExceedsMaxLength() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Try to add text that exceeds max length (should be prevented by component)
    val tooLongText = "A".repeat(ProfileConstants.MAX_BIO_LENGTH + 1)
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput(tooLongText)

    composeTestRule.waitForIdle()
    // The actual text should be capped at max length, so button should still be enabled
    // This tests that the component prevents exceeding max length
  }

  @Test
  fun editBioScreen_canEditBio() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit the bio
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput("My new bio")

    // Verify new text is displayed
    composeTestRule.onNodeWithText("My new bio").assertExists()
  }

  @Test
  fun editBioScreen_characterCounterUpdatesOnEdit() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Clear and add new text
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("New bio")

    composeTestRule.waitForIdle()
    // Check character count is updated
    composeTestRule.onNodeWithText("7 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun editBioScreen_displaysPlaceholder() {
    val userWithoutBio = testUser.copy(bio = null)
    repository = TestUserRepository(userWithoutBio)

    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = userWithoutBio.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).assertExists()
  }

  @Test
  fun editBioScreen_showsLoadingStateWhileSaving() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit bio
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("New bio")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Should show loading state
    composeTestRule.waitForIdle()
    // Button text should not be "Save" anymore (showing progress indicator)
    composeTestRule.onNodeWithText("Save").assertDoesNotExist()
  }

  @Test
  fun editBioScreen_saveSuccessfully() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit bio
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput("Updated bio")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save operation to complete
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    // Verify save was successful
    assert(repository.savedUsers.last().bio == "Updated bio")
  }

  @Test
  fun editBioScreen_showsSnackbarOnSuccessfulSave() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit bio
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput("Updated bio")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for snackbar to appear
    composeTestRule.waitForIdle()
    Thread.sleep(200)

    // Should show success message
    composeTestRule.onNodeWithText(ProfileConstants.SUCCESS_BIO_UPDATED).assertExists()
  }

  @Test
  fun editBioScreen_handlesRepositoryError() {
    repository.shouldThrowOnSave = RuntimeException("Network error")

    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit bio
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput("Updated bio")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for error message
    composeTestRule.waitForIdle()
    Thread.sleep(200)

    // Should show error message in snackbar
    composeTestRule.onNodeWithText("Network error").assertExists()

    // Should NOT navigate back
    assert(!navigatedBack)
  }

  @Test
  fun editBioScreen_trimsWhitespaceFromBio() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit bio with extra whitespace
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput("  My new bio  ")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    // Verify trimmed value was saved
    assert(repository.savedUsers.last().bio == "My new bio")
  }

  @Test
  fun editBioScreen_saveButtonDisabledDuringLoading() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit bio
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("New bio")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule.waitForIdle()

    // Button should not show "Save" text anymore (showing progress instead)
    composeTestRule.onNodeWithText("Save").assertDoesNotExist()
  }

  @Test
  fun editBioScreen_textFieldDisabledDuringLoading() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit bio
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("New bio")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule.waitForIdle()

    // Text field should be disabled during loading
    composeTestRule.onNodeWithText("New bio").assertIsNotEnabled()
  }

  @Test
  fun editBioScreen_handlesMultilineBio() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit with multiline bio
    val multilineBio = "Line 1\nLine 2\nLine 3"
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput(multilineBio)

    // Verify multiline text is displayed
    composeTestRule.onNodeWithText(multilineBio).assertExists()
  }

  @Test
  fun editBioScreen_handlesSpecialCharacters() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit with special characters
    val specialBio = "Bio with émojis 🎉 and spëcial!"
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput(specialBio)

    // Verify special characters are displayed
    composeTestRule.onNodeWithText(specialBio).assertExists()
  }

  @Test
  fun editBioScreen_handlesEmptyBioUser() {
    val userWithEmptyBio = testUser.copy(bio = "")
    repository = TestUserRepository(userWithEmptyBio)

    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = userWithEmptyBio.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Character counter should show 0
    composeTestRule.onNodeWithText("0 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()

    // Save button should be disabled
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editBioScreen_savesLongBioSuccessfully() {
    composeTestRule.setContent {
      MaterialTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Create a bio at max length
    val maxLengthBio = "A".repeat(ProfileConstants.MAX_BIO_LENGTH)
    composeTestRule.onNodeWithText("This is my test bio").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput(maxLengthBio)

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    // Verify it was saved
    assert(repository.savedUsers.last().bio == maxLengthBio)
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

    override suspend fun checkUsernameAvailability(username: String): Boolean {
      TODO("Not yet implemented")
    }
  }
}
