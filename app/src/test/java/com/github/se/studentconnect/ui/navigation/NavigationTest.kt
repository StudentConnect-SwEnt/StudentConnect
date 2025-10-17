package com.github.se.studentconnect.ui.navigation

import com.github.se.studentconnect.R
import org.junit.Assert.*
import org.junit.Test

class NavigationTest {

  // Route constants tests
  @Test
  fun route_authConstant_hasCorrectValue() {
    assertEquals("auth", Route.AUTH)
  }

  @Test
  fun route_homeConstant_hasCorrectValue() {
    assertEquals("home", Route.HOME)
  }

  @Test
  fun route_mapConstant_hasCorrectValue() {
    assertEquals("map", Route.MAP)
  }

  @Test
  fun route_mapWithLocationConstant_hasCorrectValue() {
    assertEquals("map/{latitude}/{longitude}/{zoom}", Route.MAP_WITH_LOCATION)
  }

  @Test
  fun route_activitiesConstant_hasCorrectValue() {
    assertEquals("activities", Route.ACTIVITIES)
  }

  @Test
  fun route_profileConstant_hasCorrectValue() {
    assertEquals("profile", Route.PROFILE)
  }

  @Test
  fun route_visitorProfileConstant_hasCorrectValue() {
    assertEquals("visitorProfile/{userId}", Route.VISITOR_PROFILE)
  }

  @Test
  fun route_userIdArgConstant_hasCorrectValue() {
    assertEquals("userId", Route.USER_ID_ARG)
  }

  @Test
  fun route_createPublicEventConstant_hasCorrectValue() {
    assertEquals("create_public_event", Route.CREATE_PUBLIC_EVENT)
  }

  @Test
  fun route_createPrivateEventConstant_hasCorrectValue() {
    assertEquals("create_private_event", Route.CREATE_PRIVATE_EVENT)
  }

  @Test
  fun route_eventViewConstant_hasCorrectValue() {
    assertEquals("eventView/{eventUid}", Route.EVENT_VIEW)
  }

  // Route.visitorProfile() function tests
  @Test
  fun route_visitorProfile_generatesCorrectRoute() {
    val result = Route.visitorProfile("user123")
    assertEquals("visitorProfile/user123", result)
  }

  @Test
  fun route_visitorProfile_handlesUserIdWithSpecialCharacters() {
    val result = Route.visitorProfile("user-123_abc")
    assertEquals("visitorProfile/user-123_abc", result)
  }

  @Test
  fun route_visitorProfile_handlesEmptyUserId() {
    val result = Route.visitorProfile("")
    assertEquals("visitorProfile/", result)
  }

  @Test
  fun route_visitorProfile_handlesLongUserId() {
    val longId = "a".repeat(100)
    val result = Route.visitorProfile(longId)
    assertEquals("visitorProfile/$longId", result)
  }

  // Route.mapWithLocation() function tests
  @Test
  fun route_mapWithLocation_generatesCorrectRouteWithDefaultZoom() {
    val result = Route.mapWithLocation(46.5197, 6.6323)
    assertEquals("map/46.5197/6.6323/15.0", result)
  }

  @Test
  fun route_mapWithLocation_generatesCorrectRouteWithCustomZoom() {
    val result = Route.mapWithLocation(46.5197, 6.6323, 10.5)
    assertEquals("map/46.5197/6.6323/10.5", result)
  }

  @Test
  fun route_mapWithLocation_handlesNegativeCoordinates() {
    val result = Route.mapWithLocation(-33.8688, -151.2093, 12.0)
    assertEquals("map/-33.8688/-151.2093/12.0", result)
  }

  @Test
  fun route_mapWithLocation_handlesZeroCoordinates() {
    val result = Route.mapWithLocation(0.0, 0.0, 1.0)
    assertEquals("map/0.0/0.0/1.0", result)
  }

  @Test
  fun route_mapWithLocation_handlesLargeZoomValue() {
    val result = Route.mapWithLocation(40.7128, -74.0060, 20.0)
    assertEquals("map/40.7128/-74.006/20.0", result)
  }

  @Test
  fun route_mapWithLocation_handlesSmallZoomValue() {
    val result = Route.mapWithLocation(51.5074, -0.1278, 0.5)
    assertEquals("map/51.5074/-0.1278/0.5", result)
  }

  // Route.eventView() function tests
  @Test
  fun route_eventView_generatesCorrectRouteWhenJoined() {
    val result = Route.eventView("event123", true)
    assertEquals("eventView/event123/true", result)
  }

  @Test
  fun route_eventView_generatesCorrectRouteWhenNotJoined() {
    val result = Route.eventView("event456", false)
    assertEquals("eventView/event456/false", result)
  }

  @Test
  fun route_eventView_handlesEventUidWithSpecialCharacters() {
    val result = Route.eventView("event-123_abc", true)
    assertEquals("eventView/event-123_abc/true", result)
  }

  @Test
  fun route_eventView_handlesEmptyEventUid() {
    val result = Route.eventView("", false)
    assertEquals("eventView//false", result)
  }

