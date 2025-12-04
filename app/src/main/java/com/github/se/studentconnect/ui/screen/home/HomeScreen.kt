package com.github.se.studentconnect.ui.screen.home

import android.widget.Toast
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.story.StoryRepositoryProvider
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.github.se.studentconnect.ui.calendar.EventCalendar
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.camera.CameraMode
import com.github.se.studentconnect.ui.screen.camera.CameraModeSelectorScreen
import com.github.se.studentconnect.ui.utils.EventListScreen
import com.github.se.studentconnect.ui.utils.FavoritesConfig
import com.github.se.studentconnect.ui.utils.FilterBar
import com.github.se.studentconnect.ui.utils.FilterData
import com.github.se.studentconnect.ui.utils.HomeSearchBar
import com.github.se.studentconnect.ui.utils.OrganizationSuggestionsConfig
import com.github.se.studentconnect.ui.utils.Panel
import com.github.se.studentconnect.ui.utils.formatDateHeader
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import com.github.se.studentconnect.viewmodel.NotificationUiState
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import com.github.se.studentconnect.ui.utils.loadBitmapFromEvent
import com.github.se.studentconnect.ui.utils.loadBitmapFromUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// UI Constants
private object HomeScreenConstants {
  const val STORY_SIZE_DP = 64
  const val STORY_BORDER_WIDTH_DP = 3
  const val STORY_PADDING_TOP_DP = 4
  const val STORY_ITEM_WIDTH_DP = 80
  const val STORIES_ROW_TOP_PADDING_DP = 0
  const val STORIES_ROW_BOTTOM_PADDING_DP = 12
  const val STORIES_ROW_HORIZONTAL_SPACING_DP = 16
  const val STORIES_ROW_HORIZONTAL_PADDING_DP = 8
  const val STORY_VIDEO_SPACER_HEIGHT_DP = 8
  const val PAGER_SCANNER_PAGE = 0
  const val PAGER_HOME_PAGE = 1
}

/** Test tags for HomeScreen components. */
object HomeScreenTestTags {
  const val TAB_SELECTOR = "tab_selector"
  const val TAB_INDICATOR = "tab_indicator"
  const val TAB_FOR_YOU = "tab_for_you"
  const val TAB_EVENTS = "tab_events"
  const val TAB_DISCOVER = "tab_discover"
  const val STORY_VIEWER = "story_viewer"
  const val STORY_CLOSE_BUTTON = "story_close_button"
}

/** Sliding tab selector that displays three tabs: For You, Events, and Discover. */
@Composable
fun SlidingTabSelector(
    selectedTab: HomeTabMode,
    onTabSelected: (HomeTabMode) -> Unit,
    modifier: Modifier = Modifier
) {
  val tabs = HomeTabMode.entries
  val selectedIndex = tabs.indexOf(selectedTab)
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Calculate responsive padding based on screen width
  val horizontalPadding = (screenWidth * 0.04f).coerceIn(12.dp, 24.dp)
  val verticalPadding = (screenWidth * 0.02f).coerceIn(6.dp, 12.dp)

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = horizontalPadding, vertical = verticalPadding)
              .background(
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  shape = RoundedCornerShape(24.dp))
              .padding(4.dp)
              .testTag(HomeScreenTestTags.TAB_SELECTOR)) {
        TabIndicator(selectedIndex = selectedIndex)
        TabLabels(tabs = tabs, selectedTab = selectedTab, onTabSelected = onTabSelected)
      }
}

@Composable
private fun TabIndicator(selectedIndex: Int) {
  val indicatorOffsetFraction by
      animateFloatAsState(
          targetValue = selectedIndex / 3f,
          animationSpec = tween(durationMillis = 300),
          label = "tab_indicator_offset")

  BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(40.dp)) {
    val containerWidth = maxWidth
    Box(
        modifier =
            Modifier.width(containerWidth / 3f)
                .fillMaxHeight()
                .offset(x = containerWidth * indicatorOffsetFraction)
                .background(
                    color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp))
                .testTag(HomeScreenTestTags.TAB_INDICATOR))
  }
}

@Composable
private fun TabLabels(
    tabs: List<HomeTabMode>,
    selectedTab: HomeTabMode,
    onTabSelected: (HomeTabMode) -> Unit
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    tabs.forEach { tab ->
      TabItem(tab = tab, isSelected = tab == selectedTab, onTabSelected = onTabSelected)
    }
  }
}

