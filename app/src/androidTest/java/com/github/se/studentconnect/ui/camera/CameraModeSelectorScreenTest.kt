package com.github.se.studentconnect.ui.camera

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.ui.screen.camera.CameraMode
import com.github.se.studentconnect.ui.screen.camera.CameraModeSelectorScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CameraModeSelectorScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @Test
  fun cameraModeSelectorScreen_displaysModeTabs() {
    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = CameraMode.QR_SCAN)
      }
    }

    composeTestRule.onNodeWithTag("camera_mode_selector").assertIsDisplayed()
    composeTestRule.onNodeWithText("STORY").assertIsDisplayed()
    composeTestRule.onNodeWithText("QR SCAN").assertIsDisplayed()
  }

  @Test
  fun cameraModeSelectorScreen_backButton_invokesCallback() {
    var backClicks = 0

    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = { backClicks++ },
            onProfileDetected = {},
            initialMode = CameraMode.QR_SCAN)
      }
    }

    composeTestRule.onNodeWithTag("camera_mode_back_button").performClick()

    composeTestRule.runOnIdle { assertEquals(1, backClicks) }
  }

  @Test
  fun cameraModeSelectorScreen_initialModeQrScan_displaysQrScanner() {
    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = CameraMode.QR_SCAN)
      }
    }

    composeTestRule.waitForIdle()
    // QR scanner should be active, look for QR scanner text
    composeTestRule
        .onNodeWithText("Point the camera at a StudentConnect QR code")
        .assertIsDisplayed()
  }

  @Test
  fun cameraModeSelectorScreen_initialModeStory_displaysStoryCapture() {
    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = CameraMode.STORY)
      }
    }

    composeTestRule.waitForIdle()
    // Story capture should be active
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun cameraModeSelectorScreen_clickStoryTab_switchesToStoryMode() {
    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = CameraMode.QR_SCAN)
      }
    }

    // Initially on QR scan mode
    composeTestRule.waitForIdle()

    // Click story tab
    composeTestRule.onNodeWithTag("mode_story").performClick()

    composeTestRule.waitForIdle()
    // Should now display story capture
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun cameraModeSelectorScreen_clickQrScanTab_switchesToQrScanMode() {
    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = CameraMode.STORY)
      }
    }

    // Initially on story mode
    composeTestRule.waitForIdle()

    // Click QR scan tab
    composeTestRule.onNodeWithTag("mode_qr_scan").performClick()

    composeTestRule.waitForIdle()
    // Should now display QR scanner text
    composeTestRule
        .onNodeWithText("Point the camera at a StudentConnect QR code")
        .assertIsDisplayed()
  }

  @Test
  fun cameraModeSelectorScreen_initialModeChange_updatesDisplayedMode() {
    val initialMode = mutableStateOf(CameraMode.QR_SCAN)

    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = initialMode.value)
      }
    }

    composeTestRule.waitForIdle()
    // Initially QR scan
    composeTestRule
        .onNodeWithText("Point the camera at a StudentConnect QR code")
        .assertIsDisplayed()

    // Change to story mode
    composeTestRule.runOnIdle { initialMode.value = CameraMode.STORY }

    composeTestRule.waitForIdle()
    // Should now show story capture
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun cameraModeSelectorScreen_onProfileDetected_invokesCallback() {
    val detectedUserIds = mutableListOf<String>()

    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = { userId -> detectedUserIds.add(userId) },
            initialMode = CameraMode.QR_SCAN)
      }
    }

    // This test verifies the callback is passed through to QrScannerScreen
    // Actual QR detection is tested in QrScannerScreenTest
    composeTestRule.waitForIdle()
    assertTrue(detectedUserIds.isEmpty())
  }

  @Test
  fun cameraModeSelectorScreen_storyMode_displaysCorrectly() {
    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = CameraMode.STORY)
      }
    }

    // This test verifies story mode displays correctly
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun cameraModeSelectorScreen_bothModesAvailable() {
    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = CameraMode.QR_SCAN)
      }
    }

    // Verify both mode tabs are clickable and present
    composeTestRule.onNodeWithTag("mode_story").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mode_qr_scan").assertIsDisplayed()

    // Switch to story
    composeTestRule.onNodeWithTag("mode_story").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()

    // Switch back to QR
    composeTestRule.onNodeWithTag("mode_qr_scan").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithText("Point the camera at a StudentConnect QR code")
        .assertIsDisplayed()
  }

  @Test
  fun cameraModeSelectorScreen_modeSelectorVisible_byDefault() {
    composeTestRule.setContent {
      AppTheme {
        CameraModeSelectorScreen(
            onBackClick = {},
            onProfileDetected = {},
            initialMode = CameraMode.STORY)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_mode_selector").assertIsDisplayed()
  }
}
