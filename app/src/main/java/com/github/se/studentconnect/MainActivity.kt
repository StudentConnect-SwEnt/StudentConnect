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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.service.NotificationChannelManager
import com.github.se.studentconnect.ui.activities.EventView
import com.github.se.studentconnect.ui.eventcreation.CreatePrivateEventScreen
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreen
import com.github.se.studentconnect.ui.eventcreation.EventTemplateSelectionScreen
import com.github.se.studentconnect.ui.navigation.BottomNavigationBar
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import com.github.se.studentconnect.ui.profile.JoinedEventsViewModel
import com.github.se.studentconnect.ui.profile.ProfileConstants
import com.github.se.studentconnect.ui.profile.ProfileRoutes
import com.github.se.studentconnect.ui.profile.ProfileScreenViewModel
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreen
import com.github.se.studentconnect.ui.screen.home.HomeScreen
import com.github.se.studentconnect.ui.screen.home.NotificationBanner
import com.github.se.studentconnect.ui.screen.home.NotificationViewModel
import com.github.se.studentconnect.ui.screen.home.handleNotificationClick
import com.github.se.studentconnect.ui.screen.map.MapScreen
import com.github.se.studentconnect.ui.screen.profile.FriendsListScreen
import com.github.se.studentconnect.ui.screen.profile.JoinedEventsScreen
import com.github.se.studentconnect.ui.screen.profile.OrganizationManagementScreen
import com.github.se.studentconnect.ui.screen.profile.OrganizationProfileScreen
import com.github.se.studentconnect.ui.screen.profile.ProfileNavigationCallbacks
import com.github.se.studentconnect.ui.screen.profile.ProfileScreen
import com.github.se.studentconnect.ui.screen.profile.ProfileSettingsScreen
import com.github.se.studentconnect.ui.screen.profile.UserCardScreen
import com.github.se.studentconnect.ui.screen.profile.edit.*
import com.github.se.studentconnect.ui.screen.search.SearchScreen
import com.github.se.studentconnect.ui.screen.signup.OnboardingNavigation
import com.github.se.studentconnect.ui.screen.signup.organization.OrganizationSignUpOrchestrator
import com.github.se.studentconnect.ui.screen.signup.regularuser.GetStartedScreen
import com.github.se.studentconnect.ui.screen.statistics.EventStatisticsScreen
import com.github.se.studentconnect.ui.screen.visitorprofile.VisitorProfileCallbacks
import com.github.se.studentconnect.ui.screen.visitorprofile.VisitorProfileScreen
import com.github.se.studentconnect.ui.screen.visitorprofile.VisitorProfileViewModel
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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

    // DISABLED: Schedule periodic event reminder worker (runs every 15 minutes)
    // This worker has been disabled to prevent OutOfMemoryError in CI environments.
    // Notifications are still handled via FCM push notifications and other mechanisms.
    // val eventReminderRequest =
    //     PeriodicWorkRequestBuilder<EventReminderWorker>(15, TimeUnit.MINUTES).build()
    //
    // WorkManager.getInstance(this)
    //     .enqueueUniquePeriodicWork(
    //         "event_reminder_work", ExistingPeriodicWorkPolicy.KEEP, eventReminderRequest)

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
 * ├─> AUTHENTICATION (if no Firebase user)
 * │     └─> ONBOARDING (after sign-in, if no profile exists)
 * │           └─> MAIN_APP (after profile creation)
 * ├─> ONBOARDING (if Firebase user exists but no profile)
 * │     └─> MAIN_APP (after profile creation)
 * └─> MAIN_APP (if Firebase user and profile both exist)
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

  // Track previous app state to detect transitions
  var previousAppState by remember { mutableStateOf<AppState?>(null) }
  var previousUserId by remember { mutableStateOf<String?>(null) }

  // Clear navigation stack and reset to HOME when logging out or when user changes
  LaunchedEffect(uiState.appState, uiState.currentUserId) {
    val stateChanged = previousAppState != uiState.appState
    val userChanged = previousUserId != null && previousUserId != uiState.currentUserId

    if (stateChanged && uiState.appState == AppState.AUTHENTICATION) {
      // Clear the entire back stack when logging out
      if (navController.currentBackStackEntry != null) {
        navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
      }
      selectedTab = Tab.Home
    } else if ((stateChanged && uiState.appState == AppState.MAIN_APP) ||
        (userChanged && uiState.appState == AppState.MAIN_APP && uiState.currentUserId != null)) {
      // When entering MAIN_APP or when user changes, navigate to HOME
      val currentRoute = navController.currentBackStackEntry?.destination?.route
      if (currentRoute != Route.HOME) {
        navController.navigate(Route.HOME) {
          popUpTo(navController.graph.startDestinationId) { inclusive = false }
          launchSingleTop = true
        }
        selectedTab = Tab.Home
      }
    }

    previousAppState = uiState.appState
    previousUserId = uiState.currentUserId
  }

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
          onQRScannerStateChange = onQRScannerStateChange,
          logOut = { viewModel.onLogoutComplete() })
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
 * @param logOut Callback to log out the user
 */
