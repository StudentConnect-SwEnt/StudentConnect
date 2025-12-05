package com.github.se.studentconnect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.service.EventReminderWorker
import com.github.se.studentconnect.service.NotificationChannelManager
import com.github.se.studentconnect.ui.activities.EventView
import com.github.se.studentconnect.ui.eventcreation.CreatePrivateEventScreen
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreen
import com.github.se.studentconnect.ui.navigation.BottomNavigationBar
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import com.github.se.studentconnect.ui.profile.ProfileConstants
import com.github.se.studentconnect.ui.profile.ProfileRoutes
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreen
import com.github.se.studentconnect.ui.screen.home.HomeScreen
import com.github.se.studentconnect.ui.screen.map.MapScreen
import com.github.se.studentconnect.ui.screen.profile.FriendsListScreen
import com.github.se.studentconnect.ui.screen.profile.JoinedEventsScreen
import com.github.se.studentconnect.ui.screen.profile.OrganizationProfileScreen
import com.github.se.studentconnect.ui.screen.profile.ProfileNavigationCallbacks
import com.github.se.studentconnect.ui.screen.profile.ProfileScreen
import com.github.se.studentconnect.ui.screen.profile.ProfileSettingsScreen
import com.github.se.studentconnect.ui.screen.profile.UserCardScreen
import com.github.se.studentconnect.ui.screen.profile.edit.*
import com.github.se.studentconnect.ui.screen.search.SearchScreen
import com.github.se.studentconnect.ui.screen.signup.OnboardingNavigation
import com.github.se.studentconnect.ui.screen.signup.regularuser.GetStartedScreen
import com.github.se.studentconnect.ui.screen.visitorprofile.VisitorProfileScreen
import com.github.se.studentconnect.ui.screen.visitorprofile.VisitorProfileViewModel
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/**
 * Provide an OkHttpClient client for network requests.
 *
 * Property `client` is mutable for testing purposes.
 */
object HttpClientProvider {
  var client: OkHttpClient = OkHttpClient()
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize notification channels
    NotificationChannelManager.createNotificationChannels(this)

    // Schedule periodic event reminder worker (runs every 15 minutes)
    val eventReminderRequest =
        PeriodicWorkRequestBuilder<EventReminderWorker>(15, TimeUnit.MINUTES).build()

    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(
            "event_reminder_work", ExistingPeriodicWorkPolicy.KEEP, eventReminderRequest)

    setContent {
      AppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background,
        ) {
          MainContent()
        }
      }
    }
  }
}

/**
 * Main content composable that manages the entire app flow using a ViewModel and state machine.
 *
 * This composable delegates all authentication and user profile checking logic to MainViewModel,
 * following MVVM principles. The ViewModel manages the AppState and provides clean separation
 * between UI and business logic.
 *
 * **State Machine Flow:**
 *
 * ```
 * LOADING (Initial)
 *   ├─> AUTHENTICATION (if no Firebase user)
 *   │     └─> ONBOARDING (after sign-in, if no profile exists)
 *   │           └─> MAIN_APP (after profile creation)
 *   ├─> ONBOARDING (if Firebase user exists but no profile)
 *   │     └─> MAIN_APP (after profile creation)
 *   └─> MAIN_APP (if Firebase user and profile both exist)
 * ```
 *
 * **First-Time User Flow:**
 * 1. App starts → ViewModel checks auth state
 * 2. No Firebase user found → AUTHENTICATION state (GetStartedScreen)
 * 3. User signs in with Google → ViewModel's onUserSignedIn() called
 * 4. ViewModel checks profile → no profile exists → ONBOARDING state
 * 5. User completes onboarding → profile saved to Firestore
 * 6. ViewModel's onUserProfileCreated() called → MAIN_APP state
 *
 * **Returning User Flow:**
 * 1. App starts → ViewModel checks auth state
 * 2. Firebase user found → ViewModel checks Firestore for profile
 * 3. Profile exists → directly to MAIN_APP state
 *
 * The key distinction: User profiles are only created in Firestore during onboarding. Firebase Auth
 * (authentication) is separate from Firestore (user profile storage).
 */
