package com.github.se.studentconnect.ui.screen.camera

import android.net.Uri
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
  val coroutineScope = rememberCoroutineScope()
  var isStoryPreviewShowing by remember { mutableStateOf(false) }

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
              onStoryAccepted = onStoryAccepted,
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
    if (!isStoryPreviewShowing) {
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
