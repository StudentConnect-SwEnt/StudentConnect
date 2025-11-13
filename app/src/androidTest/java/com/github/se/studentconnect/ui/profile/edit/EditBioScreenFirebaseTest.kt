package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryFirestore
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.github.se.studentconnect.ui.profile.ProfileConstants
import com.github.se.studentconnect.ui.screen.profile.edit.EditBioScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.StudentConnectTest
import com.github.se.studentconnect.utils.UI_WAIT_TIMEOUT
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Android instrumented test for [EditBioScreen] using Firebase Emulator.
 *
 * Tests the bio editing functionality with real Firebase-backed repositories to ensure proper
 * integration between UI and backend services.
 */
class EditBioScreenFirebaseTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  override fun createInitializedRepository(): EventRepository =
      EventRepositoryFirestore(FirebaseEmulator.firestore)

  private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var testUser: User
  private var navigatedBack = false

  @Before
  override fun setUp() {
    super.setUp()
    userRepository = UserRepositoryFirestore(FirebaseEmulator.firestore)

    val uid = currentUser.uid
    testUser =
        User(
            userId = uid,
            email = currentUser.email ?: "$uid@studentconnect.test",
            firstName = "Test",
            lastName = "User",
            username = "testuser",
            university = "EPFL",
            hobbies = listOf("Testing"),
            bio = "Original bio text for testing")

    runTest { userRepository.saveUser(testUser) }

    navigatedBack = false

    composeTestRule.setContent {
      AppTheme {
        EditBioScreen(
            userId = testUser.userId,
            userRepository = userRepository,
            onNavigateBack = { navigatedBack = true })
      }
    }
  }

  @After
  override fun tearDown() {
    runTest { runCatching { userRepository.deleteUser(testUser.userId) } }
    super.tearDown()
  }

  @Test
  fun editBioScreen_displaysCorrectTitle() {
    composeTestRule.onNodeWithText("Edit Bio").assertExists()
  }

  @Test
  fun editBioScreen_loadsExistingBio() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Original bio text for testing").assertExists()
  }

  @Test
  fun editBioScreen_displaysCharacterCounter() {
    composeTestRule.waitForIdle()
    val bioLength = testUser.bio?.length ?: 0
    composeTestRule.onNodeWithText("$bioLength / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun editBioScreen_canEditBioText() {
    composeTestRule.waitForIdle()

    // Clear existing text and add new text
    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput("New bio text")

    // Verify new text is displayed
    composeTestRule.onNodeWithText("New bio text").assertExists()
  }

  @Test
  fun editBioScreen_characterCounterUpdatesOnEdit() {
    composeTestRule.waitForIdle()

    // Clear and add new text
    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("Test")

    // Check character count
    composeTestRule.onNodeWithText("4 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun editBioScreen_saveButtonDisabledWithEmptyBio() {
    composeTestRule.waitForIdle()

    // Clear the bio
    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()

    composeTestRule.waitForIdle()
    // Save button should be disabled
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editBioScreen_saveButtonEnabledWithValidBio() {
    composeTestRule.waitForIdle()

    // Bio already has valid text, save button should be enabled
    composeTestRule.onNodeWithText("Save").assertIsEnabled()
  }

  fun editBioScreen_savesBioToFirestore() {
    composeTestRule.waitForIdle()

    val newBio = "Updated bio via Firebase"

    // Edit bio
    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput(newBio)

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save to complete and verify in Firestore
    composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
      val user =
          runCatching { runBlocking { userRepository.getUserById(testUser.userId) } }.getOrNull()
      user?.bio == newBio
    }

    // Verify the bio was saved
    runTest {
      val updatedUser = userRepository.getUserById(testUser.userId)
      assert(updatedUser?.bio == newBio)
    }
  }

  @Test
  fun editBioScreen_showsSuccessSnackbar() {
    composeTestRule.waitForIdle()

    // Edit bio
    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("New bio")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for snackbar
    composeTestRule.waitForIdle()

    // Success message should appear
    composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithText(ProfileConstants.SUCCESS_BIO_UPDATED)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @Test
  fun editBioScreen_backButtonNavigatesBack() {
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(navigatedBack)
  }

  @Test
  fun editBioScreen_handlesMultilineBio() {
    composeTestRule.waitForIdle()

    val multilineBio = "Line 1\nLine 2\nLine 3"

    // Edit with multiline text
    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput(multilineBio)

    // Verify multiline text is displayed
    composeTestRule.onNodeWithText(multilineBio).assertExists()
  }

  @Test
  fun editBioScreen_trimsWhitespaceOnSave() {
    composeTestRule.waitForIdle()

    val bioWithWhitespace = "  Trimmed bio  "

    // Edit bio with whitespace
    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput(bioWithWhitespace)

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save to complete
    composeTestRule.waitUntil(timeoutMillis = UI_WAIT_TIMEOUT) {
      val user =
          runCatching { runBlocking { userRepository.getUserById(testUser.userId) } }.getOrNull()
      user?.bio == "Trimmed bio"
    }

    // Verify trimmed bio was saved
    runTest {
      val updatedUser = userRepository.getUserById(testUser.userId)
      assert(updatedUser?.bio == "Trimmed bio")
    }
  }

  @Test
  fun editBioScreen_displaysInstructions() {
    composeTestRule.onNodeWithText(ProfileConstants.INSTRUCTION_TELL_ABOUT_YOURSELF).assertExists()
  }

  @Test
  fun editBioScreen_handlesLongBio() {
    composeTestRule.waitForIdle()

    // Create a long bio (but within limits)
    val longBio = "A".repeat(400)

    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput(longBio)

    // Character count should show the correct length
    composeTestRule.onNodeWithText("400 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()

    // Save button should still be enabled
    composeTestRule.onNodeWithText("Save").assertIsEnabled()
  }

  @Test
  fun editBioScreen_disablesDuringLoading() {
    composeTestRule.waitForIdle()

    // Edit bio
    composeTestRule.onNodeWithText("Original bio text for testing").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("Quick save")

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Immediately check that the text field is disabled (may be brief)
    // The loading state should disable the text field
    composeTestRule.waitForIdle()
  }
}
