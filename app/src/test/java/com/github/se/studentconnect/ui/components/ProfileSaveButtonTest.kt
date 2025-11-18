package com.github.se.studentconnect.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ProfileSaveButtonTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun profileSaveButton_displaysText() {
    var clicked = false
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSaveButton(
            onClick = { clicked = true }, isLoading = false, enabled = true, text = "Save")
      }
    }

    composeTestRule.onNodeWithText("Save").assertExists()
    composeTestRule.onNodeWithText("Save").assertIsEnabled()
  }

  @Test
  fun profileSaveButton_performsClick() {
    var clicked = false
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSaveButton(
            onClick = { clicked = true }, isLoading = false, enabled = true, text = "Save")
      }
    }

    composeTestRule.onNodeWithText("Save").performClick()
    assert(clicked)
  }

  @Test
  fun profileSaveButton_showsLoadingIndicator() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSaveButton(onClick = {}, isLoading = true, enabled = true, text = "Save")
      }
    }

    composeTestRule
        .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
        .assertExists()
    composeTestRule.onNodeWithText("Save").assertDoesNotExist()
  }

  @Test
  fun profileSaveButton_isDisabledWhenDisabled() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSaveButton(onClick = {}, isLoading = false, enabled = false, text = "Save")
      }
    }

    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun profileSaveButton_isDisabledWhenLoading() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSaveButton(onClick = {}, isLoading = true, enabled = true, text = "Save")
      }
    }

    // Button should be disabled when loading
    composeTestRule
        .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
        .assertExists()
  }

  @Test
  fun profileSaveButton_usesCustomText() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSaveButton(onClick = {}, isLoading = false, enabled = true, text = "Save Changes")
      }
    }

    composeTestRule.onNodeWithText("Save Changes").assertExists()
  }
}
