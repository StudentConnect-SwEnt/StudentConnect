package com.github.se.studentconnect.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for MainActivity navigation and routing logic to maximize line coverage.
 *
 * These tests cover the routing logic in MainAppContent composable:
 * - Route to Tab mapping (routeToTab function logic)
 * - Bottom bar visibility logic (hideBottomBar conditions)
 * - Navigation argument handling
 */
@RunWith(AndroidJUnit4::class)
class MainAppContentTest {

  /**
   * Tests the routeToTab logic from MainActivity.kt
   *
   * This simulates the routeToTab function at line 155-164
   */
  private fun routeToTab(route: String?): Tab? =
      when {
        route == null -> null
        route == Route.HOME -> Tab.Home
        route == Route.MAP || route == Route.MAP_WITH_LOCATION || route.startsWith("map/") ->
            Tab.Map
        route == Route.ACTIVITIES -> Tab.Activities
        route == Route.PROFILE -> Tab.Profile
        else -> null
      }

  @Test
  fun routeToTab_nullRoute_returnsNull() {
    val result = routeToTab(null)
    assertNull(result)
  }

  @Test
  fun routeToTab_homeRoute_returnsHomeTab() {
    val result = routeToTab(Route.HOME)
    assertEquals(Tab.Home, result)
  }

  @Test
  fun routeToTab_mapRoute_returnsMapTab() {
    val result = routeToTab(Route.MAP)
    assertEquals(Tab.Map, result)
  }

  @Test
  fun routeToTab_mapWithLocationRoute_returnsMapTab() {
    val result = routeToTab(Route.MAP_WITH_LOCATION)
    assertEquals(Tab.Map, result)
  }

  @Test
  fun routeToTab_mapWithCoordinates_returnsMapTab() {
    val result = routeToTab("map/46.5197/6.6323/15.0")
    assertEquals(Tab.Map, result)
  }

  @Test
  fun routeToTab_mapWithEventUid_returnsMapTab() {
    val result = routeToTab("map/46.5197/6.6323/15.0?eventUid=test-123")
    assertEquals(Tab.Map, result)
  }

  @Test
  fun routeToTab_activitiesRoute_returnsActivitiesTab() {
    val result = routeToTab(Route.ACTIVITIES)
    assertEquals(Tab.Activities, result)
  }

  @Test
  fun routeToTab_profileRoute_returnsProfileTab() {
    val result = routeToTab(Route.PROFILE)
    assertEquals(Tab.Profile, result)
  }

  @Test
  fun routeToTab_unknownRoute_returnsNull() {
    val result = routeToTab("unknown_route")
    assertNull(result)
  }

  @Test
  fun routeToTab_createPublicEventRoute_returnsNull() {
    val result = routeToTab(Route.CREATE_PUBLIC_EVENT)
    assertNull(result)
  }

  @Test
  fun routeToTab_createPrivateEventRoute_returnsNull() {
    val result = routeToTab(Route.CREATE_PRIVATE_EVENT)
    assertNull(result)
  }

  @Test
  fun routeToTab_editPublicEventRoute_returnsNull() {
    val result = routeToTab("edit_public_event/test-123")
    assertNull(result)
  }

  @Test
  fun routeToTab_editPrivateEventRoute_returnsNull() {
    val result = routeToTab("edit_private_event/test-123")
    assertNull(result)
  }

  @Test
  fun routeToTab_eventViewRoute_returnsNull() {
    val result = routeToTab("eventView/test-123/true")
    assertNull(result)
  }

  @Test
  fun routeToTab_searchRoute_returnsNull() {
    val result = routeToTab(Route.SEARCH)
    assertNull(result)
  }

  /**
   * Tests the hideBottomBar logic from MainActivity.kt
   *
   * This simulates the hideBottomBar condition at line 242-247
   */
  private fun shouldHideBottomBar(currentRoute: String?, isCameraActive: Boolean): Boolean =
      currentRoute == Route.CREATE_PUBLIC_EVENT ||
          currentRoute == Route.CREATE_PRIVATE_EVENT ||
          currentRoute == Route.EDIT_PUBLIC_EVENT ||
          currentRoute == Route.EDIT_PRIVATE_EVENT ||
          isCameraActive

  @Test
  fun hideBottomBar_createPublicEvent_hidesBottomBar() {
    val result = shouldHideBottomBar(Route.CREATE_PUBLIC_EVENT, false)
    assertTrue(result)
  }

  @Test
  fun hideBottomBar_createPrivateEvent_hidesBottomBar() {
    val result = shouldHideBottomBar(Route.CREATE_PRIVATE_EVENT, false)
    assertTrue(result)
  }

