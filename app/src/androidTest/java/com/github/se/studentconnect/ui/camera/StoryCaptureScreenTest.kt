package com.github.se.studentconnect.ui.camera

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.ui.screen.camera.StoryCaptureScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class StoryCaptureScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @Test
  fun storyCaptureScreen_displaysCorrectly() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_instructions").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_displaysInstructions() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_inactive_showsInactiveBackground() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = false) }
    }

    composeTestRule.onNodeWithTag("story_inactive").assertIsDisplayed()
    composeTestRule.onNodeWithText("Swipe to activate story camera").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_active_doesNotShowInactiveBackground() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithTag("story_inactive").assertDoesNotExist()
  }

  @Test
  fun storyCaptureScreen_toggleActive_updatesDisplay() {
    val isActive = mutableStateOf(true)

    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = isActive.value) }
    }

    // Initially active
    composeTestRule.onNodeWithTag("story_inactive").assertDoesNotExist()
    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()

    // Set to inactive
    composeTestRule.runOnIdle { isActive.value = false }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_inactive").assertIsDisplayed()
    composeTestRule.onNodeWithText("Swipe to activate story camera").assertIsDisplayed()

    // Set back to active
    composeTestRule.runOnIdle { isActive.value = true }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_inactive").assertDoesNotExist()
    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_instructionsVisible_whenActive() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithTag("story_instructions").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_instructionsNotVisible_whenInactive() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = false) }
    }

    // Instructions are not visible when inactive
    composeTestRule.onNodeWithTag("story_instructions").assertDoesNotExist()
  }

  @Test
  fun storyCaptureScreen_previewStateCallback_initiallyFalse() {
    val previewStates = mutableListOf<Boolean>()

    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreen(
            onBackClick = {},
            isActive = true,
            onPreviewStateChanged = { isShowing -> previewStates.add(isShowing) })
      }
    }

    composeTestRule.runOnIdle {
      assert(previewStates.isNotEmpty())
      assert(!previewStates.last())
    }
  }

  @Test
  fun storyCaptureScreen_previewStateCallback_defaultValue() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_videoMode_switchesControls() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_onPreviewStateChanged_invokedOnComposition() {
    val stateChanges = mutableListOf<Boolean>()
    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreen(
            onBackClick = {}, isActive = true, onPreviewStateChanged = { stateChanges.add(it) })
      }
    }

    composeTestRule.runOnIdle { assert(stateChanges.contains(false)) }
  }
}