@Composable
private fun RowScope.TabItem(
    tab: HomeTabMode,
    isSelected: Boolean,
    onTabSelected: (HomeTabMode) -> Unit
) {
  val tabTestTag = getTabTestTag(tab)
  val tabStringRes = getTabStringResource(tab)

  Box(
      modifier =
          Modifier.weight(1f).height(40.dp).clickable { onTabSelected(tab) }.testTag(tabTestTag),
      contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(tabStringRes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color =
                if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

private fun getTabTestTag(tab: HomeTabMode): String {
  return when (tab) {
    HomeTabMode.FOR_YOU -> HomeScreenTestTags.TAB_FOR_YOU
    HomeTabMode.EVENTS -> HomeScreenTestTags.TAB_EVENTS
    HomeTabMode.DISCOVER -> HomeScreenTestTags.TAB_DISCOVER
  }
}

private fun getTabStringResource(tab: HomeTabMode): Int {
  return when (tab) {
    HomeTabMode.FOR_YOU -> R.string.tab_for_you
    HomeTabMode.EVENTS -> R.string.tab_all_events
    HomeTabMode.DISCOVER -> R.string.tab_discover
  }
}

/** DI-friendly overload that wires default view models and exposes callback hooks. */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: HomePageViewModel = run {
      val context = LocalContext.current
      val storyRepository = StoryRepositoryProvider.repository
      viewModel {
        HomePageViewModel(
            context = context, locationRepository = null, storyRepository = storyRepository)
      }
    },
    notificationViewModel: NotificationViewModel = viewModel(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {},
    onCameraActiveChange: (Boolean) -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsState()
  val favoriteEventIds by viewModel.favoriteEventIds.collectAsState()

  LaunchedEffect(Unit) { viewModel.refresh() }

  HomeScreen(
      navController = navController,
      shouldOpenQRScanner = shouldOpenQRScanner,
      onQRScannerClosed = onQRScannerClosed,
      onCameraActiveChange = onCameraActiveChange,
      onClickStory = { _, _ -> /* TODO: update seen count for story */ },
      uiState = uiState,
      notificationViewModel = notificationViewModel,
      favoriteEventIds = favoriteEventIds,
      onDateSelected = { date -> viewModel.onDateSelected(date) },
      onCalendarClick = { viewModel.showCalendar() },
      onCalendarDismiss = { viewModel.hideCalendar() },
      onApplyFilters = viewModel::applyFilters,
      onFavoriteToggle = viewModel::toggleFavorite,
      onToggleFavoritesFilter = { viewModel.toggleFavoritesFilter() },
      onClearScrollTarget = { viewModel.clearScrollTarget() },
      onTabSelected = { tab -> viewModel.selectTab(tab) },
      onRefreshStories = { viewModel.refreshStories() })
}

/** Core Home screen implementation containing pager, filters, notifications, and stories. */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {},
    onCameraActiveChange: (Boolean) -> Unit = {},
    onClickStory: (Event, Int) -> Unit = { _, _ -> },
    uiState: HomePageUiState = HomePageUiState(),
    notificationViewModel: NotificationViewModel? = null,
    favoriteEventIds: Set<String> = emptySet(),
    onDateSelected: (Date) -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onCalendarDismiss: () -> Unit = {},
    onApplyFilters: (FilterData) -> Unit = {},
    onFavoriteToggle: (String) -> Unit = {},
    onToggleFavoritesFilter: () -> Unit = {},
    onClearScrollTarget: () -> Unit = {},
    onTabSelected: (HomeTabMode) -> Unit = {},
    onRefreshStories: () -> Unit = {}
) {
  var showNotifications by remember { mutableStateOf(false) }
  var cameraMode by remember { mutableStateOf(CameraMode.QR_SCAN) }
  var selectedStory by remember { mutableStateOf<Event?>(null) }
  var selectedStoryIndex by remember { mutableStateOf(0) }
  var showStoryViewer by remember { mutableStateOf(false) }
  val notificationUiState =
      notificationViewModel?.uiState?.collectAsState()?.value ?: NotificationUiState()
  val sheetState =
      rememberModalBottomSheetState(
          initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
  val pagerState =
      rememberPagerState(
          initialPage = HomeScreenConstants.PAGER_HOME_PAGE,
          pageCount = { HomeScreenConstants.PAGER_HOME_PAGE + 1 })
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  var previousSelectedDate by remember { mutableStateOf<Date?>(null) }
  val context = LocalContext.current

  // Separate scroll states for each tab to maintain independent scroll positions
  val forYouListState = rememberLazyListState()
  val allEventsListState = rememberLazyListState()
  val discoverListState = rememberLazyListState()

  // Automatically open QR scanner if requested
  LaunchedEffect(shouldOpenQRScanner) {
    if (shouldOpenQRScanner && pagerState.currentPage != HomeScreenConstants.PAGER_SCANNER_PAGE) {
      pagerState.animateScrollToPage(HomeScreenConstants.PAGER_SCANNER_PAGE)
    }
  }

  // Notify parent about camera active state
  LaunchedEffect(pagerState.currentPage) {
    onCameraActiveChange(pagerState.currentPage == HomeScreenConstants.PAGER_SCANNER_PAGE)
  }

  ModalBottomSheetLayout(
      modifier = Modifier.testTag("calendar_modal"),
      sheetState = sheetState,
      sheetContent = {
        EventCalendar(selectedDate = uiState.selectedDate, onDateSelected = onDateSelected)
      }) {
        Scaffold(
            modifier = Modifier.fillMaxSize().testTag("HomePage"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
              if (pagerState.currentPage == HomeScreenConstants.PAGER_HOME_PAGE) {
                HomeTopBar(
                    notificationState =
                        NotificationState(
                            showNotifications = showNotifications,
                            notifications = notificationUiState.notifications,
                            unreadCount = notificationUiState.unreadCount),
                    notificationCallbacks =
                        NotificationCallbacks(
                            onNotificationClick = { showNotifications = !showNotifications },
                            onDismiss = { showNotifications = false },
                            onNotificationRead = { notificationViewModel?.markAsRead(it) },
                            onNotificationDelete = {
                              notificationViewModel?.deleteNotification(it)
                            },
                            onFriendRequestAccept = { notificationId, fromUserId ->
                              coroutineScope.launch {
                                try {
                                  val userId = FirebaseAuth.getInstance().currentUser?.uid
                                  if (userId != null) {
                                    FriendsRepositoryProvider.repository.acceptFriendRequest(
                                        userId, fromUserId)
                                    notificationViewModel?.deleteNotification(notificationId)
                                  }
                                } catch (e: Exception) {
                                  android.util.Log.e(
                                      "HomeScreen", "Failed to accept friend request", e)
                                }
                              }
                            },
                            onFriendRequestReject = { notificationId, fromUserId ->
                              coroutineScope.launch {
                                try {
                                  val userId = FirebaseAuth.getInstance().currentUser?.uid
                                  if (userId != null) {
                                    FriendsRepositoryProvider.repository.rejectFriendRequest(
                                        userId, fromUserId)
                                    notificationViewModel?.deleteNotification(notificationId)
                                  }
                                } catch (e: Exception) {
                                  android.util.Log.e(
                                      "HomeScreen", "Failed to reject friend request", e)
                                }
                              }
                            },
                            onOrganizationInvitationAccept = { notificationId, organizationId ->
                              coroutineScope.launch {
                                try {
                                  val userId = FirebaseAuth.getInstance().currentUser?.uid
                                  if (userId != null) {
                                    OrganizationRepositoryProvider.repository
                                        .acceptMemberInvitation(organizationId, userId)
                                    notificationViewModel?.deleteNotification(notificationId)
                                  }
                                } catch (e: Exception) {
                                  android.util.Log.e(
                                      "HomeScreen", "Failed to accept organization invitation", e)
                                }
                              }
                            },
                            onOrganizationInvitationReject = { notificationId, organizationId ->
                              coroutineScope.launch {
                                try {
                                  val userId = FirebaseAuth.getInstance().currentUser?.uid
                                  if (userId != null) {
                                    OrganizationRepositoryProvider.repository
                                        .rejectMemberInvitation(organizationId, userId)
                                    notificationViewModel?.deleteNotification(notificationId)
                                  }
                                } catch (e: Exception) {
                                  android.util.Log.e(
                                      "HomeScreen", "Failed to reject organization invitation", e)
                                }
                              }
                            }),
                    navController = navController)
              }
            }) { paddingValues ->
              HorizontalPager(
                  state = pagerState,
                  modifier = Modifier.fillMaxSize().padding(paddingValues),
                  userScrollEnabled = true) { page ->
                    when (page) {
                      HomeScreenConstants.PAGER_SCANNER_PAGE -> {
                        // Camera Mode Selector page (Story/QR Scanner)
                        CameraModeSelectorScreen(
                            onBackClick = {
                              onQRScannerClosed()
                              cameraMode = CameraMode.QR_SCAN
                              coroutineScope.launch {
                                pagerState.animateScrollToPage(HomeScreenConstants.PAGER_HOME_PAGE)
                              }
                            },
                            onProfileDetected = { userId ->
                              // Navigate to visitor profile and return to home page
                              onQRScannerClosed()
                              cameraMode = CameraMode.QR_SCAN
                              navController.navigate(Route.visitorProfile(userId))
                              coroutineScope.launch {
                                pagerState.scrollToPage(HomeScreenConstants.PAGER_HOME_PAGE)
                              }
                            },
                            onStoryAccepted = { _, _, _ ->
                              // Story upload happens in CameraModeSelectorScreen
                              // After upload completes, refresh only the stories (not everything)
                              onQRScannerClosed()
                              cameraMode = CameraMode.QR_SCAN
                              coroutineScope.launch {
                                pagerState.scrollToPage(HomeScreenConstants.PAGER_HOME_PAGE)
                                // Refresh only stories to avoid showing loading spinner
                                onRefreshStories()
                              }
                            },
                            initialMode = cameraMode)
                      }
                      HomeScreenConstants.PAGER_HOME_PAGE -> {
                        // Home content page
                        Box(modifier = Modifier.fillMaxSize()) {
                          if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                          } else {
                            Column {
                              SlidingTabSelector(
                                  selectedTab = uiState.selectedTab, onTabSelected = onTabSelected)

                              // Tab content pager
                              val tabPagerState =
                                  rememberPagerState(
                                      initialPage = uiState.selectedTab.ordinal, pageCount = { 3 })

                              // Sync tab selection with pager
                              LaunchedEffect(uiState.selectedTab) {
                                tabPagerState.animateScrollToPage(uiState.selectedTab.ordinal)
                              }

                              // Sync pager with tab selection (when user swipes)
                              // Use settledPage instead of currentPage to avoid triggering during
                              // animation
                              LaunchedEffect(tabPagerState.settledPage) {
                                val newTab = HomeTabMode.entries[tabPagerState.settledPage]
                                if (newTab != uiState.selectedTab) {
                                  onTabSelected(newTab)
                                }
                              }

                              HorizontalPager(
                                  state = tabPagerState,
                                  modifier = Modifier.fillMaxSize(),
                                  userScrollEnabled = true) { _ ->
                                    // Use different scroll state for each tab
                                    val currentListState =
                                        when (page) {
                                          0 -> forYouListState // FOR_YOU
                                          1 -> allEventsListState // EVENTS
                                          2 -> discoverListState // DISCOVER
                                          else -> forYouListState
                                        }

                                    Column {
                                      FilterBar(
                                          context = LocalContext.current,
                                          onCalendarClick = onCalendarClick,
                                          onApplyFilters = onApplyFilters,
                                          showOnlyFavorites = uiState.showOnlyFavorites,
                                          onToggleFavorites = onToggleFavoritesFilter)
                                      EventListScreen(
                                          navController = navController,
                                          events = uiState.events, // Same events for now
                                          hasJoined = false,
                                          listState = currentListState,
                                          favoritesConfig =
                                              FavoritesConfig(
                                                  favoriteEventIds = favoriteEventIds,
                                                  onFavoriteToggle = onFavoriteToggle),
                                          organizationSuggestionsConfig =
                                              OrganizationSuggestionsConfig(
                                                  organizations = uiState.organizations,
                                                  onOrganizationClick = { orgId ->
                                                    navController.navigate(
                                                        Route.organizationProfile(orgId))
                                                  }),
                                          topContent = {
                                            StoriesRow(
                                                onAddStoryClick = {
                                                  cameraMode = CameraMode.STORY
                                                  coroutineScope.launch {
                                                    pagerState.animateScrollToPage(
                                                        HomeScreenConstants.PAGER_SCANNER_PAGE)
                                                  }
                                                },
                                                onClick = { event, seenStories ->
                                                  selectedStory = event
                                                  selectedStoryIndex = seenStories
                                                  showStoryViewer = true
                                                  onClickStory(event, seenStories)
                                                },
                                                stories = uiState.subscribedEventsStories,
                                                eventStories = uiState.eventStories)
                                          })
                                    }
                                  }
                            }
                          }
                        }
                      }
                    }
                  }
            }

        // Story Viewer Overlay
        selectedStory?.let { event ->
          val stories = uiState.eventStories[event.uid] ?: emptyList()
          val context = LocalContext.current
          val storyRepository = remember { StoryRepositoryProvider.repository }

          if (stories.isNotEmpty()) {
            StoryViewer(
                event = event,
                stories = stories,
                initialStoryIndex = selectedStoryIndex,
                isVisible = showStoryViewer,
                onDismiss = { showStoryViewer = false },
                onDeleteStory = { storyId ->
                  coroutineScope.launch {
                    try {
                      val currentUserId =
                          com.github.se.studentconnect.model.authentication.AuthenticationProvider
                              .currentUser
                      val success = storyRepository.deleteStory(storyId, currentUserId)

                      Toast.makeText(
                              context,
                              if (success) context.getString(R.string.story_deleted)
                              else context.getString(R.string.story_delete_failed),
                              Toast.LENGTH_SHORT)
                          .show()
                      if (success) onRefreshStories()
                    } catch (e: Exception) {
                      Toast.makeText(
                              context,
                              context.getString(R.string.story_error, e.message),
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                  }
                })
          }
        }

        // Handle scroll to date functionality
        LaunchedEffect(uiState.scrollToDate, uiState.selectedTab) {
          uiState.scrollToDate?.let { targetDate ->
            // Use the appropriate list state based on the selected tab
            val targetListState =
                when (uiState.selectedTab) {
                  HomeTabMode.FOR_YOU -> forYouListState
                  HomeTabMode.EVENTS -> allEventsListState
                  HomeTabMode.DISCOVER -> discoverListState
                }
            scrollToDate(
                listState = targetListState,
                events = uiState.events,
                targetDate = targetDate,
                hasTopContent = true, // Stories row is present as top content
                hasOrganizations = uiState.organizations.isNotEmpty(),
                organizationsCount = uiState.organizations.size)
            onClearScrollTarget()
          }
        }

        // Handle modal visibility
        LaunchedEffect(uiState.isCalendarVisible) {
          when {
            uiState.isCalendarVisible && !sheetState.isVisible -> {
              sheetState.show()
            }
            !uiState.isCalendarVisible && sheetState.isVisible -> {
              sheetState.hide()
            }
          }
        }

        // Handle modal dismissal (when user taps outside or swipes down)
        LaunchedEffect(sheetState.isVisible) {
          if (!sheetState.isVisible && uiState.isCalendarVisible) {
            onCalendarDismiss()
          }
        }

        // Check for empty events when date is selected
        LaunchedEffect(uiState.selectedDate, uiState.events) {
          val selectedDate = uiState.selectedDate
          // Only show snackbar on active date selection (not on initial load)
          if (selectedDate != null && previousSelectedDate != selectedDate) {
            // Filter events for the selected date
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            val eventsForDate =
                uiState.events.filter { event ->
                  val eventCalendar = Calendar.getInstance()
                  eventCalendar.time = event.start.toDate()

                  eventCalendar[Calendar.YEAR] == calendar[Calendar.YEAR] &&
                      eventCalendar[Calendar.MONTH] == calendar[Calendar.MONTH] &&
                      eventCalendar[Calendar.DAY_OF_MONTH] == calendar[Calendar.DAY_OF_MONTH]
                }

            // Show snackbar if no events found for the selected date
            if (eventsForDate.isEmpty()) {
              snackbarHostState.showSnackbar(context.getString(R.string.text_no_events_on_date))
              // Dismiss after 2.5 seconds (between 2-3 seconds)
              delay(2500)
              snackbarHostState.currentSnackbarData?.dismiss()
            }

            previousSelectedDate = selectedDate
          } else if (selectedDate == null) {
            previousSelectedDate = null
          }
        }
      }
}

/** Aggregates notification UI state that feeds badge and dropdown rendering. */
data class NotificationState(
    val showNotifications: Boolean,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0
)

/** Callbacks emitted from the notification dropdown interactions. */
data class NotificationCallbacks(
    val onNotificationClick: () -> Unit,
    val onDismiss: () -> Unit,
    val onNotificationRead: (String) -> Unit = {},
    val onNotificationDelete: (String) -> Unit = {},
    val onFriendRequestAccept: (String, String) -> Unit = { _, _ -> },
    val onFriendRequestReject: (String, String) -> Unit = { _, _ -> },
    val onOrganizationInvitationAccept: (String, String) -> Unit = { _, _ -> },
    val onOrganizationInvitationReject: (String, String) -> Unit = { _, _ -> }
)

/** Search bar plus notification icon row shown at the top of the Home page. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    notificationState: NotificationState,
    notificationCallbacks: NotificationCallbacks,
    navController: NavHostController = rememberNavController()
) {
  TopAppBar(
      title = {
        HomeSearchBar(
            modifier = Modifier.clickable(onClick = { navController.navigate(Route.SEARCH) }),
            query = "",
            onQueryChange = {},
            enabled = false, // only a dummy search bar to navigate to the search screen
        )
      },
      actions = {
        NotificationDropdown(
            notificationState = notificationState,
            notificationCallbacks = notificationCallbacks,
            navController = navController)
      })
}

/** Wraps the notification bell button and the dropdown that lists notifications. */
@Composable
private fun NotificationDropdown(
    notificationState: NotificationState,
    notificationCallbacks: NotificationCallbacks,
    navController: NavHostController
) {
  Box {
    IconButton(
        onClick = notificationCallbacks.onNotificationClick,
        modifier = Modifier.testTag("NotificationButton")) {
          NotificationBadge(unreadCount = notificationState.unreadCount)
        }
    DropdownMenu(
        expanded = notificationState.showNotifications,
        onDismissRequest = notificationCallbacks.onDismiss,
        modifier =
            Modifier.background(Color.Transparent)
                .shadow(0.dp)
                .testTag(ActivitiesScreenTestTags.INVITATIONS_POPOVER)) {
          Panel<Notification>(
              items = notificationState.notifications,
              title = stringResource(R.string.title_notifications),
              itemContent = { notification ->
                NotificationItem(
                    notification = notification,
                    onRead = { notificationCallbacks.onNotificationRead(notification.id) },
                    onDelete = { notificationCallbacks.onNotificationDelete(notification.id) },
                    onAccept =
                        getAcceptCallback(
                            notification,
                            notificationCallbacks.onFriendRequestAccept,
                            notificationCallbacks.onOrganizationInvitationAccept),
                    onReject =
                        getRejectCallback(
                            notification,
                            notificationCallbacks.onFriendRequestReject,
                            notificationCallbacks.onOrganizationInvitationReject),
                    onClick = {
                      handleNotificationClick(
                          notification,
                          navController,
                          notificationCallbacks.onNotificationRead,
                          notificationCallbacks.onDismiss)
                    })
              })
        }
  }
}

@Composable
private fun NotificationBadge(unreadCount: Int) {
  BadgedBox(
      badge = {
        if (unreadCount > 0) {
          Badge { Text(unreadCount.toString()) }
        }
      }) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = stringResource(R.string.content_description_notifications))
      }
}

/**
 * Banner notification that appears at the top of the screen
 *
 * @param notification The notification to display, or null if no notification should be shown
 * @param onDismiss Callback invoked when the banner is dismissed (auto or manual)
 * @param onClick Callback invoked when the user clicks on the banner
 */
@Composable
fun NotificationBanner(notification: Notification?, onDismiss: () -> Unit, onClick: () -> Unit) {
  var visible by remember(notification) { mutableStateOf(notification != null) }

  // Auto-dismiss after 4 seconds
  LaunchedEffect(notification) {
    if (notification != null) {
      visible = true
      delay(4000)
      visible = false
      delay(300) // Wait for animation to complete
      onDismiss()
    }
  }

  AnimatedVisibility(
      visible = visible && notification != null,
      enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
      modifier =
          Modifier.fillMaxWidth()
              .zIndex(999f)
              .testTag(com.github.se.studentconnect.resources.C.Tag.notification_banner)) {
        notification?.let {
          NotificationBannerCard(
              notification = it, onDismiss = { visible = false }, onClick = onClick)
        }
      }
}

@Composable
private fun NotificationBannerCard(
    notification: Notification,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .clickable {
                onDismiss()
                onClick()
              }
              .testTag("NotificationBanner_${notification.id}"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              NotificationBannerContent(notification = notification)
              NotificationBannerDismissButton(onDismiss = onDismiss)
            }
      }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NotificationBannerContent(
    notification: Notification
) {
  val message =
      when (notification) {
        is Notification.FriendRequest -> notification.getMessage()
        is Notification.EventStarting -> notification.getMessage()
        is Notification.EventInvitation -> notification.getMessage()
        is Notification.OrganizationMemberInvitation -> notification.getMessage()
      }

  Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NotificationIcon(notification = notification)
        Column {
          Text(
              text = getNotificationBannerTitle(notification),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text(
              text = message,
              style = MaterialTheme.typography.bodySmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
      }
}

@Composable
private fun NotificationBannerDismissButton(onDismiss: () -> Unit) {
  IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = stringResource(R.string.notification_banner_dismiss),
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onPrimaryContainer)
  }
}

@Composable
private fun getNotificationBannerTitle(notification: Notification): String {
  return when (notification) {
    is Notification.FriendRequest ->
        stringResource(R.string.notification_banner_title_friend_request)
    is Notification.EventStarting ->
        stringResource(R.string.notification_banner_title_event_starting)
    is Notification.EventInvitation ->
        stringResource(R.string.notification_banner_title_event_invitation)
    is Notification.OrganizationMemberInvitation ->
        stringResource(R.string.notification_banner_title_organization_invitation)
  }
}

private fun getAcceptCallback(
    notification: Notification,
    onFriendRequestAccept: (String, String) -> Unit,
    onOrganizationInvitationAccept: (String, String) -> Unit
): (() -> Unit)? {
  return when (notification) {
    is Notification.FriendRequest -> {
      { onFriendRequestAccept(notification.id, notification.fromUserId) }
    }
    is Notification.OrganizationMemberInvitation -> {
      { onOrganizationInvitationAccept(notification.id, notification.organizationId) }
    }
    else -> null
  }
}

private fun getRejectCallback(
    notification: Notification,
    onFriendRequestReject: (String, String) -> Unit,
    onOrganizationInvitationReject: (String, String) -> Unit
): (() -> Unit)? {
  return when (notification) {
    is Notification.FriendRequest -> {
      { onFriendRequestReject(notification.id, notification.fromUserId) }
    }
    is Notification.OrganizationMemberInvitation -> {
      { onOrganizationInvitationReject(notification.id, notification.organizationId) }
    }
    else -> null
  }
}

/**
 * Handles notification click by navigating to the appropriate screen based on notification type.
 *
 * @param notification The notification that was clicked
 * @param navController Navigation controller for screen navigation
 * @param onNotificationRead Callback to mark the notification as read
 * @param onDismiss Callback to dismiss the notification dropdown
 */
fun handleNotificationClick(
    notification: Notification,
    navController: NavHostController,
    onNotificationRead: (String) -> Unit,
    onDismiss: () -> Unit
) {
  when (notification) {
    is Notification.FriendRequest -> {
      navController.navigate(Route.visitorProfile(notification.fromUserId))
    }
    is Notification.EventStarting -> {
      navController.navigate("eventView/${notification.eventId}/true")
    }
    is Notification.EventInvitation -> {
      navController.navigate("eventView/${notification.eventId}/true")
    }
    is Notification.OrganizationMemberInvitation -> {
      navController.navigate("organizationProfile/${notification.organizationId}")
    }
  }
  onNotificationRead(notification.id)
  onDismiss()
}

/** Single notification card with optional friend request controls. */
@Composable
fun NotificationItem(
    notification: Notification,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onClick: () -> Unit
) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag("NotificationItem_${notification.id}"),
      colors =
          CardDefaults.cardColors(containerColor = getNotificationBackgroundColor(notification))) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
          NotificationContent(notification = notification, onClick = onClick, onDelete = onDelete)

          if (shouldShowActionButtons(notification, onAccept, onReject)) {
            FriendRequestActions(
                notificationId = notification.id,
                onAccept = onAccept!!,
                onReject = onReject!!,
                onRead = onRead)
          }
        }
      }
}

