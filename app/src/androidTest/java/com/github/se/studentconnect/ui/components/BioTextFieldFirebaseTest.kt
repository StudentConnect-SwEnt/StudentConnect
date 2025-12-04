package com.github.se.studentconnect.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.ui.profile.ProfileConstants
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.StudentConnectTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Android instrumented test for [BioTextField] component.
 *
 * Tests the reusable bio text field component in a real Android environment to ensure proper UI
 * rendering and interaction.
 */
class BioTextFieldFirebaseTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun bioTextField_displaysValue() {
    composeTestRule.setContent {
      AppTheme { BioTextField(value = "Test bio text", onValueChange = {}) }
    }

    composeTestRule.onNodeWithText("Test bio text").assertExists()
  }

  @Test
  fun bioTextField_displaysPlaceholder() {
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = "",
            onValueChange = {},
            config = BioTextFieldConfig(placeholder = "Enter your bio"))
      }
    }

    composeTestRule.onNodeWithText("Enter your bio").assertExists()
  }

  @Test
  fun bioTextField_showsCharacterCount() {
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = "Hello World",
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    composeTestRule.onNodeWithText("11 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun bioTextField_hidesCharacterCountWhenDisabled() {
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = "Hello World",
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = false))
      }
    }

    composeTestRule.onNodeWithText("11 / ${ProfileConstants.MAX_BIO_LENGTH}").assertDoesNotExist()
  }

  @Test
  fun bioTextField_showsErrorMessage() {
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = "", onValueChange = {}, isError = true, errorMessage = "Bio cannot be empty")
      }
    }

    composeTestRule.onNodeWithText("Bio cannot be empty").assertExists()
  }

  @Test
  fun bioTextField_handlesTextInput() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("") }
      AppTheme { BioTextField(value = value, onValueChange = { value = it }) }
    }

    // Input text
    composeTestRule
        .onNodeWithText(ProfileConstants.PLACEHOLDER_BIO)
        .performTextInput("New bio text")

    // Verify text is displayed
    composeTestRule.onNodeWithText("New bio text").assertExists()
  }

  @Test
  fun bioTextField_updatesCharacterCount() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("") }
      AppTheme {
        BioTextField(
            value = value,
            onValueChange = { value = it },
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    // Initial count
    composeTestRule.onNodeWithText("0 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()

    // Add text
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("Testing")

    // Updated count
    composeTestRule.onNodeWithText("7 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun bioTextField_outlinedStyleWorks() {
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = "Outlined style test",
            onValueChange = {},
            config = BioTextFieldConfig(style = BioTextFieldStyle.Outlined))
      }
    }

    composeTestRule.onNodeWithText("Outlined style test").assertExists()
  }

  @Test
  fun bioTextField_borderedStyleWorks() {
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = "Bordered style test",
            onValueChange = {},
            config = BioTextFieldConfig(style = BioTextFieldStyle.Bordered))
      }
    }

    composeTestRule.onNodeWithText("Bordered style test").assertExists()
  }

  @Test
  fun bioTextField_respectsEnabledState() {
    composeTestRule.setContent {
      AppTheme { BioTextField(value = "Disabled test", onValueChange = {}, enabled = false) }
    }

    // Verify text field is not enabled
    composeTestRule.onNodeWithText("Disabled test").assertIsNotEnabled()
  }

  @Test
  fun bioTextField_isEnabledByDefault() {
    composeTestRule.setContent {
      AppTheme { BioTextField(value = "Enabled test", onValueChange = {}) }
    }

    composeTestRule.onNodeWithText("Enabled test").assertIsEnabled()
  }

  @Test
  fun bioTextField_handlesMultilineText() {
    val multilineText = "Line 1\nLine 2\nLine 3"
    composeTestRule.setContent {
      AppTheme { BioTextField(value = multilineText, onValueChange = {}) }
    }

    composeTestRule.onNodeWithText(multilineText).assertExists()
  }

  @Test
  fun bioTextField_handlesLongText() {
    val longText = "A".repeat(400)
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = longText,
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    composeTestRule.onNodeWithText("400 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun bioTextField_handlesSpecialCharacters() {
    val specialText = "Bio with émojis 🎉 and spëcial!"
    composeTestRule.setContent {
      AppTheme { BioTextField(value = specialText, onValueChange = {}) }
    }

    composeTestRule.onNodeWithText(specialText).assertExists()
  }

  @Test
  fun bioTextField_canClearText() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("Initial text") }
      AppTheme { BioTextField(value = value, onValueChange = { value = it }) }
    }

    // Clear the text
    composeTestRule.onNodeWithText("Initial text").performTextClearance()

    // Verify placeholder is shown
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).assertExists()
  }

  @Test
  fun bioTextField_displaysCustomMaxCharacters() {
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = "Test",
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true, maxCharacters = 100))
      }
    }

    composeTestRule.onNodeWithText("4 / 100").assertExists()
  }

  @Test
  fun bioTextField_errorStylingWithErrorMessage() {
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = "",
            onValueChange = {},
            isError = true,
            errorMessage = ProfileConstants.ERROR_BIO_EMPTY)
      }
    }

    composeTestRule.onNodeWithText(ProfileConstants.ERROR_BIO_EMPTY).assertExists()
  }

  @Test
  fun bioTextField_handlesTextReplacement() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("Original") }
      AppTheme { BioTextField(value = value, onValueChange = { value = it }) }
    }

    // Replace text
    composeTestRule.onNodeWithText("Original").performTextReplacement("Replaced")

    // Verify new text
    composeTestRule.onNodeWithText("Replaced").assertExists()
  }

  @Test
  fun bioTextField_supportsTextSelection() {
    composeTestRule.setContent {
      AppTheme { BioTextField(value = "Selectable text", onValueChange = {}) }
    }

    // Text should be present and support text actions
    composeTestRule.onNodeWithText("Selectable text").assertExists()
  }

  @Test
  fun bioTextField_handlesEmptyValue() {
    composeTestRule.setContent { AppTheme { BioTextField(value = "", onValueChange = {}) } }

    // Should display placeholder
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).assertExists()
  }

  @Test
  fun bioTextField_characterCountWithUnicode() {
    val unicodeText = "Hello 👋 World 🌍"
    composeTestRule.setContent {
      AppTheme {
        BioTextField(
            value = unicodeText,
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    val expectedLength = unicodeText.length
    composeTestRule
        .onNodeWithText("$expectedLength / ${ProfileConstants.MAX_BIO_LENGTH}")
        .assertExists()
  }
}
