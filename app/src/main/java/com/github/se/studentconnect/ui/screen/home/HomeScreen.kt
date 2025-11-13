package com.github.se.studentconnect.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.ui.calendar.EventCalendar
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.camera.CameraMode
import com.github.se.studentconnect.ui.screen.camera.CameraModeSelectorScreen
import com.github.se.studentconnect.ui.utils.EventListScreen
import com.github.se.studentconnect.ui.utils.FilterBar
import com.github.se.studentconnect.ui.utils.Panel
import com.github.se.studentconnect.ui.utils.formatDateHeader
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import kotlinx.coroutines.launch

// UI Constants
private object HomeScreenConstants {
  const val STORY_SIZE_DP = 64
  const val STORY_BORDER_WIDTH_DP = 3
  const val STORY_PADDING_TOP_DP = 4
  const val STORIES_ROW_TOP_PADDING_DP = 0
  const val STORIES_ROW_BOTTOM_PADDING_DP = 12
  const val STORIES_ROW_HORIZONTAL_SPACING_DP = 16
  const val STORIES_ROW_HORIZONTAL_PADDING_DP = 8
  const val SEARCH_BAR_CORNER_RADIUS_DP = 24
  const val SEARCH_BAR_END_PADDING_DP = 8
  const val PAGER_SCANNER_PAGE = 0
  const val PAGER_HOME_PAGE = 1
}

/** DI-friendly overload that wires default view models and exposes callback hooks. */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: HomePageViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {},
    onCameraActiveChange: (Boolean) -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsState()
  val notificationUiState by notificationViewModel.uiState.collectAsState()
  val favoriteEventIds by viewModel.favoriteEventIds.collectAsState()

  LaunchedEffect(Unit) { viewModel.refresh() }

  HomeScreen(
      navController = navController,
      shouldOpenQRScanner = shouldOpenQRScanner,
      onQRScannerClosed = onQRScannerClosed,
      onCameraActiveChange = onCameraActiveChange,
      onClickStory = { e, i -> viewModel.updateSeenStories(e, i) },
      uiState = uiState,
      viewModel = viewModel,
      notificationViewModel = notificationViewModel,
      favoriteEventIds = favoriteEventIds,
      onDateSelected = { date -> viewModel.onDateSelected(date) },
      onCalendarClick = { viewModel.showCalendar() },
      onCalendarDismiss = { viewModel.hideCalendar() },
      onApplyFilters = viewModel::applyFilters,
      onFavoriteToggle = viewModel::toggleFavorite,
      onToggleFavoritesFilter = { viewModel.toggleFavoritesFilter() },
      onClearScrollTarget = { viewModel.clearScrollTarget() })
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
    notificationViewModel: NotificationViewModel = viewModel(),
    viewModel: HomePageViewModel = viewModel(),
    favoriteEventIds: Set<String> = emptySet(),
    onDateSelected: (Date) -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onCalendarDismiss: () -> Unit = {},
    onApplyFilters: (com.github.se.studentconnect.ui.utils.FilterData) -> Unit = {},
    onFavoriteToggle: (String) -> Unit = {},
    onToggleFavoritesFilter: () -> Unit = {},
    onClearScrollTarget: () -> Unit = {}
) {
  var showNotifications by remember { mutableStateOf(false) }
  var cameraMode by remember { mutableStateOf(CameraMode.QR_SCAN) }
  val notificationUiState by notificationViewModel.uiState.collectAsState()
  var selectedStory by remember { mutableStateOf<Event?>(null) }
  var showStoryViewer by remember { mutableStateOf(false) }
  val sheetState =
      rememberModalBottomSheetState(
          initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
  val pagerState =
      rememberPagerState(
          initialPage = HomeScreenConstants.PAGER_HOME_PAGE,
          pageCount = { HomeScreenConstants.PAGER_HOME_PAGE + 1 })
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  LaunchedEffect(Unit) { viewModel.refresh() }

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
                    notificationState =
                        NotificationState(
                            showNotifications = showNotifications,
                            notifications = notificationUiState.notifications,
                            unreadCount = notificationUiState.unreadCount),
                    notificationCallbacks =
                        NotificationCallbacks(
                            onNotificationClick = { showNotifications = !showNotifications },
                            onDismiss = { showNotifications = false },
                            onNotificationRead = { notificationViewModel.markAsRead(it) },
                            onNotificationDelete = { notificationViewModel.deleteNotification(it) },
                            onFriendRequestAccept = { notificationId, fromUserId ->
                              coroutineScope.launch {
                                try {
                                  val userId = FirebaseAuth.getInstance().currentUser?.uid
                                  if (userId != null) {
                                    FriendsRepositoryProvider.repository.acceptFriendRequest(
                                        userId, fromUserId)
                                    notificationViewModel.deleteNotification(notificationId)
                                  }
                                } catch (e: Exception) {
                                  // Handle error silently or show a message
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
                                    notificationViewModel.deleteNotification(notificationId)
                                  }
                                } catch (e: Exception) {
                                  // Handle error silently or show a message
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
                            onStoryCapture = { imageBytes ->
                              // For now, just return to home page
                              // TODO: Implement story upload functionality
                              onQRScannerClosed()
                              cameraMode = CameraMode.QR_SCAN
                              coroutineScope.launch {
                                pagerState.scrollToPage(HomeScreenConstants.PAGER_HOME_PAGE)
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
                              FilterBar(
                                  context = LocalContext.current,
                                  onCalendarClick = onCalendarClick,
                                  onApplyFilters = onApplyFilters,
                                  showOnlyFavorites = uiState.showOnlyFavorites,
                                  onToggleFavorites = onToggleFavoritesFilter)
                              EventListScreen(
                                  navController = navController,
                                  events = uiState.events,
                                  hasJoined = false,
                                  listState = listState,
                                  favoriteEventIds = favoriteEventIds,
                                  onFavoriteToggle = onFavoriteToggle,
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
                                          showStoryViewer = true
                                          onClickStory(event, seenStories)
                                        },
                                        stories = uiState.subscribedEventsStories)
                                  })
                            }
                          }
                        }
                      }
                    }
                  }
            }

        // Story Viewer Overlay
        selectedStory?.let { story ->
          StoryViewer(
              event = story, isVisible = showStoryViewer, onDismiss = { showStoryViewer = false })
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
            onCalendarDismiss()
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
    val onFriendRequestReject: (String, String) -> Unit = { _, _ -> }
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
        NotificationDropdown(
            notificationState = notificationState,
            notificationCallbacks = notificationCallbacks,
            navController = navController)
      })
}