@Composable
private fun getNotificationBackgroundColor(notification: Notification): Color {
  return when {
    notification is Notification.EventInvitation && !notification.isRead ->
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    notification is Notification.EventInvitation && notification.isRead ->
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
    !notification.isRead -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else -> MaterialTheme.colorScheme.surface
  }
}

@Composable
private fun NotificationContent(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.weight(1f).clickable(role = Role.Button) { onClick() },
            verticalAlignment = Alignment.CenterVertically) {
              NotificationIcon(notification = notification)
              Spacer(modifier = Modifier.width(12.dp))
              NotificationMessage(notification = notification, modifier = Modifier.weight(1f))
            }
        DeleteButton(notificationId = notification.id, onDelete = onDelete)
      }
}

@Composable
private fun NotificationIcon(notification: Notification) {
  val icon =
      when (notification) {
        is Notification.FriendRequest -> Icons.Default.Person
        is Notification.EventStarting -> Icons.Default.Event
      }
      val imageBitmap = user?.let { loadBitmapFromUser(context, user!!) }
      if (imageBitmap != null) {
        Image(
            imageBitmap,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
      } else {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary)
      }
    }
    is Notification.EventStarting -> {
      var event: Event? = null
      LaunchedEffect(event) {
        event = EventRepositoryProvider.repository.getEvent(notification.eventId)
      }
      val imageBitmap = event?.let { loadBitmapFromEvent(context, event) }
      if (imageBitmap != null) {
        Image(
            imageBitmap,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
      } else {
        Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary)
      }
    }
      is Notification.EventInvitation -> Icons.Default.Email
      is Notification.OrganizationMemberInvitation -> Icons.Default.Group
  }

    Icon(
          imageVector = icon,
    contentDescription = null,
    modifier = Modifier.size(24.dp),
    tint = MaterialTheme.colorScheme.primary)
}

