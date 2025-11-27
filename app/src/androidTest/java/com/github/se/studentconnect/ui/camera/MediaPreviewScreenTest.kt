package com.github.se.studentconnect.ui.camera

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.ui.screen.camera.MediaPreviewScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import java.io.File
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MediaPreviewScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var testImageUri: Uri
  private lateinit var testFile: File
  private val context: Context = ApplicationProvider.getApplicationContext()

  @Before
  fun setup() {
    testFile = File(context.cacheDir, "test_image.jpg")
    testFile.createNewFile()
    testImageUri = Uri.fromFile(testFile)
  }

  @After
  fun cleanup() {
    testFile.delete()
  }

  @Test
  fun mediaPreviewScreen_displaysPhotoPreview() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = false, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("media_preview_actions").assertIsDisplayed()
  }

  @Test
  fun mediaPreviewScreen_displaysVideoPreview() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = true, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
  }

  @Test
  fun mediaPreviewScreen_retakeButton_invokesCallback() {
    var retakeClicked = false

    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(
            mediaUri = testImageUri,
            isVideo = false,
            onAccept = {},
            onRetake = { retakeClicked = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Retake").performClick()
    composeTestRule.runOnIdle { assert(retakeClicked) }
  }

  @Test
  fun mediaPreviewScreen_acceptButton_invokesCallback() {
    var acceptClicked = false

    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(
            mediaUri = testImageUri,
            isVideo = false,
            onAccept = { acceptClicked = true },
            onRetake = {})
      }
    }

    composeTestRule.onNodeWithContentDescription("Accept").performClick()
    composeTestRule.runOnIdle { assert(acceptClicked) }
  }

  @Test
  fun mediaPreviewScreen_displaysRetakeButton() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = false, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_retake").assertIsDisplayed()
  }

  @Test
  fun mediaPreviewScreen_displaysAcceptButton() {
    composeTestRule.setContent {
      AppTheme {
        MediaPreviewScreen(mediaUri = testImageUri, isVideo = false, onAccept = {}, onRetake = {})
      }
    }

    composeTestRule.onNodeWithTag("media_preview_accept").assertIsDisplayed()
  }
}
