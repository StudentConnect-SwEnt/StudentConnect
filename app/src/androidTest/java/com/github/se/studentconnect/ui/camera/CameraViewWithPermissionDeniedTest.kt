// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.camera

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.utils.StudentConnectTest
import junit.framework.TestCase.assertTrue
import org.junit.*

/** Tests for [CameraView] where CAMERA permission is explicitly denied. */
class CameraViewWithPermissionDeniedTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  override fun createInitializedRepository(): EventRepository {
    return EventRepositoryProvider.repository
  }

  @Before
  fun revokeCameraPermission() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val pkg = instrumentation.targetContext.packageName
    // Revoke permission before each test
    instrumentation.uiAutomation.revokeRuntimePermission(pkg, Manifest.permission.CAMERA)
    // Allow time for permission state to propagate
    Thread.sleep(100)
  }

  @Test
  fun cameraView_rendersNoPermissionUI_whenPermissionDenied() {
    var noPermissionShown = false
    var permissionDeniedCalled = false

    composeTestRule.setContent {
      CameraView(
          noPermission = { Box { noPermissionShown = true } },
          onCameraPermissionDenied = { permissionDeniedCalled = true },
          onImageCaptured = {},
          onError = {})
    }

    // Wait for the permission launcher to be triggered and denied
    composeTestRule.waitForIdle()

    // The permission launcher should trigger, which will call onCameraPermissionDenied
    // After that, noPermission should be shown on recomposition
    composeTestRule.waitUntil(timeoutMillis = 5000) { noPermissionShown || permissionDeniedCalled }

    assertTrue("Expected noPermission UI to be shown", noPermissionShown)
  }
}
