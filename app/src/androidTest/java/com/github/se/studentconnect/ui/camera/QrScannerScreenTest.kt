package com.github.se.studentconnect.ui.camera

import androidx.activity.ComponentActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.camera.AnalyzerProvider
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QrScannerScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun qrScannerScreen_inactive_showsPlaceholderAndInstructions() {
    composeTestRule.setContent {
      AppTheme { QrScannerScreen(onBackClick = {}, onProfileDetected = {}, isActive = false) }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_screen).assertIsDisplayed()
    composeTestRule.onNodeWithText("Swipe right to activate the scanner").assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_instructions).assertIsDisplayed()
  }

  @Test
  fun qrScannerScreen_backClick_invokesCallback() {
    var backClicks = 0

    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(onBackClick = { backClicks++ }, onProfileDetected = {}, isActive = false)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_back).performClick()

    composeTestRule.runOnIdle { assertEquals(1, backClicks) }
  }

  @Test
  fun qrScannerScreen_active_createsAnalyzerAndRendersCameraContent() {
    val factory = TestAnalyzerFactory()

    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) },
            analyzerProvider = factory)
      }
    }

    composeTestRule.onNodeWithTag("camera_stub").assertIsDisplayed()
    composeTestRule.waitForIdle()

    // Analyzer may be created multiple times due to recomposition (roiRect changes on layout)
    // Verify it was created at least once
    composeTestRule.runOnIdle {
      assert(factory.createdCount >= 1) {
        "Expected at least 1 analyzer creation, got ${factory.createdCount}"
      }
    }
  }

  @Test
  fun qrScannerScreen_inactive_doesNotCreateAnalyzer() {
    val factory = TestAnalyzerFactory()

    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            isActive = false,
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) },
            analyzerProvider = factory)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_placeholder).assertIsDisplayed()
    composeTestRule.runOnIdle { assertEquals(0, factory.createdCount) }
  }

  @Test
  fun qrScannerScreen_errorClearedWhenDeactivated() {
    val factory = TestAnalyzerFactory()
    val isActive = mutableStateOf(true)

    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            isActive = isActive.value,
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) },
            analyzerProvider = factory)
      }
    }

    composeTestRule.runOnIdle { factory.signalError(Throwable("Camera error. Please retry.")) }
    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_error).assertIsDisplayed()

    composeTestRule.runOnIdle { isActive.value = false }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_error).assertDoesNotExist()
    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_placeholder).assertIsDisplayed()
  }

  @Test
  fun qrScannerScreen_detectionInvokesCallbackOnce() {
    val factory = TestAnalyzerFactory()
    val detectedIds = mutableListOf<String>()

    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = { detectedIds.add(it) },
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) },
            analyzerProvider = factory)
      }
    }

    composeTestRule.runOnIdle { factory.triggerDetection("user-999") }
    composeTestRule.runOnIdle { factory.triggerDetection("user-1000") }

    composeTestRule.runOnIdle { assertEquals(listOf("user-999"), detectedIds) }
  }

  @Test
  fun qrScannerScreen_showsStoryModeUi_whenEnabled() {
    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            isActive = true,
            showStoryModeUi = true,
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) })
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_story_controls).assertIsDisplayed()
  }

  @Test
  fun qrScannerScreen_hidesInstructions_whenStoryModeEnabled() {
    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            isActive = true,
            showStoryModeUi = true,
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) })
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_instructions).assertDoesNotExist()
  }

  @Test
  fun qrScannerScreen_showsFocusFrame() {
    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) })
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_focus).assertIsDisplayed()
  }

  @Test
  fun qrScannerScreen_showsErrorMessage() {
    val factory = TestAnalyzerFactory()

    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) },
            analyzerProvider = factory)
      }
    }

    composeTestRule.runOnIdle { factory.signalError(Throwable("Test error message")) }
    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_error).assertIsDisplayed()
    composeTestRule.onNodeWithText("Test error message").assertIsDisplayed()
  }

  @Test
  fun qrScannerScreen_errorMessageCleared_afterNewDetection() {
    val factory = TestAnalyzerFactory()

    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) },
            analyzerProvider = factory)
      }
    }

    composeTestRule.runOnIdle { factory.signalError(Throwable("Error")) }
    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_error).assertIsDisplayed()

    composeTestRule.runOnIdle { factory.triggerDetection("user-123") }
    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_error).assertDoesNotExist()
  }

  @Test
  fun storyModeControls_photoMode_showsPhotoText() {
    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            isActive = true,
            showStoryModeUi = true,
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) })
      }
    }

    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()
  }

  @Test
  fun storyModeSelector_showsPhotoAndVideoOptions() {
    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            isActive = true,
            showStoryModeUi = true,
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) })
      }
    }

    composeTestRule.onNodeWithText("PHOTO").assertIsDisplayed()
    composeTestRule.onNodeWithText("VIDEO").assertIsDisplayed()
  }

  @Test
  fun captureButtonPreview_visible_whenStoryModeEnabled() {
    composeTestRule.setContent {
      AppTheme {
        QrScannerScreen(
            onBackClick = {},
            onProfileDetected = {},
            isActive = true,
            showStoryModeUi = true,
            cameraContent = { Box(Modifier.fillMaxSize().testTag("camera_stub")) })
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.qr_scanner_story_capture_button).assertIsDisplayed()
  }
}

private class TestAnalyzerFactory : AnalyzerProvider {

  var createdCount = 0
  private var onDetected: ((String) -> Unit)? = null
  private var onError: ((Throwable) -> Unit)? = null

  override fun invoke(
      onProfileDetected: (String) -> Unit,
      onError: (Throwable) -> Unit,
      roi: android.graphics.RectF?
  ): ImageAnalysis.Analyzer? {
    createdCount++
    this.onDetected = onProfileDetected
    this.onError = onError
    return ImageAnalysis.Analyzer { imageProxy: ImageProxy -> imageProxy.close() }
  }

  fun triggerDetection(userId: String) {
    onDetected?.invoke(userId)
  }

  fun signalError(throwable: Throwable) {
    onError?.invoke(throwable)
  }
}
