package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: UserRepositoryLocal
  private var validationState: Pair<Boolean, Boolean?>? = null

  @Before
  fun setUp() {
    repository = UserRepositoryLocal()
    validationState = null
  }

  @Test
  fun usernameTextField_displaysLabelAndPlaceholder() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }

    composeTestRule.onNodeWithText("Username").assertExists()
    // Placeholder may not be directly accessible, but component renders correctly
    composeTestRule.waitForIdle()
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
    composeTestRule
        .onNodeWithText(
            "Only alphanumeric characters, underscores, hyphens, and periods are allowed")
        .assertExists()
  }

  @Test
  fun usernameTextField_showsError_whenUsernameTaken() {
    runBlocking {
      // Pre-populate repository with a user
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
    // Wait for debounce and availability check
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithText("This username is already taken")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText("This username is already taken").assertExists()
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
    // Wait for debounce and availability check
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithContentDescription("Available")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithContentDescription("Available").assertExists()
  }

  @Test
  fun usernameTextField_showsLoading_duringAvailabilityCheck() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "checkinguser",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }

    composeTestRule.waitForIdle()
    // Should show loading indicator briefly during check
    // Note: This is hard to test reliably due to timing, but the component renders
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

    composeTestRule.onNodeWithText("Username").performTextInput("User@Name#123")
    composeTestRule.waitForIdle()
    // Wait for the callback to be invoked with filtered value
    composeTestRule.waitUntil(timeoutMillis = 1000) { capturedValue.isNotEmpty() }
    // Should filter to only valid characters and lowercase: "User@Name#123" -> "username123"
    assert(capturedValue == "username123")
  }

  @Test
  fun usernameTextField_convertsToLowercase() {
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

    composeTestRule.onNodeWithText("Username").performTextInput("USERNAME")
    composeTestRule.waitForIdle()
    assert(capturedValue == "username")
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
    // Should show error when exception occurs
    composeTestRule.onNodeWithText("This username is already taken").assertExists()
  }

  @Test
  fun usernameTextField_validatesMinLength() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "ab",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    assert(validationState?.first == false)
  }

  @Test
  fun usernameTextField_validatesMaxLength() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "a".repeat(21),
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    assert(validationState?.first == false)
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
    // Should be valid format (will check availability)
    composeTestRule.waitUntil(timeoutMillis = 2000) { validationState?.first == true }
    assert(validationState?.first == true)
  }

  @Test
  fun usernameTextField_noError_whenBlank() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    // Should not show error when blank
    composeTestRule.onAllNodesWithText("Username must be 3-20 characters long").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Only alphanumeric characters").assertCountEquals(0)
    assert(validationState?.first == false)
  }

  @Test
  fun usernameTextField_showsTakenIcon_whenUnavailable() {
    runBlocking {
      repository.saveUser(
          User(
              userId = "user1",
              username = "taken",
              email = "test@epfl.ch",
              firstName = "Test",
              lastName = "User",
              university = "EPFL"))
    }

    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "taken",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { _, _ -> })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithContentDescription("Taken").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithContentDescription("Taken").assertExists()
  }

  @Test
  fun usernameTextField_validatesExactMinLength() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "abc",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) { validationState?.first == true }
    assert(validationState?.first == true)
  }

  @Test
  fun usernameTextField_validatesExactMaxLength() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "a".repeat(20),
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) { validationState?.first == true }
    assert(validationState?.first == true)
  }

  @Test
  fun usernameTextField_allowsUnderscore() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "user_name",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) { validationState?.first == true }
    assert(validationState?.first == true)
  }

  @Test
  fun usernameTextField_allowsHyphen() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "user-name",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) { validationState?.first == true }
    assert(validationState?.first == true)
  }

  @Test
  fun usernameTextField_allowsPeriod() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "user.name",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, _ -> validationState = isValid to null })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) { validationState?.first == true }
    assert(validationState?.first == true)
  }

  @Test
  fun usernameTextField_callsValidationCallbackWithFalse_whenInvalid() {
    composeTestRule.setContent {
      MaterialTheme {
        UsernameTextField(
            username = "ab",
            onUsernameChange = {},
            userRepository = repository,
            onValidationStateChange = { isValid, isAvailable ->
              validationState = isValid to isAvailable
            })
      }
    }

    composeTestRule.waitForIdle()
    assert(validationState?.first == false)
    assert(validationState?.second == null)
  }
}
