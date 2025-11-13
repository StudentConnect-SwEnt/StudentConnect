package com.github.se.studentconnect.ui.camera

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
  fun storyCaptureScreen_instructionsVisible_whenInactive() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = false) }
    }

    // Instructions are visible even when inactive (they appear over the inactive background)
    composeTestRule.onNodeWithTag("story_instructions").assertIsDisplayed()
  }
}

/** Tests for the PermissionRequired composable when camera permission is not granted. */
class StoryCaptureScreenPermissionTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun storyCaptureScreen_noPermission_showsPermissionRequired() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    // Wait for the camera to check permissions and display the permission message
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("story_permission").fetchSemanticsNodes().isNotEmpty()
    }

    // The permission message should be displayed when camera access is not granted
    composeTestRule.onNodeWithTag("story_permission").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Camera permission is required to capture photos.")
        .assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_noPermission_backButtonExists() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    // Wait for the permission UI to appear
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes().isNotEmpty()
    }

    // The back button should be displayed
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_noPermission_backButtonClick() {
    var backClicked = false

    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = { backClicked = true }, isActive = true) }
    }

    // Wait for the permission UI to appear
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes().isNotEmpty()
    }

    // Click the back button
    composeTestRule.onNodeWithContentDescription("Back").performClick()

    // Verify callback was invoked
    composeTestRule.runOnIdle { assert(backClicked) }
  }

  @Test
  fun storyCaptureScreen_noPermission_permissionTextIsVisible() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    // Wait for the permission UI to appear
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithText("Camera permission is required to capture photos.")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify the specific permission text is visible
    composeTestRule
        .onNodeWithText("Camera permission is required to capture photos.")
        .assertIsDisplayed()
  }
}
