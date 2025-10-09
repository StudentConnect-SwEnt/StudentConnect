package com.github.se.studentconnect.ui.navigation

import androidx.navigation.NavHostController

sealed class Screen(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false,
) {
  object Auth : Screen(route = "auth", name = "Authentication")

  object Home : Screen(route = "home", name = "Home", isTopLevelDestination = true)

  object Map : Screen(route = "map", name = "Map", isTopLevelDestination = true)

  object Profile : Screen(route = "profile", name = "Profile", isTopLevelDestination = true)

  object Events : Screen(route = "events", name = "Events", isTopLevelDestination = true)

  object EventCreation :
      Screen(route = "event_creation", name = "Create Event", isTopLevelDestination = true)
}

open class NavigationActions(private val navController: NavHostController) {
  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: Screen) {
    if (screen.isTopLevelDestination && currentRoute() == screen.route) {
      // If the user is already on the top-level destination, do nothing
      return
    }
    navController.navigate(screen.route) {
      if (screen.isTopLevelDestination) {
        launchSingleTop = true
        popUpTo(screen.route) { inclusive = true }
      }

      if (screen !is Screen.Auth) {
        // Restore state when reselecting a previously selected item
        restoreState = true
      }
    }
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
