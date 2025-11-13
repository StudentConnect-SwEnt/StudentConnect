// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.camera

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
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

  @Test
  fun cameraView_rendersNoPermissionUI_whenPermissionDenied() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val pkg = instrumentation.targetContext.packageName

    // First ensure permission is revoked
    instrumentation.uiAutomation.revokeRuntimePermission(pkg, Manifest.permission.CAMERA)
    instrumentation.waitForIdleSync()

    var noPermissionShown = false
    var errorOccurred = false

    composeTestRule.setContent {
      CameraView(
          noPermission = {
            Box(modifier = Modifier.testTag("noPermissionBox")) { noPermissionShown = true }
          },
          onImageCaptured = {},
          onError = { errorOccurred = true })
    }

    // The noPermission composable should be called since permission is denied
    composeTestRule.waitForIdle()

    // Verify the no permission UI is rendered (either immediately or after permission denial)
    composeTestRule.onNodeWithTag("noPermissionBox").assertExists()
    assertTrue("Expected noPermission UI to be shown", noPermissionShown)
  }
}
