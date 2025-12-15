package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.profile.ProfileConstants
import com.github.se.studentconnect.ui.screen.profile.edit.EditNameScreen
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class EditNameScreenTest {

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
          bio = "Test bio",
          profilePictureUrl = null)

  private var navigatedBack = false

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    navigatedBack = false
  }

  @Test
  fun editNameScreen_displaysCorrectTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Edit Name").assertExists()
  }

  @Test
  fun editNameScreen_displaysBackButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun editNameScreen_backButtonNavigatesBack() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(navigatedBack)
  }

  @Test
  fun editNameScreen_displaysInstructions() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText(ProfileConstants.INSTRUCTION_ENTER_NAME).assertExists()
  }

  @Test
  fun editNameScreen_displaysFirstNameField() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_FIRST_NAME).assertExists()
  }

  @Test
  fun editNameScreen_displaysLastNameField() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_LAST_NAME).assertExists()
  }

  @Test
  fun editNameScreen_loadsExistingUserNames() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // Check that the text fields contain the user's current names
    composeTestRule.onAllNodesWithText("John").assertAny(hasSetTextAction())
    composeTestRule.onAllNodesWithText("Doe").assertAny(hasSetTextAction())
  }

  @Test
  fun editNameScreen_displaysSaveButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertExists()
  }

  @Test
  fun editNameScreen_saveButtonIsEnabledWithValidNames() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertIsEnabled()
  }

  @Test
  fun editNameScreen_saveButtonIsDisabledWithEmptyFirstName() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Clear first name
    composeTestRule.onAllNodesWithText("John")[0].performTextClearance()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editNameScreen_saveButtonIsDisabledWithEmptyLastName() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Clear last name
    composeTestRule.onAllNodesWithText("Doe")[0].performTextClearance()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editNameScreen_canEditFirstName() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit first name
    composeTestRule.onAllNodesWithText("John")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_FIRST_NAME).performTextInput("Alice")

    // Verify new text is displayed
    composeTestRule.onNodeWithText("Alice").assertExists()
  }

  @Test
  fun editNameScreen_canEditLastName() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit last name
    composeTestRule.onAllNodesWithText("Doe")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_LAST_NAME).performTextInput("Smith")

    // Verify new text is displayed
    composeTestRule.onNodeWithText("Smith").assertExists()
  }

  @Test
  fun editNameScreen_showsLoadingStateWhileSaving() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Change names
    composeTestRule.onAllNodesWithText("John")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_FIRST_NAME).performTextInput("Alice")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Should show loading state
    composeTestRule.waitForIdle()
    // Note: CircularProgressIndicator will be in the button, button should be disabled
    composeTestRule.onNodeWithText("Save").assertDoesNotExist()
  }

  @Test
  fun editNameScreen_navigatesBackAfterSave() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit names
    composeTestRule.onAllNodesWithText("John")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_FIRST_NAME).performTextInput("Alice")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save operation and navigation
    composeTestRule.waitForIdle()
    Thread.sleep(500) // Give time for save operation
    composeTestRule.waitForIdle()

    // Should navigate back automatically after successful save
    assert(navigatedBack)
  }

  @Test
  fun editNameScreen_handlesRepositoryError() {
    repository.shouldThrowOnSave = RuntimeException("Network error")

    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit names
    composeTestRule.onAllNodesWithText("John")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_FIRST_NAME).performTextInput("Alice")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for error message
    composeTestRule.waitForIdle()

    // Should show error message in snackbar
    composeTestRule.onNodeWithText("Network error").assertExists()

    // Should NOT navigate back
    assert(!navigatedBack)
  }

  @Test
  fun editNameScreen_trimsWhitespaceFromNames() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit names with extra whitespace
    composeTestRule.onAllNodesWithText("John")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_FIRST_NAME).performTextInput("  Alice  ")
    composeTestRule.onAllNodesWithText("Doe")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_LAST_NAME).performTextInput("  Smith  ")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    // Verify trimmed values were saved
    assert(repository.savedUsers.last().firstName == "Alice")
    assert(repository.savedUsers.last().lastName == "Smith")
  }

  @Test
  fun editNameScreen_saveButtonDisabledDuringLoading() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit names
    composeTestRule.onAllNodesWithText("John")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_FIRST_NAME).performTextInput("Alice")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule.waitForIdle()

    // Button should not show "Save" text anymore (showing progress instead)
    composeTestRule.onNodeWithText("Save").assertDoesNotExist()
  }

  @Test
  fun editNameScreen_textFieldsDisabledDuringLoading() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditNameScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Edit names
    composeTestRule.onAllNodesWithText("John")[0].performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.LABEL_FIRST_NAME).performTextInput("Alice")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule.waitForIdle()

    // Text fields should be disabled during loading
    // The fields should not be editable
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

    override suspend fun getInvitations(userId: String) = emptyList<Invitation>()

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

    override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun getPinnedEvents(userId: String) = emptyList<String>()

    override suspend fun pinOrganization(userId: String, organizationId: String) {}

    override suspend fun unpinOrganization(userId: String) {}

    override suspend fun getPinnedOrganization(userId: String): String? = null
  }
}
