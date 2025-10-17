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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
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
import okhttp3.OkHttpClient

/**
 * Provide an OkHttpClient client for network requests.
 *
 * Property `client` is mutable for testing purposes.
 */
object HttpClientProvider {
  var client: OkHttpClient = OkHttpClient()
}

/** App state machine with clear states for managing authentication and onboarding flow. */
private enum class AppState {
  LOADING, // Checking auth status on app start
  AUTHENTICATION, // Show GetStartedScreen (not authenticated)
  ONBOARDING, // Show SignUpOrchestrator (authenticated but no profile)
  MAIN_APP // Show main app with navigation (authenticated with profile)
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
 * Main content composable that manages the entire app flow using a state machine.
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
 * 1. App starts → LOADING state
 * 2. No Firebase user found → AUTHENTICATION state (GetStartedScreen)
 * 3. User signs in with Google → currentUserId updated
 * 4. LaunchedEffect checks profile → no profile exists → ONBOARDING state
 * 5. User completes onboarding → profile saved to Firestore
 * 6. onSignUpComplete callback → MAIN_APP state
 *
 * **Returning User Flow:**
 * 1. App starts → LOADING state
 * 2. Firebase user found → checks Firestore for profile
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

  var appState by remember { mutableStateOf(AppState.LOADING) }
  var currentUserId by remember { mutableStateOf<String?>(null) }
  var currentUserEmail by remember { mutableStateOf<String?>(null) }

  val userRepository = UserRepositoryProvider.repository

  // Initial auth check on app start
  LaunchedEffect(Unit) {
    if (!AuthenticationProvider.local) {
      // Production mode: Check Firebase Auth state
      val firebaseUser = Firebase.auth.currentUser

      if (firebaseUser == null) {
        // No Firebase user - needs authentication
        android.util.Log.d("MainActivity", "No authenticated user - showing GetStarted")
        appState = AppState.AUTHENTICATION
      } else {
        // Firebase user exists - check if profile exists in Firestore
        android.util.Log.d("MainActivity", "Found authenticated user: ${firebaseUser.uid}")
        currentUserId = firebaseUser.uid
        currentUserEmail = firebaseUser.email ?: ""

        val existingUser = userRepository.getUserById(firebaseUser.uid)
        if (existingUser != null) {
          android.util.Log.d("MainActivity", "User profile found - showing main app")
          appState = AppState.MAIN_APP
        } else {
          android.util.Log.d("MainActivity", "No user profile - showing onboarding")
          appState = AppState.ONBOARDING
        }
      }
    } else {
      // Local testing mode
      android.util.Log.d("MainActivity", "Local testing mode")
      currentUserId = AuthenticationProvider.currentUser
      currentUserEmail = "test@epfl.ch"

      val existingUser = userRepository.getUserById(AuthenticationProvider.currentUser)
      appState = if (existingUser == null) AppState.ONBOARDING else AppState.MAIN_APP
    }
  }

  // Handle post-authentication check
  LaunchedEffect(currentUserId) {
    if (currentUserId != null && appState == AppState.AUTHENTICATION) {
      android.util.Log.d(
          "MainActivity", "Checking profile for newly authenticated user: $currentUserId")
      val existingUser = userRepository.getUserById(currentUserId!!)
      if (existingUser != null) {
        android.util.Log.d("MainActivity", "Returning user - showing main app")
        appState = AppState.MAIN_APP
      } else {
        android.util.Log.d("MainActivity", "First-time user - showing onboarding")
        appState = AppState.ONBOARDING
      }
    }
  }

  // Render based on app state
  when (appState) {
    AppState.LOADING -> {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // Loading screen
      }
    }
    AppState.AUTHENTICATION -> {
      GetStartedScreen(
          onSignedIn = { uid ->
            android.util.Log.d("MainActivity", "User signed in: $uid")
            val firebaseUser = Firebase.auth.currentUser
            currentUserId = uid
            currentUserEmail = firebaseUser?.email ?: ""
          })
    }
    AppState.ONBOARDING -> {
      if (currentUserId != null && currentUserEmail != null) {
        android.util.Log.d("MainActivity", "Showing onboarding for: $currentUserId")
        SignUpOrchestrator(
            firebaseUserId = currentUserId!!,
            email = currentUserEmail!!,
            userRepository = userRepository,
            onSignUpComplete = { user ->
              android.util.Log.d("MainActivity", "Onboarding complete: ${user.userId}")
              appState = AppState.MAIN_APP
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
