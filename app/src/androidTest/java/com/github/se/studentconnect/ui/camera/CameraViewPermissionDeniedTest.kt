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
class CameraViewPermissionDeniedTest : StudentConnectTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  override fun createInitializedRepository(): EventRepository {
    return EventRepositoryProvider.repository
  }

  companion object {
    @JvmStatic
    @BeforeClass
    fun revokeCameraPermission() {
      val instrumentation = InstrumentationRegistry.getInstrumentation()
      val pkg = instrumentation.targetContext.packageName
      // Revoke permission before any camera binding happens
      instrumentation.uiAutomation.revokeRuntimePermission(pkg, Manifest.permission.CAMERA)
    }
  }

  @Test
  fun cameraView_rendersNoPermissionUI_whenPermissionDenied() {
    var noPermissionShown = false

    composeTestRule.setContent {
      CameraView(
          noPermission = { Box { noPermissionShown = true } }, onImageCaptured = {}, onError = {})
    }

    composeTestRule.waitForIdle()
    assertTrue(noPermissionShown)
  }
}
