package com.github.se.studentconnect.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.ui.events.EventListScreen
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.screen.filters.FilterBar
import com.github.se.studentconnect.ui.utils.Panel
import com.github.se.studentconnect.viewmodel.HomePageViewModel
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
  var showNotifications by remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
  val coroutineScope = rememberCoroutineScope()
  val favoriteEventIds by viewModel.favoriteEventIds.collectAsState()

  LaunchedEffect(Unit) { viewModel.refresh() }

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
              notifications = notificationUiState.notifications,
              unreadCount = notificationUiState.unreadCount,
              onNotificationRead = { notificationViewModel.markAsRead(it) },
              onNotificationDelete = { notificationViewModel.deleteNotification(it) },
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
                            onApplyFilters = viewModel::applyFilters)
                        EventListScreen(
                            navController = navController,
                            events = uiState.events,
                            hasJoined = false,
                            favoriteEventIds = favoriteEventIds,
                            onFavoriteToggle = viewModel::toggleFavorite)
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
fun HomeTopBar(
    showNotifications: Boolean,
    onNotificationClick: () -> Unit,
    onDismiss: () -> Unit,
    notifications: List<Notification> = emptyList(),
    unreadCount: Int = 0,
    onNotificationRead: (String) -> Unit = {},
    onNotificationDelete: (String) -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
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
                  contentDescription = "Search Icon")
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent))
      },
      actions = {
        Box {
          // Notification icon button with badge
          IconButton(
              onClick = onNotificationClick, modifier = Modifier.testTag("NotificationButton")) {
                BadgedBox(
                    badge = {
                      if (unreadCount > 0) {
                        Badge { Text(unreadCount.toString()) }
                      }
                    }) {
                      Icon(
                          imageVector = Icons.Default.Notifications,
                          contentDescription = "Notifications")
                    }
              }
          DropdownMenu(
              expanded = showNotifications,
              onDismissRequest = onDismiss,
              modifier =
                  Modifier.background(Color.Transparent)
                      .shadow(0.dp)
                      .testTag(ActivitiesScreenTestTags.INVITATIONS_POPOVER)) {
                Panel<Notification>(
                    items = notifications,
                    title = "Notifications",
                    itemContent = { notification ->
                      NotificationItem(
                          notification = notification,
                          onRead = { onNotificationRead(notification.id) },
                          onDelete = { onNotificationDelete(notification.id) },
                          onClick = {
                            when (notification) {
                              is Notification.FriendRequest -> {
                                // Navigate to visitor profile
                                navController.navigate(
                                    Route.visitorProfile(notification.fromUserId))
                                onNotificationRead(notification.id)
                                onDismiss()
                              }
                              is Notification.EventStarting -> {
                                // Navigate to event view
                                navController.navigate("eventView/${notification.eventId}/true")
                                onNotificationRead(notification.id)
                                onDismiss()
                              }
                            }
                          })
                    })
              }
        }
      })
}

@Composable
fun NotificationItem(
    notification: Notification,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .clickable { onClick() }
              .testTag("NotificationItem_${notification.id}"),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (notification.isRead) MaterialTheme.colorScheme.surface
                  else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Row(
                  modifier = Modifier.weight(1f),
                  horizontalArrangement = Arrangement.Start,
                  verticalAlignment = Alignment.CenterVertically) {
                    // Icon based on notification type
                    Icon(
                        imageVector =
                            when (notification) {
                              is Notification.FriendRequest -> Icons.Default.Person
                              is Notification.EventStarting -> Icons.Default.Event
                            },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.width(12.dp))

                    // Notification message
                    Column(modifier = Modifier.weight(1f)) {
                      val message: String =
                          when (notification) {
                            is Notification.FriendRequest -> notification.getMessage()
                            is Notification.EventStarting -> notification.getMessage()
                          }
                      Text(
                          text = message,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight =
                              if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                          maxLines = 2,
                          overflow = TextOverflow.Ellipsis)
                    }
                  }

              // Delete button
              IconButton(
                  onClick = { onDelete() },
                  modifier =
                      Modifier.size(24.dp).testTag("DeleteNotificationButton_${notification.id}")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp))
                  }
            }
      }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
  AppTheme { HomeScreen() }
}
