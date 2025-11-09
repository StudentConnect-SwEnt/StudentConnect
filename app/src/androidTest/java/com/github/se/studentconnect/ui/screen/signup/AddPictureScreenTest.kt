package com.github.se.studentconnect.ui.screen.signup

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.ui.theme.AppTheme
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddPictureScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private val DEFAULT_PLACEHOLDER = "ic_user".toUri()

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

  @Test
  fun skipButton_setsPlaceholderAndInvokesCallback() {
    val viewModel = SignUpViewModel()
    var skipped = false

    composeTestRule.setContent {
      AppTheme {
        AddPictureScreen(
            viewModel = viewModel, onSkip = { skipped = true }, onContinue = {}, onBack = {})
      }
    }

    composeTestRule.onNodeWithText("Skip").performClick()
    composeTestRule.waitForIdle()

    assertTrue(skipped)
    assertEquals(DEFAULT_PLACEHOLDER, viewModel.state.value.profilePictureUri)
    composeTestRule.onNodeWithText("Continue").assertIsEnabled()
  }

  @Test
  fun continueButton_invokesCallbackWhenEnabled() {
    val viewModel = SignUpViewModel()
    var continued = false

    composeTestRule.setContent {
      AppTheme {
        AddPictureScreen(
            viewModel = viewModel, onSkip = {}, onContinue = { continued = true }, onBack = {})
      }
    }

    composeTestRule.runOnIdle {
      viewModel.setProfilePictureUri(Uri.parse("file://sample/enabled.png"))
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Continue").performClick()
    assertTrue(continued)
  }

  @Test
  fun selectedBitmap_showsTapToChangePrompt() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val tempFile = createTempImageFile(context.cacheDir)
    val viewModel = SignUpViewModel().apply { setProfilePictureUri(Uri.fromFile(tempFile)) }

    composeTestRule.setContent {
      AppTheme {
        AddPictureScreen(viewModel = viewModel, onSkip = {}, onContinue = {}, onBack = {})
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tap to change photo", useUnmergedTree = true).assertExists()

    tempFile.delete()
  }

  private fun createTempImageFile(cacheDir: File): File {
    val file = File(cacheDir, "add_picture_${System.currentTimeMillis()}.png")
    val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawColor(Color.GREEN)
    FileOutputStream(file).use { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) }
    bitmap.recycle()
    return file
  }
}
