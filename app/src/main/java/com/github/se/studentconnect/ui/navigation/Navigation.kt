package com.github.se.studentconnect.ui.navigation

import com.github.se.studentconnect.R

object Route {
  const val AUTH = "auth"
  const val GET_STARTED = "get_started"
  const val BASIC_INFO = "basic_info"
  const val NATIONALITY = "nationality"
  const val ADD_PICTURE = "add_picture"
  const val DESCRIPTION = "description"
  const val EXPERIENCES = "experiences"

  const val HOME = "home"
  const val MAP = "map"
  const val MAP_WITH_LOCATION = "map/{latitude}/{longitude}/{zoom}?eventUid={eventUid}"
  const val ACTIVITIES = "activities"
  const val PROFILE = "profile"
  const val SEARCH = "search"

  const val VISITOR_PROFILE = "visitorProfile/{userId}"
  const val JOINED_EVENTS = "joinedEvents?userId={userId}"
  const val ORGANIZATION_PROFILE = "organizationProfile/{organizationId}"

  const val USER_ID_ARG = "userId"
  const val ORGANIZATION_ID_ARG = "organizationId"

  fun visitorProfile(userId: String): String = "visitorProfile/$userId"

  fun joinedEvents(userId: String? = null): String =
      if (userId != null) "joinedEvents?userId=$userId" else "joinedEvents"

  fun organizationProfile(organizationId: String): String = "organizationProfile/$organizationId"

  const val CREATE_PUBLIC_EVENT = "create_public_event"
  const val CREATE_PRIVATE_EVENT = "create_private_event"
  const val EDIT_PUBLIC_EVENT = "edit_public_event/{eventUid}"
  const val EDIT_PRIVATE_EVENT = "edit_private_event/{eventUid}"

  // Added route for the event view, with eventUid as an argument
  const val EVENT_VIEW = "eventView/{eventUid}"

  // Poll routes
  const val POLLS_LIST = "pollsList/{eventUid}"
  const val POLL_SCREEN = "poll/{eventUid}/{pollUid}"

  // Statistics route
  const val EVENT_STATISTICS = "eventStatistics/{eventUid}"

  fun mapWithLocation(
      latitude: Double,
      longitude: Double,
      zoom: Double = 15.0,
      eventUid: String? = null
  ): String {
    val baseRoute = "map/$latitude/$longitude/$zoom"
    return if (eventUid != null) "$baseRoute?eventUid=$eventUid" else baseRoute
  }

  fun eventView(eventUid: String, hasJoined: Boolean): String {
    return "eventView/$eventUid/$hasJoined"
  }

  fun editPublicEvent(eventUid: String): String = "edit_public_event/$eventUid"

  fun editPrivateEvent(eventUid: String): String = "edit_private_event/$eventUid"

  fun pollsListScreen(eventUid: String): String = "pollsList/$eventUid"

  fun pollScreen(eventUid: String, pollUid: String): String = "poll/$eventUid/$pollUid"

  fun eventStatistics(eventUid: String): String = "eventStatistics/$eventUid"
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

  object JoinedEvents : Screen(route = Route.JOINED_EVENTS, name = "Joined Events")

  object CreatePublicEvent :
      Screen(route = Route.CREATE_PUBLIC_EVENT, name = "Create Public Event")

  object CreatePrivateEvent :
      Screen(route = Route.CREATE_PRIVATE_EVENT, name = "Create Private Event")

  object EditPublicEvent : Screen(route = Route.EDIT_PUBLIC_EVENT, name = "Edit Public Event")

  object EditPrivateEvent : Screen(route = Route.EDIT_PRIVATE_EVENT, name = "Edit Private Event")

  // Added screen for the event view
  object EventView : Screen(route = Route.EVENT_VIEW, name = "Event Details")

  object Search : Screen(route = Route.SEARCH, name = "Search")

  object EventStatistics : Screen(route = Route.EVENT_STATISTICS, name = "Event Statistics")
}

sealed class Tab(val name: String, val icon: Int, val destination: Screen) {
  object Home : Tab("Home", R.drawable.ic_home, Screen.Home)

  object Map : Tab("Map", R.drawable.ic_vector, Screen.Map)

  object Activities : Tab("Activities", R.drawable.ic_ticket, Screen.Activities)

  object Profile : Tab("Profile", R.drawable.ic_user, Screen.Profile)
}

val bottomNavigationTabs = listOf(Tab.Home, Tab.Map, Tab.Activities, Tab.Profile)
