package com.github.se.studentconnect.ui.screen.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.github.se.studentconnect.ui.profile.ProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Instagram-style Profile screen showing user information, stats, and pinned events.
 * Main profile view for the StudentConnect app.
 *
 * @param currentUserId The ID of the current user (default for demo purposes)
 * @param userRepository Repository for user data operations
 * @param viewModel ViewModel for profile screen
 * @param onNavigateToSettings Callback to navigate to settings/edit profile screen
 * @param onNavigateToMap Callback to navigate to map screen
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUserId: String = "mock_user_123",
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: ProfileViewModel = viewModel { ProfileViewModel(userRepository, currentUserId) },
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToMap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsState()
    val friendsCount by viewModel.friendsCount.collectAsState()
    val eventsCount by viewModel.eventsCount.collectAsState()
    val pinnedEvents by viewModel.pinnedEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reload data when screen becomes visible again
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserProfile()
                viewModel.loadPinnedEvents()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Settings Icon
                    IconButton(
                        onClick = {
                            if (onNavigateToSettings != null) {
                                onNavigateToSettings()
                            } else {
                                Toast.makeText(context, "Settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Map Icon
                    IconButton(
                        onClick = {
                            if (onNavigateToMap != null) {
                                onNavigateToMap()
                            } else {
                                Toast.makeText(context, "Map feature coming soon", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_vector),
                            contentDescription = "Map",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val currentUser = user) {
            null -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Profile Header Section (Profile Picture + Stats + User Info)
                    ProfileHeader(
                        user = currentUser,
                        friendsCount = friendsCount,
                        eventsCount = eventsCount,
                        onFriendsClick = {
                            // TODO: Navigate to friends list screen
                            Toast.makeText(context, "Friends list coming soon", Toast.LENGTH_SHORT).show()
                        },
                        onEventsClick = {
                            // TODO: Navigate to event history screen
                            Toast.makeText(context, "Event history coming soon", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    // Divider
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Pinned Events Section
                    PinnedEventsSection(
                        pinnedEvents = pinnedEvents,
                        onEventClick = { event ->
                            // TODO: Navigate to event details screen
                            Toast.makeText(
                                context,
                                "Event details coming soon: ${event.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onViewAllClick = {
                            // TODO: Navigate to all pinned events screen
                            Toast.makeText(
                                context,
                                "View all events coming soon",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun ProfileViewModel.loadPinnedEvents() {
    TODO("Not yet implemented")
}
