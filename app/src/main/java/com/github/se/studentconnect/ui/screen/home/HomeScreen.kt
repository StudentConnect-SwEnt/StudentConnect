package com.github.se.studentconnect.ui.screens

import FilterBar
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.events.EventListScreen
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.camera.QrScannerScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.ui.utils.Panel
import com.github.se.studentconnect.viewmodel.HomePageViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: HomePageViewModel = viewModel(),
    shouldOpenQRScanner: Boolean = false,
    onQRScannerClosed: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  var showNotifications by remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
  val coroutineScope = rememberCoroutineScope()

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
                        FilterBar(LocalContext.current)
                        EventListScreen(
                            navController = navController, events = uiState.events, false)
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
    navController: NavHostController = rememberNavController()
) {
  TopAppBar(
      title = {
        TextField(
            value = "",
            onValueChange = {},
            modifier =
                Modifier.fillMaxWidth()
                    .padding(end = 8.dp)
                    .clickable(onClick = { navController.navigate(Route.SEARCH) }),
            enabled = false,
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
                      .testTag(ActivitiesScreenTestTags.INVITATIONS_POPOVER)) {
                Panel<Invitation>(title = "Notifications")
              }
        }
      })
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
  AppTheme { HomeScreen() }
}