  // Screen.Auth tests
  @Test
  fun screen_auth_hasCorrectRoute() {
    assertEquals(Route.AUTH, Screen.Auth.route)
  }

  @Test
  fun screen_auth_hasCorrectName() {
    assertEquals("Authentication", Screen.Auth.name)
  }

  @Test
  fun screen_auth_isNotTopLevelDestination() {
    assertFalse(Screen.Auth.isTopLevelDestination)
  }

  // Screen.Home tests
  @Test
  fun screen_home_hasCorrectRoute() {
    assertEquals(Route.HOME, Screen.Home.route)
  }

  @Test
  fun screen_home_hasCorrectName() {
    assertEquals("Home", Screen.Home.name)
  }

  @Test
  fun screen_home_isTopLevelDestination() {
    assertTrue(Screen.Home.isTopLevelDestination)
  }

  // Screen.Map tests
  @Test
  fun screen_map_hasCorrectRoute() {
    assertEquals(Route.MAP, Screen.Map.route)
  }

  @Test
  fun screen_map_hasCorrectName() {
    assertEquals("Map", Screen.Map.name)
  }

  @Test
  fun screen_map_isTopLevelDestination() {
    assertTrue(Screen.Map.isTopLevelDestination)
  }

  // Screen.Activities tests
  @Test
  fun screen_activities_hasCorrectRoute() {
    assertEquals(Route.ACTIVITIES, Screen.Activities.route)
  }

  @Test
  fun screen_activities_hasCorrectName() {
    assertEquals("Activities", Screen.Activities.name)
  }

  @Test
  fun screen_activities_isTopLevelDestination() {
    assertTrue(Screen.Activities.isTopLevelDestination)
  }

  // Screen.Profile tests
  @Test
  fun screen_profile_hasCorrectRoute() {
    assertEquals(Route.PROFILE, Screen.Profile.route)
  }

  @Test
  fun screen_profile_hasCorrectName() {
    assertEquals("Profile", Screen.Profile.name)
  }

  @Test
  fun screen_profile_isTopLevelDestination() {
    assertTrue(Screen.Profile.isTopLevelDestination)
  }

  // Screen.CreatePublicEvent tests
  @Test
  fun screen_createPublicEvent_hasCorrectRoute() {
    assertEquals(Route.CREATE_PUBLIC_EVENT, Screen.CreatePublicEvent.route)
  }

  @Test
  fun screen_createPublicEvent_hasCorrectName() {
    assertEquals("Create Public Event", Screen.CreatePublicEvent.name)
  }

  @Test
  fun screen_createPublicEvent_isNotTopLevelDestination() {
    assertFalse(Screen.CreatePublicEvent.isTopLevelDestination)
  }

  // Screen.CreatePrivateEvent tests
  @Test
  fun screen_createPrivateEvent_hasCorrectRoute() {
    assertEquals(Route.CREATE_PRIVATE_EVENT, Screen.CreatePrivateEvent.route)
  }

  @Test
  fun screen_createPrivateEvent_hasCorrectName() {
    assertEquals("Create Private Event", Screen.CreatePrivateEvent.name)
  }

  @Test
  fun screen_createPrivateEvent_isNotTopLevelDestination() {
    assertFalse(Screen.CreatePrivateEvent.isTopLevelDestination)
  }

  // Screen.EventView tests
  @Test
  fun screen_eventView_hasCorrectRoute() {
    assertEquals(Route.EVENT_VIEW, Screen.EventView.route)
  }

  @Test
  fun screen_eventView_hasCorrectName() {
    assertEquals("Event Details", Screen.EventView.name)
  }

  @Test
  fun screen_eventView_isNotTopLevelDestination() {
    assertFalse(Screen.EventView.isTopLevelDestination)
  }

  // Tab.Home tests
  @Test
  fun tab_home_hasCorrectName() {
    assertEquals("Home", Tab.Home.name)
  }

  @Test
  fun tab_home_hasCorrectIcon() {
    assertEquals(R.drawable.ic_home, Tab.Home.icon)
  }

  @Test
  fun tab_home_hasCorrectDestination() {
    assertEquals(Screen.Home, Tab.Home.destination)
  }

  // Tab.Map tests
  @Test
  fun tab_map_hasCorrectName() {
    assertEquals("Map", Tab.Map.name)
  }

  @Test
  fun tab_map_hasCorrectIcon() {
    assertEquals(R.drawable.ic_vector, Tab.Map.icon)
  }

  @Test
  fun tab_map_hasCorrectDestination() {
    assertEquals(Screen.Map, Tab.Map.destination)
  }

  // Tab.Activities tests
  @Test
  fun tab_activities_hasCorrectName() {
    assertEquals("Activities", Tab.Activities.name)
  }

  @Test
  fun tab_activities_hasCorrectIcon() {
    assertEquals(R.drawable.ic_ticket, Tab.Activities.icon)
  }

  @Test
  fun tab_activities_hasCorrectDestination() {
    assertEquals(Screen.Activities, Tab.Activities.destination)
  }

