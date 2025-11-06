package com.github.se.studentconnect.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.ui.calendar.EventCalendar
import com.github.se.studentconnect.ui.events.EventListScreen
import com.github.se.studentconnect.ui.events.formatDateHeader
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.utils.FilterBar
import com.github.se.studentconnect.ui.utils.Panel
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth
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
    notificationViewModel: NotificationViewModel = viewModel(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  val notificationUiState by notificationViewModel.uiState.collectAsState()
  val favoriteEventIds by viewModel.favoriteEventIds.collectAsState()
  var showNotifications by remember { mutableStateOf(false) }
  val sheetState =
      rememberModalBottomSheetState(
          initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
  val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  LaunchedEffect(Unit) { viewModel.refresh() }

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

data class NotificationState(
    val showNotifications: Boolean,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0
)

data class NotificationCallbacks(
    val onNotificationClick: () -> Unit,
    val onDismiss: () -> Unit,
    val onNotificationRead: (String) -> Unit = {},
    val onNotificationDelete: (String) -> Unit = {},
    val onFriendRequestAccept: (String, String) -> Unit = { _, _ -> },
    val onFriendRequestReject: (String, String) -> Unit = { _, _ -> }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    notificationState: NotificationState,
    notificationCallbacks: NotificationCallbacks,
    navController: NavHostController = rememberNavController()
) {
  TopAppBar(
      title = { SearchTextField() },
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

  Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = fontWeight,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = modifier)
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
