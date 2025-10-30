package com.github.se.studentconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.se.studentconnect.model.notification.NotificationRepositoryFirestore
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.service.EventReminderWorker
import com.github.se.studentconnect.service.NotificationChannelManager
import com.github.se.studentconnect.ui.activities.EventView
import com.github.se.studentconnect.ui.navigation.BottomNavigationBar
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreen
import com.github.se.studentconnect.ui.screen.map.MapScreen
import com.github.se.studentconnect.ui.screen.profile.ProfileScreen
import com.github.se.studentconnect.ui.screen.profile.VisitorProfileRoute
import com.github.se.studentconnect.ui.screen.signup.GetStartedScreen
import com.github.se.studentconnect.ui.screen.signup.SignUpOrchestrator
import com.github.se.studentconnect.ui.screens.HomeScreen
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
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

    // Initialize notification repository
    NotificationRepositoryProvider.setRepository(
        NotificationRepositoryFirestore(FirebaseFirestore.getInstance()))

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
  val uiState by viewModel.uiState.collectAsState()

  // Initial auth check on app start
  LaunchedEffect(Unit) { viewModel.checkInitialAuthState() }

  // Render based on app state from ViewModel
  when (uiState.appState) {
    AppState.LOADING -> {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // Loading screen
      }
    }
    AppState.AUTHENTICATION -> {
      GetStartedScreen(
          onSignedIn = { uid ->
            val firebaseUser = Firebase.auth.currentUser
            viewModel.onUserSignedIn(uid, firebaseUser?.email ?: "")
          })
    }
    AppState.ONBOARDING -> {
      if (uiState.currentUserId != null && uiState.currentUserEmail != null) {
        android.util.Log.d("MainActivity", "Showing onboarding for: ${uiState.currentUserId}")
        SignUpOrchestrator(
            firebaseUserId = uiState.currentUserId!!,
            email = uiState.currentUserEmail!!,
            userRepository = userRepository,
            onSignUpComplete = { user ->
              android.util.Log.d("MainActivity", "Onboarding complete: ${user.userId}")
              viewModel.onUserProfileCreated()
            })
      }
    }
    AppState.MAIN_APP -> {
      MainAppContent(
          navController = navController,
          selectedTab = selectedTab,
          onTabSelected = { selectedTab = it },
          shouldOpenQRScanner = shouldOpenQRScanner,
          onQRScannerStateChange = { shouldOpenQRScanner = it })
    }
  }
}

@Composable
private fun MainAppContent(
    navController: androidx.navigation.NavHostController,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    shouldOpenQRScanner: Boolean,
    onQRScannerStateChange: (Boolean) -> Unit
) {
  Scaffold(
      bottomBar = {
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
      }) { paddingValues ->
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
                onQRScannerClosed = { onQRScannerStateChange(false) })
          }
          composable(Route.MAP) { MapScreen() }
          composable(
              Route.MAP_WITH_LOCATION,
              arguments =
                  listOf(
                      navArgument("latitude") { type = NavType.StringType },
                      navArgument("longitude") { type = NavType.StringType },
                      navArgument("zoom") { type = NavType.StringType })) { backStackEntry ->
                val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
                val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
                val zoom = backStackEntry.arguments?.getString("zoom")?.toDoubleOrNull() ?: 15.0
                MapScreen(targetLatitude = latitude, targetLongitude = longitude, targetZoom = zoom)
              }
          composable(Route.ACTIVITIES) { ActivitiesScreen(navController) }
          composable(Route.PROFILE) {
            ProfileScreen(currentUserId = AuthenticationProvider.currentUser)
          }
          composable(
              Route.VISITOR_PROFILE,
              arguments = listOf(navArgument(Route.USER_ID_ARG) { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString(Route.USER_ID_ARG)
                requireNotNull(userId) { "User ID is required." }
                VisitorProfileRoute(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onScanAgain = {
                      onQRScannerStateChange(true)
                      navController.popBackStack()
                    })
              }
          composable(
              route = "eventView/{eventUid}/{hasJoined}",
              arguments =
                  listOf(
                      navArgument("eventUid") { type = NavType.StringType },
                      navArgument("hasJoined") { type = NavType.BoolType })) { backStackEntry ->
                val eventUid = backStackEntry.arguments?.getString("eventUid")
                val hasJoined = backStackEntry.arguments?.getBoolean("hasJoined") ?: false
                requireNotNull(eventUid) { "Event UID is required." }
                EventView(eventUid = eventUid, navController = navController, hasJoined = hasJoined)
              }
        }
      }
}
