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
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.BottomNavigationBar
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import com.github.se.studentconnect.ui.profile.MockUserRepository
import com.github.se.studentconnect.ui.profile.ProfileRoutes
import com.github.se.studentconnect.ui.profile.ProfileSettingsScreen
import com.github.se.studentconnect.ui.profile.edit.EditNameScreen
import com.github.se.studentconnect.ui.profile.edit.EditProfilePictureScreen
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreen
import com.github.se.studentconnect.ui.screen.map.MapScreen
import com.github.se.studentconnect.ui.screens.HomeScreen
import com.github.se.studentconnect.ui.theme.AppTheme
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

@Composable
fun MainContent() {
  val navController = rememberNavController()
  var selectedTab by remember { mutableStateOf<Tab>(Tab.Home) }

  Scaffold(
      bottomBar = {
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tab: Tab ->
              selectedTab = tab
              navController.navigate(tab.destination.route) {
                launchSingleTop = true
                restoreState = true
              }
            },
        )
      }) { paddingValues ->
        // Shared mock repository for all profile screens (for demo purposes)
        val sharedMockRepository = remember { MockUserRepository() }

        NavHost(
            navController = navController,
            startDestination = Route.HOME,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
          composable(Route.HOME) { HomeScreen() }
          composable(Route.MAP) { MapScreen() }
          composable(Route.ACTIVITIES) { ActivitiesScreen() }

          // Profile Settings Screen (Main Profile View)
          composable(Route.PROFILE) {
            ProfileSettingsScreen(
                currentUserId = "mock_user_123",
                userRepository = sharedMockRepository,
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
                onNavigateBack = {
                  // No back navigation needed since this is the main profile view
                })
          }

          // Edit Profile Picture Screen
          composable(
              route = ProfileRoutes.EDIT_PICTURE,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "mock_user_123"
                EditProfilePictureScreen(
                    userId = userId,
                    userRepository = sharedMockRepository,
                    onNavigateBack = { navController.popBackStack() })
              }

          // Edit Name Screen
          composable(
              route = ProfileRoutes.EDIT_NAME,
              arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                  backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "mock_user_123"
                EditNameScreen(
                    userId = userId,
                    userRepository = sharedMockRepository,
                    onNavigateBack = { navController.popBackStack() })
              }
        }
      }
}
