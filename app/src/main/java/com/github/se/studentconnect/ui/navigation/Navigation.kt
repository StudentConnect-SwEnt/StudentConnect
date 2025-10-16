package com.github.se.studentconnect.ui.navigation

import com.github.se.studentconnect.R

/** List of possible routes in the navigation bar * */
object Route {
  const val AUTH = "auth"
  const val HOME = "home"
  const val MAP = "map"
  const val ACTIVITIES = "activities"
  const val PROFILE = "profile"
}

/** Represents different screens in the app with their routes and top level destination status. */
sealed class Screen(
    val name: String,
    val route: String,
    val isTopLevelDestination: Boolean = false,
) {
  object Auth : Screen("Authentication", route = Route.AUTH)

  object Home : Screen("Home", route = Route.HOME, isTopLevelDestination = true)

  object Map : Screen("Map", route = Route.MAP, isTopLevelDestination = true)

  object Activities : Screen("Activities", route = Route.ACTIVITIES, isTopLevelDestination = true)

  object Profile : Screen("Profile", route = Route.PROFILE, isTopLevelDestination = true)
}

/** Represents a tab in the bottom navigation bar with their icon and destination screen. */
sealed class Tab(val icon: Int, val destination: Screen) {
  object Home : Tab(R.drawable.ic_home, Screen.Home)

  object Map : Tab(R.drawable.ic_vector, Screen.Map)

  object Activities : Tab(R.drawable.ic_ticket, Screen.Activities)

  object Profile : Tab(R.drawable.ic_user, Screen.Profile)
}

val bottomNavigationTabs = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile)
