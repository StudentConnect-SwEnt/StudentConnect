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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryFirestore
import com.github.se.studentconnect.ui.profile.ProfileScreenViewModel
import com.github.se.studentconnect.ui.profile.components.PinnedEventsSection
import com.github.se.studentconnect.ui.profile.components.ProfileHeader
import com.github.se.studentconnect.ui.profile.components.ProfileHeaderCallbacks
import com.github.se.studentconnect.ui.profile.components.ProfileStats
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Navigation callbacks for ProfileScreen
 *
 * @param onNavigateToSettings Callback to navigate to settings/edit profile screen
 * @param onNavigateToUserCard Callback to navigate to user card screen
 * @param onNavigateToFriendsList Callback to navigate to friends list screen with userId parameter
 * @param onNavigateToJoinedEvents Callback to navigate to joined events screen
 * @param onNavigateToEventDetails Callback to navigate to event details screen
 * @param onNavigateToOrganizationManagement Callback to navigate to organization management screen
 * @param onLogout Callback to handle logout action
 */
data class ProfileNavigationCallbacks(
    val onNavigateToSettings: (() -> Unit)? = null,
    val onNavigateToUserCard: (() -> Unit)? = null,
    val onNavigateToFriendsList: ((String) -> Unit)? = null,
    val onNavigateToJoinedEvents: (() -> Unit)? = null,
    val onNavigateToEventDetails: ((String) -> Unit)? = null,
    val onNavigateToOrganizationManagement: (() -> Unit)? = null,
    val onLogout: (() -> Unit)? = null
)

/**
 * Profile screen showing user information and stats. Main profile view for the StudentConnect app.
 *
 * @param currentUserId The ID of the current user (default for demo purposes)
 * @param userRepository Repository for user data operations
 * @param viewModel ViewModel for profile screen
 * @param navigationCallbacks Navigation callbacks grouped in a data class
 * @param modifier Modifier for the composable
 * @param logout Callback for logout action
 */
@Composable
fun ProfileScreen(
    currentUserId: String,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    eventRepository: EventRepository = EventRepositoryProvider.repository,
    organizationRepository: OrganizationRepository = OrganizationRepositoryProvider.repository,
    viewModel: ProfileScreenViewModel = viewModel {
      ProfileScreenViewModel(
          userRepository = userRepository,
          friendsRepository = FriendsRepositoryProvider.repository,
          eventRepository = eventRepository,
          organizationRepository = organizationRepository,
          currentUserId = currentUserId)
    },
    navigationCallbacks: ProfileNavigationCallbacks = ProfileNavigationCallbacks(),
    modifier: Modifier = Modifier,
    logout: () -> Unit = {},
) {
  val user by viewModel.user.collectAsState()
  val friendsCount by viewModel.friendsCount.collectAsState()
  val eventsCount by viewModel.eventsCount.collectAsState()
  val pinnedEvents by viewModel.pinnedEvents.collectAsState()
  val userOrganizations by viewModel.userOrganizations.collectAsState()

  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Dynamic top padding based on screen width
  val topPadding = screenWidth * 0.04f

  val friendsListComingSoon = stringResource(R.string.toast_friends_list_coming_soon)
  val editProfileText = stringResource(R.string.toast_edit_profile)
  val userCardText = stringResource(R.string.toast_user_card)

  // Reload profile data when screen becomes visible
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
        viewModel.loadUserProfile()
        viewModel.loadPinnedEvents()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  Scaffold(
      modifier = modifier,
  ) { paddingValues ->
    when (val currentUser = user) {
      null -> {
        // Show loading spinner while data loads
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
                    .padding(top = topPadding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top) {
              // Profile header with picture, stats, and user info
              ProfileHeader(
                  user = currentUser,
                  stats = ProfileStats(friendsCount = friendsCount, eventsCount = eventsCount),
                  callbacks =
                      ProfileHeaderCallbacks(
                          onFriendsClick = {
                            navigationCallbacks.onNavigateToFriendsList?.invoke(currentUserId)
                                ?: Toast.makeText(
                                        context, friendsListComingSoon, Toast.LENGTH_SHORT)
                                    .show()
                          },
                          onEventsClick = {
                            navigationCallbacks.onNavigateToJoinedEvents?.invoke()
                          },
                          onEditClick = {
                            navigationCallbacks.onNavigateToSettings?.invoke()
                                ?: Toast.makeText(context, editProfileText, Toast.LENGTH_SHORT)
                                    .show()
                          },
                          onUserCardClick = {
                            navigationCallbacks.onNavigateToUserCard?.invoke()
                                ?: Toast.makeText(context, userCardText, Toast.LENGTH_SHORT).show()
                          },
                          onOrganizationClick = {
                            navigationCallbacks.onNavigateToOrganizationManagement?.invoke()
                          },
                          onLogoutClick = logout),
                  userOrganizations = userOrganizations)

              // Pinned events section
              PinnedEventsSection(
                  pinnedEvents = pinnedEvents,
                  onEventClick = { event ->
                    navigationCallbacks.onNavigateToEventDetails?.invoke(event.uid)
                  })
            }
      }
    }
  }
}