@Composable
internal fun MainAppContent(
    navController: NavHostController,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    shouldOpenQRScanner: Boolean,
    onQRScannerStateChange: (Boolean) -> Unit,
    logOut: () -> Unit = {}
) {
  // Track whether camera mode selector is currently active to conditionally hide bottom nav
  var isCameraActive by remember { mutableStateOf(false) }

  // Notification ViewModel for app-wide notifications
  val notificationViewModel: NotificationViewModel = viewModel()
  val notificationUiState by notificationViewModel.uiState.collectAsState()

  val currentBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentBackStackEntry?.destination?.route

  // Hide bottom bar for create/edit event screens
  val hideBottomBar =
      currentRoute == Route.CREATE_PUBLIC_EVENT ||
          currentRoute == Route.CREATE_PRIVATE_EVENT ||
          currentRoute == Route.EDIT_PUBLIC_EVENT ||
          currentRoute == Route.EDIT_PRIVATE_EVENT ||
          isCameraActive

  Box(modifier = Modifier.fillMaxSize()) {
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
                },
                onCreateFromTemplate = {
                  navController.navigate(Route.SELECT_EVENT_TEMPLATE) { launchSingleTop = true }
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
              // Use currentUserId as key to ensure ViewModel is recreated when user changes
              val profileViewModel: ProfileScreenViewModel =
                  viewModel(
                      key = "profile_$currentUserId",
                      factory =
                          object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                              return ProfileScreenViewModel(
                                  userRepository = userRepository,
                                  friendsRepository = FriendsRepositoryProvider.repository,
                                  eventRepository = EventRepositoryProvider.repository,
                                  organizationRepository =
                                      OrganizationRepositoryProvider.repository,
                                  currentUserId = currentUserId)
                                  as T
                            }
                          })
              ProfileScreen(
                  currentUserId = currentUserId,
                  userRepository = userRepository,
                  viewModel = profileViewModel,
                  navigationCallbacks =
                      ProfileNavigationCallbacks(
                          onNavigateToSettings = { navController.navigate(ProfileRoutes.SETTINGS) },
                          onNavigateToUserCard = {
                            navController.navigate(ProfileRoutes.USER_CARD)
                          },
                          onNavigateToFriendsList = { userId ->
                            navController.navigate(ProfileRoutes.friendsList(userId))
                          },
                          onNavigateToJoinedEvents = {
                            navController.navigate(Route.JOINED_EVENTS)
                          },
                          onNavigateToEventDetails = { eventId ->
                            navController.navigate(Route.eventView(eventId, true))
                          },
                          onNavigateToOrganizationManagement = {
                            navController.navigate(ProfileRoutes.ORGANIZATION_MANAGEMENT)
                          }),
                  logout = logOut)
            }

            // Joined Events Screen
            composable(
                route = Route.JOINED_EVENTS,
                arguments =
                    listOf(
                        navArgument("userId") {
                          type = NavType.StringType
                          nullable = true
                          defaultValue = null
                        })) { backStackEntry ->
                  val userId = backStackEntry.arguments?.getString("userId")
                  val viewModel: JoinedEventsViewModel =
                      viewModel(
                          factory =
                              object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                  @Suppress("UNCHECKED_CAST")
                                  return JoinedEventsViewModel(targetUserId = userId) as T
                                }
                              })
                  JoinedEventsScreen(
                      navController = navController,
                      viewModel = viewModel,
                      userId = userId,
                      onNavigateBack = { navController.popBackStack() })
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
                  val friendsCount by vm.friendsCount.collectAsState()
                  val eventsCount by vm.eventsCount.collectAsState()
                  val pinnedEvents by vm.pinnedEvents.collectAsState()

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
                          friendsCount = friendsCount,
                          eventsCount = eventsCount,
                          pinnedEvents = pinnedEvents,
                          callbacks =
                              VisitorProfileCallbacks(
                                  onBackClick = { navController.popBackStack() },
                                  onAddFriendClick = { vm.sendFriendRequest() },
                                  onCancelFriendClick = { vm.cancelFriendRequest() },
                                  onRemoveFriendClick = { vm.removeFriend() },
                                  onFriendsClick = {
                                    navController.navigate(
                                        ProfileRoutes.FRIENDS_LIST.replace("{userId}", userId))
                                  },
                                  onEventsClick = {
                                    navController.navigate(Route.joinedEvents(userId))
                                  },
                                  onEventClick = { event ->
                                    navController.navigate(Route.eventView(event.uid, true))
                                  }),
                          friendRequestStatus = uiState.friendRequestStatus)
                    }
                    else -> {
                      // show an error state
                      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.errorMessage ?: ProfileConstants.ERROR_PROFILE_NOT_FOUND)
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
                      requireNotNull(
                          backStackEntry.arguments?.getString(Route.ORGANIZATION_ID_ARG)) {
                            "Organization ID is required"
                          }
                  OrganizationProfileScreen(
                      organizationId = organizationId,
                      onBackClick = { navController.popBackStack() })
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

            // Organization Management Screen
            composable(ProfileRoutes.ORGANIZATION_MANAGEMENT) {
              OrganizationManagementScreen(
                  currentUserId = currentUserId,
                  onBack = { navController.popBackStack() },
                  onCreateOrganization = {
                    // Navigate to organization creation route
                    navController.navigate(ProfileRoutes.CREATE_ORGANIZATION)
                  },
                  onOrganizationClick = { organizationId ->
                    navController.navigate(Route.organizationProfile(organizationId))
                  })
            }

            // Create Organization Screen
            composable(ProfileRoutes.CREATE_ORGANIZATION) {
              OrganizationSignUpOrchestrator(
                  firebaseUserId = currentUserId,
                  onBackToSelection = {
                    // User cancelled organization creation
                    navController.popBackStack()
                  })
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

            // Create from template routes
            composable(Route.SELECT_EVENT_TEMPLATE) {
              EventTemplateSelectionScreen(navController = navController)
            }

            composable(
                Route.CREATE_PUBLIC_EVENT_FROM_TEMPLATE,
                arguments =
                    listOf(navArgument("templateEventUid") { type = NavType.StringType })) {
                    backStackEntry ->
                  val templateEventUid = backStackEntry.arguments?.getString("templateEventUid")
                  requireNotNull(templateEventUid) { "Template Event UID is required." }
                  CreatePublicEventScreen(
                      navController = navController, templateEventId = templateEventUid)
                }

            composable(
                Route.CREATE_PRIVATE_EVENT_FROM_TEMPLATE,
                arguments =
                    listOf(navArgument("templateEventUid") { type = NavType.StringType })) {
                    backStackEntry ->
                  val templateEventUid = backStackEntry.arguments?.getString("templateEventUid")
                  requireNotNull(templateEventUid) { "Template Event UID is required." }
                  CreatePrivateEventScreen(
                      navController = navController, templateEventId = templateEventUid)
                }

            composable(
                Route.EDIT_PRIVATE_EVENT,
                arguments = listOf(navArgument("eventUid") { type = NavType.StringType })) {
                    backStackEntry ->
                  val eventUid = backStackEntry.arguments?.getString("eventUid")
                  requireNotNull(eventUid) { "Event UID is required to edit a private event." }
                  CreatePrivateEventScreen(
                      navController = navController, existingEventId = eventUid)
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

            // Event Statistics screen
            composable(
                Route.EVENT_STATISTICS,
                arguments = listOf(navArgument("eventUid") { type = NavType.StringType })) {
                    backStackEntry ->
                  val eventUid = backStackEntry.arguments?.getString("eventUid")
                  requireNotNull(eventUid) { "Event UID is required for statistics screen." }
                  EventStatisticsScreen(eventUid = eventUid, navController = navController)
                }

            // Event Chat screen
            composable(
                Route.EVENT_CHAT,
                arguments = listOf(navArgument("eventUid") { type = NavType.StringType })) {
                    backStackEntry ->
                  val eventUid = backStackEntry.arguments?.getString("eventUid")
                  requireNotNull(eventUid) { "Event UID is required for chat screen." }
                  com.github.se.studentconnect.ui.chat.EventChatScreen(
                      eventId = eventUid, navController = navController)
                }
          }
        }

    // App-wide notification banner
    NotificationBanner(
        notification = notificationUiState.latestNotification,
        onDismiss = { notificationViewModel.clearLatestNotification() },
        onClick = {
          notificationUiState.latestNotification?.let { notification ->
            handleNotificationClick(
                notification,
                navController,
                { notificationViewModel.markAsRead(it) },
                { notificationViewModel.clearLatestNotification() })
          }
        })
  }
}