@Composable
private fun NotificationMessage(notification: Notification, modifier: Modifier = Modifier) {
  val message =
      when (notification) {
        is Notification.FriendRequest -> notification.getMessage()
        is Notification.EventStarting -> notification.getMessage()
        is Notification.EventInvitation -> notification.getMessage()
        is Notification.OrganizationMemberInvitation -> notification.getMessage()
      }
  val fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal

  // Get username and time info
  val (username, timeAgo) = getUsernameAndTime(notification)

  Column(modifier = modifier) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag("NotificationMessage_${notification.id}"))

    Spacer(modifier = Modifier.height(4.dp))

    // Display time and username tags
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = timeAgo,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
              text = "•",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
              text = username,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
  }
}

private fun getUsernameAndTime(notification: Notification): Pair<String, String> {
  val username =
      when (notification) {
        is Notification.FriendRequest -> notification.fromUserName
        is Notification.EventStarting -> notification.eventTitle
        is Notification.EventInvitation -> notification.invitedByName
        is Notification.OrganizationMemberInvitation -> notification.invitedByName
      }

  val timeAgo =
      notification.timestamp?.let {
        val now = System.currentTimeMillis()
        val notificationTime = it.toDate().time
        val diffMillis = now - notificationTime

        when {
          diffMillis < 60_000 -> "just now"
          diffMillis < 3600_000 -> "${diffMillis / 60_000}m ago"
          diffMillis < 86400_000 -> "${diffMillis / 3600_000}h ago"
          else -> "${diffMillis / 86400_000}d ago"
        }
      } ?: "just now"

  return Pair(username, timeAgo)
}

