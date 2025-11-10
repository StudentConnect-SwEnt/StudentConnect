package com.github.se.studentconnect.ui.screen.camera

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.ui.camera.CameraView

@Composable
fun StoryCaptureScreen(
    onBackClick: () -> Unit,
    onCapture: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
  Box(modifier = modifier.fillMaxSize().background(Color.Black).testTag("story_capture_screen")) {
    if (isActive) {
      CameraView(
          modifier = Modifier.fillMaxSize(),
          enableImageCapture = true,
          onImageCaptured = {
            // Capture button does nothing for now
            // TODO: Implement story capture
          },
          onError = {
            // Ignore errors for now
          },
          noPermission = { PermissionRequired(onBackClick = onBackClick) })
    } else {
      InactiveCameraBackground()
    }

    // Back button
    IconButton(
        onClick = onBackClick,
        modifier =
            Modifier.align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
                .testTag("story_back_button")) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White)
        }

    // Instructions
    Column(
        modifier =
            Modifier.align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 200.dp)
                .testTag("story_instructions"),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = "Tap the button to take a photo",
              style = MaterialTheme.typography.titleMedium,
              textAlign = TextAlign.Center,
              color = Color.White)
        }
  }
}

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
