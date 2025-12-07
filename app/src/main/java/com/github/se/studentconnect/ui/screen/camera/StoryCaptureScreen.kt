package com.github.se.studentconnect.ui.screen.camera

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.camera.CameraView
import com.github.se.studentconnect.ui.components.EventSelectionState

/**
 * Minimal story capture preview that toggles between live camera and inactive placeholder.
 *
 * @param onBackClick Callback when user wants to go back
 * @param onStoryAccepted Callback when user accepts the media with selected event
 * @param eventSelectionState State for loading joined events
 * @param onLoadEvents Callback to load user's joined events
 * @param modifier Modifier for the screen
 * @param isActive Whether the camera is active
 * @param onPreviewStateChanged Callback when preview state changes
 */
@Composable
fun StoryCaptureScreen(
    onBackClick: () -> Unit,
    onStoryAccepted: (Uri, Boolean, Event?) -> Unit = { _, _, _ -> },
    eventSelectionState: EventSelectionState = EventSelectionState.Success(emptyList()),
    onLoadEvents: () -> Unit = {},
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onPreviewStateChanged: (Boolean) -> Unit = {}
) {
  var storyCaptureMode by remember { mutableStateOf(StoryCaptureMode.PHOTO) }
  var capturedMediaUri by remember { mutableStateOf<Uri?>(null) }
  var showPreview by remember { mutableStateOf(false) }

  // Get screen dimensions to calculate aspect ratio for portrait mode
  val configuration = androidx.compose.ui.platform.LocalConfiguration.current
  val screenHeight = configuration.screenHeightDp
  val screenWidth = configuration.screenWidthDp

  // Notify parent when preview state changes
  LaunchedEffect(showPreview) { onPreviewStateChanged(showPreview) }

  Box(modifier = modifier.fillMaxSize().background(Color.Black).testTag("story_capture_screen")) {
    if (showPreview && capturedMediaUri != null) {
      // Show preview screen
      MediaPreviewScreen(
          mediaUri = capturedMediaUri!!,
          isVideo = storyCaptureMode == StoryCaptureMode.VIDEO,
          onAccept = { selectedEvent ->
            onStoryAccepted(
                capturedMediaUri!!, storyCaptureMode == StoryCaptureMode.VIDEO, selectedEvent)
            showPreview = false
            capturedMediaUri = null
          },
          onRetake = {
            // Go back to camera
            showPreview = false
            capturedMediaUri = null
          },
          eventSelectionConfig =
              EventSelectionConfig(state = eventSelectionState, onLoadEvents = onLoadEvents))
    } else if (isActive) {
      // Show camera view with full-screen configuration
      CameraView(
          modifier = Modifier.fillMaxSize(),
          enableImageCapture = storyCaptureMode == StoryCaptureMode.PHOTO,
          enableVideoCapture = storyCaptureMode == StoryCaptureMode.VIDEO,
          captureButton = { isRecording ->
            CaptureButtonPreview(selectedMode = storyCaptureMode, isRecording = isRecording)
          },
          onImageCaptured = { uri ->
            capturedMediaUri = uri
            showPreview = true
          },
          onVideoCaptured = { uri ->
            capturedMediaUri = uri
            showPreview = true
          },
          onError = { error -> Log.e("StoryCaptureScreen", "Camera error occurred", error) },
          noPermission = { PermissionRequired(onBackClick = onBackClick) },
          // Configure for full-screen story capture
          // Use 16:9 aspect ratio which will be captured in portrait orientation
          imageCaptureConfig = {
            // Use 16:9 for full-screen portrait capture (will be rotated to 9:16)
            setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
          })

      // Only show mode controls when not showing preview
      Column(
          modifier =
              Modifier.align(Alignment.BottomCenter)
                  .padding(horizontal = 32.dp, vertical = 160.dp)
                  .testTag("story_instructions"),
          horizontalAlignment = Alignment.CenterHorizontally) {
            StoryModeControls(
                selectedMode = storyCaptureMode,
                onModeSelected = { mode -> storyCaptureMode = mode },
                showCaptureButton = false)
          }
    } else {
      InactiveCameraBackground()
    }
  }
}

/** Explains that camera permissions are needed before launching the story capture flow. */
@Composable
private fun PermissionRequired(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
  Column(
      modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
        Text(
            text = "Camera permission is required to capture photos.",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp).testTag("story_permission"))
        IconButton(onClick = onBackClick, modifier = Modifier.padding(top = 24.dp)) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White)
        }
      }
}

/** Placeholder shown when story capture is not the active page in the camera pager. */
@Composable
private fun InactiveCameraBackground(modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.5f))
              .testTag("story_inactive"),
      contentAlignment = Alignment.Center) {
        Text(
            text = "Swipe to activate story camera",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp))
      }
}