@Composable
private fun DeleteButton(notificationId: String, onDelete: () -> Unit) {
  IconButton(
      onClick = { onDelete() },
      modifier = Modifier.size(24.dp).testTag("DeleteNotificationButton_$notificationId")) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.content_description_delete),
            modifier = Modifier.size(16.dp))
      }
}

private fun shouldShowActionButtons(
    notification: Notification,
    onAccept: (() -> Unit)?,
    onReject: (() -> Unit)?
): Boolean {
  return (notification is Notification.FriendRequest ||
      notification is Notification.OrganizationMemberInvitation) &&
      onAccept != null &&
      onReject != null
}

@Composable
private fun FriendRequestActions(
    notificationId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onRead: () -> Unit
) {
  Spacer(modifier = Modifier.height(8.dp))
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Button(
        onClick = {
          onAccept()
          onRead()
        },
        modifier =
            Modifier.weight(1f)
                .testTag(
                    com.github.se.studentconnect.resources.C.Tag.getAcceptNotificationButtonTag(
                        notificationId)),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
          Text(stringResource(R.string.button_accept))
        }
    Button(
        onClick = {
          onReject()
          onRead()
        },
        modifier =
            Modifier.weight(1f)
                .testTag(
                    com.github.se.studentconnect.resources.C.Tag.getRejectNotificationButtonTag(
                        notificationId)),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
          Text(stringResource(R.string.button_reject))
        }
  }
}