@Composable
fun MainContent() {
  val navController = rememberNavController()
  var selectedTab by remember { mutableStateOf<Tab>(Tab.Home) }
  var shouldOpenQRScanner by remember { mutableStateOf(false) }

  val userRepository = UserRepositoryProvider.repository
  val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(userRepository))
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  fun routeToTab(route: String?): Tab? =
      when {
        route == null -> null
        route == Route.HOME -> Tab.Home
        route == Route.MAP || route == Route.MAP_WITH_LOCATION || route.startsWith("map/") ->
            Tab.Map
        route == Route.ACTIVITIES -> Tab.Activities
        route == Route.PROFILE -> Tab.Profile
        else -> null
      }
  val derivedTab = routeToTab(currentRoute)
  LaunchedEffect(derivedTab) {
    if (derivedTab != null && derivedTab != selectedTab) {
      selectedTab = derivedTab
    }
  }
  val uiState by viewModel.uiState.collectAsState()

  // Initial auth check on app start
  LaunchedEffect(Unit) { viewModel.checkInitialAuthState() }

  // Render based on app state from ViewModel
  AppNavigationOrchestrator(
      appState = uiState.appState,
      uiState = uiState,
      viewModel = viewModel,
      userRepository = userRepository,
      navController = navController,
      selectedTab = selectedTab,
      onTabSelected = { selectedTab = it },
      shouldOpenQRScanner = shouldOpenQRScanner,
      onQRScannerStateChange = { shouldOpenQRScanner = it })
}

@Composable
private fun AppNavigationOrchestrator(
    appState: AppState,
    uiState: MainUIState,
    viewModel: MainViewModel,
    userRepository: UserRepository,
    navController: NavHostController,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    shouldOpenQRScanner: Boolean,
    onQRScannerStateChange: (Boolean) -> Unit
) {
  when (appState) {
    AppState.LOADING -> {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // Loading screen
      }
    }
    AppState.AUTHENTICATION -> {
      LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser != null) {
          Log.d("MainActivity", "Performing delayed Safe SignOut.")
          try {
            Firebase.auth.signOut()
          } catch (e: Exception) {
            Log.e("MainActivity", "SignOut error", e)
          }
        }
      }

      GetStartedScreen(
          onSignedIn = { uid ->
            val firebaseUser = Firebase.auth.currentUser
            viewModel.onUserSignedIn(uid, firebaseUser?.email ?: "")
          })
    }
    AppState.ONBOARDING -> {
      if (uiState.currentUserId != null && uiState.currentUserEmail != null) {
        OnboardingNavigation(
            firebaseUserId = uiState.currentUserId,
            email = uiState.currentUserEmail,
            userRepository = userRepository,
            onOnboardingComplete = { isLogout ->
              if (isLogout) {
                viewModel.onLogoutComplete()
              } else {
                viewModel.onUserProfileCreated()
              }
            })
      }
    }
    AppState.MAIN_APP -> {
      MainAppContent(
          navController = navController,
          selectedTab = selectedTab,
          onTabSelected = onTabSelected,
          shouldOpenQRScanner = shouldOpenQRScanner,
          onQRScannerStateChange = onQRScannerStateChange)
    }
  }
}

/**
 * Main app content composable that contains the scaffold with bottom navigation and main screens.
 *
 * This composable manages the bottom navigation bar visibility and handles navigation between
 * different screens. The bottom navigation bar is conditionally hidden when the camera mode
 * selector is active to provide a full-screen camera experience.
 *
 * @param navController The navigation controller for handling navigation between screens
 * @param selectedTab The currently selected bottom navigation tab
 * @param onTabSelected Callback invoked when a tab is selected
 * @param shouldOpenQRScanner Whether the QR scanner should be opened automatically
 * @param onQRScannerStateChange Callback to notify when QR scanner state changes
 */
