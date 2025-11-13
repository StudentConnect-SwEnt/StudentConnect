package com.github.se.studentconnect.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.map.MapScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationWithEventUidTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun mapNavigation_withEventUid_passesParameterCorrectly() {
    val testLatitude = 46.5197
    val testLongitude = 6.6323
    val testZoom = 15.0
    val testEventUid = "test-event-123"

    var capturedLatitude: Double? = null
    var capturedLongitude: Double? = null
    var capturedZoom: Double? = null
    var capturedEventUid: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "start") {
        composable("start") {
          // Start screen - immediately navigate to map with parameters
          androidx.compose.runtime.LaunchedEffect(Unit) {
            val route =
                Route.mapWithLocation(
                    testLatitude, testLongitude, testZoom, eventUid = testEventUid)
            navController.navigate(route)
          }
        }
        composable(
            Route.MAP_WITH_LOCATION,
            arguments =
                listOf(
                    androidx.navigation.navArgument("latitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("longitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("zoom") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("eventUid") {
                      type = androidx.navigation.NavType.StringType
                      nullable = true
                      defaultValue = null
                    })) { backStackEntry ->
              capturedLatitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
              capturedLongitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
              capturedZoom = backStackEntry.arguments?.getString("zoom")?.toDoubleOrNull()
              capturedEventUid = backStackEntry.arguments?.getString("eventUid")

              MapScreen(
                  targetLatitude = capturedLatitude,
                  targetLongitude = capturedLongitude,
                  targetZoom = capturedZoom ?: 15.0,
                  targetEventUid = capturedEventUid)
            }
      }
    }

    composeTestRule.waitForIdle()

    // Verify all parameters were captured correctly
    assert(capturedLatitude == testLatitude) {
      "Expected latitude $testLatitude, got $capturedLatitude"
    }
    assert(capturedLongitude == testLongitude) {
      "Expected longitude $testLongitude, got $capturedLongitude"
    }
    assert(capturedZoom == testZoom) { "Expected zoom $testZoom, got $capturedZoom" }
    assert(capturedEventUid == testEventUid) {
      "Expected eventUid $testEventUid, got $capturedEventUid"
    }
  }

  @Test
  fun mapNavigation_withoutEventUid_passesNullCorrectly() {
    val testLatitude = 46.5197
    val testLongitude = 6.6323
    val testZoom = 12.0

    var capturedEventUid: String? = "not-set"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "start") {
        composable("start") {
          androidx.compose.runtime.LaunchedEffect(Unit) {
            val route =
                Route.mapWithLocation(testLatitude, testLongitude, testZoom, eventUid = null)
            navController.navigate(route)
          }
        }
        composable(
            Route.MAP_WITH_LOCATION,
            arguments =
                listOf(
                    androidx.navigation.navArgument("latitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("longitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("zoom") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("eventUid") {
                      type = androidx.navigation.NavType.StringType
                      nullable = true
                      defaultValue = null
                    })) { backStackEntry ->
              capturedEventUid = backStackEntry.arguments?.getString("eventUid")

              MapScreen(
                  targetLatitude = testLatitude,
                  targetLongitude = testLongitude,
                  targetZoom = testZoom,
                  targetEventUid = capturedEventUid)
            }
      }
    }

    composeTestRule.waitForIdle()

    // When eventUid is not provided, it should be null
    assert(capturedEventUid == null) { "Expected eventUid to be null, got $capturedEventUid" }
  }

  @Test
  fun mapNavigation_withDefaultZoom_usesCorrectDefault() {
    val testLatitude = 46.5197
    val testLongitude = 6.6323

    var capturedZoom: Double? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "start") {
        composable("start") {
          androidx.compose.runtime.LaunchedEffect(Unit) {
            // Use default zoom (should be 15.0)
            val route = Route.mapWithLocation(testLatitude, testLongitude)
            navController.navigate(route)
          }
        }
        composable(
            Route.MAP_WITH_LOCATION,
            arguments =
                listOf(
                    androidx.navigation.navArgument("latitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("longitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("zoom") {
                      type = androidx.navigation.NavType.StringType
                    })) { backStackEntry ->
              capturedZoom = backStackEntry.arguments?.getString("zoom")?.toDoubleOrNull()

              MapScreen(
                  targetLatitude = testLatitude,
                  targetLongitude = testLongitude,
                  targetZoom = capturedZoom ?: 15.0)
            }
      }
    }

    composeTestRule.waitForIdle()

    // Default zoom should be 15.0
    assert(capturedZoom == 15.0) { "Expected default zoom 15.0, got $capturedZoom" }
  }

  @Test
  fun mapNavigation_withAllParameters_routeFormatIsCorrect() {
    val route = Route.mapWithLocation(46.5197, 6.6323, 18.0, eventUid = "event-123")

    // Verify route format
    assert(route.contains("map/46.5197/6.6323/18.0")) {
      "Route should contain coordinates and zoom"
    }
    assert(route.contains("eventUid=event-123")) { "Route should contain eventUid query parameter" }
  }

  @Test
  fun mapNavigation_withoutEventUid_routeFormatIsCorrect() {
    val route = Route.mapWithLocation(46.5197, 6.6323, 18.0, eventUid = null)

    // Verify route format - should not have query parameter
    assert(route.contains("map/46.5197/6.6323/18.0")) {
      "Route should contain coordinates and zoom"
    }
    assert(!route.contains("eventUid")) { "Route should not contain eventUid when null" }
  }

  @Test
  fun mapNavigation_multipleNavigationsWithDifferentEventUids_workCorrectly() {
    val eventUids = listOf("event-1", "event-2", null, "event-3")
    val capturedEventUids = mutableListOf<String?>()

    composeTestRule.setContent {
      val navController = rememberNavController()
      var navigationIndex = 0

      NavHost(navController = navController, startDestination = "start") {
        composable("start") {
          androidx.compose.runtime.LaunchedEffect(navigationIndex) {
            if (navigationIndex < eventUids.size) {
              val route =
                  Route.mapWithLocation(
                      46.5197, 6.6323, 15.0, eventUid = eventUids[navigationIndex])
              navController.navigate(route)
              navigationIndex++
            }
          }
        }
        composable(
            Route.MAP_WITH_LOCATION,
            arguments =
                listOf(
                    androidx.navigation.navArgument("eventUid") {
                      type = androidx.navigation.NavType.StringType
                      nullable = true
                      defaultValue = null
                    })) { backStackEntry ->
              val eventUid = backStackEntry.arguments?.getString("eventUid")
              capturedEventUids.add(eventUid)

              MapScreen(
                  targetLatitude = 46.5197, targetLongitude = 6.6323, targetEventUid = eventUid)
            }
      }
    }

    composeTestRule.waitForIdle()

    // At least one navigation should have occurred
    assert(capturedEventUids.isNotEmpty()) { "Expected at least one navigation" }
  }

  @Test
  fun mapNavigation_southPoleCoordinates_handledCorrectly() {
    val lat = -90.0
    val lon = -180.0
    val eventUid = "event-south-pole"

    var capturedLat: Double? = null
    var capturedLon: Double? = null
    var capturedEventUid: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "start") {
        composable("start") {
          androidx.compose.runtime.LaunchedEffect(Unit) {
            val route = Route.mapWithLocation(lat, lon, eventUid = eventUid)
            navController.navigate(route)
          }
        }
        composable(
            Route.MAP_WITH_LOCATION,
            arguments =
                listOf(
                    androidx.navigation.navArgument("latitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("longitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("eventUid") {
                      type = androidx.navigation.NavType.StringType
                      nullable = true
                      defaultValue = null
                    })) { backStackEntry ->
              capturedLat = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
              capturedLon = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
              capturedEventUid = backStackEntry.arguments?.getString("eventUid")

              MapScreen(
                  targetLatitude = capturedLat,
                  targetLongitude = capturedLon,
                  targetEventUid = capturedEventUid)
            }
      }
    }

    composeTestRule.waitForIdle()

    assert(capturedLat == lat) { "Expected latitude $lat, got $capturedLat" }
    assert(capturedLon == lon) { "Expected longitude $lon, got $capturedLon" }
    assert(capturedEventUid == eventUid) { "Expected eventUid $eventUid, got $capturedEventUid" }
  }

  @Test
  fun mapNavigation_northPoleCoordinates_handledCorrectly() {
    val lat = 90.0
    val lon = 180.0
    val eventUid = "event-north-pole"

    var capturedLat: Double? = null
    var capturedLon: Double? = null
    var capturedEventUid: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "start") {
        composable("start") {
          androidx.compose.runtime.LaunchedEffect(Unit) {
            val route = Route.mapWithLocation(lat, lon, eventUid = eventUid)
            navController.navigate(route)
          }
        }
        composable(
            Route.MAP_WITH_LOCATION,
            arguments =
                listOf(
                    androidx.navigation.navArgument("latitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("longitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("eventUid") {
                      type = androidx.navigation.NavType.StringType
                      nullable = true
                      defaultValue = null
                    })) { backStackEntry ->
              capturedLat = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
              capturedLon = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
              capturedEventUid = backStackEntry.arguments?.getString("eventUid")

              MapScreen(
                  targetLatitude = capturedLat,
                  targetLongitude = capturedLon,
                  targetEventUid = capturedEventUid)
            }
      }
    }

    composeTestRule.waitForIdle()

    assert(capturedLat == lat) { "Expected latitude $lat, got $capturedLat" }
    assert(capturedLon == lon) { "Expected longitude $lon, got $capturedLon" }
    assert(capturedEventUid == eventUid) { "Expected eventUid $eventUid, got $capturedEventUid" }
  }

  @Test
  fun mapNavigation_nullIslandCoordinates_handledCorrectly() {
    val lat = 0.0
    val lon = 0.0
    val eventUid = "event-null-island"

    var capturedLat: Double? = null
    var capturedLon: Double? = null
    var capturedEventUid: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "start") {
        composable("start") {
          androidx.compose.runtime.LaunchedEffect(Unit) {
            val route = Route.mapWithLocation(lat, lon, eventUid = eventUid)
            navController.navigate(route)
          }
        }
        composable(
            Route.MAP_WITH_LOCATION,
            arguments =
                listOf(
                    androidx.navigation.navArgument("latitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("longitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("eventUid") {
                      type = androidx.navigation.NavType.StringType
                      nullable = true
                      defaultValue = null
                    })) { backStackEntry ->
              capturedLat = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
              capturedLon = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
              capturedEventUid = backStackEntry.arguments?.getString("eventUid")

              MapScreen(
                  targetLatitude = capturedLat,
                  targetLongitude = capturedLon,
                  targetEventUid = capturedEventUid)
            }
      }
    }

    composeTestRule.waitForIdle()

    assert(capturedLat == lat) { "Expected latitude $lat, got $capturedLat" }
    assert(capturedLon == lon) { "Expected longitude $lon, got $capturedLon" }
    assert(capturedEventUid == eventUid) { "Expected eventUid $eventUid, got $capturedEventUid" }
  }
}
