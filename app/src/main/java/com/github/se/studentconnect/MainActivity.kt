package com.github.se.studentconnect

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.activities.ActivitiesScreen
import com.github.se.studentconnect.ui.eventcreation.CreatePrivateEventScreen
import com.github.se.studentconnect.ui.eventcreation.CreatePublicEventScreen
import com.github.se.studentconnect.ui.navigation.BottomNavigationBar
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import com.github.se.studentconnect.ui.screen.home.HomeScreen
import com.github.se.studentconnect.ui.screen.map.MapScreen
import com.github.se.studentconnect.ui.screen.profile.ProfileScreen
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

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              MainContent()
            }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  // Sync selected tab with current route
  val selectedTab =
      when (currentRoute) {
        Route.HOME -> Tab.Home
        Route.MAP -> Tab.Map
        Route.ACTIVITIES -> Tab.Activities
        Route.PROFILE -> Tab.Profile
        else -> Tab.Home
      }

  Scaffold(
      topBar = {
        when (currentRoute) {
          Route.CREATE_PUBLIC_EVENT -> {
            TopAppBar(
                title = { Text("Public Event Creation") },
                navigationIcon = {
                  IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
                })
          }
          Route.CREATE_PRIVATE_EVENT -> {
            TopAppBar(
                title = { Text("Private Event Creation") },
                navigationIcon = {
                  IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
                })
          }
        }
      },
      bottomBar = {
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tab: Tab ->
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
            exitTransition = { ExitTransition.None }) {
              composable(Route.HOME) { HomeScreen() }
              composable(Route.MAP) { MapScreen() }
              composable(
                  Route.MAP_WITH_LOCATION,
                  arguments =
                      listOf(
                          navArgument("latitude") { type = NavType.StringType },
                          navArgument("longitude") { type = NavType.StringType },
                          navArgument("zoom") { type = NavType.StringType })) { backStackEntry ->
                    val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
                    val longitude =
                        backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
                    val zoom = backStackEntry.arguments?.getString("zoom")?.toDoubleOrNull() ?: 15.0
                    MapScreen(
                        targetLatitude = latitude, targetLongitude = longitude, targetZoom = zoom)
                  }
              composable(Route.ACTIVITIES) { ActivitiesScreen(navController) }
              composable(Route.PROFILE) { ProfileScreen() }
              composable(Route.CREATE_PUBLIC_EVENT) { CreatePublicEventScreen() }
              composable(Route.CREATE_PRIVATE_EVENT) { CreatePrivateEventScreen() }
            }
      }
}