@Composable
internal fun MainAppContent(
    navController: NavHostController,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    shouldOpenQRScanner: Boolean,
    onQRScannerStateChange: (Boolean) -> Unit
) {
  // Track whether camera mode selector is currently active to conditionally hide bottom nav
  var isCameraActive by remember { mutableStateOf(false) }

  val currentBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentBackStackEntry?.destination?.route

  // Hide bottom bar for create/edit event screens
  val hideBottomBar =
      currentRoute == Route.CREATE_PUBLIC_EVENT ||
          currentRoute == Route.CREATE_PRIVATE_EVENT ||
          currentRoute == Route.EDIT_PUBLIC_EVENT ||
          currentRoute == Route.EDIT_PRIVATE_EVENT ||
          isCameraActive

  Scaffold(
      bottomBar = {
        if (!isCameraActive) {
          BottomNavigationBar(
              selectedTab = selectedTab,
              onTabSelected = { tab ->
                onTabSelected(tab)
                onQRScannerStateChange(false)
                navController.navigate(tab.destination.route) {
                  launchSingleTop = true
                  restoreState = true
                }
              },
              onCreatePublicEvent = {
                navController.navigate(Route.CREATE_PUBLIC_EVENT) { launchSingleTop = true }
              },
              onCreatePrivateEvent = {
                navController.navigate(Route.CREATE_PRIVATE_EVENT) { launchSingleTop = true }
              })
        }
      }) { paddingValues ->
        // Use real repository from provider
        val userRepository = UserRepositoryProvider.repository
        val currentUserId = Firebase.auth.currentUser?.uid ?: ""

        NavHost(
            navController = navController,
            startDestination = Route.HOME,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
          composable(Route.HOME) {
            HomeScreen(
                navController = navController,
                shouldOpenQRScanner = shouldOpenQRScanner,
                onQRScannerClosed = { onQRScannerStateChange(false) },
                onCameraActiveChange = { isActive -> isCameraActive = isActive })
          }
          composable(Route.SEARCH) { SearchScreen(navController = navController) }
          composable(Route.MAP) { MapScreen() }
          composable(
              Route.MAP_WITH_LOCATION,
              arguments =
                  listOf(
                      navArgument("latitude") { type = NavType.StringType },
                      navArgument("longitude") { type = NavType.StringType },
                      navArgument("zoom") { type = NavType.StringType },
                      navArgument("eventUid") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                      })) { backStackEntry ->
                val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
                val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
                val zoom = backStackEntry.arguments?.getString("zoom")?.toDoubleOrNull() ?: 15.0
                val eventUid = backStackEntry.arguments?.getString("eventUid")
                MapScreen(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    targetZoom = zoom,
                    targetEventUid = eventUid)
              }
          composable(Route.ACTIVITIES) { ActivitiesScreen(navController) }

          // Profile Screen (Main Profile View)
          composable(Route.PROFILE) {
            ProfileScreen(
                currentUserId = currentUserId,
                userRepository = userRepository,
                navigationCallbacks =
                    ProfileNavigationCallbacks(
                        onNavigateToSettings = { navController.navigate(ProfileRoutes.SETTINGS) },
                        onNavigateToUserCard = { navController.navigate(ProfileRoutes.USER_CARD) },
                        onNavigateToFriendsList = { userId ->
                          navController.navigate(ProfileRoutes.friendsList(userId))
                        },
                        onNavigateToJoinedEvents = { navController.navigate(Route.JOINED_EVENTS) },
                        onNavigateToEventDetails = { eventId ->
                          navController.navigate(Route.eventView(eventId, true))
                        }))
          }

          // Joined Events Screen
          composable(Route.JOINED_EVENTS) {
            JoinedEventsScreen(
                navController = navController, onNavigateBack = { navController.popBackStack() })
          }

          // Visitor Profile Screen (shown when clicking on other users)
          composable(
              route = Route.VISITOR_PROFILE,
              arguments = listOf(navArgument(Route.USER_ID_ARG) { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString(Route.USER_ID_ARG) ?: ""
                val vm: VisitorProfileViewModel = viewModel()
                // Load profile when userId changes
                LaunchedEffect(userId) { if (userId.isNotEmpty()) vm.loadProfile(userId) }

                val uiState by vm.uiState.collectAsState()

                when {
                  uiState.isLoading -> {
                    // simple loading indicator
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      CircularProgressIndicator()
                    }
                  }
                  uiState.user != null -> {
                    VisitorProfileScreen(
                        user = uiState.user!!,
                        onBackClick = { navController.popBackStack() },
                        onAddFriendClick = { vm.sendFriendRequest() },
                        onCancelFriendClick = { vm.cancelFriendRequest() },
                        onRemoveFriendClick = { vm.removeFriend() },
                        friendRequestStatus = uiState.friendRequestStatus)
                  }
                  else -> {
                    // show an error state
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      Text(text = uiState.errorMessage ?: ProfileConstants.ERROR_PROFILE_NOT_FOUND)
                    }
                  }
                }
              }

          // Organization Profile Screen
          composable(
              route = Route.ORGANIZATION_PROFILE,
              arguments =
                  listOf(navArgument(Route.ORGANIZATION_ID_ARG) { type = NavType.StringType })) {
                  backStackEntry ->
                val organizationId =
                    requireNotNull(backStackEntry.arguments?.getString(Route.ORGANIZATION_ID_ARG)) {
                      "Organization ID is required"
                    }
                OrganizationProfileScreen(
                    organizationId = organizationId, onBackClick = { navController.popBackStack() })
              }

          // Friends List Screen
          composable(
              route = ProfileRoutes.FRIENDS_LIST,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId
                FriendsListScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() },
                    onFriendClick = { friendId ->
                      navController.navigate(Route.visitorProfile(friendId))
                    },
                    userRepository = userRepository)
              }

          // User Card Screen
          composable(ProfileRoutes.USER_CARD) {
            UserCardScreen(
                currentUserId = currentUserId,
                userRepository = userRepository,
                onNavigateBack = { navController.popBackStack() })
          }

          // Profile Settings Screen (Edit Profile View)
          composable(ProfileRoutes.SETTINGS) {
            ProfileSettingsScreen(
                currentUserId = currentUserId,
                userRepository = userRepository,
                onNavigateToEditPicture = { userId ->
                  navController.navigate(ProfileRoutes.editPicture(userId))
                },
                onNavigateToEditName = { userId ->
                  navController.navigate(ProfileRoutes.editName(userId))
                },
                onNavigateToEditBio = { userId ->
                  navController.navigate(ProfileRoutes.editBio(userId))
                },
                onNavigateToEditActivities = { userId ->
                  navController.navigate(ProfileRoutes.editActivities(userId))
                },
                onNavigateToEditBirthday = { userId ->
                  navController.navigate(ProfileRoutes.editBirthday(userId))
                },
                onNavigateToEditNationality = { userId ->
                  navController.navigate(ProfileRoutes.editNationality(userId))
                },
                onNavigateBack = { navController.popBackStack() })
          }

          // Edit Profile Picture Screen
          composable(
              route = ProfileRoutes.EDIT_PICTURE,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId
                EditProfilePictureScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() },
                    userRepository = userRepository)
              }

          // Edit Name Screen
          composable(
              route = ProfileRoutes.EDIT_NAME,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId
                EditNameScreen(
                    userId = userId,
                    userRepository = userRepository,
                    onNavigateBack = { navController.popBackStack() })
              }

          // Edit Nationality Screen
          composable(
              route = ProfileRoutes.EDIT_NATIONALITY,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId
                EditNationalityScreen(
                    userId = userId,
                    userRepository = userRepository,
                    onNavigateBack = { navController.popBackStack() })
              }
          // Edit Birthday Screen
          composable(
              route = ProfileRoutes.EDIT_BIRTHDAY,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId
                EditBirthdayScreen(
                    userId = userId,
                    userRepository = userRepository,
                    onNavigateBack = { navController.popBackStack() })
              }
          composable(
              route = ProfileRoutes.EDIT_ACTIVITIES,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId
                EditActivitiesScreen(
                    userId = userId,
                    userRepository = userRepository,
                    onNavigateBack = { navController.popBackStack() })
              }

          // Edit Bio Screen
          composable(
              route = ProfileRoutes.EDIT_BIO,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId
                EditBioScreen(
                    userId = userId,
                    userRepository = userRepository,
                    onNavigateBack = { navController.popBackStack() })
              }

          composable(
              route = "eventView/{eventUid}/{hasJoined}",
              arguments =
                  listOf(
                      navArgument("eventUid") { type = NavType.StringType },
                      navArgument("hasJoined") { type = NavType.BoolType })) { backStackEntry ->
                val eventUid = backStackEntry.arguments?.getString("eventUid")
                requireNotNull(eventUid) { "Event UID is required." }
                EventView(eventUid = eventUid, navController = navController)
              }
          composable(Route.CREATE_PRIVATE_EVENT) {
            CreatePrivateEventScreen(navController = navController)
          }
          composable(Route.CREATE_PUBLIC_EVENT) {
            CreatePublicEventScreen(navController = navController)
          }
          composable(
              Route.EDIT_PRIVATE_EVENT,
              arguments = listOf(navArgument("eventUid") { type = NavType.StringType })) {
                  backStackEntry ->
                val eventUid = backStackEntry.arguments?.getString("eventUid")
                requireNotNull(eventUid) { "Event UID is required to edit a private event." }
                CreatePrivateEventScreen(navController = navController, existingEventId = eventUid)
              }
          composable(
              Route.EDIT_PUBLIC_EVENT,
              arguments = listOf(navArgument("eventUid") { type = NavType.StringType })) {
                  backStackEntry ->
                val eventUid = backStackEntry.arguments?.getString("eventUid")
                requireNotNull(eventUid) { "Event UID is required to edit a public event." }
                CreatePublicEventScreen(navController = navController, existingEventId = eventUid)
              }

          // Poll screens
          composable(
              Route.POLLS_LIST,
              arguments = listOf(navArgument("eventUid") { type = NavType.StringType })) {
                  backStackEntry ->
                val eventUid = backStackEntry.arguments?.getString("eventUid")
                requireNotNull(eventUid) { "Event UID is required for polls list." }
                com.github.se.studentconnect.ui.poll.PollsListScreen(
                    eventUid = eventUid, navController = navController)
              }

          composable(
              Route.POLL_SCREEN,
              arguments =
                  listOf(
                      navArgument("eventUid") { type = NavType.StringType },
                      navArgument("pollUid") { type = NavType.StringType })) { backStackEntry ->
                val eventUid = backStackEntry.arguments?.getString("eventUid")
                val pollUid = backStackEntry.arguments?.getString("pollUid")
                requireNotNull(eventUid) { "Event UID is required for poll screen." }
                requireNotNull(pollUid) { "Poll UID is required for poll screen." }
                com.github.se.studentconnect.ui.poll.PollScreen(
                    eventUid = eventUid, pollUid = pollUid, navController = navController)
              }
        }
      }
}
