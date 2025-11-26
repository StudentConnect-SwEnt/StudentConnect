package com.github.se.studentconnect.ui.screen.signup

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationLogoScreen
import com.github.se.studentconnect.ui.screen.signup.regularuser.DEFAULT_PLACEHOLDER
import com.github.se.studentconnect.ui.screen.signup.regularuser.SignUpViewModel
import com.github.se.studentconnect.ui.theme.AppTheme
import java.io.File
import java.io.FileOutputStream
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
    val tempFile = createTempImageFile(ctx.cacheDir)

    composeTestRule.setContent {
      AppTheme {
        OrganizationLogoScreen(
            viewModel = viewModel, onSkip = {}, onContinue = { continued = true }, onBack = {})
      }
    }

    composeTestRule.runOnIdle { viewModel.setProfilePictureUri(Uri.fromFile(tempFile)) }
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithText(
            ctx.getString(R.string.instruction_tap_to_change_photo), useUnmergedTree = true)
        .assertExists()
    composeTestRule.onNodeWithText(ctx.getString(R.string.button_continue)).performClick()
    composeTestRule.runOnIdle { assertEquals(true, continued) }

    tempFile.delete()
  }

  private fun createTempImageFile(cacheDir: File): File {
    val file = File(cacheDir, "organization_logo_${System.currentTimeMillis()}.png")
    val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawColor(Color.BLUE)
    FileOutputStream(file).use { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) }
    bitmap.recycle()
    return file
  }
}
