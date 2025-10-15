package com.github.se.studentconnect.ui.screens

import FilterBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.github.se.studentconnect.ui.utils.Panel
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreenTestTags
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.events.EventListScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.viewmodel.HomePageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: HomePageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNotifications by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) { viewModel.refresh() }
    Scaffold(
        modifier = Modifier.fillMaxSize()
            .padding(vertical = 30.dp)
            .testTag("HomePage"),
        topBar = {
            HomeTopBar(
                showNotifications,
                onNotificationClick = {
                    showNotifications = !showNotifications
                },
                onDismiss = {
                    showNotifications = false
                })
        })
    { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column {
                    FilterBar(LocalContext.current)
                    EventListScreen(navController = navController, events = uiState.events, false)
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
    onDismiss: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                placeholder = { Text("Search for events...") },
                leadingIcon = { Icon(
                    painter = painterResource(id = R.drawable.ic_search)
                    , contentDescription = "Search Icon") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors =
                    TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent))
        },
        actions = {
            Box{
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
    AppTheme {
        HomeScreen()
    }
}