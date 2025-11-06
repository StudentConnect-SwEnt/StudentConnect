package com.github.se.studentconnect.ui.screen.signup

import android.net.Uri
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddPictureScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun addPictureScreen_initialState_disablesContinueButton() {
    val viewModel = SignUpViewModel()

    composeTestRule.setContent {
      AppTheme {
        AddPictureScreen(viewModel = viewModel, onSkip = {}, onContinue = {}, onBack = {})
      }
    }

    composeTestRule.onNodeWithText("Upload/Take your profile photo").assertExists()
    composeTestRule.onNodeWithText("Continue").assertIsNotEnabled()
  }

  @Test
  fun addPictureScreen_selectingPhoto_enablesContinueAndShowsSelectionHint() {
    val viewModel = SignUpViewModel()

    composeTestRule.setContent {
      AppTheme {
        AddPictureScreen(viewModel = viewModel, onSkip = {}, onContinue = {}, onBack = {})
      }
    }

    composeTestRule.runOnIdle {
      viewModel.setProfilePictureUri(Uri.parse("file://sample/photo.png"))
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Photo selected").assertExists()
    composeTestRule.onNodeWithText("Continue").assertIsEnabled()
  }

  @Test
  fun addPictureScreen_existingSelection_displaysChangePrompt() {
    val viewModel =
        SignUpViewModel().apply { setProfilePictureUri(Uri.parse("file://already/there.jpg")) }

    composeTestRule.setContent {
      AppTheme {
        AddPictureScreen(viewModel = viewModel, onSkip = {}, onContinue = {}, onBack = {})
      }
    }

    composeTestRule.onNodeWithText("Photo selected").assertExists()
    composeTestRule.onNodeWithContentDescription("Upload photo").assertExists()
  }
}
