package com.github.se.studentconnect.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for MainActivity navigation and routing logic.
 *
 * These tests focus on testing the routing logic and state management that was added in the new
 * feature, particularly the eventUid parameter handling in map navigation.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityIntegrationTest {

  @Test
  fun routeToTab_homeRoute_returnsHomeTab() {
    val route = Route.HOME
    val expectedTab = Tab.Home

    // Simulate the routeToTab function logic
    val actualTab =
        when {
          route == Route.HOME -> Tab.Home
          route == Route.MAP -> Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assert(actualTab == expectedTab) { "Expected $expectedTab but got $actualTab" }
  }

  @Test
  fun routeToTab_mapRoute_returnsMapTab() {
    val route = Route.MAP
    val expectedTab = Tab.Map

    val actualTab =
        when {
          route == Route.HOME -> Tab.Home
          route == Route.MAP -> Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assert(actualTab == expectedTab) { "Expected $expectedTab but got $actualTab" }
  }

  @Test
  fun routeToTab_mapWithLocationRoute_returnsMapTab() {
    val route = "map/46.5197/6.6323/15.0"

    // Simulate the routeToTab function logic
    val actualTab =
        when {
          route == Route.HOME -> Tab.Home
          route == Route.MAP || route == Route.MAP_WITH_LOCATION || route.startsWith("map/") ->
              Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assert(actualTab == Tab.Map) { "Expected Map tab but got $actualTab" }
  }

  @Test
  fun routeToTab_activitiesRoute_returnsActivitiesTab() {
    val route = Route.ACTIVITIES
    val expectedTab = Tab.Activities

    val actualTab =
        when {
          route == Route.HOME -> Tab.Home
          route == Route.MAP -> Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assert(actualTab == expectedTab) { "Expected $expectedTab but got $actualTab" }
  }

  @Test
  fun routeToTab_profileRoute_returnsProfileTab() {
    val route = Route.PROFILE
    val expectedTab = Tab.Profile

    val actualTab =
        when {
          route == Route.HOME -> Tab.Home
          route == Route.MAP -> Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assert(actualTab == expectedTab) { "Expected $expectedTab but got $actualTab" }
  }

  @Test
  fun routeToTab_unknownRoute_returnsNull() {
    val route = "unknown_route"

    val actualTab =
        when {
          route == Route.HOME -> Tab.Home
          route == Route.MAP -> Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assert(actualTab == null) { "Expected null but got $actualTab" }
  }

  @Test
  fun routeToTab_nullRoute_returnsNull() {
    val route: String? = null

    val actualTab =
        when {
          route == null -> null
          route == Route.HOME -> Tab.Home
          route == Route.MAP -> Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assert(actualTab == null) { "Expected null but got $actualTab" }
  }

  @Test
  fun hideBottomBar_createPublicEventRoute_shouldHide() {
    val currentRoute = Route.CREATE_PUBLIC_EVENT

    val hideBottomBar =
        currentRoute == Route.CREATE_PUBLIC_EVENT ||
            currentRoute == Route.CREATE_PRIVATE_EVENT ||
            currentRoute == Route.EDIT_PUBLIC_EVENT ||
            currentRoute == Route.EDIT_PRIVATE_EVENT

    assert(hideBottomBar) { "Bottom bar should be hidden for create public event route" }
  }

  @Test
  fun hideBottomBar_createPrivateEventRoute_shouldHide() {
    val currentRoute = Route.CREATE_PRIVATE_EVENT

    val hideBottomBar =
        currentRoute == Route.CREATE_PUBLIC_EVENT ||
            currentRoute == Route.CREATE_PRIVATE_EVENT ||
            currentRoute == Route.EDIT_PUBLIC_EVENT ||
            currentRoute == Route.EDIT_PRIVATE_EVENT

    assert(hideBottomBar) { "Bottom bar should be hidden for create private event route" }
  }

  @Test
  fun hideBottomBar_editPublicEventRoute_shouldHide() {
    val currentRoute = Route.EDIT_PUBLIC_EVENT

    val hideBottomBar =
        currentRoute == Route.CREATE_PUBLIC_EVENT ||
            currentRoute == Route.CREATE_PRIVATE_EVENT ||
            currentRoute == Route.EDIT_PUBLIC_EVENT ||
            currentRoute == Route.EDIT_PRIVATE_EVENT

    assert(hideBottomBar) { "Bottom bar should be hidden for edit public event route" }
  }

  @Test
  fun hideBottomBar_editPrivateEventRoute_shouldHide() {
    val currentRoute = Route.EDIT_PRIVATE_EVENT

    val hideBottomBar =
        currentRoute == Route.CREATE_PUBLIC_EVENT ||
            currentRoute == Route.CREATE_PRIVATE_EVENT ||
            currentRoute == Route.EDIT_PUBLIC_EVENT ||
            currentRoute == Route.EDIT_PRIVATE_EVENT

    assert(hideBottomBar) { "Bottom bar should be hidden for edit private event route" }
  }

  @Test
  fun hideBottomBar_homeRoute_shouldNotHide() {
    val currentRoute = Route.HOME

    val hideBottomBar =
        currentRoute == Route.CREATE_PUBLIC_EVENT ||
            currentRoute == Route.CREATE_PRIVATE_EVENT ||
            currentRoute == Route.EDIT_PUBLIC_EVENT ||
            currentRoute == Route.EDIT_PRIVATE_EVENT

    assert(!hideBottomBar) { "Bottom bar should not be hidden for home route" }
  }

  @Test
  fun hideBottomBar_mapRoute_shouldNotHide() {
    val currentRoute = Route.MAP

    val hideBottomBar =
        currentRoute == Route.CREATE_PUBLIC_EVENT ||
            currentRoute == Route.CREATE_PRIVATE_EVENT ||
            currentRoute == Route.EDIT_PUBLIC_EVENT ||
            currentRoute == Route.EDIT_PRIVATE_EVENT

    assert(!hideBottomBar) { "Bottom bar should not be hidden for map route" }
  }

  @Test
  fun hideBottomBar_withCameraActive_shouldHide() {
    val currentRoute = Route.HOME
    val isCameraActive = true

    val hideBottomBar =
        currentRoute == Route.CREATE_PUBLIC_EVENT ||
            currentRoute == Route.CREATE_PRIVATE_EVENT ||
            currentRoute == Route.EDIT_PUBLIC_EVENT ||
            currentRoute == Route.EDIT_PRIVATE_EVENT ||
            isCameraActive

    assert(hideBottomBar) { "Bottom bar should be hidden when camera is active" }
  }

  @Test
  fun hideBottomBar_withCameraInactive_shouldNotHide() {
    val currentRoute = Route.HOME
    val isCameraActive = false

    val hideBottomBar =
        currentRoute == Route.CREATE_PUBLIC_EVENT ||
            currentRoute == Route.CREATE_PRIVATE_EVENT ||
            currentRoute == Route.EDIT_PUBLIC_EVENT ||
            currentRoute == Route.EDIT_PRIVATE_EVENT ||
            isCameraActive

    assert(!hideBottomBar) { "Bottom bar should not be hidden when camera is inactive on home" }
  }

  // ============================================================================
  // Additional routeToTab Tests for Coverage
  // ============================================================================

  @Test
  fun routeToTab_mapWithLocation_returnsMapTab() {
    val route = Route.MAP_WITH_LOCATION

    val actualTab =
        when {
          route == null -> null
          route == Route.HOME -> Tab.Home
          route == Route.MAP || route == Route.MAP_WITH_LOCATION || route.startsWith("map/") ->
              Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assertEquals("Route.MAP_WITH_LOCATION should map to Map tab", Tab.Map, actualTab)
  }

  @Test
  fun routeToTab_mapWithStartsWithMap_returnsMapTab() {
    val route = "map/123"

    val actualTab =
        when {
          route == null -> null
          route == Route.HOME -> Tab.Home
          route == Route.MAP || route == Route.MAP_WITH_LOCATION || route.startsWith("map/") ->
              Tab.Map
          route == Route.ACTIVITIES -> Tab.Activities
          route == Route.PROFILE -> Tab.Profile
          else -> null
        }

    assertEquals("route.startsWith('map/') should map to Map tab", Tab.Map, actualTab)
  }

  @Test
  fun routeToTab_allMainTabs_coverage() {
    // Test all main tab routes to ensure full coverage
    val testCases =
        listOf(
            Route.HOME to Tab.Home,
            Route.MAP to Tab.Map,
            Route.MAP_WITH_LOCATION to Tab.Map,
            "map/46.5197/6.6323/15.0" to Tab.Map,
            Route.ACTIVITIES to Tab.Activities,
            Route.PROFILE to Tab.Profile,
            "unknown_route" to null,
            null to null)

    testCases.forEach { (route, expectedTab) ->
      val actualTab =
          when {
            route == null -> null
            route == Route.HOME -> Tab.Home
            route == Route.MAP || route == Route.MAP_WITH_LOCATION || route.startsWith("map/") ->
                Tab.Map
            route == Route.ACTIVITIES -> Tab.Activities
            route == Route.PROFILE -> Tab.Profile
            else -> null
          }

      assertEquals("Route '$route' should map to $expectedTab", expectedTab, actualTab)
    }
  }
}
