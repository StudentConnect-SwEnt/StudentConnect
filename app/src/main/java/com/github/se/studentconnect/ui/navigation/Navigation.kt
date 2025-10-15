package com.github.se.studentconnect.ui.navigation

import com.github.se.studentconnect.R

object Route {
  const val AUTH = "auth"
  const val HOME = "home"
  const val MAP = "map"
  const val MAP_WITH_LOCATION = "map/{latitude}/{longitude}/{zoom}"
  const val ACTIVITIES = "activities"
  const val PROFILE = "profile"
  const val CREATE_PUBLIC_EVENT = "create_public_event"
  const val CREATE_PRIVATE_EVENT = "create_private_event"

  fun mapWithLocation(latitude: Double, longitude: Double, zoom: Double = 15.0): String {
    return "map/$latitude/$longitude/$zoom"
  }
}

sealed class Screen(
  val route: String,
  val name: String,
  val isTopLevelDestination: Boolean = false,
) {
  object Auth : Screen(route = Route.AUTH, name = "Authentication")

  object Home : Screen(route = Route.HOME, name = "Home", isTopLevelDestination = true)

  object Map : Screen(route = Route.MAP, name = "Map", isTopLevelDestination = true)

  object Activities :
    Screen(route = Route.ACTIVITIES, name = "Activities", isTopLevelDestination = true)

  object Profile : Screen(route = Route.PROFILE, name = "Profile", isTopLevelDestination = true)

  object CreatePublicEvent :
    Screen(route = Route.CREATE_PUBLIC_EVENT, name = "Create Public Event")

  object CreatePrivateEvent :
    Screen(route = Route.CREATE_PRIVATE_EVENT, name = "Create Private Event")
}

sealed class Tab(val name: String, val icon: Int, val destination: Screen) {
  object Home : Tab("Home", R.drawable.ic_home, Screen.Home)

  object Map : Tab("Map", R.drawable.ic_vector, Screen.Map)

  object Activities : Tab("Activities", R.drawable.ic_ticket, Screen.Activities)

  object Profile : Tab("Profile", R.drawable.ic_user, Screen.Profile)
}

val bottomNavigationTabs = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile)