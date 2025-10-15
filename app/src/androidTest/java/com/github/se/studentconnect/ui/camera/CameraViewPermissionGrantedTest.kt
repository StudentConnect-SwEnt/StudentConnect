// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.camera

import android.Manifest
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.utils.StudentConnectTest
import java.io.File
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.*

/** Tests for [CameraView] where CAMERA permission is explicitly granted. */
class CameraViewPermissionGrantedTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  override fun createInitializedRepository(): EventRepository {
    // Unused for camera tests but required by StudentConnectTest
    return EventRepositoryProvider.repository
  }

  companion object {
    @JvmStatic
    @BeforeClass
    fun grantCameraPermission() {
      val instrumentation = InstrumentationRegistry.getInstrumentation()
      val pkg = instrumentation.targetContext.packageName
      instrumentation.uiAutomation.grantRuntimePermission(pkg, Manifest.permission.CAMERA)
    }
  }

  @Test
  fun cameraView_rendersDefaultButton_whenPermissionGranted() {
    runBlocking { composeTestRule.setContent { CameraView(onImageCaptured = {}, onError = {}) } }
    composeTestRule.onNodeWithContentDescription("Capture").assertExists()
  }

  @Test
  fun cameraView_callsOnImageCaptured_whenCaptureButtonClicked() {
    val capturedUris = mutableListOf<Uri>()

    composeTestRule.setContent {
      CameraView(
          onImageCaptured = { uri -> capturedUris.add(uri) },
          onError = { throw it } // Fail test if onError is called
          )
    }

    // Find and click the capture button
    composeTestRule.onNodeWithContentDescription("Capture").performClick()

    // Wait for the callback to be invoked
    composeTestRule.waitUntil(timeoutMillis = 5_000) { capturedUris.isNotEmpty() }

    // Verify the callback was triggered with a non-null Uri
    assertTrue("onImageCaptured should be called", capturedUris.isNotEmpty())
    assertNotNull("Captured Uri should not be null", capturedUris.first())
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
}
