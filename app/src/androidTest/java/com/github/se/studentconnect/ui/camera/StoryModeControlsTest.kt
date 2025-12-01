package com.github.se.studentconnect.ui.camera

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.camera.CaptureButtonPreview
import com.github.se.studentconnect.ui.screen.camera.StoryCaptureMode
import com.github.se.studentconnect.ui.screen.camera.StoryModeControls
import com.github.se.studentconnect.ui.screen.camera.StoryModeOption
import com.github.se.studentconnect.ui.screen.camera.StoryModeSelector
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StoryModeControlsTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun storyModeControls_photoMode_showsPhotoText() {
    composeTestRule.setContent {
      AppTheme {
        StoryModeControls(
            selectedMode = StoryCaptureMode.PHOTO, onModeSelected = {}, showCaptureButton = true)
      }
    }

    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()
  }

  @Test
  fun storyModeControls_videoMode_showsVideoText() {
    composeTestRule.setContent {
      AppTheme {
        StoryModeControls(
            selectedMode = StoryCaptureMode.VIDEO, onModeSelected = {}, showCaptureButton = true)
      }
    }

    composeTestRule.onNodeWithText("Tap to start recording").assertIsDisplayed()
  }

  @Test
  fun storyModeControls_hideCaptureButton() {
    composeTestRule.setContent {
      AppTheme {
        StoryModeControls(
            selectedMode = StoryCaptureMode.PHOTO, onModeSelected = {}, showCaptureButton = false)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_story_capture_button).assertDoesNotExist()
  }

  @Test
  fun storyModeControls_customInfoText() {
    composeTestRule.setContent {
      AppTheme {
        StoryModeControls(
            selectedMode = StoryCaptureMode.PHOTO,
            onModeSelected = {},
            infoText = "Custom info text")
      }
    }

    composeTestRule.onNodeWithText("Custom info text").assertIsDisplayed()
  }

  @Test
  fun storyModeControls_nullInfoText() {
    composeTestRule.setContent {
      AppTheme {
        StoryModeControls(
            selectedMode = StoryCaptureMode.PHOTO, onModeSelected = {}, infoText = null)
      }
    }

    composeTestRule.onNodeWithText("Tap the button to take a photo").assertDoesNotExist()
  }

  @Test
  fun storyModeSelector_photoSelected_initially() {
    composeTestRule.setContent {
      AppTheme { StoryModeSelector(selectedMode = StoryCaptureMode.PHOTO, onModeSelected = {}) }
    }

    composeTestRule.onNodeWithText("PHOTO").assertIsDisplayed()
    composeTestRule.onNodeWithText("VIDEO").assertIsDisplayed()
  }

  @Test
  fun storyModeSelector_videoSelected_initially() {
    composeTestRule.setContent {
      AppTheme { StoryModeSelector(selectedMode = StoryCaptureMode.VIDEO, onModeSelected = {}) }
    }

    composeTestRule.onNodeWithText("PHOTO").assertIsDisplayed()
    composeTestRule.onNodeWithText("VIDEO").assertIsDisplayed()
  }

  @Test
  fun storyModeSelector_clickPhoto_invokesCallback() {
    var selectedMode: StoryCaptureMode? = null

    composeTestRule.setContent {
      AppTheme {
        StoryModeSelector(
            selectedMode = StoryCaptureMode.VIDEO, onModeSelected = { selectedMode = it })
      }
    }

    composeTestRule.onNodeWithText("PHOTO").performClick()
    composeTestRule.runOnIdle { assertEquals(StoryCaptureMode.PHOTO, selectedMode) }
  }

  @Test
  fun storyModeSelector_clickVideo_invokesCallback() {
    var selectedMode: StoryCaptureMode? = null

    composeTestRule.setContent {
      AppTheme {
        StoryModeSelector(
            selectedMode = StoryCaptureMode.PHOTO, onModeSelected = { selectedMode = it })
      }
    }

    composeTestRule.onNodeWithText("VIDEO").performClick()
    composeTestRule.runOnIdle { assertEquals(StoryCaptureMode.VIDEO, selectedMode) }
  }

  @Test
  fun storyModeOption_selected_displaysCorrectly() {
    composeTestRule.setContent {
      AppTheme { StoryModeOption(label = "TEST", isSelected = true, onClick = {}) }
    }

    composeTestRule.onNodeWithText("TEST").assertIsDisplayed()
  }

  @Test
  fun storyModeOption_notSelected_displaysCorrectly() {
    composeTestRule.setContent {
      AppTheme { StoryModeOption(label = "TEST", isSelected = false, onClick = {}) }
    }

    composeTestRule.onNodeWithText("TEST").assertIsDisplayed()
  }

  @Test
  fun storyModeOption_click_invokesCallback() {
    var clicked = false

    composeTestRule.setContent {
      AppTheme { StoryModeOption(label = "TEST", isSelected = false, onClick = { clicked = true }) }
    }

    composeTestRule.onNodeWithText("TEST").performClick()
    composeTestRule.runOnIdle { assert(clicked) }
  }

  @Test
  fun captureButtonPreview_photoMode_displayed() {
    composeTestRule.setContent {
      AppTheme { CaptureButtonPreview(selectedMode = StoryCaptureMode.PHOTO) }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_story_capture_button).assertIsDisplayed()
  }

  @Test
  fun captureButtonPreview_videoMode_displayed() {
    composeTestRule.setContent {
      AppTheme { CaptureButtonPreview(selectedMode = StoryCaptureMode.VIDEO) }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_story_capture_button).assertIsDisplayed()
  }

  @Test
  fun captureButtonPreview_videoMode_recording() {
    composeTestRule.setContent {
      AppTheme { CaptureButtonPreview(selectedMode = StoryCaptureMode.VIDEO, isRecording = true) }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_story_capture_button).assertIsDisplayed()
  }

  @Test
  fun captureButtonPreview_videoMode_notRecording() {
    composeTestRule.setContent {
      AppTheme { CaptureButtonPreview(selectedMode = StoryCaptureMode.VIDEO, isRecording = false) }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_story_capture_button).assertIsDisplayed()
  }

  @Test
  fun captureButtonPreview_photoMode_notRecording() {
    composeTestRule.setContent {
      AppTheme { CaptureButtonPreview(selectedMode = StoryCaptureMode.PHOTO, isRecording = false) }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_story_capture_button).assertIsDisplayed()
  }
}
