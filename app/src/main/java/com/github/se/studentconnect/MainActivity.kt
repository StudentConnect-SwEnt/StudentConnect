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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.BottomNavigationBar
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import com.github.se.studentconnect.ui.screen.activities.ActivitiesScreen
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
            onCenterButtonClick = {
              // Handle center button click - placeholder for now
            },
        )
      }
  ) { paddingValues ->
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
      composable(Route.PROFILE) { ProfileScreen() }
    }
  }
}
