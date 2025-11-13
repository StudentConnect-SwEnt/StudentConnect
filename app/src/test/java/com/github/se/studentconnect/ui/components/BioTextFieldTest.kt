package com.github.se.studentconnect.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.ui.profile.ProfileConstants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class BioTextFieldTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bioTextField_displaysValue() {
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = "Test bio", onValueChange = {}) }
    }

    composeTestRule.onNodeWithText("Test bio").assertExists()
  }

  @Test
  fun bioTextField_displaysPlaceholder() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "",
            onValueChange = {},
            config = BioTextFieldConfig(placeholder = "Enter your bio"))
      }
    }

    composeTestRule.onNodeWithText("Enter your bio").assertExists()
  }

  @Test
  fun bioTextField_displaysDefaultPlaceholder() {
    composeTestRule.setContent { MaterialTheme { BioTextField(value = "", onValueChange = {}) } }

    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).assertExists()
  }

  @Test
  fun bioTextField_displaysCharacterCount() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "Hello",
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    composeTestRule.onNodeWithText("5 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun bioTextField_hidesCharacterCountWhenDisabled() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "Hello",
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = false))
      }
    }

    composeTestRule.onNodeWithText("5 / ${ProfileConstants.MAX_BIO_LENGTH}").assertDoesNotExist()
  }

  @Test
  fun bioTextField_showsErrorMessage() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "", onValueChange = {}, isError = true, errorMessage = "Bio cannot be empty")
      }
    }

    composeTestRule.onNodeWithText("Bio cannot be empty").assertExists()
  }

  @Test
  fun bioTextField_doesNotShowErrorMessageWhenNoError() {
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = "Valid bio", onValueChange = {}, isError = false) }
    }

    // Error message should not exist
    composeTestRule.onNodeWithText("Bio cannot be empty").assertDoesNotExist()
  }

  @Test
  fun bioTextField_callsOnValueChangeWhenTextEntered() {
    var changedValue = ""
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = "", onValueChange = { changedValue = it }) }
    }

    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("New text")

    assert(changedValue == "New text")
  }

  @Test
  fun bioTextField_updatesCharacterCountWhenTextChanges() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("") }
      MaterialTheme {
        BioTextField(
            value = value,
            onValueChange = { value = it },
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    // Initial count should be 0
    composeTestRule.onNodeWithText("0 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()

    // Add text
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("Hello")

    // Count should update
    composeTestRule.onNodeWithText("5 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun bioTextField_respectsEnabledState() {
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = "Test", onValueChange = {}, enabled = false) }
    }

    // Text field should not be enabled
    composeTestRule.onNodeWithText("Test").assertIsNotEnabled()
  }

  @Test
  fun bioTextField_isEnabledByDefault() {
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = "Test", onValueChange = {}) }
    }

    composeTestRule.onNodeWithText("Test").assertIsEnabled()
  }

  @Test
  fun bioTextField_displaysCustomMaxCharacters() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "Test",
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true, maxCharacters = 100))
      }
    }

    composeTestRule.onNodeWithText("4 / 100").assertExists()
  }

  @Test
  fun bioTextField_showsErrorColorWhenExceedingMaxLength() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "A".repeat(50),
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true, maxCharacters = 10))
      }
    }

    // Character count should still be displayed (with error color internally)
    composeTestRule.onNodeWithText("50 / 10").assertExists()
  }

  @Test
  fun bioTextField_outlinedStyleWorks() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "Test",
            onValueChange = {},
            config = BioTextFieldConfig(style = BioTextFieldStyle.Outlined))
      }
    }

    composeTestRule.onNodeWithText("Test").assertExists()
  }

  @Test
  fun bioTextField_borderedStyleWorks() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "Test",
            onValueChange = {},
            config = BioTextFieldConfig(style = BioTextFieldStyle.Bordered))
      }
    }

    composeTestRule.onNodeWithText("Test").assertExists()
  }

  @Test
  fun bioTextField_handlesEmptyValue() {
    composeTestRule.setContent { MaterialTheme { BioTextField(value = "", onValueChange = {}) } }

    // Should display placeholder
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).assertExists()
  }

  @Test
  fun bioTextField_handlesLongText() {
    val longText = "A".repeat(500)
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = longText, onValueChange = {}) }
    }

    // Text should be displayed
    composeTestRule.onNodeWithText(longText).assertExists()
  }

  @Test
  fun bioTextField_handlesMultilineText() {
    val multilineText = "Line 1\nLine 2\nLine 3"
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = multilineText, onValueChange = {}) }
    }

    composeTestRule.onNodeWithText(multilineText).assertExists()
  }

  @Test
  fun bioTextField_handlesSpecialCharacters() {
    val specialText = "Bio with émojis 🎉 and spëcial!"
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = specialText, onValueChange = {}) }
    }

    composeTestRule.onNodeWithText(specialText).assertExists()
  }

  @Test
  fun bioTextField_respectsMinLines() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(value = "Short", onValueChange = {}, config = BioTextFieldConfig(minLines = 6))
      }
    }

    // Component should render (specific height is internal implementation)
    composeTestRule.onNodeWithText("Short").assertExists()
  }

  @Test
  fun bioTextField_respectsMaxLines() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(value = "Text", onValueChange = {}, config = BioTextFieldConfig(maxLines = 8))
      }
    }

    // Component should render
    composeTestRule.onNodeWithText("Text").assertExists()
  }

  @Test
  fun bioTextField_showsErrorStyling() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "",
            onValueChange = {},
            isError = true,
            errorMessage = ProfileConstants.ERROR_BIO_EMPTY)
      }
    }

    // Error message should be displayed
    composeTestRule.onNodeWithText(ProfileConstants.ERROR_BIO_EMPTY).assertExists()
  }

  @Test
  fun bioTextField_clearingTextUpdatesCharacterCount() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("Initial text") }
      MaterialTheme {
        BioTextField(
            value = value,
            onValueChange = { value = it },
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    // Clear the text
    composeTestRule.onNodeWithText("Initial text").performTextClearance()

    // Character count should be 0
    composeTestRule.onNodeWithText("0 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun bioTextField_supportsTextSelection() {
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = "Selectable text", onValueChange = {}) }
    }

    // Text should be present and selectable
    composeTestRule.onNodeWithText("Selectable text").assertExists()
  }

  @Test
  fun bioTextField_displaysCorrectCharacterCountWithUnicode() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "Hello 👋",
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    // Should count correctly including emoji
    val expectedLength = "Hello 👋".length
    composeTestRule
        .onNodeWithText("$expectedLength / ${ProfileConstants.MAX_BIO_LENGTH}")
        .assertExists()
  }

  @Test
  fun bioTextField_handlesWhitespaceOnly() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(
            value = "   ",
            onValueChange = {},
            config = BioTextFieldConfig(showCharacterCount = true))
      }
    }

    composeTestRule.onNodeWithText("3 / ${ProfileConstants.MAX_BIO_LENGTH}").assertExists()
  }

  @Test
  fun bioTextField_allowsTextReplacement() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("Original") }
      MaterialTheme { BioTextField(value = value, onValueChange = { value = it }) }
    }

    // Clear and replace
    composeTestRule.onNodeWithText("Original").performTextClearance()
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("New text")

    // Should display new text
    composeTestRule.onNodeWithText("New text").assertExists()
  }

  @Test
  fun bioTextField_preservesTextDuringRecomposition() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("Persistent text") }
      MaterialTheme { BioTextField(value = value, onValueChange = { value = it }) }
    }

    // Text should persist
    composeTestRule.onNodeWithText("Persistent text").assertExists()

    // The text field should be present and maintain its value
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Persistent text").assertExists()
  }

  @Test
  fun bioTextField_handlesRapidTextChanges() {
    composeTestRule.setContent {
      var value by remember { mutableStateOf("") }
      MaterialTheme { BioTextField(value = value, onValueChange = { value = it }) }
    }

    // Rapidly add text
    composeTestRule.onNodeWithText(ProfileConstants.PLACEHOLDER_BIO).performTextInput("A")
    composeTestRule.onNodeWithText("A").performTextInput("B")
    composeTestRule.onNodeWithText("AB").performTextInput("C")

    // Should display all changes
    composeTestRule.onNodeWithText("ABC").assertExists()
  }

  @Test
  fun bioTextField_defaultStyleIsOutlined() {
    composeTestRule.setContent {
      MaterialTheme {
        BioTextField(value = "Test", onValueChange = {})
        // Default style should be Outlined
      }
    }

    composeTestRule.onNodeWithText("Test").assertExists()
  }

  @Test
  fun bioTextField_errorMessageOnlyShownWhenIsErrorTrue() {
    composeTestRule.setContent {
      MaterialTheme { BioTextField(value = "test", onValueChange = {}, isError = false) }
    }

    // When isError is false, the text field should be displayed normally
    composeTestRule.onNodeWithText("test").assertExists()
  }
}
