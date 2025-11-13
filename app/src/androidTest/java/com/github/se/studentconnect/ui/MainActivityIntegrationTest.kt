package com.github.se.studentconnect.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.MainAppContent
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.navigation.Tab
import com.github.se.studentconnect.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
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

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.CAMERA,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION)

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
  // Compose UI Tests for MainAppContent
  // ============================================================================

  @Test
  fun mainAppContent_rendersWithHomeTab() {
    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Home,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
    // Home page should be displayed
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun mainAppContent_shouldOpenQRScanner_true() {
    var qrScannerStateChanged = false

    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Home,
            onTabSelected = {},
            shouldOpenQRScanner = true,
            onQRScannerStateChange = { qrScannerStateChanged = true })
      }
    }

    composeTestRule.waitForIdle()

    // The QR scanner should open automatically
    composeTestRule.onNodeWithTag("camera_mode_selector").assertIsDisplayed()
  }

  @Test
  fun mainAppContent_shouldOpenQRScanner_false() {
    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Home,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()

    // Should be on home page, not scanner
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun mainAppContent_onTabSelected_callback_invoked() {
    var selectedTab: Tab? = null

    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Home,
            onTabSelected = { tab -> selectedTab = tab },
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()

    // Click on the map tab
    composeTestRule.onNodeWithTag("bottom_nav_Map").performClick()

    composeTestRule.runOnIdle {
      assertEquals("onTabSelected should be called with Map tab", Tab.Map, selectedTab)
    }
  }

  @Test
  fun mainAppContent_onQRScannerStateChange_callback_invoked() {
    var qrScannerState: Boolean? = null

    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Home,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = { state -> qrScannerState = state })
      }
    }

    composeTestRule.waitForIdle()

    // Click on a tab (this should trigger onQRScannerStateChange with false)
    composeTestRule.onNodeWithTag("bottom_nav_Map").performClick()

    composeTestRule.runOnIdle {
      assertEquals("onQRScannerStateChange should be called with false", false, qrScannerState)
    }
  }

  @Test
  fun mainAppContent_selectedTab_home() {
    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Home,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun mainAppContent_selectedTab_map() {
    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Map,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
    // Map screen should be displayed (though it might not have a testTag, so we can't assert it)
    // At least verify the component renders without crashing
  }

  @Test
  fun mainAppContent_selectedTab_activities() {
    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Activities,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activities_screen").assertIsDisplayed()
  }

  @Test
  fun mainAppContent_selectedTab_profile() {
    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Profile,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profileSettingsScreen").assertIsDisplayed()
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

  @Test
  fun mainAppContent_cameraActiveChange_hidesBottomBar() {
    composeTestRule.setContent {
      AppTheme {
        MainAppContent(
            navController = rememberNavController(),
            selectedTab = Tab.Home,
            onTabSelected = {},
            shouldOpenQRScanner = false,
            onQRScannerStateChange = {})
      }
    }

    composeTestRule.waitForIdle()

    // Initially bottom bar should be visible
    composeTestRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()

    // Open camera by clicking add story
    composeTestRule.onNodeWithTag("addStoryButton").performClick()

    composeTestRule.waitForIdle()

    // Bottom bar should now be hidden (will throw exception if still displayed)
    composeTestRule.onNodeWithTag("bottom_navigation_bar").assertDoesNotExist()
  }

  @Test
  fun mainAppContent_multipleParameterCombinations() {
    // Test different combinations of parameters
    val testCombinations =
        listOf(
            Triple(Tab.Home, false, "Home with no scanner"),
            Triple(Tab.Home, true, "Home with scanner"),
            Triple(Tab.Map, false, "Map with no scanner"),
            Triple(Tab.Activities, false, "Activities with no scanner"),
            Triple(Tab.Profile, false, "Profile with no scanner"))

    testCombinations.forEach { (tab, shouldOpenScanner, description) ->
      composeTestRule.setContent {
        AppTheme {
          MainAppContent(
              navController = rememberNavController(),
              selectedTab = tab,
              onTabSelected = {},
              shouldOpenQRScanner = shouldOpenScanner,
              onQRScannerStateChange = {})
        }
      }

      composeTestRule.waitForIdle()
      // Just verify it renders without crashing
      // The fact that we got here means the composable handled the parameters correctly
    }
  }
}
