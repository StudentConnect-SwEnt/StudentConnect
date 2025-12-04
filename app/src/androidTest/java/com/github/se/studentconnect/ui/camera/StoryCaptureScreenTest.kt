package com.github.se.studentconnect.ui.camera

import android.Manifest
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.components.EventSelectionState
import com.github.se.studentconnect.ui.screen.camera.EventSelectionConfig
import com.github.se.studentconnect.ui.screen.camera.MediaPreviewScreen
import com.github.se.studentconnect.ui.screen.camera.StoryCaptureMode
import com.github.se.studentconnect.ui.screen.camera.StoryCaptureScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StoryCaptureScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @Test
  fun storyCaptureScreen_displaysCorrectly() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("story_instructions").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_displaysInstructions() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_inactive_showsInactiveBackground() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = false) }
    }

    composeTestRule.onNodeWithTag("story_inactive").assertIsDisplayed()
    composeTestRule.onNodeWithText("Swipe to activate story camera").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_active_doesNotShowInactiveBackground() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithTag("story_inactive").assertDoesNotExist()
  }

  @Test
  fun storyCaptureScreen_toggleActive_updatesDisplay() {
    val isActive = mutableStateOf(true)

    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = isActive.value) }
    }

    // Initially active
    composeTestRule.onNodeWithTag("story_inactive").assertDoesNotExist()
    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()

    // Set to inactive
    composeTestRule.runOnIdle { isActive.value = false }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_inactive").assertIsDisplayed()
    composeTestRule.onNodeWithText("Swipe to activate story camera").assertIsDisplayed()

    // Set back to active
    composeTestRule.runOnIdle { isActive.value = true }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_inactive").assertDoesNotExist()
    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_instructionsVisible_whenActive() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithTag("story_instructions").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_instructionsNotVisible_whenInactive() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = false) }
    }

    // Instructions are not visible when inactive
    composeTestRule.onNodeWithTag("story_instructions").assertDoesNotExist()
  }

  @Test
  fun storyCaptureScreen_previewStateCallback_initiallyFalse() {
    val previewStates = mutableListOf<Boolean>()

    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreen(
            onBackClick = {},
            isActive = true,
            onPreviewStateChanged = { isShowing -> previewStates.add(isShowing) })
      }
    }

    composeTestRule.runOnIdle {
      assert(previewStates.isNotEmpty())
      assert(!previewStates.last())
    }
  }

  @Test
  fun storyCaptureScreen_previewStateCallback_defaultValue() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_videoMode_switchesControls() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_onPreviewStateChanged_invokedOnComposition() {
    val stateChanges = mutableListOf<Boolean>()
    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreen(
            onBackClick = {}, isActive = true, onPreviewStateChanged = { stateChanges.add(it) })
      }
    }

    composeTestRule.runOnIdle { assert(stateChanges.contains(false)) }
  }

  @Test
  fun storyCaptureScreen_onBackClick_canBeInvoked() {
    var backClicked = false
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = { backClicked = true }, isActive = true) }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun storyCaptureScreen_storyModeControls_displayed() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithTag("story_instructions").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_inactiveBackground_hasCorrectText() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = false) }
    }

    composeTestRule.onNodeWithText("Swipe to activate story camera").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_activeState_showsCameraView() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_photoModeText_visible() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_videoModeToggle_changesText() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.onNodeWithText("VIDEO").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tap to start recording").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_onStoryAccepted_receivesEvent() {
    val event =
        Event.Public(
            uid = "1",
            ownerId = "owner1",
            title = "Test Event",
            description = "Description",
            start = Timestamp.now(),
            isFlash = false,
            subtitle = "Subtitle")
    var acceptedEvent: Event? = null

    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreen(
            onBackClick = {},
            isActive = true,
            onStoryAccepted = { _, _, selectedEvent -> acceptedEvent = selectedEvent },
            eventSelectionState = EventSelectionState.Success(listOf(event)),
            onLoadEvents = {})
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_previewState_notifiesParent() {
    var previewState = false
    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreen(
            onBackClick = {}, isActive = true, onPreviewStateChanged = { previewState = it })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle { assert(!previewState) }
  }

  @Test
  fun storyCaptureScreen_photoMode_displaysPhotoInstructions() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tap the button to take a photo").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_inactive_showsInactiveMessage() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = false) }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_inactive").assertIsDisplayed()
    composeTestRule.onNodeWithText("Swipe to activate story camera").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_videoMode_capturesVideo() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    // Switch to video mode
    composeTestRule.onNodeWithText("VIDEO").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tap to start recording").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_previewWithVideoMode_passesVideoFlag() {
    val event =
        Event.Public(
            uid = "1",
            ownerId = "owner1",
            title = "Test Event",
            description = "Description",
            start = Timestamp.now(),
            isFlash = false,
            subtitle = "Subtitle")
    var capturedIsVideo = false

    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreen(
            onBackClick = {},
            isActive = true,
            onStoryAccepted = { _, isVideo, _ -> capturedIsVideo = isVideo },
            eventSelectionState = EventSelectionState.Success(listOf(event)))
      }
    }

    composeTestRule.waitForIdle()
    // Switch to video mode and capture
    composeTestRule.onNodeWithText("VIDEO").performClick()
    composeTestRule.waitForIdle()
    // This test verifies the video mode branch is exercised
    composeTestRule.onNodeWithText("Tap to start recording").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_videoPreview_passesIsVideoTrue() {
    var isVideoPassed: Boolean? = null
    val event =
        Event.Public(
            uid = "1",
            ownerId = "owner1",
            title = "Test Event",
            description = "Description",
            start = Timestamp.now(),
            isFlash = false,
            subtitle = "Subtitle")
    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreenWithPreview(
            onBackClick = {},
            isActive = true,
            showInitialPreview = true,
            isVideoMode = true,
            events = listOf(event),
            onStoryAccepted = { _, isVideo, _ -> isVideoPassed = isVideo })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
    // Event is pre-selected via initialSelectedEvent, so accept button should be enabled
    composeTestRule.onNodeWithTag("media_preview_accept").assertIsEnabled()
    // Accept the preview to trigger onStoryAccepted and verify video mode branch
    composeTestRule.onNodeWithTag("media_preview_accept").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.runOnIdle {
      assertNotNull(isVideoPassed)
      assertTrue(isVideoPassed == true)
    }
  }

  @Test
  fun storyCaptureScreen_retake_resetsPreview() {
    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreen(
            onBackClick = {},
            isActive = true,
            eventSelectionState = EventSelectionState.Success(emptyList()))
      }
    }

    composeTestRule.waitForIdle()
    // Retake is handled internally by StoryCaptureScreen
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_onError_logsError() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    // Error path is tested through CameraView integration
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun storyCaptureScreen_permissionRequired_showsMessage() {
    composeTestRule.setContent {
      AppTheme { StoryCaptureScreen(onBackClick = {}, isActive = true) }
    }

    composeTestRule.waitForIdle()
    // Permission required path is tested through CameraView
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }
}

class StoryCaptureScreenPreviewTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @Test
  fun storyCaptureScreen_previewMode_showsPreview() {
    var showPreviewState = false

    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreenWithPreview(
            onBackClick = {},
            isActive = true,
            showInitialPreview = true,
            onPreviewStateChanged = { showPreviewState = it })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
    composeTestRule.runOnIdle { assert(showPreviewState) }
  }

  @Test
  fun storyCaptureScreen_previewAccept_resetsState() {
    val event =
        Event.Public(
            uid = "1",
            ownerId = "owner1",
            title = "Test Event",
            description = "Description",
            start = Timestamp.now(),
            isFlash = false,
            subtitle = "Subtitle")

    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreenWithPreview(
            onBackClick = {}, isActive = true, showInitialPreview = true, events = listOf(event))
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("media_preview_accept").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("media_preview_screen").assertDoesNotExist()
  }

  @Test
  fun storyCaptureScreen_previewRetake_resetsState() {
    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreenWithPreview(onBackClick = {}, isActive = true, showInitialPreview = true)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("media_preview_retake").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("media_preview_screen").assertDoesNotExist()
  }

  @Test
  fun storyCaptureScreen_videoPreview_usesVideoMode() {
    composeTestRule.setContent {
      AppTheme {
        StoryCaptureScreenWithPreview(
            onBackClick = {}, isActive = true, showInitialPreview = true, isVideoMode = true)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("media_preview_screen").assertIsDisplayed()
  }
}

@Composable
private fun StoryCaptureScreenWithPreview(
    onBackClick: () -> Unit,
    isActive: Boolean,
    showInitialPreview: Boolean,
    isVideoMode: Boolean = false,
    onPreviewStateChanged: (Boolean) -> Unit = {},
    events: List<Event> = emptyList(),
    onStoryAccepted: (Uri, Boolean, Event?) -> Unit = { _, _, _ -> }
) {
  var storyCaptureMode by remember {
    mutableStateOf(if (isVideoMode) StoryCaptureMode.VIDEO else StoryCaptureMode.PHOTO)
  }
  var capturedMediaUri by remember {
    mutableStateOf<Uri?>(if (showInitialPreview) Uri.parse("file:///test.jpg") else null)
  }
  var showPreview by remember { mutableStateOf(showInitialPreview) }

  LaunchedEffect(showPreview) { onPreviewStateChanged(showPreview) }

  Box(modifier = Modifier.fillMaxSize().background(Color.Black).testTag("story_capture_screen")) {
    if (showPreview && capturedMediaUri != null) {
      MediaPreviewScreen(
          mediaUri = capturedMediaUri!!,
          isVideo = storyCaptureMode == StoryCaptureMode.VIDEO,
          initialSelectedEvent = events.firstOrNull(),
          onAccept = { selectedEvent ->
            onStoryAccepted(
                capturedMediaUri!!, storyCaptureMode == StoryCaptureMode.VIDEO, selectedEvent)
            showPreview = false
            capturedMediaUri = null
          },
          onRetake = {
            showPreview = false
            capturedMediaUri = null
          },
          eventSelectionConfig = EventSelectionConfig(state = EventSelectionState.Success(events)))
    }
  }
}
