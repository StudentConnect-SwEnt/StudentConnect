package com.github.se.studentconnect.ui.screen.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.github.se.studentconnect.ui.profile.ProfileScreenViewModel
import com.github.se.studentconnect.ui.profile.components.ProfileHeader
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Profile screen showing user information and stats. Main profile view for the StudentConnect app.
 *
 * @param currentUserId The ID of the current user (default for demo purposes)
 * @param userRepository Repository for user data operations
 * @param viewModel ViewModel for profile screen
 * @param onNavigateToSettings Callback to navigate to settings/edit profile screen
 * @param onNavigateToUserCard Callback to navigate to user card screen
 * @param modifier Modifier for the composable
 */
@Composable
fun ProfileScreen(
    currentUserId: String = "mock_user_123",
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: ProfileScreenViewModel = viewModel {
      ProfileScreenViewModel(
          userRepository = userRepository,
          friendsRepository = FriendsRepositoryProvider.repository,
          currentUserId = currentUserId)
    },
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToUserCard: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val friendsCount by viewModel.friendsCount.collectAsState()
  val eventsCount by viewModel.eventsCount.collectAsState()

  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Reload data when screen becomes visible again
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.loadUserProfile()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  Scaffold(modifier = modifier) { paddingValues ->
    when (val currentUser = user) {
      null -> {
        // Loading state
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
      }
      else -> {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top) {
              // Profile Header Section (Profile Picture + Stats + User Info)
              ProfileHeader(
                  user = currentUser,
                  friendsCount = friendsCount,
                  eventsCount = eventsCount,
                  onFriendsClick = {
                    // Here we'll implement the view of the list of friend
                    Toast.makeText(context, "Friends list coming soon", Toast.LENGTH_SHORT).show()
                  },
                  onEventsClick = {
                    // Here we'll implement the view of all the events the user joined
                    Toast.makeText(context, "Event history coming soon", Toast.LENGTH_SHORT).show()
                  },
                  onEditClick = {
                    if (onNavigateToSettings != null) {
                      onNavigateToSettings()
                    } else {
                      Toast.makeText(context, "Edit Profile", Toast.LENGTH_SHORT).show()
                    }
                  },
                  onUserCardClick = {
                    if (onNavigateToUserCard != null) {
                      onNavigateToUserCard()
                    } else {
                      Toast.makeText(context, "User Card", Toast.LENGTH_SHORT).show()
                    }
                  })
            }
      }
    }
  }
}
