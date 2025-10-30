package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.visitorProfile.VisitorProfileScreen
import com.github.se.studentconnect.ui.screen.visitorProfile.VisitorProfileViewModel

@Composable
fun VisitorProfileRoute(
    userId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onScanAgain: (() -> Unit)? = null,
    viewModel: VisitorProfileViewModel = viewModel()
) {
  LaunchedEffect(userId) { viewModel.loadProfile(userId) }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  // Show snackbar for friend request messages
  LaunchedEffect(uiState.friendRequestMessage) {
    uiState.friendRequestMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearFriendRequestMessage()
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    when {
      uiState.isLoading -> VisitorProfileLoading(modifier = Modifier.fillMaxSize())
      uiState.user != null ->
          VisitorProfileScreen(
              user = uiState.user!!,
              onBackClick = onBackClick,
              onAddFriendClick = { viewModel.sendFriendRequest() },
              friendRequestStatus = uiState.friendRequestStatus,
              modifier = Modifier.fillMaxSize())
      else ->
          VisitorProfileError(
              message = uiState.errorMessage ?: "Profile not found.",
              onRetry = { viewModel.loadProfile(userId, forceRefresh = true) },
              onScanAgain = onScanAgain,
              onBackClick = onBackClick,
              modifier = Modifier.fillMaxSize())
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
  }
}

@Composable
private fun VisitorProfileLoading(modifier: Modifier = Modifier) {
  Box(
      modifier = modifier.fillMaxSize().testTag(C.Tag.visitor_profile_loading),
      contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
}

@Composable
private fun VisitorProfileError(
    message: String,
    onRetry: () -> Unit,
    onScanAgain: (() -> Unit)?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Surface(modifier = modifier.fillMaxSize().testTag(C.Tag.visitor_profile_error)) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Text(
              text = message,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(bottom = 8.dp))

          // Show "Scan Again" button if we came from QR scanner
          if (onScanAgain != null) {
            Button(
                onClick = onScanAgain,
                modifier = Modifier.padding(top = 24.dp).testTag("scan_again_button")) {
                  Text(text = "Scan Again")
                }
          } else {
            // Otherwise show regular "Try again" button
            Button(onClick = onRetry, modifier = Modifier.padding(top = 24.dp)) {
              Text(text = "Try again")
            }
          }

          Button(onClick = onBackClick, modifier = Modifier.padding(top = 12.dp)) {
            Text(text = "Back to Home")
          }
        }
  }
}
