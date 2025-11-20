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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.github.se.studentconnect.ui.profile.ProfileScreenViewModel
import com.github.se.studentconnect.ui.profile.components.ProfileHeader
import com.github.se.studentconnect.ui.profile.components.ProfileStats
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
    currentUserId: String,
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

  // Pre-fetch string resources for use in callbacks
  val friendsListComingSoon = stringResource(R.string.toast_friends_list_coming_soon)
  val eventHistoryComingSoon = stringResource(R.string.toast_event_history_coming_soon)
  val editProfileText = stringResource(R.string.toast_edit_profile)
  val userCardText = stringResource(R.string.toast_user_card)

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
                  stats = ProfileStats(friendsCount = friendsCount, eventsCount = eventsCount),
                  onFriendsClick = {
                    // Here we'll implement the view of the list of friend
                    Toast.makeText(context, friendsListComingSoon, Toast.LENGTH_SHORT).show()
                  },
                  onEventsClick = {
                    // Here we'll implement the view of all the events the user joined
                    Toast.makeText(context, eventHistoryComingSoon, Toast.LENGTH_SHORT).show()
                  },
                  onEditClick = {
                    onNavigateToSettings?.invoke()
                        ?: Toast.makeText(context, editProfileText, Toast.LENGTH_SHORT).show()
                  },
                  onUserCardClick = {
                    onNavigateToUserCard?.invoke()
                        ?: Toast.makeText(context, userCardText, Toast.LENGTH_SHORT).show()
                  })
            }
      }
    }
  }
}
