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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.calendar.EventCalendar
import com.github.se.studentconnect.ui.events.EventListScreen
import com.github.se.studentconnect.ui.events.formatDateHeader
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.utils.FilterBar
import com.github.se.studentconnect.ui.utils.Panel
import java.util.Date
import kotlinx.coroutines.launch

// UI Constants
private object HomeScreenConstants {
  const val STORY_SIZE_DP = 64
  const val STORY_BORDER_WIDTH_DP = 2
  const val STORY_PADDING_TOP_DP = 2
  const val STORIES_ROW_TOP_PADDING_DP = 12
  const val STORIES_ROW_BOTTOM_PADDING_DP = 4
  const val STORIES_ROW_HORIZONTAL_SPACING_DP = 16
  const val STORIES_ROW_HORIZONTAL_PADDING_DP = 8
  const val SEARCH_BAR_CORNER_RADIUS_DP = 24
  const val SEARCH_BAR_END_PADDING_DP = 8
  const val PAGER_SCANNER_PAGE = 0
  const val PAGER_HOME_PAGE = 1
}

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
      navController = navController,
      shouldOpenQRScanner = shouldOpenQRScanner,
      onQRScannerClosed = onQRScannerClosed,
      onClickStory = { e, i -> viewModel.updateSeenStories(e, i) },
      uiState = uiState,
      favoriteEventIds = favoriteEventIds,
      onDateSelected = { date -> viewModel.onDateSelected(date) },
      onCalendarClick = { viewModel.showCalendar() },
      onApplyFilters = viewModel::applyFilters,
      onFavoriteToggle = viewModel::toggleFavorite,
      onClearScrollTarget = { viewModel.clearScrollTarget() })
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {},
    onClickStory: (Event, Int) -> Unit = { _, _ -> },
    uiState: HomePageUiState = HomePageUiState(),
    favoriteEventIds: Set<String> = emptySet(),
    onDateSelected: (Date) -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onApplyFilters: (com.github.se.studentconnect.ui.utils.FilterData) -> Unit = {},
    onFavoriteToggle: (String) -> Unit = {},
    onClearScrollTarget: () -> Unit = {}
) {
  var showNotifications by remember { mutableStateOf(false) }
  val sheetState =
      rememberModalBottomSheetState(
          initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
  val pagerState =
      rememberPagerState(
          initialPage = HomeScreenConstants.PAGER_HOME_PAGE,
          pageCount = { HomeScreenConstants.PAGER_HOME_PAGE + 1 })
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  // Automatically open QR scanner if requested
  LaunchedEffect(shouldOpenQRScanner) {
    if (shouldOpenQRScanner && pagerState.currentPage != HomeScreenConstants.PAGER_SCANNER_PAGE) {
      pagerState.animateScrollToPage(HomeScreenConstants.PAGER_SCANNER_PAGE)
    }
  }

  ModalBottomSheetLayout(
      modifier = Modifier.testTag("calendar_modal"),
      sheetState = sheetState,
      sheetContent = {
        EventCalendar(
            events = uiState.events,
            selectedDate = uiState.selectedDate,
            onDateSelected = onDateSelected)
      }) {
        Scaffold(
            modifier = Modifier.fillMaxSize().testTag("HomePage"),
            topBar = {
              if (pagerState.currentPage == HomeScreenConstants.PAGER_HOME_PAGE) {
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
                      HomeScreenConstants.PAGER_SCANNER_PAGE -> {
                        // QR Scanner page
                        QrScannerScreen(
                            onBackClick = {
                              onQRScannerClosed()
                              coroutineScope.launch {
                                pagerState.animateScrollToPage(HomeScreenConstants.PAGER_HOME_PAGE)
                              }
                            },
                            onProfileDetected = { userId ->
                              // Navigate to visitor profile and return to home page
                              onQRScannerClosed()
                              navController.navigate(Route.visitorProfile(userId))
                              coroutineScope.launch {
                                pagerState.scrollToPage(HomeScreenConstants.PAGER_HOME_PAGE)
                              }
                            },
                            isActive =
                                pagerState.currentPage == HomeScreenConstants.PAGER_SCANNER_PAGE)
                      }
                      HomeScreenConstants.PAGER_HOME_PAGE -> {
                        // Home content page
                        Box(modifier = Modifier.fillMaxSize()) {
                          if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                          } else {
                            Column {
                              FilterBar(
                                  context = LocalContext.current,
                                  onCalendarClick = onCalendarClick,
                                  onApplyFilters = onApplyFilters)
                              StoriesRow(
                                  onAddStoryClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                  },
                                  onClick = onClickStory,
                                  stories = uiState.subscribedEventsStories)
                              EventListScreen(
                                  navController = navController,
                                  events = uiState.events,
                                  hasJoined = false,
                                  listState = listState,
                                  favoriteEventIds = favoriteEventIds,
                                  onFavoriteToggle = onFavoriteToggle)
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
            onClearScrollTarget()
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
            onCalendarClick()
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
            modifier =
                Modifier.fillMaxWidth()
                    .padding(end = HomeScreenConstants.SEARCH_BAR_END_PADDING_DP.dp),
            placeholder = { Text("Search for events...") },
            leadingIcon = {
              Icon(
                  painter = painterResource(id = R.drawable.ic_search),
                  contentDescription = "Search Icon",
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(HomeScreenConstants.SEARCH_BAR_CORNER_RADIUS_DP.dp),
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
fun StoryItem(
    name: String,
    avatarRes: Int,
    viewed: Boolean,
    onClick: () -> Unit,
    contentDescription: String = "Story for $name",
    testTag: String = ""
) {
  val borderColor =
      if (viewed) androidx.compose.material3.MaterialTheme.colorScheme.outline
      else androidx.compose.material3.MaterialTheme.colorScheme.primary
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
          Modifier.testTag(if (viewed) "story_viewed" else "story_unseen")
              .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)) {
        Image(
            painter = painterResource(avatarRes),
            contentDescription = contentDescription,
            modifier =
                Modifier.size(HomeScreenConstants.STORY_SIZE_DP.dp)
                    .clip(CircleShape)
                    .border(
                        width = HomeScreenConstants.STORY_BORDER_WIDTH_DP.dp,
                        color = borderColor,
                        shape = CircleShape)
                    .clickable(onClick = onClick))
        Text(
            text = name,
            modifier =
                Modifier.padding(top = HomeScreenConstants.STORY_PADDING_TOP_DP.dp)
                    .testTag("story_text_$name"),
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            maxLines = 1)
      }
}

@Composable
fun StoriesRow(
    onAddStoryClick: () -> Unit,
    onClick: (Event, Int) -> Unit,
    stories: Map<Event, Pair<Int, Int>>
) {
  // Filter stories to only show events with actual stories (totalStories > 0)
  val eventsWithStories =
      stories.filter { (_, storyCounts) ->
        val (_, totalStories) = storyCounts
        totalStories > 0
      }

  LazyRow(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  top = HomeScreenConstants.STORIES_ROW_TOP_PADDING_DP.dp,
                  bottom = HomeScreenConstants.STORIES_ROW_BOTTOM_PADDING_DP.dp)
              .testTag("stories_row"),
      horizontalArrangement =
          Arrangement.spacedBy(HomeScreenConstants.STORIES_ROW_HORIZONTAL_SPACING_DP.dp),
      contentPadding =
          PaddingValues(horizontal = HomeScreenConstants.STORIES_ROW_HORIZONTAL_PADDING_DP.dp)) {
        // Add Story Button (always first)
        item {
          val primaryColor = MaterialTheme.colorScheme.primary
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.testTag("addStoryButton")) {
                Box(
                    modifier =
                        Modifier.size(HomeScreenConstants.STORY_SIZE_DP.dp)
                            .drawBehind {
                              drawCircle(
                                  color = primaryColor,
                                  style =
                                      Stroke(
                                          width =
                                              HomeScreenConstants.STORY_BORDER_WIDTH_DP.dp.toPx(),
                                          pathEffect =
                                              PathEffect.dashPathEffect(
                                                  floatArrayOf(10f, 10f), 0f)))
                            }
                            .clickable { onAddStoryClick() },
                    contentAlignment = Alignment.Center) {
                      Icon(
                          imageVector = Icons.Default.Add,
                          contentDescription = "Add Story",
                          tint = primaryColor,
                          modifier = Modifier.size(32.dp))
                    }
                Spacer(modifier = Modifier.height(HomeScreenConstants.STORY_PADDING_TOP_DP.dp))
                Text(
                    text = "Add Story",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface)
              }
        }

        // Existing stories
        items(eventsWithStories.toList()) { (event, storyCounts) ->
          val (seenStories, totalStories) = storyCounts
          val allStoriesViewed = seenStories >= totalStories
          StoryItem(
              name = event.title,
              avatarRes = R.drawable.avatar_12, // Default avatar for now
              viewed = allStoriesViewed,
              onClick = { onClick(event, seenStories) },
              contentDescription = "Event Story",
              testTag = "story_item_${event.uid}")
        }
      }
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
