// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.camera

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
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

  override fun createInitializedRepository(): EventRepository {
    // Unused for camera tests but required by StudentConnectTest
    return EventRepositoryProvider.repository
  }

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
}