@Composable
private fun SearchTextField() {
  TextField(
      value = "",
      onValueChange = {},
      modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
      placeholder = { Text("Search for events...") },
      leadingIcon = {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search Icon")
      },
      singleLine = true,
      shape = RoundedCornerShape(24.dp),
      colors =
          TextFieldDefaults.colors(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent))
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
              title = "Notifications",
              itemContent = { notification ->
                NotificationItem(
                    notification = notification,
                    onRead = { notificationCallbacks.onNotificationRead(notification.id) },
                    onDelete = { notificationCallbacks.onNotificationDelete(notification.id) },
                    onAccept =
                        getAcceptCallback(
                            notification, notificationCallbacks.onFriendRequestAccept),
                    onReject =
                        getRejectCallback(
                            notification, notificationCallbacks.onFriendRequestReject),
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
        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
      }
}

private fun getAcceptCallback(
    notification: Notification,
    onFriendRequestAccept: (String, String) -> Unit
): (() -> Unit)? {
  return if (notification is Notification.FriendRequest) {
    { onFriendRequestAccept(notification.id, notification.fromUserId) }
  } else null
}

private fun getRejectCallback(
    notification: Notification,
    onFriendRequestReject: (String, String) -> Unit
): (() -> Unit)? {
  return if (notification is Notification.FriendRequest) {
    { onFriendRequestReject(notification.id, notification.fromUserId) }
  } else null
}

private fun handleNotificationClick(
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
  return if (notification.isRead) {
    MaterialTheme.colorScheme.surface
  } else {
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
  }
}

@Composable
private fun NotificationContent(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth().clickable { onClick() },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        NotificationIcon(notification = notification)
        Spacer(modifier = Modifier.width(12.dp))
        NotificationMessage(notification = notification, modifier = Modifier.weight(1f))
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
        overflow = TextOverflow.Ellipsis)

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
        is Notification.FriendRequest -> notification.fromUserId
        is Notification.EventStarting -> notification.userId
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
            contentDescription = "Delete",
            modifier = Modifier.size(16.dp))
      }
}

private fun shouldShowActionButtons(
    notification: Notification,
    onAccept: (() -> Unit)?,
    onReject: (() -> Unit)?
): Boolean {
  return notification is Notification.FriendRequest && onAccept != null && onReject != null
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
        modifier = Modifier.weight(1f).testTag("AcceptFriendRequestButton_$notificationId"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
          Text("Accept")
        }
    Button(
        onClick = {
          onReject()
          onRead()
        },
        modifier = Modifier.weight(1f).testTag("RejectFriendRequestButton_$notificationId"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
          Text("Reject")
        }
  }
}

/** Renders a single story bubble with seen/unseen styling. */
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
      if (viewed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
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
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1)
      }
}

/** Horizontal list of story bubbles with a leading "Add Story" affordance. */
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

@Composable
fun StoryViewer(event: Event, isVisible: Boolean, onDismiss: () -> Unit) {
  AnimatedVisibility(
      visible = isVisible,
      enter =
          fadeIn(animationSpec = tween(300)) +
              scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
      exit =
          fadeOut(animationSpec = tween(200)) +
              scaleOut(targetScale = 0.9f, animationSpec = tween(200)),
      modifier = Modifier.fillMaxSize().zIndex(1000f)) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .clickable { onDismiss() }
                    .testTag("story_viewer")) {
              // Close button
              IconButton(
                  onClick = onDismiss,
                  modifier =
                      Modifier.align(Alignment.TopEnd)
                          .padding(16.dp)
                          .testTag("story_close_button")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close story",
                        tint = Color.White)
                  }

              // Story content - placeholder image for now
              Box(
                  modifier = Modifier.fillMaxSize().padding(vertical = 80.dp),
                  contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                          Image(
                              painter = painterResource(id = R.drawable.avatar_12),
                              contentDescription = "Story content",
                              modifier =
                                  Modifier.fillMaxWidth(0.9f).clip(RoundedCornerShape(12.dp)))
                          Spacer(modifier = Modifier.height(16.dp))
                          Text(
                              text = event.title,
                              color = Color.White,
                              style = MaterialTheme.typography.headlineSmall)
                        }
                  }
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
    val targetDateHeader = formatDateHeader(Timestamp(targetDate))

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