/** Renders a single story bubble with seen/unseen styling. */
@Composable
fun StoryItem(
    name: String,
    avatarRes: Int? = null,
    avatarUrl: String? = null,
    viewed: Boolean,
    onClick: () -> Unit,
    contentDescription: String? = null,
    testTag: String = ""
) {
  val defaultContentDescription = stringResource(R.string.content_description_story_for, name)
  val finalContentDescription = contentDescription ?: defaultContentDescription
  val borderColor =
      if (viewed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary

  // Download profile picture using MediaRepository (same as profile screen)
  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, avatarUrl, repository) {
        value =
            avatarUrl?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure { // just do nothing
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  Box(modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(if (viewed) "story_viewed" else "story_unseen")) {
          Box(
              modifier =
                  Modifier.size(HomeScreenConstants.STORY_SIZE_DP.dp)
                      .clip(CircleShape)
                      .border(
                          width = HomeScreenConstants.STORY_BORDER_WIDTH_DP.dp,
                          color = borderColor,
                          shape = CircleShape)
                      .clickable(onClick = onClick),
              contentAlignment = Alignment.Center) {
                when {
                  imageBitmap != null -> {
                    // Show downloaded profile picture
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = finalContentDescription,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop)
                  }
                  avatarRes != null -> {
                    // Show local drawable
                    Image(
                        painter = painterResource(avatarRes),
                        contentDescription = finalContentDescription,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop)
                  }
                  else -> {
                    // Show default avatar with initial
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center) {
                          Text(
                              text = name.take(1).uppercase(),
                              style = MaterialTheme.typography.titleLarge,
                              color = MaterialTheme.colorScheme.primary)
                        }
                  }
                }
              }
          Text(
              text = name,
              modifier =
                  Modifier.padding(top = HomeScreenConstants.STORY_PADDING_TOP_DP.dp)
                      .width(HomeScreenConstants.STORY_ITEM_WIDTH_DP.dp)
                      .testTag("story_text_$name"),
              style = MaterialTheme.typography.bodySmall,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              textAlign = TextAlign.Center)
        }
  }
}

