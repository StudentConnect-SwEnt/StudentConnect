package com.github.se.studentconnect.ui.screen.signup

import android.net.Uri
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationLogoScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun initialState_showsTitleAndContinueDisabled() {
    val viewModel = SignUpViewModel()
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    composeTestRule.setContent {
      AppTheme {
        OrganizationLogoScreen(viewModel = viewModel, onSkip = {}, onContinue = {}, onBack = {})
      }
    }

    composeTestRule.onNodeWithText(ctx.getString(R.string.title_upload_logo)).assertExists()
    composeTestRule.onNodeWithText(ctx.getString(R.string.subtitle_upload_org_logo)).assertExists()
    composeTestRule.onNodeWithText(ctx.getString(R.string.button_continue)).assertIsNotEnabled()
  }

  @Test
  fun skip_setsPlaceholderAndEnablesContinue() {
    val viewModel = SignUpViewModel()
    var skipped = false
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    composeTestRule.setContent {
      AppTheme {
        OrganizationLogoScreen(
            viewModel = viewModel, onSkip = { skipped = true }, onContinue = {}, onBack = {})
      }
    }

    composeTestRule.onNodeWithText(ctx.getString(R.string.button_skip)).performClick()
    composeTestRule.waitForIdle()

    assertEquals(DEFAULT_PLACEHOLDER, viewModel.state.value.profilePictureUri)
    assertEquals(true, skipped)
    composeTestRule.onNodeWithText(ctx.getString(R.string.button_continue)).assertIsEnabled()
  }

  @Test
  fun selectingPhoto_enablesContinue_and_invokesOnContinue() {
    val viewModel = SignUpViewModel()
    var continued = false
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    composeTestRule.setContent {
      AppTheme {
        OrganizationLogoScreen(
            viewModel = viewModel, onSkip = {}, onContinue = { continued = true }, onBack = {})
      }
    }

    composeTestRule.runOnIdle {
      viewModel.setProfilePictureUri(Uri.parse("file://sample/logo.png"))
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(ctx.getString(R.string.text_photo_selected)).assertExists()
    composeTestRule.onNodeWithText(ctx.getString(R.string.button_continue)).performClick()
    composeTestRule.runOnIdle { assertEquals(true, continued) }
  }
}
