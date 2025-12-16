package com.github.se.studentconnect.ui.screen.camera

import android.content.Context
import android.net.Uri
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.story.StoryRepository
import com.github.se.studentconnect.model.story.StoryRepositoryProvider
import kotlinx.coroutines.launch

enum class CameraMode {
  STORY,
  QR_SCAN
}

/** Test tags for CameraModeSelectorScreen components. */
object CameraModeSelectorTestTags {
  const val UPLOAD_LOADING_OVERLAY = "upload_loading_overlay"
}

/** Parameters for story upload handling. */
internal data class StoryUploadParams(
    val mediaUri: Uri,
    val isVideo: Boolean,
    val selectedEvent: Event?,
    val isUploading: Boolean,
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val storyRepository: StoryRepository
)

/** Callbacks for story upload handling. */
internal data class StoryUploadCallbacks(
    val onUploadStateChange: (Boolean) -> Unit,
    val onStoryAccepted: (Uri, Boolean, Event?) -> Unit
)

/**
 * Handles the story upload logic. Extracted for testability.
 *
 * @return true if upload was initiated, false otherwise
 */
internal fun handleStoryUpload(
    params: StoryUploadParams,
    callbacks: StoryUploadCallbacks
): Boolean {
  if (params.selectedEvent == null) {
    Toast.makeText(
            params.context,
            params.context.getString(R.string.story_select_event),
            Toast.LENGTH_SHORT)
        .show()
    return false
  }

  if (params.isUploading) {
    Toast.makeText(
            params.context,
            params.context.getString(R.string.story_upload_in_progress),
            Toast.LENGTH_SHORT)
        .show()
    return false
  }

  val currentUserId = AuthenticationProvider.currentUser
  if (currentUserId.isEmpty()) {
    Toast.makeText(
            params.context,
            params.context.getString(R.string.story_login_required),
            Toast.LENGTH_SHORT)
        .show()
    return false
  }

  // Show message if offline (but still allow attempt)
  if (!com.github.se.studentconnect.utils.NetworkUtils.isNetworkAvailable(params.context)) {
    Toast.makeText(
            params.context,
            params.context.getString(R.string.offline_no_internet_try_later),
            Toast.LENGTH_LONG)
        .show()
    // Continue with upload attempt - it will fail naturally if offline
  }

  callbacks.onUploadStateChange(true)
  Toast.makeText(
          params.context, params.context.getString(R.string.story_uploading), Toast.LENGTH_SHORT)
      .show()

  params.lifecycleOwner.lifecycleScope.launch {
    try {
      val story =
          params.storyRepository.uploadStory(
              params.mediaUri, params.selectedEvent.uid, currentUserId, params.context)

      if (story != null) {
        Toast.makeText(
                params.context,
                params.context.getString(R.string.story_uploaded),
                Toast.LENGTH_SHORT)
            .show()
        callbacks.onStoryAccepted(params.mediaUri, params.isVideo, params.selectedEvent)
      } else {
        Toast.makeText(
                params.context,
                params.context.getString(R.string.story_upload_failed),
                Toast.LENGTH_LONG)
            .show()
      }
    } catch (e: Exception) {
      Toast.makeText(
              params.context,
              params.context.getString(R.string.story_upload_error, e.message),
              Toast.LENGTH_LONG)
          .show()
    } finally {
      callbacks.onUploadStateChange(false)
    }
  }

  return true
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
  val pagerState =
      rememberPagerState(initialPage = initialMode.ordinal, pageCount = { CameraMode.entries.size })
  // Use lifecycleScope to prevent cancellation on navigation for story upload
  val lifecycleOwner = LocalLifecycleOwner.current
  // Use rememberCoroutineScope for UI operations (page navigation)
  val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
  val context = LocalContext.current
  var isStoryPreviewShowing by remember { mutableStateOf(false) }
  var isUploading by remember { mutableStateOf(false) }

  // ViewModel for managing event selection state
  val storyRepository = StoryRepositoryProvider.repository
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
                handleStoryUpload(
                    params =
                        StoryUploadParams(
                            mediaUri = mediaUri,
                            isVideo = isVideo,
                            selectedEvent = selectedEvent,
                            isUploading = isUploading,
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            storyRepository = storyRepository),
                    callbacks =
                        StoryUploadCallbacks(
                            onUploadStateChange = { uploading -> isUploading = uploading },
                            onStoryAccepted = onStoryAccepted))
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
                  .testTag(CameraModeSelectorTestTags.UPLOAD_LOADING_OVERLAY),
          contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  CircularProgressIndicator(color = Color.White)
                  Text(
                      text = stringResource(R.string.story_uploading),
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