/** Horizontal list of story bubbles with a leading "Add Story" affordance. */
@Composable
fun StoriesRow(
    onAddStoryClick: () -> Unit,
    onClick: (Event, Int) -> Unit,
    stories: Map<Event, Pair<Int, Int>>,
    eventStories: Map<String, List<StoryWithUser>> = emptyMap()
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
          PaddingValues(horizontal = HomeScreenConstants.STORIES_ROW_HORIZONTAL_PADDING_DP.dp),
      userScrollEnabled = true) {
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
                          contentDescription =
                              stringResource(R.string.content_description_add_story),
                          tint = primaryColor,
                          modifier = Modifier.size(32.dp))
                    }
                Spacer(modifier = Modifier.height(HomeScreenConstants.STORY_PADDING_TOP_DP.dp))
                Text(
                    text = stringResource(R.string.content_description_add_story),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface)
              }
        }

        // Existing stories - display username of first story uploader
        items(eventsWithStories.toList()) { (event, storyCounts) ->
          val (seenStories, totalStories) = storyCounts
          val allStoriesViewed = seenStories >= totalStories

          // Get the first story's user info for display
          val firstStory = eventStories[event.uid]?.firstOrNull()
          val profilePictureUrl = firstStory?.profilePictureUrl

          StoryItem(
              name = event.title,
              avatarUrl = profilePictureUrl,
              viewed = allStoriesViewed,
              onClick = { onClick(event, seenStories) },
              contentDescription = stringResource(R.string.content_description_event_story),
              testTag = "story_item_${event.uid}")
        }
      }
}

@Composable
private fun StoryMediaContent(currentStory: StoryWithUser) {
  when (currentStory.story.mediaType) {
    com.github.se.studentconnect.model.story.MediaType.IMAGE -> {
      StoryImageContent(currentStory)
    }
    com.github.se.studentconnect.model.story.MediaType.VIDEO -> {
      StoryVideoPlaceholder()
    }
  }
}

@Composable
private fun StoryImageContent(currentStory: StoryWithUser) {
  androidx.compose.foundation.layout.BoxWithConstraints(
      modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(currentStory.story.mediaUrl)
                    .crossfade(true)
                    .build(),
            contentDescription =
                stringResource(R.string.story_image_description, currentStory.username),
            modifier =
                Modifier.width(screenHeight).height(screenWidth).graphicsLayer { rotationZ = 90f },
            contentScale = ContentScale.Crop,
            onError = {
              // no image loaded
            })
      }
}

@Composable
private fun StoryVideoPlaceholder() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Text(
              text = stringResource(R.string.story_video),
              color = Color.White,
              style = MaterialTheme.typography.headlineSmall)
          Spacer(modifier = Modifier.height(HomeScreenConstants.STORY_VIDEO_SPACER_HEIGHT_DP.dp))
          Text(
              text = stringResource(R.string.story_video_coming_soon),
              color = Color.White.copy(alpha = 0.7f),
              style = MaterialTheme.typography.bodyMedium)
        }
  }
}

@Composable
private fun StoryProgressIndicators(stories: List<StoryWithUser>, currentStoryIndex: Int) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        stories.forEachIndexed { index, _ ->
          Box(
              modifier =
                  Modifier.weight(1f)
                      .height(2.dp)
                      .background(
                          if (index <= currentStoryIndex) Color.White
                          else Color.White.copy(alpha = 0.3f),
                          shape = RoundedCornerShape(1.dp)))
        }
      }
}

@Composable
private fun StoryUserHeader(
    currentStory: StoryWithUser,
    eventTitle: String,
    avatarBitmap: ImageBitmap?
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp),
      verticalAlignment = Alignment.CenterVertically) {
        // User avatar using downloaded bitmap
        Box(
            modifier =
                Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center) {
              if (avatarBitmap != null) {
                Image(
                    bitmap = avatarBitmap,
                    contentDescription = "Profile picture of ${currentStory.username}",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop)
              } else {
                // Show initial as fallback
                Text(
                    text = currentStory.username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold)
              }
            }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
              text = currentStory.username,
              color = Color.White,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold)
          Text(
              text = eventTitle,
              color = Color.White.copy(alpha = 0.7f),
              style = MaterialTheme.typography.bodySmall)
        }
      }
}

@Composable
private fun StoryDeleteDialog(
    showDeleteConfirmation: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
  if (showDeleteConfirmation) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.story_delete_title)) },
        text = { Text(stringResource(R.string.story_delete_message)) },
        confirmButton = {
          Button(
              onClick = onConfirmDelete,
              colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text(stringResource(R.string.story_delete_button))
              }
        },
        dismissButton = {
          Button(
              onClick = onDismiss,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(stringResource(R.string.story_cancel_button))
              }
        })
  }
}

@Composable
private fun StoryViewerContent(
    stories: List<StoryWithUser>,
    currentStoryIndex: Int,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    onDismiss: () -> Unit,
    event: Event,
    avatarBitmap: ImageBitmap?,
    currentUserId: String,
    onShowDeleteConfirmation: () -> Unit
) {
  val currentStory = stories[currentStoryIndex]

  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(Color.Black)
              .pointerInput(stories.size, currentStoryIndex) {
                detectTapGestures(
                    onTap = { offset ->
                      val screenWidth = size.width
                      if (offset.x < screenWidth / 2) {
                        onNavigatePrevious()
                      } else {
                        onNavigateNext()
                      }
                    })
              }
              .testTag(HomeScreenTestTags.STORY_VIEWER)) {
        StoryMediaContent(currentStory)

        Box(modifier = Modifier.align(Alignment.TopCenter)) {
          StoryProgressIndicators(stories, currentStoryIndex)
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
          StoryUserHeader(currentStory, event.title, avatarBitmap)
        }

        IconButton(
            onClick = onDismiss,
            modifier =
                Modifier.align(Alignment.TopEnd)
                    .padding(16.dp)
                    .testTag(HomeScreenTestTags.STORY_CLOSE_BUTTON)) {
              Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = stringResource(R.string.content_description_close_story),
                  tint = Color.White)
            }

        if (currentStory.userId == currentUserId) {
          IconButton(
              onClick = onShowDeleteConfirmation,
              modifier =
                  Modifier.align(Alignment.BottomStart)
                      .padding(16.dp)
                      .testTag("story_delete_button")) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete story",
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp))
              }
        }
      }
}

