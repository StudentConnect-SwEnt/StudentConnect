// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.camera

import androidx.compose.ui.test.*
import org.junit.*


// test was removed because it's implementation changed for the new version of the camera view

/**
 * Tests for [CameraView] where CAMERA permission is explicitly denied. class
 * CameraViewWithPermissionDeniedTest : StudentConnectTest() {
 *
 * @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
 *
 * override fun createInitializedRepository(): EventRepository { return
 * EventRepositoryProvider.repository }
 *
 * @Test fun cameraView_rendersNoPermissionUI_whenPermissionDenied() { val instrumentation =
 *   InstrumentationRegistry.getInstrumentation() val pkg =
 *   instrumentation.targetContext.packageName
 *
 * // Revoke camera permission instrumentation.uiAutomation.revokeRuntimePermission(pkg,
 * Manifest.permission.CAMERA) instrumentation.waitForIdleSync()
 *
 * var noPermissionShown = false
 *
 * composeTestRule.setContent { CameraView( noPermission = { Box(modifier =
 * Modifier.testTag("noPermissionBox")) { noPermissionShown = true } }, onImageCaptured = {},
 * onError = {}, requestPermissionAutomatically = false) }
 *
 * composeTestRule.waitForIdle()
 *
 * // Verify the no permission UI is rendered
 * composeTestRule.onNodeWithTag("noPermissionBox").assertExists() assertTrue("Expected noPermission
 * UI to be shown", noPermissionShown) } }
 */
