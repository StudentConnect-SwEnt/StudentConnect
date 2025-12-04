package com.github.se.studentconnect.ui.screen.camera

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.story.StoryRepositoryProvider
import kotlinx.coroutines.launch

enum class CameraMode {
  STORY,
  QR_SCAN
}

/** Hosts the story camera and QR scanner pages with iOS-style swipe navigation. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CameraModeSelectorScreen(
    onBackClick: () -> Unit,
    onProfileDetected: (String) -> Unit,
    onStoryAccepted: (Uri, Boolean, Event?) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
    initialMode: CameraMode = CameraMode.QR_SCAN
) {
  val context = LocalContext.current
  val pagerState =
      rememberPagerState(initialPage = initialMode.ordinal, pageCount = { CameraMode.entries.size })
  // Use lifecycleScope to prevent cancellation on navigation for story upload
  val lifecycleOwner = LocalLifecycleOwner.current
  // Use rememberCoroutineScope for UI operations (page navigation)
  val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
  var isStoryPreviewShowing by remember { mutableStateOf(false) }
  var isUploading by remember { mutableStateOf(false) }

  // ViewModel for managing event selection state
  val storyRepository = StoryRepositoryProvider.getRepository(context)
  val viewModel: CameraModeSelectorViewModel =
      viewModel(factory = CameraModeSelectorViewModelFactory(storyRepository))
  val eventSelectionState by viewModel.eventSelectionState.collectAsState()

  // React to changes in initialMode
  LaunchedEffect(initialMode) {
    if (pagerState.currentPage != initialMode.ordinal) {
      pagerState.scrollToPage(initialMode.ordinal)
    }
  }

  Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
    // Horizontal pager for swipeable camera modes
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
      when (CameraMode.entries[page]) {
        CameraMode.STORY -> {
          StoryCaptureScreen(
              onBackClick = onBackClick,
              onStoryAccepted = { mediaUri, isVideo, selectedEvent ->
                // Upload story to Firestore
                if (selectedEvent != null && !isUploading) {
                  val currentUserId = AuthenticationProvider.currentUser
                  if (currentUserId.isNotEmpty()) {
                    isUploading = true
                    Toast.makeText(context, "Uploading story...", Toast.LENGTH_SHORT).show()

                    // Use lifecycleOwner.lifecycleScope to prevent cancellation on navigation
                    lifecycleOwner.lifecycleScope.launch {
                      try {
                        Log.d(
                            "CameraModeSelectorScreen",
                            "Starting story upload - Event: ${selectedEvent.uid}, User: $currentUserId, URI: $mediaUri")

                        val story =
                            storyRepository.uploadStory(mediaUri, selectedEvent.uid, currentUserId)

                        if (story != null) {
                          Log.d(
                              "CameraModeSelectorScreen",
                              "Story uploaded successfully: ${story.storyId}, mediaUrl: ${story.mediaUrl}")
                          Toast.makeText(context, "Story uploaded!", Toast.LENGTH_SHORT).show()
                          // Call the original callback ONLY AFTER successful upload
                          onStoryAccepted(mediaUri, isVideo, selectedEvent)
                        } else {
                          Log.e(
                              "CameraModeSelectorScreen",
                              "Failed to upload story: uploadStory returned null. Check Firebase permissions and media file.")
                          Toast.makeText(
                                  context,
                                  "Failed to upload story. Check connection and permissions.",
                                  Toast.LENGTH_LONG)
                              .show()
                        }
                      } catch (e: Exception) {
                        Log.e(
                            "CameraModeSelectorScreen",
                            "Exception during story upload: ${e.javaClass.simpleName} - ${e.message}",
                            e)
                        e.printStackTrace()
                        Toast.makeText(context, "Upload error: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                      } finally {
                        isUploading = false
                      }
                    }
                  } else {
                    Log.e("CameraModeSelectorScreen", "User not authenticated")
                    Toast.makeText(
                            context, "You must be logged in to upload stories", Toast.LENGTH_SHORT)
                        .show()
                  }
                } else if (isUploading) {
                  Toast.makeText(context, "Upload in progress...", Toast.LENGTH_SHORT).show()
                } else {
                  Log.w("CameraModeSelectorScreen", "No event selected for story")
                  Toast.makeText(
                          context, "Please select an event for your story", Toast.LENGTH_SHORT)
                      .show()
                }
              },
              eventSelectionState = eventSelectionState,
              onLoadEvents = { viewModel.loadJoinedEvents() },
              isActive = pagerState.currentPage == page,
              onPreviewStateChanged = { isPreviewShowing ->
                isStoryPreviewShowing = isPreviewShowing
              })
        }
        CameraMode.QR_SCAN -> {
          QrScannerScreen(
              onBackClick = onBackClick,
              onProfileDetected = onProfileDetected,
              isActive = pagerState.currentPage == page)
        }
      }
    }

    // Back button overlay
    IconButton(
        onClick = onBackClick,
        modifier =
            Modifier.align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
                .testTag("camera_mode_back_button")) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White)
        }

    // Bottom mode selector (like iPhone camera) - hide when story preview is showing
    if (!isStoryPreviewShowing && !isUploading) {
      Row(
          modifier =
              Modifier.align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .padding(bottom = 32.dp)
                  .testTag("camera_mode_selector"),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically) {
            CameraModeTab(
                text = "STORY",
                isSelected = pagerState.currentPage == CameraMode.STORY.ordinal,
                onClick = {
                  coroutineScope.launch { pagerState.animateScrollToPage(CameraMode.STORY.ordinal) }
                },
                modifier = Modifier.testTag("mode_story"))

            CameraModeTab(
                text = "QR SCAN",
                isSelected = pagerState.currentPage == CameraMode.QR_SCAN.ordinal,
                onClick = {
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(CameraMode.QR_SCAN.ordinal)
                  }
                },
                modifier = Modifier.testTag("mode_qr_scan"))
          }
    }

    // Upload loading overlay
    if (isUploading) {
      Box(
          modifier =
              Modifier.fillMaxSize()
                  .background(Color.Black.copy(alpha = 0.7f))
                  .testTag("upload_loading_overlay"),
          contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  CircularProgressIndicator(color = Color.White)
                  Text(
                      text = "Uploading story...",
                      color = Color.White,
                      style = MaterialTheme.typography.bodyLarge)
                }
          }
    }
  }
}

/** Renders a selectable tab label for the camera mode picker. */
@Composable
private fun CameraModeTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = if (isSelected) 16.sp else 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
      }
}
