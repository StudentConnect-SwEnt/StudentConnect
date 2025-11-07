package com.github.se.studentconnect.ui.screen.signup

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryLocal
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsernameTextFieldTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var repository: UserRepositoryLocal
  private var validationState: Pair<Boolean, Boolean?>? = null

  @Before
  fun setUp() {
    repository = UserRepositoryLocal()
    validationState = null
  }

  @Test
  fun usernameTextField_displaysLabel() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithText("Username").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("Username").assertExists()
  }

  @Test
  fun usernameTextField_showsError_whenTooShort() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "ab",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithText("Username must be 3-20 characters long")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText("Username must be 3-20 characters long").assertExists()
  }

  @Test
  fun usernameTextField_showsError_whenTooLong() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "a".repeat(21),
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithText("Username must be 3-20 characters long")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText("Username must be 3-20 characters long").assertExists()
  }

  @Test
  fun usernameTextField_showsError_whenInvalidCharacters() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "user@name",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithText(
              "Only alphanumeric characters, underscores, hyphens, and periods are allowed")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule
        .onNodeWithText(
            "Only alphanumeric characters, underscores, hyphens, and periods are allowed")
        .assertExists()
  }

  @Test
  fun usernameTextField_showsError_whenUsernameTaken() {
    runBlocking {
      repository.saveUser(
          User(
              userId = "user1",
              username = "takenuser",
              email = "test@epfl.ch",
              firstName = "Test",
              lastName = "User",
              university = "EPFL"))
    }

    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "takenuser",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithText("This username is already taken")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText("This username is already taken").assertExists()
    composeTestRule.onNodeWithContentDescription("Taken").assertExists()
  }

  @Test
  fun usernameTextField_showsCheckmark_whenUsernameAvailable() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "availableuser",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithContentDescription("Available")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithContentDescription("Available").assertExists()
  }

  @Test
  fun usernameTextField_filtersInvalidCharacters() {
    var capturedValue = ""
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "",
            onUsernameChange = { capturedValue = it },
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("Username").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)
          .fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)
        .onFirst()
        .performTextReplacement("User@Name#123")
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      capturedValue.isNotEmpty()
    }
    composeTestRule.runOnIdle {
      assert(capturedValue == "username123") {
        "Expected 'username123' but got '$capturedValue'"
      }
    }
  }

  @Test
  fun usernameTextField_callsValidationCallback() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "validuser",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, isAvailable ->
              validationState = isValid to isAvailable
            })
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 2000) {
      validationState != null && validationState!!.first == true && validationState!!.second == true
    }
    assert(validationState?.first == true)
    assert(validationState?.second == true)
  }

  @Test
  fun usernameTextField_handlesExceptionDuringAvailabilityCheck() {
    val failingRepository =
        object : UserRepository by repository {
          override suspend fun checkUsernameAvailability(username: String): Boolean {
            throw Exception("Network error")
          }
        }

    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "testuser",
            onUsernameChange = {},
            userRepository = failingRepository,
            onValidationStateChange = { _, _ -> })
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithText("This username is already taken")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText("This username is already taken").assertExists()
  }

  @Test
  fun usernameTextField_allowsValidCharacters() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "user_name-123.test",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) { validationState?.first == true }
    assert(validationState?.first == true)
  }
}