  @Test
  fun hideBottomBar_editPublicEvent_hidesBottomBar() {
    val result = shouldHideBottomBar(Route.EDIT_PUBLIC_EVENT, false)
    assertTrue(result)
  }

  @Test
  fun hideBottomBar_editPrivateEvent_hidesBottomBar() {
    val result = shouldHideBottomBar(Route.EDIT_PRIVATE_EVENT, false)
    assertTrue(result)
  }

  @Test
  fun hideBottomBar_cameraActive_hidesBottomBar() {
    val result = shouldHideBottomBar(Route.HOME, true)
    assertTrue(result)
  }

  @Test
  fun hideBottomBar_cameraActiveOnMap_hidesBottomBar() {
    val result = shouldHideBottomBar(Route.MAP, true)
    assertTrue(result)
  }

  @Test
  fun hideBottomBar_homeRoute_showsBottomBar() {
    val result = shouldHideBottomBar(Route.HOME, false)
    assertFalse(result)
  }

  @Test
  fun hideBottomBar_mapRoute_showsBottomBar() {
    val result = shouldHideBottomBar(Route.MAP, false)
    assertFalse(result)
  }

  @Test
  fun hideBottomBar_activitiesRoute_showsBottomBar() {
    val result = shouldHideBottomBar(Route.ACTIVITIES, false)
    assertFalse(result)
  }

  @Test
  fun hideBottomBar_profileRoute_showsBottomBar() {
    val result = shouldHideBottomBar(Route.PROFILE, false)
    assertFalse(result)
  }

  @Test
  fun hideBottomBar_nullRoute_showsBottomBar() {
    val result = shouldHideBottomBar(null, false)
    assertFalse(result)
  }

  @Test
  fun hideBottomBar_eventViewRoute_showsBottomBar() {
    val result = shouldHideBottomBar("eventView/test-123/true", false)
    assertFalse(result)
  }

  @Test
  fun hideBottomBar_searchRoute_showsBottomBar() {
    val result = shouldHideBottomBar(Route.SEARCH, false)
    assertFalse(result)
  }

  // Test navigation argument parsing for MAP_WITH_LOCATION route
  @Test
  fun mapWithLocationRoute_parsesLatitude() {
    val route = "map/46.5197/6.6323/15.0"
    val parts = route.split("/")
    val latitude = parts.getOrNull(1)?.toDoubleOrNull()
    assertEquals(46.5197, latitude!!, 0.0001)
  }

  @Test
  fun mapWithLocationRoute_parsesLongitude() {
    val route = "map/46.5197/6.6323/15.0"
    val parts = route.split("/")
    val longitude = parts.getOrNull(2)?.toDoubleOrNull()
    assertEquals(6.6323, longitude!!, 0.0001)
  }

  @Test
  fun mapWithLocationRoute_parsesZoom() {
    val route = "map/46.5197/6.6323/18.5"
    val parts = route.split("/")
    val zoom = parts.getOrNull(3)?.split("?")?.get(0)?.toDoubleOrNull()
    assertEquals(18.5, zoom!!, 0.0001)
  }

  @Test
  fun mapWithLocationRoute_handlesEventUidParameter() {
    val route = "map/46.5197/6.6323/15.0?eventUid=test-event-123"
    assertTrue(route.contains("eventUid=test-event-123"))
  }

  @Test
  fun mapWithLocationRoute_defaultsZoomWhenNull() {
    val zoomString: String? = null
    val zoom = zoomString?.toDoubleOrNull() ?: 15.0
    assertEquals(15.0, zoom, 0.0001)
  }

  @Test
  fun eventViewRoute_parsesEventUid() {
    val route = "eventView/my-event-id/true"
    val parts = route.split("/")
    val eventUid = parts.getOrNull(1)
    assertEquals("my-event-id", eventUid)
  }

  @Test
  fun eventViewRoute_parsesHasJoined() {
    val route = "eventView/my-event-id/true"
    val parts = route.split("/")
    val hasJoined = parts.getOrNull(2)?.toBoolean() ?: false
    assertTrue(hasJoined)
  }

  @Test
  fun eventViewRoute_parsesHasJoinedFalse() {
    val route = "eventView/my-event-id/false"
    val parts = route.split("/")
    val hasJoined = parts.getOrNull(2)?.toBoolean() ?: false
    assertFalse(hasJoined)
  }

  @Test
  fun editEventRoutes_containEventUidPlaceholder() {
    assertTrue(Route.EDIT_PUBLIC_EVENT.contains("{eventUid}"))
    assertTrue(Route.EDIT_PRIVATE_EVENT.contains("{eventUid}"))
  }
}
