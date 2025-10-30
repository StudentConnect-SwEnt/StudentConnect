package com.github.se.studentconnect.ui.screens

import FilterBar
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.events.EventListScreen
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.ui.utils.Panel
import com.github.se.studentconnect.viewmodel.HomePageUiState
import com.github.se.studentconnect.viewmodel.HomePageViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: HomePageViewModel = viewModel(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(Unit) { viewModel.refresh() }

  HomeScreen(
      navController,
      shouldOpenQRScanner,
      onQRScannerClosed,
      { e, i -> viewModel.updateSeenStories(e, i) },
      uiState,
  )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {},
    onClickStory: (Event, Int) -> Unit = { e, i -> },
    uiState: HomePageUiState = HomePageUiState(),
) {
  var showNotifications by remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
  val coroutineScope = rememberCoroutineScope()

  // Automatically open QR scanner if requested
  LaunchedEffect(shouldOpenQRScanner) {
    if (shouldOpenQRScanner && pagerState.currentPage != 0) {
      pagerState.animateScrollToPage(0)
    }
  }

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("HomePage"),
      topBar = {
        if (pagerState.currentPage == 1) {
          HomeTopBar(
              showNotifications = showNotifications,
              onNotificationClick = { showNotifications = !showNotifications },
              onDismiss = { showNotifications = false },
          )
        }
      },
  ) { paddingValues ->
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        userScrollEnabled = true,
    ) { page ->
      when (page) {
        0 -> {
          // QR Scanner page
          QrScannerScreen(
              onBackClick = {
                onQRScannerClosed()
                coroutineScope.launch { pagerState.animateScrollToPage(1) }
              },
              onProfileDetected = { userId ->
                // Navigate to visitor profile and return to home page
                onQRScannerClosed()
                navController.navigate(Route.visitorProfile(userId))
                coroutineScope.launch { pagerState.scrollToPage(1) }
              },
              isActive = pagerState.currentPage == 0,
          )
        }
        1 -> {
          // Home content page
          Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
              CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
              Column {
                StoriesRow(
                    onClick = onClickStory,
                    uiState.subscribedEventsStories,
                )
                FilterBar(LocalContext.current)
                EventListScreen(navController = navController, events = uiState.events, false)
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(showNotifications: Boolean, onNotificationClick: () -> Unit, onDismiss: () -> Unit) {
  TopAppBar(
      title = {
        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
            placeholder = { Text("Search for events...") },
            leadingIcon = {
              Icon(
                  painter = painterResource(id = R.drawable.ic_search),
                  contentDescription = "Search Icon",
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
        )
      },
      actions = {
        Box {
          // Notification icon button
          IconButton(onClick = onNotificationClick) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
          }
          DropdownMenu(
              expanded = showNotifications,
              onDismissRequest = onDismiss,
              modifier =
                  Modifier.background(Color.Transparent)
                      .shadow(0.dp)
                      .testTag(ActivitiesScreenTestTags.INVITATIONS_POPOVER),
          ) {
            Panel<Invitation>(title = "Notifications")
          }
        }
      },
  )
}

@Composable
fun StoryItem(onClick: () -> Unit, viewed: Boolean) {
  val borderColor = remember { if (viewed) Color.Gray else Color.Magenta }
  Image(
      painter = painterResource(R.drawable.avatar_12),
      contentDescription = "Event Story",
      modifier =
          Modifier.size(LocalWindowInfo.current.containerSize.width.dp * 0.08f)
              .clip(CircleShape)
              .border(
                  width = LocalWindowInfo.current.containerSize.width.dp * 0.004f,
                  color = borderColor,
                  shape = CircleShape,
              )
              .clickable(onClick = onClick),
  )
}

@Composable
fun StoriesRow(onClick: (e: Event, i: Int) -> Unit, stories: Map<Event, Pair<Int, Int>>) {
  LazyRow(
      horizontalArrangement =
          Arrangement.spacedBy(LocalWindowInfo.current.containerSize.width.dp * 0.01f),
      contentPadding = PaddingValues(LocalWindowInfo.current.containerSize.width.dp * 0.01f)) {
        val storiesFilter = stories.filter { it.value.first != 0 }
        items(storiesFilter.entries.toList().filter { it.value.second < it.value.first }) { story ->
          StoryItem({ onClick(story.key, story.value.second) }, false)
        }
        items(storiesFilter.entries.toList().filter { it.value.second == it.value.first }) { story
          ->
          StoryItem({ onClick(story.key, story.value.second) }, true)
        }
      }
}

@Preview(showBackground = true)
@Composable
fun StoriesPreview() {
  AppTheme {
    Row {
      StoryItem({}, true)
      StoryItem({}, false)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
  val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Summer Festival",
          subtitle = "Best summer event",
          description = "Join us for an amazing summer festival.",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("music", "outdoor"),
      )

  val testEvent2 =
      Event.Public(
          uid = "event-2",
          title = "Tech Conference",
          subtitle = "Latest in tech",
          description = "Explore the latest technology trends.",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "SwissTech"),
          website = "https://example.com",
          ownerId = "owner2",
          isFlash = false,
          tags = listOf("tech", "networking"),
      )
  AppTheme {
    HomeScreen(
        onClickStory = { e, i -> },
        uiState =
            HomePageUiState(
                isLoading = false,
                events = listOf(testEvent1, testEvent2),
                subscribedEventsStories =
                    mapOf(
                        pairs =
                            arrayOf(Pair(testEvent1, Pair(1, 1)), Pair(testEvent2, Pair(2, 1))))),
    )
  }
}
