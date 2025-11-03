package com.github.se.studentconnect.ui.screen.home

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.calendar.EventCalendar
import com.github.se.studentconnect.ui.events.EventListScreen
import com.github.se.studentconnect.ui.events.formatDateHeader
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.utils.FilterBar
import com.github.se.studentconnect.ui.utils.Panel
import com.github.se.studentconnect.viewmodel.HomePageUiState
import com.github.se.studentconnect.viewmodel.HomePageViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

import java.util.Date
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: HomePageViewModel = viewModel(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsState()
  val favoriteEventIds by viewModel.favoriteEventIds.collectAsState()
  
  LaunchedEffect(Unit) { viewModel.refresh() }

  HomeScreen(
      navController,
      shouldOpenQRScanner,
      onQRScannerClosed,
      { e, i -> viewModel.updateSeenStories(e, i) },
      uiState,
      favoriteEventIds
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
    favotiteEventIds: Set<String>
) {
  var showNotifications by remember { mutableStateOf(false) }
  val sheetState =
      rememberModalBottomSheetState(
          initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
  val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  // Automatically open QR scanner if requested
  LaunchedEffect(shouldOpenQRScanner) {
    if (shouldOpenQRScanner && pagerState.currentPage != 0) {
      pagerState.animateScrollToPage(0)
    }
  }
  
  ModalBottomSheetLayout(
      modifier = Modifier.testTag("calendar_modal"),
      sheetState = sheetState,
      sheetContent = {
        EventCalendar(
            events = uiState.events,
            selectedDate = uiState.selectedDate,
            onDateSelected = { date -> viewModel.onDateSelected(date) })
      }) {
        Scaffold(
            modifier = Modifier.fillMaxSize().testTag("HomePage"),
            topBar = {
              if (pagerState.currentPage == 1) {
                HomeTopBar(
                    showNotifications = showNotifications,
                    onNotificationClick = { showNotifications = !showNotifications },
                    onDismiss = { showNotifications = false })
              }
            }) { paddingValues ->
              HorizontalPager(
                  state = pagerState,
                  modifier = Modifier.fillMaxSize().padding(paddingValues),
                  userScrollEnabled = true) { page ->
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
                            isActive = pagerState.currentPage == 0)
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
                                  uiState.subscribedEventsStories)
                              FilterBar(
                                  context = LocalContext.current,
                                  onCalendarClick = { viewModel.showCalendar() },
                                  onApplyFilters = viewModel::applyFilters)
                              EventListScreen(
                                  navController = navController,
                                  events = uiState.events,
                                  hasJoined = false,
                                  listState = listState,
                                  favoriteEventIds = favoriteEventIds,
                                  onFavoriteToggle = viewModel::toggleFavorite)
                            }
                          }
                        }
                      }
                    }
                  }
            }

        // Handle scroll to date functionality
        LaunchedEffect(uiState.scrollToDate) {
          uiState.scrollToDate?.let { targetDate ->
            scrollToDate(listState, uiState.events, targetDate)
            viewModel.clearScrollTarget()
          }
        }

        // Handle modal visibility
        LaunchedEffect(uiState.isCalendarVisible) {
          if (uiState.isCalendarVisible) {
            sheetState.show()
          } else {
            sheetState.hide()
          }
        }

        // Handle modal dismissal (when user taps outside or swipes down)
        LaunchedEffect(sheetState.isVisible) {
          if (!sheetState.isVisible && uiState.isCalendarVisible) {
            // Modal was dismissed by user interaction, update ViewModel state
            viewModel.hideCalendar()
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
/**
 * Scrolls to the specified date in the event list. Finds the date header and scrolls to it
 * smoothly.
 */
private suspend fun scrollToDate(
    listState: LazyListState,
    events: List<com.github.se.studentconnect.model.event.Event>,
    targetDate: Date
) {
  try {
    // Handle empty events list
    if (events.isEmpty()) {
      return
    }

    // Group events by date header to find the target section
    val groupedEvents = events.groupBy { event -> formatDateHeader(event.start) }

    // Find the target date header
    val targetDateHeader = formatDateHeader(com.google.firebase.Timestamp(targetDate))

    // Calculate the index to scroll to
    var currentIndex = 0
    for ((dateHeader, eventsOnDate) in groupedEvents) {
      if (dateHeader == targetDateHeader) {
        // Found the target date, scroll to it with bounds checking
        val maxIndex = listState.layoutInfo.totalItemsCount - 1
        val scrollIndex = minOf(currentIndex, maxIndex)
        listState.animateScrollToItem(scrollIndex)
        return
      }
      // Move to next section (header + events)
      currentIndex += 1 + eventsOnDate.size
    }

    // If date not found, scroll to top
    listState.animateScrollToItem(0)
  } catch (e: Exception) {
    // Handle any unexpected errors gracefully
    // In production, you might want to log this error
    listState.animateScrollToItem(0)
  }
}
