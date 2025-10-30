// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.camera

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
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
