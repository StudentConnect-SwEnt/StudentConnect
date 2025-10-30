package com.github.se.studentconnect.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.ui.screen.profile.EditableProfileField
import com.github.se.studentconnect.ui.screen.profile.EditableProfileFieldMultiline
import com.github.se.studentconnect.ui.screen.profile.EditableProfileFieldNumeric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditableProfileFieldTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun editableProfileField_allowsInlineEditingAndSave() {
    var savedValue: String? = null
    composeTestRule.setContent {
      var isEditing by remember { mutableStateOf(false) }
      var value by remember { mutableStateOf("Switzerland") }

      EditableProfileField(
          label = "Country",
          value = value,
          isEditing = isEditing,
          isLoading = false,
          errorMessage = null,
          onEditClick = { isEditing = true },
          onSave = {
            savedValue = it
            value = it
            isEditing = false
          },
          onCancel = { isEditing = false })
    }

    composeTestRule.onNodeWithText("Country").assertIsDisplayed()
    composeTestRule.onNodeWithText("Switzerland").assertIsDisplayed()

    composeTestRule.onNodeWithContentDescription("Edit Country").performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextClearance()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("France")
    composeTestRule.onNodeWithContentDescription("Save").performClick()

    assertEquals("France", savedValue)
    composeTestRule.onNodeWithText("France").assertIsDisplayed()
  }

  @Test
  fun editableProfileField_cancelRestoresOriginalValue() {
    var cancelled = false
    var savedValue: String? = null
    composeTestRule.setContent {
      var isEditing by remember { mutableStateOf(false) }
      var value by remember { mutableStateOf("Italy") }

      EditableProfileField(
          label = "Country",
          value = value,
          isEditing = isEditing,
          isLoading = false,
          errorMessage = null,
          onEditClick = { isEditing = true },
          onSave = {
            savedValue = it
            value = it
            isEditing = false
          },
          onCancel = {
            cancelled = true
            isEditing = false
          })
    }

    composeTestRule.onNodeWithContentDescription("Edit Country").performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextClearance()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Spain")
    composeTestRule.onNodeWithContentDescription("Cancel").performClick()

    assertTrue(cancelled)
    assertEquals(null, savedValue)
    composeTestRule.onNodeWithText("Italy").assertIsDisplayed()
  }

  @Test
  fun editableProfileField_displaysPlaceholderWhenValueMissing() {
    composeTestRule.setContent {
      EditableProfileField(
          label = "University",
          value = "",
          isEditing = false,
          isLoading = false,
          errorMessage = null,
          onEditClick = {},
          onSave = {},
          onCancel = {},
          isEditable = false)
    }

    composeTestRule.onNodeWithText("Not specified").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Edit University").assertDoesNotExist()
  }

  @Test
  fun editableProfileField_showsErrorMessageWhileEditing() {
    composeTestRule.setContent {
      EditableProfileField(
          label = "Country",
          value = "France",
          isEditing = true,
          isLoading = false,
          errorMessage = "Invalid country",
          onEditClick = {},
          onSave = {},
          onCancel = {})
    }

    composeTestRule.onNode(hasSetTextAction()).assertIsDisplayed()
    composeTestRule.onNodeWithText("Invalid country").assertIsDisplayed()
  }

  @Test
  fun editableProfileField_showsLoadingIndicatorWhenLoading() {
    composeTestRule.setContent {
      EditableProfileField(
          label = "Bio",
          value = "Loading...",
          isEditing = false,
          isLoading = true,
          errorMessage = null,
          onEditClick = {},
          onSave = {},
          onCancel = {})
    }

    // Loading indicator should be visible, edit button should not
    composeTestRule.onNodeWithContentDescription("Edit Bio").assertDoesNotExist()
  }

  @Test
  fun editableProfileField_multilineVariant() {
    var savedValue: String? = null
    composeTestRule.setContent {
      var isEditing by remember { mutableStateOf(false) }
      var value by remember { mutableStateOf("Short bio") }

      EditableProfileFieldMultiline(
          label = "Bio",
          value = value,
          isEditing = isEditing,
          isLoading = false,
          errorMessage = null,
          onEditClick = { isEditing = true },
          onSave = {
            savedValue = it
            value = it
            isEditing = false
          },
          onCancel = { isEditing = false })
    }

    composeTestRule.onNodeWithText("Short bio").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Edit Bio").performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextClearance()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Updated bio with multiple lines")
    composeTestRule.onNodeWithContentDescription("Save").performClick()

    assertEquals("Updated bio with multiple lines", savedValue)
  }

  @Test
  fun editableProfileField_numericVariant() {
    var savedValue: String? = null
    composeTestRule.setContent {
      var isEditing by remember { mutableStateOf(false) }
      var value by remember { mutableStateOf("25") }

      EditableProfileFieldNumeric(
          label = "Age",
          value = value,
          isEditing = isEditing,
          isLoading = false,
          errorMessage = null,
          onEditClick = { isEditing = true },
          onSave = {
            savedValue = it
            value = it
            isEditing = false
          },
          onCancel = { isEditing = false })
    }

    composeTestRule.onNodeWithText("25").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Edit Age").performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextClearance()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("30")
    composeTestRule.onNodeWithContentDescription("Save").performClick()

    assertEquals("30", savedValue)
  }

  @Test
  fun editableProfileField_doesNotShowEditButtonWhenNotEditable() {
    composeTestRule.setContent {
      EditableProfileField(
          label = "Email",
          value = "john@example.com",
          isEditing = false,
          isLoading = false,
          errorMessage = null,
          onEditClick = {},
          onSave = {},
          onCancel = {},
          isEditable = false)
    }

    composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Edit Email").assertDoesNotExist()
  }

  @Test
  fun editableProfileField_cannotEditWhenNotEditable() {
    composeTestRule.setContent {
      EditableProfileField(
          label = "Email",
          value = "john@example.com",
          isEditing = true,
          isLoading = false,
          errorMessage = null,
          onEditClick = {},
          onSave = {},
          onCancel = {},
          isEditable = false)
    }

    // When not editable, should not show text field even if isEditing is true
    composeTestRule.onNode(hasSetTextAction()).assertDoesNotExist()
    composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
  }

  @Test
  fun editableProfileField_saveClearsErrorMessage() {
    composeTestRule.setContent {
      var isEditing by remember { mutableStateOf(true) }
      var errorMessage by remember { mutableStateOf<String?>("Error") }

      EditableProfileField(
          label = "Country",
          value = "France",
          isEditing = isEditing,
          isLoading = false,
          errorMessage = errorMessage,
          onEditClick = { isEditing = true },
          onSave = {
            errorMessage = null
            isEditing = false
          },
          onCancel = { isEditing = false })
    }

    composeTestRule.onNodeWithText("Error").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Save").performClick()
    composeTestRule.onNodeWithText("Error").assertDoesNotExist()
  }

  @Test
  fun editableProfileField_cancelClearsErrorMessage() {
    composeTestRule.setContent {
      var isEditing by remember { mutableStateOf(true) }
      var errorMessage by remember { mutableStateOf<String?>("Error") }

      EditableProfileField(
          label = "Country",
          value = "France",
          isEditing = isEditing,
          isLoading = false,
          errorMessage = errorMessage,
          onEditClick = { isEditing = true },
          onSave = {},
          onCancel = {
            errorMessage = null
            isEditing = false
          })
    }

    composeTestRule.onNodeWithText("Error").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Cancel").performClick()
    composeTestRule.onNodeWithText("Error").assertDoesNotExist()
  }

  @Test
  fun editableProfileField_showsLabelAndValue() {
    composeTestRule.setContent {
      EditableProfileField(
          label = "University",
          value = "EPFL",
          isEditing = false,
          isLoading = false,
          errorMessage = null,
          onEditClick = {},
          onSave = {},
          onCancel = {})
    }

    composeTestRule.onNodeWithText("University").assertIsDisplayed()
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
  }
}
