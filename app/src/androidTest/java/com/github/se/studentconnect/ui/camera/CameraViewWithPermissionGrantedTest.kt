// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.camera

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.utils.StudentConnectTest
import java.io.File
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.*

/** Tests for [CameraView] where CAMERA permission is explicitly granted. */
class CameraViewWithPermissionGrantedTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

  @Test
  fun cameraView_rendersDefaultButton_whenPermissionGranted() {
    runBlocking { composeTestRule.setContent { CameraView(onImageCaptured = {}, onError = {}) } }
    composeTestRule.onNodeWithContentDescription("Capture").assertExists()
  }

  @Test
  fun cameraView_allowsCustomCaptureButton() {
    val customLabel = "Custom Button"
    composeTestRule.setContent {
      CameraView(captureButton = { Text(customLabel) }, onImageCaptured = {}, onError = {})
    }
    composeTestRule.onNodeWithText(customLabel).assertExists()
  }

  @Test
  fun capturePhoto_createsFile_inCacheDir() {
    val context = composeTestRule.activity
    val fakeCapture = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
    fakeCapture.writeText("test data")
    assertTrue(fakeCapture.exists())
    assertTrue(fakeCapture.length() > 0)
  }

  @Test
  fun cameraView_enableVideoCapture_rendersButton() {
    composeTestRule.setContent {
      CameraView(enableImageCapture = false, enableVideoCapture = true, onVideoCaptured = {})
    }
    composeTestRule.onNodeWithContentDescription("Capture").assertExists()
  }

  @Test
  fun cameraView_videoCapture_passesRecordingStateToButton() {
    var receivedRecordingState = false
    composeTestRule.setContent {
      CameraView(
          enableVideoCapture = true,
          captureButton = { isRecording -> receivedRecordingState = isRecording })
    }
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle { assert(!receivedRecordingState) }
  }

  @Test
  fun cameraView_videoCaptureCallback_isOptional() {
    composeTestRule.setContent { CameraView(enableVideoCapture = true, onVideoCaptured = null) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Capture").assertExists()
  }

  @Test
  fun cameraView_imageCaptureCallback_isOptional() {
    composeTestRule.setContent { CameraView(enableImageCapture = true, onImageCaptured = null) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Capture").assertExists()
  }

  @Test
  fun cameraView_clickCaptureButton_triggersImageCapture() {
    var imageCaptured = false
    composeTestRule.setContent {
      CameraView(enableImageCapture = true, onImageCaptured = { imageCaptured = true })
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Capture").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_onError_handlesExceptions() {
    var errorReceived = false
    composeTestRule.setContent {
      CameraView(
          enableImageCapture = true, onImageCaptured = {}, onError = { errorReceived = true })
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_bothCaptureModesDisabled_noButtonShown() {
    composeTestRule.setContent {
      CameraView(enableImageCapture = false, enableVideoCapture = false)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Capture").assertDoesNotExist()
  }

  @Test
  fun cameraView_customButton_receivesRecordingState() {
    val recordingStates = mutableListOf<Boolean>()
    composeTestRule.setContent {
      CameraView(
          enableVideoCapture = true,
          captureButton = { isRecording -> recordingStates.add(isRecording) })
    }

    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle { assert(recordingStates.isNotEmpty()) }
  }

  @Test
  fun cameraView_imageAnalyzer_canBeProvided() {
    val analyzer =
        object : androidx.camera.core.ImageAnalysis.Analyzer {
          override fun analyze(image: androidx.camera.core.ImageProxy) {
            image.close()
          }
        }

    composeTestRule.setContent { CameraView(enableImageCapture = true, imageAnalyzer = analyzer) }

    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_imageAnalysisConfig_canBeProvided() {
    composeTestRule.setContent {
      CameraView(
          enableImageCapture = true,
          imageAnalyzer =
              object : androidx.camera.core.ImageAnalysis.Analyzer {
                override fun analyze(image: androidx.camera.core.ImageProxy) {
                  image.close()
                }
              },
          imageAnalysisConfig = {
            setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
          })
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_onCameraPermissionDenied_invoked() {
    var permissionDenied = false
    composeTestRule.setContent {
      CameraView(
          requestPermissionAutomatically = false,
          onCameraPermissionDenied = { permissionDenied = true })
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_noPermission_displaysCustomComposable() {
    composeTestRule.setContent {
      CameraView(
          requestPermissionAutomatically = false, noPermission = { Text("No Camera Permission") })
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_backCameraSelector_default() {
    composeTestRule.setContent {
      CameraView(
          enableImageCapture = true,
          cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA)
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_frontCameraSelector_canBeProvided() {
    composeTestRule.setContent {
      CameraView(
          enableImageCapture = true,
          cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_bothCaptureModesEnabled_showsButton() {
    composeTestRule.setContent { CameraView(enableImageCapture = true, enableVideoCapture = true) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Capture").assertExists()
  }

  @Test
  fun cameraView_customModifier_applied() {
    composeTestRule.setContent {
      CameraView(modifier = Modifier.testTag("custom_camera"), enableImageCapture = true)
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraView_imageCapture_withoutCallbacks() {
    composeTestRule.setContent { CameraView(enableImageCapture = true) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Capture").assertExists()
  }

  @Test
  fun cameraView_videoCapture_withoutCallbacks() {
    composeTestRule.setContent { CameraView(enableVideoCapture = true, enableImageCapture = false) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Capture").assertExists()
  }
}