  // Tab.Profile tests
  @Test
  fun tab_profile_hasCorrectName() {
    assertEquals("Profile", Tab.Profile.name)
  }

  @Test
  fun tab_profile_hasCorrectIcon() {
    assertEquals(R.drawable.ic_user, Tab.Profile.icon)
  }

  @Test
  fun tab_profile_hasCorrectDestination() {
    assertEquals(Screen.Profile, Tab.Profile.destination)
  }

  // bottomNavigationTabs list tests
  @Test
  fun bottomNavigationTabs_hasCorrectSize() {
    assertEquals(4, bottomNavigationTabs.size)
  }

  @Test
  fun bottomNavigationTabs_containsHomeTab() {
    assertTrue(bottomNavigationTabs.contains(Tab.Home))
  }

  @Test
  fun bottomNavigationTabs_containsMapTab() {
    assertTrue(bottomNavigationTabs.contains(Tab.Map))
  }

  @Test
  fun bottomNavigationTabs_containsActivitiesTab() {
    assertTrue(bottomNavigationTabs.contains(Tab.Activities))
  }

  @Test
  fun bottomNavigationTabs_containsProfileTab() {
    assertTrue(bottomNavigationTabs.contains(Tab.Profile))
  }

  @Test
  fun bottomNavigationTabs_hasCorrectOrder() {
    assertEquals(Tab.Home, bottomNavigationTabs[0])
    assertEquals(Tab.Map, bottomNavigationTabs[1])
    assertEquals(Tab.Activities, bottomNavigationTabs[2])
    assertEquals(Tab.Profile, bottomNavigationTabs[3])
  }

  @Test
  fun bottomNavigationTabs_allDestinationsAreTopLevel() {
    bottomNavigationTabs.forEach { tab ->
      assertTrue(
          "Tab ${tab.name} destination should be top level", tab.destination.isTopLevelDestination)
    }
  }

  @Test
  fun bottomNavigationTabs_allTabsHaveUniqueNames() {
    val names = bottomNavigationTabs.map { it.name }.toSet()
    assertEquals(bottomNavigationTabs.size, names.size)
  }

  @Test
  fun bottomNavigationTabs_allTabsHaveUniqueIcons() {
    val icons = bottomNavigationTabs.map { it.icon }.toSet()
    assertEquals(bottomNavigationTabs.size, icons.size)
  }

  @Test
  fun bottomNavigationTabs_allTabsHaveNonBlankNames() {
    bottomNavigationTabs.forEach { tab -> assertTrue(tab.name.isNotBlank()) }
  }

  // Cross-validation tests
  @Test
  fun screen_allTopLevelDestinations_areInBottomNavigationTabs() {
    val topLevelScreens =
        listOf(Screen.Home, Screen.Map, Screen.Activities, Screen.Profile).filter {
          it.isTopLevelDestination
        }
    val bottomNavDestinations = bottomNavigationTabs.map { it.destination }

    topLevelScreens.forEach { screen ->
      assertTrue(
          "Screen ${screen.name} should be in bottom navigation", screen in bottomNavDestinations)
    }
  }

  @Test
  fun route_allConstants_areNotBlank() {
    assertTrue(Route.AUTH.isNotBlank())
    assertTrue(Route.HOME.isNotBlank())
    assertTrue(Route.MAP.isNotBlank())
    assertTrue(Route.MAP_WITH_LOCATION.isNotBlank())
    assertTrue(Route.ACTIVITIES.isNotBlank())
    assertTrue(Route.PROFILE.isNotBlank())
    assertTrue(Route.VISITOR_PROFILE.isNotBlank())
    assertTrue(Route.USER_ID_ARG.isNotBlank())
    assertTrue(Route.CREATE_PUBLIC_EVENT.isNotBlank())
    assertTrue(Route.CREATE_PRIVATE_EVENT.isNotBlank())
    assertTrue(Route.EVENT_VIEW.isNotBlank())
  }

  @Test
  fun screen_allScreens_haveNonBlankRoutes() {
    assertTrue(Screen.Auth.route.isNotBlank())
    assertTrue(Screen.Home.route.isNotBlank())
    assertTrue(Screen.Map.route.isNotBlank())
    assertTrue(Screen.Activities.route.isNotBlank())
    assertTrue(Screen.Profile.route.isNotBlank())
    assertTrue(Screen.CreatePublicEvent.route.isNotBlank())
    assertTrue(Screen.CreatePrivateEvent.route.isNotBlank())
    assertTrue(Screen.EventView.route.isNotBlank())
  }

  @Test
  fun screen_allScreens_haveNonBlankNames() {
    assertTrue(Screen.Auth.name.isNotBlank())
    assertTrue(Screen.Home.name.isNotBlank())
    assertTrue(Screen.Map.name.isNotBlank())
    assertTrue(Screen.Activities.name.isNotBlank())
    assertTrue(Screen.Profile.name.isNotBlank())
    assertTrue(Screen.CreatePublicEvent.name.isNotBlank())
    assertTrue(Screen.CreatePrivateEvent.name.isNotBlank())
    assertTrue(Screen.EventView.name.isNotBlank())
  }
}