@Composable
fun StoryViewer(
    event: Event,
    stories: List<StoryWithUser>,
    initialStoryIndex: Int = 0,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDeleteStory: (String) -> Unit = {}
) {
  var currentStoryIndex by
      remember(event.uid) { mutableStateOf(initialStoryIndex.coerceIn(0, stories.size - 1)) }
  val context = LocalContext.current
  val currentUserId =
      com.github.se.studentconnect.model.authentication.AuthenticationProvider.currentUser
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  // Get current story's profile picture URL
  val currentProfilePictureUrl =
      if (stories.isNotEmpty()) stories[currentStoryIndex].profilePictureUrl else null

  // Download profile picture using MediaRepository (same as profile screen)
  val repository = MediaRepositoryProvider.repository
  val avatarBitmap by
      produceState<ImageBitmap?>(initialValue = null, currentProfilePictureUrl, repository) {
        value =
            currentProfilePictureUrl?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure { // just do nothing
                  }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }

  AnimatedVisibility(
      visible = isVisible,
      enter =
          fadeIn(animationSpec = tween(300)) +
              scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
      exit =
          fadeOut(animationSpec = tween(200)) +
              scaleOut(targetScale = 0.9f, animationSpec = tween(200)),
      modifier = Modifier.fillMaxSize().zIndex(1000f)) {
        if (stories.isEmpty()) {
          Box(
              modifier =
                  Modifier.fillMaxSize()
                      .background(Color.Black)
                      .clickable { onDismiss() }
                      .testTag(HomeScreenTestTags.STORY_VIEWER)) {
                Text(
                    text = stringResource(R.string.story_no_stories),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center))
              }
        } else {
          StoryViewerContent(
              stories = stories,
              currentStoryIndex = currentStoryIndex,
              onNavigatePrevious = {
                if (currentStoryIndex > 0) {
                  currentStoryIndex--
                } else {
                  onDismiss()
                }
              },
              onNavigateNext = {
                if (currentStoryIndex < stories.size - 1) {
                  currentStoryIndex++
                } else {
                  onDismiss()
                }
              },
              onDismiss = onDismiss,
              event = event,
              avatarBitmap = avatarBitmap,
              currentUserId = currentUserId,
              onShowDeleteConfirmation = { showDeleteConfirmation = true })

          StoryDeleteDialog(
              showDeleteConfirmation = showDeleteConfirmation,
              onDismiss = { showDeleteConfirmation = false },
              onConfirmDelete = {
                showDeleteConfirmation = false
                val storyToDelete = stories[currentStoryIndex]
                onDeleteStory(storyToDelete.story.storyId)
                if (stories.size == 1) {
                  onDismiss()
                } else if (currentStoryIndex >= stories.size - 1) {
                  currentStoryIndex = (currentStoryIndex - 1).coerceAtLeast(0)
                }
              })
        }
      }
}

/**
 * Builds an index map of date headers in the event list. This mirrors the structure of
 * EventListScreen's LazyColumn to ensure accurate index calculation regardless of dynamic content.
 *
 * @param events The list of events to analyze
 * @param hasTopContent Whether there's a header item (e.g., stories row)
 * @param hasOrganizations Whether organizations are being displayed
 * @param organizationsCount Number of organizations (if any)
 * @return A map from date header strings to their indices in the LazyColumn
 */
private fun buildDateHeaderIndexMap(
    events: List<Event>,
    hasTopContent: Boolean,
    hasOrganizations: Boolean = false,
    organizationsCount: Int = 0
): Map<String, Int> {
  if (events.isEmpty()) return emptyMap()

  val sortedEvents = events.sortedBy { it.start }
  val groupedEvents = sortedEvents.groupBy { event -> formatDateHeader(event.start) }

  // Calculate organization insertion index (same logic as in EventListScreen)
  val dateGroups = groupedEvents.keys.toList()
  val orgInsertionIndex =
      if (hasOrganizations && organizationsCount > 0 && dateGroups.size >= 2) {
        // Random insertion between 1st and 2nd date group
        1 // Using index 1 for consistency
      } else {
        -1
      }

  val indexMap = mutableMapOf<String, Int>()
  var currentIndex = if (hasTopContent) 1 else 0 // Account for topContent header if present

  groupedEvents.entries.forEachIndexed { groupIndex, (dateHeader, eventsOnDate) ->
    // The date header is at currentIndex
    indexMap[dateHeader] = currentIndex
    // Move past the header and all events in this date section
    currentIndex += 1 + eventsOnDate.size

    // Account for organization suggestions inserted after this date group
    if (groupIndex == orgInsertionIndex) {
      currentIndex += 1 // One item for organization suggestions
    }
  }

  return indexMap
}

/**
 * Scrolls to the specified date in the event list by finding the date header's actual position.
 * This approach is robust to dynamic content changes (like suggestion cards) because it searches
 * for the date header by its key rather than pre-calculating offsets.
 *
 * @param listState The LazyListState controlling the event list
 * @param events The list of events being displayed
 * @param targetDate The date to scroll to
 * @param hasTopContent Whether there's a header item (e.g., stories row) before the event list
 * @param hasOrganizations Whether organizations are being displayed
 * @param organizationsCount Number of organizations (if any)
 */
private suspend fun scrollToDate(
    listState: LazyListState,
    events: List<Event>,
    targetDate: Date,
    hasTopContent: Boolean = true,
    hasOrganizations: Boolean = false,
    organizationsCount: Int = 0
) {
  try {
    if (events.isEmpty()) return

    // Build the index map that mirrors EventListScreen's structure
    val dateHeaderIndexMap =
        buildDateHeaderIndexMap(events, hasTopContent, hasOrganizations, organizationsCount)

    // Find the target date header string
    val targetDateHeader = formatDateHeader(Timestamp(targetDate))

    // Look up the index in our map
    val targetIndex = dateHeaderIndexMap[targetDateHeader]

    if (targetIndex != null) {
      // Ensure index is within bounds
      val maxIndex = listState.layoutInfo.totalItemsCount - 1
      val scrollIndex = minOf(targetIndex, maxIndex.coerceAtLeast(0))
      listState.animateScrollToItem(scrollIndex)
    } else {
      // Date not found in the list, scroll to top
      listState.animateScrollToItem(0)
    }
  } catch (e: Exception) {
    // Handle any unexpected errors gracefully by scrolling to top

    try {
      listState.animateScrollToItem(0)
    } catch (scrollError: Exception) {
      // Ignore scroll errors if list is not yet initialized
    }
  }
}
