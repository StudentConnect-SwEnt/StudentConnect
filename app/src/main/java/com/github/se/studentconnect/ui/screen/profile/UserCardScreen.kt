package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.github.se.studentconnect.ui.profile.UserCardViewModel
import com.github.se.studentconnect.ui.profile.components.UserCard
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Screen that displays the user's digital card (UserCard).
 *
 * Shows a flippable card with the user's information on one side and a QR code on the other side.
 *
 * @param currentUserId The ID of the current user
 * @param userRepository Repository for user data operations
 * @param onNavigateBack Callback to navigate back to the previous screen
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCardScreen(
    currentUserId: String,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: UserCardViewModel = viewModel {
      UserCardViewModel(userRepository = userRepository, currentUserId = currentUserId)
    },
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val error by viewModel.error.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface))
      },
      modifier = modifier) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              when {
                isLoading -> {
                  CircularProgressIndicator()
                }
                error != null -> {
                  Text(
                      text = error ?: stringResource(R.string.error_unknown),
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.bodyLarge)
                }
                user != null -> {
                  Column(
                      modifier = Modifier.fillMaxSize().padding(24.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center) {
                        // Display the user card
                        UserCard(user = user!!)

                        // Optional: Add instruction text
                        Text(
                            text = stringResource(R.string.user_card_tap_to_flip),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 24.dp))
                      }
                }
              }
            }
      }
}
