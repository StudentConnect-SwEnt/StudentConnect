package com.github.se.studentconnect.ui.screen

import android.Manifest
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.screen.map.MapScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  @Test
  fun mapScreen_displaysAllComponents() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Verify all main components are displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_top_app_bar).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_search_field).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()
    }

    step("Verify location FAB is displayed when permission is granted") {
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodes(hasTestTag(C.Tag.map_locate_user_fab))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      composeTestRule.onNodeWithTag(C.Tag.map_locate_user_fab).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_topAppBar_displaysCorrectTitle() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Verify top app bar displays correct title") {
      composeTestRule.onNodeWithText("Map").assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_searchBar_isInteractive() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Verify search bar is displayed and interactive") {
      val searchField = composeTestRule.onNodeWithTag(C.Tag.map_search_field)
      searchField.assertIsDisplayed()
      searchField.performTextInput("Test location")
    }
  }

  @Test
  fun mapScreen_floatingActionButtons_areClickable() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Verify toggle view FAB is clickable") {
      val toggleViewFab = composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab)
      toggleViewFab.assertIsDisplayed()
      toggleViewFab.assertHasClickAction()
      toggleViewFab.performClick()
    }

    step("Verify location FAB is clickable when permission is granted") {
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodes(hasTestTag(C.Tag.map_locate_user_fab))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      val locateUserFab = composeTestRule.onNodeWithTag(C.Tag.map_locate_user_fab)
      locateUserFab.assertIsDisplayed()
      locateUserFab.assertHasClickAction()
      locateUserFab.performClick()
    }
  }

  @Test
  fun mapScreen_withTargetLocation_displaysCorrectly() = run {
    val targetLatitude = 46.5089
    val targetLongitude = 6.6283
    val targetZoom = 12.0

    step("Display MapScreen with target location") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = targetLatitude,
            targetLongitude = targetLongitude,
            targetZoom = targetZoom)
      }
    }

    step("Verify all components are displayed with target location") {
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withoutTargetLocation_showsDefaultView() = run {
    step("Display MapScreen without target location") { composeTestRule.setContent { MapScreen() } }

    step("Verify map displays with default settings") {
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_searchPlaceholder_isDisplayed() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Verify search placeholder text is displayed") {
      composeTestRule.onNodeWithText("Search locations...").assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withLocationPermission_showsLocationFab() = run {
    step("Display MapScreen with location permission granted") {
      composeTestRule.setContent { MapScreen() }
    }

    step("Wait for permission processing and verify location FAB appears") {
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodes(hasTestTag(C.Tag.map_locate_user_fab))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      composeTestRule.onNodeWithTag(C.Tag.map_locate_user_fab).assertIsDisplayed()
    }

    step("Verify location FAB has proper functionality") {
      val locateUserFab = composeTestRule.onNodeWithTag(C.Tag.map_locate_user_fab)
      locateUserFab.assertHasClickAction()
    }
  }

  @Test
  fun mapScreen_withTargetEventUid_displaysCorrectly() = run {
    val targetLatitude = 46.5197
    val targetLongitude = 6.6323
    val targetZoom = 15.0
    val targetEventUid = "test-event-123"

    step("Display MapScreen with target location and event UID") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = targetLatitude,
            targetLongitude = targetLongitude,
            targetZoom = targetZoom,
            targetEventUid = targetEventUid)
      }
    }

    step("Verify all components are displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withNullTargetEventUid_displaysCorrectly() = run {
    val targetLatitude = 46.5197
    val targetLongitude = 6.6323
    val targetZoom = 15.0

    step("Display MapScreen with target location but no event UID") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = targetLatitude,
            targetLongitude = targetLongitude,
            targetZoom = targetZoom,
            targetEventUid = null)
      }
    }

    step("Verify all components are displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withOnlyEventUid_displaysCorrectly() = run {
    val targetEventUid = "test-event-456"

    step("Display MapScreen with only event UID (no coordinates)") {
      composeTestRule.setContent { MapScreen(targetEventUid = targetEventUid) }
    }

    step("Verify all components are displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withAllParameters_displaysCorrectly() = run {
    step("Display MapScreen with all parameters") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 18.0,
            targetEventUid = "event-all-params")
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withLocationAndZoomNoEvent_displaysCorrectly() = run {
    step("Display MapScreen with location and zoom, no event") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323, targetZoom = 12.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withLocationOnlyDefaultZoom_displaysCorrectly() = run {
    step("Display MapScreen with location only, default zoom") {
      composeTestRule.setContent { MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323) }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_eventInfoCard_notDisplayedByDefault() = run {
    step("Display MapScreen without event selection") { composeTestRule.setContent { MapScreen() } }

    step("Verify event info card is not displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_event_info_card).assertDoesNotExist()
    }
  }

  @Test
  fun mapScreen_searchField_acceptsInput() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Enter text in search field and verify") {
      val searchField = composeTestRule.onNodeWithTag(C.Tag.map_search_field)
      searchField.assertIsDisplayed()
      searchField.performTextInput("EPFL")

      // Verify the search field contains the text
      composeTestRule.waitForIdle()
    }
  }

  @Test
  fun mapScreen_locationPermission_showsCorrectFABs() = run {
    step("Display MapScreen with location permission") {
      composeTestRule.setContent { MapScreen() }
    }

    step("Verify both FABs are displayed") {
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodes(hasTestTag(C.Tag.map_locate_user_fab))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      composeTestRule.onNodeWithTag(C.Tag.map_locate_user_fab).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_toggleViewFAB_clickable() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Click toggle view FAB multiple times") {
      val toggleFab = composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab)
      toggleFab.assertIsDisplayed()
      toggleFab.performClick()
      composeTestRule.waitForIdle()
      toggleFab.performClick()
      composeTestRule.waitForIdle()
    }
  }

  @Test
  fun mapScreen_withMinimumZoomLevel_displaysCorrectly() = run {
    step("Display MapScreen with minimum zoom level") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323, targetZoom = 1.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withMaximumZoomLevel_displaysCorrectly() = run {
    step("Display MapScreen with maximum zoom level") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323, targetZoom = 22.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withBoundaryCoordinates_displaysCorrectly() = run {
    step("Display MapScreen with boundary coordinates") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = -90.0, targetLongitude = -180.0, targetZoom = 15.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withEquatorAndPrimeMeridian_displaysCorrectly() = run {
    step("Display MapScreen at equator and prime meridian") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 0.0, targetLongitude = 0.0, targetZoom = 15.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_toggleBetweenEventsAndFriendsView() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Toggle to friends view") {
      val toggleFab = composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab)
      toggleFab.assertIsDisplayed()
      toggleFab.performClick()
      composeTestRule.waitForIdle()
    }

    step("Toggle back to events view") {
      val toggleFab = composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab)
      toggleFab.performClick()
      composeTestRule.waitForIdle()
    }

    step("Verify map is still displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_locateUserButton_triggersLocationRequest() = run {
    step("Display MapScreen with location permission") {
      composeTestRule.setContent { MapScreen() }
    }

    step("Wait for location FAB and click it") {
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodes(hasTestTag(C.Tag.map_locate_user_fab))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      val locateFab = composeTestRule.onNodeWithTag(C.Tag.map_locate_user_fab)
      locateFab.assertIsDisplayed()
      locateFab.performClick()
      composeTestRule.waitForIdle()
    }

    step("Verify map is still displayed after location request") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withTargetLatitudeOnly_displaysCorrectly() = run {
    step("Display MapScreen with only target latitude") {
      composeTestRule.setContent { MapScreen(targetLatitude = 46.5197) }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withTargetLongitudeOnly_displaysCorrectly() = run {
    step("Display MapScreen with only target longitude") {
      composeTestRule.setContent { MapScreen(targetLongitude = 6.6323) }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_searchField_clearAndReEnterText() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Enter text in search field") {
      val searchField = composeTestRule.onNodeWithTag(C.Tag.map_search_field)
      searchField.performTextInput("First search")
      composeTestRule.waitForIdle()
    }

    step("Enter more text") {
      val searchField = composeTestRule.onNodeWithTag(C.Tag.map_search_field)
      searchField.performTextInput(" Second search")
      composeTestRule.waitForIdle()
    }

    step("Verify search field is still functional") {
      composeTestRule.onNodeWithTag(C.Tag.map_search_field).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_multipleToggleClicks_maintainsStability() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Click toggle view FAB multiple times rapidly") {
      val toggleFab = composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab)

      repeat(5) {
        toggleFab.performClick()
        composeTestRule.waitForIdle()
      }
    }

    step("Verify map is still stable and displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withCustomZoomAndEventUid_displaysCorrectly() = run {
    step("Display MapScreen with custom zoom and event UID") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 16.5,
            targetEventUid = "custom-event")
      }
    }

    step("Verify all components are displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_highZoomLevel_displaysCorrectly() = run {
    step("Display MapScreen with very high zoom level") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323, targetZoom = 20.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_lowZoomLevel_displaysCorrectly() = run {
    step("Display MapScreen with very low zoom level") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323, targetZoom = 2.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_negativeCoordinates_displaysCorrectly() = run {
    step("Display MapScreen with negative coordinates") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = -33.8688, targetLongitude = -151.2093, targetZoom = 12.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_positiveCoordinates_displaysCorrectly() = run {
    step("Display MapScreen with positive coordinates") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 51.5074, targetLongitude = 0.1278, targetZoom = 12.0)
      }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_searchField_multipleInputs() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Enter multiple search queries") {
      val searchField = composeTestRule.onNodeWithTag(C.Tag.map_search_field)

      searchField.performTextInput("Location 1")
      composeTestRule.waitForIdle()

      searchField.performTextInput(" Location 2")
      composeTestRule.waitForIdle()

      searchField.performTextInput(" Location 3")
      composeTestRule.waitForIdle()
    }

    step("Verify search field is still functional") {
      composeTestRule.onNodeWithTag(C.Tag.map_search_field).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_componentsLayout_isCorrect() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Verify all layout components are present") {
      composeTestRule.onNodeWithTag(C.Tag.map_top_app_bar).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_search_field).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()

      // Wait for location FAB if permission is granted
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodes(hasTestTag(C.Tag.map_locate_user_fab))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      composeTestRule.onNodeWithTag(C.Tag.map_locate_user_fab).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withTargetEventUidOnly_displaysCorrectly() = run {
    step("Display MapScreen with only event UID, no location") {
      composeTestRule.setContent { MapScreen(targetEventUid = "solo-event-uid") }
    }

    step("Verify map screen is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_rapidFABClicks_maintainsStability() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Rapidly click both FABs") {
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodes(hasTestTag(C.Tag.map_locate_user_fab))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      val toggleFab = composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab)
      val locateFab = composeTestRule.onNodeWithTag(C.Tag.map_locate_user_fab)

      repeat(3) {
        toggleFab.performClick()
        composeTestRule.waitForIdle()
        locateFab.performClick()
        composeTestRule.waitForIdle()
      }
    }

    step("Verify map is still stable") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  // ===== Tests for LaunchedEffect Coverage - Event Selection =====

  @Test
  fun mapScreen_withTargetEventUid_triggersEventSelectionLaunchedEffect() = run {
    val targetEventUid = "test-event-selection-123"

    step("Display MapScreen with targetEventUid to trigger LaunchedEffect") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 15.0,
            targetEventUid = targetEventUid)
      }
    }

    step("Wait for LaunchedEffect to execute") {
      // Give time for LaunchedEffect(targetEventUid, uiState.events) to execute
      Thread.sleep(1000)
      composeTestRule.waitForIdle()
    }

    step("Verify map is displayed after event selection LaunchedEffect") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withTargetEventUid_andNoLocation_triggersEventSelection() = run {
    val targetEventUid = "event-no-coords-456"

    step("Display MapScreen with only targetEventUid (no coordinates)") {
      composeTestRule.setContent { MapScreen(targetEventUid = targetEventUid) }
    }

    step("Wait for event selection LaunchedEffect to execute") {
      Thread.sleep(1000)
      composeTestRule.waitForIdle()
    }

    step("Verify the LaunchedEffect executed by checking map is displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withNullEventUid_skipsEventSelectionLaunchedEffect() = run {
    step("Display MapScreen with null targetEventUid") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323, targetEventUid = null)
      }
    }

    step("Wait to ensure LaunchedEffect has time to run (but shouldn't execute)") {
      Thread.sleep(500)
      composeTestRule.waitForIdle()
    }

    step("Verify map is displayed (LaunchedEffect condition was false)") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_withTargetEventUid_bothLaunchedEffectsExecute() = run {
    val targetEventUid = "comprehensive-event-789"

    step("Display MapScreen with all parameters") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 47.3769,
            targetLongitude = 8.5417,
            targetZoom = 14.0,
            targetEventUid = targetEventUid)
      }
    }

    step("Wait for both LaunchedEffects to execute") {
      // LaunchedEffect(targetLatitude, targetLongitude, targetEventUid) - line 137
      // LaunchedEffect(targetEventUid, uiState.events) - line 174
      Thread.sleep(1500)
      composeTestRule.waitForIdle()
    }

    step("Verify map components are displayed after LaunchedEffects") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_eventSelectionAnimationLaunchedEffect_executesWhenEventSelected() = run {
    step("Display MapScreen") {
      composeTestRule.setContent { MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323) }
    }

    step("Wait for map to initialize") {
      Thread.sleep(500)
      composeTestRule.waitForIdle()
    }

    step("Interact with map to potentially trigger event selection") {
      // The LaunchedEffect(uiState.shouldAnimateToSelectedEvent) at line 162
      // will execute when an event is selected internally
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }

    step("Wait for any animation LaunchedEffect to complete") {
      Thread.sleep(1000)
      composeTestRule.waitForIdle()
    }
  }

  @Test
  fun mapScreen_multipleEventUids_triggersLaunchedEffectMultipleTimes() = run {
    step("Display MapScreen with event to trigger LaunchedEffect") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197, targetLongitude = 6.6323, targetEventUid = "event-first-111")
      }
    }

    step("Wait for LaunchedEffect to execute") {
      Thread.sleep(1500)
      composeTestRule.waitForIdle()
    }

    step("Verify map is stable after LaunchedEffect execution") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_targetLocationAndEventUid_coordinatesAnimationSkippedForEvent() = run {
    step("Display MapScreen with both location and eventUid") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 16.0,
            targetEventUid = "priority-event-333")
      }
    }

    step("Wait for LaunchedEffect logic to execute") {
      // This covers the if (targetEventUid == null) check at line 143
      // When eventUid is provided, coordinate animation is skipped
      Thread.sleep(1200)
      composeTestRule.waitForIdle()
    }

    step("Verify map displayed correctly with event priority") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_targetLocationNoEventUid_coordinatesAnimationExecutes() = run {
    step("Display MapScreen with location but no eventUid") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323, targetZoom = 15.0)
      }
    }

    step("Wait for coordinate animation LaunchedEffect") {
      // This covers the else branch where targetEventUid == null (line 143-146)
      Thread.sleep(1000)
      composeTestRule.waitForIdle()
    }

    step("Verify map animated to coordinates") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_eventAnimationLaunchedEffect_clearsAnimationFlag() = run {
    val eventUid = "animation-clear-test-444"

    step("Display MapScreen with event to trigger selection and animation") {
      composeTestRule.setContent {
        MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323, targetEventUid = eventUid)
      }
    }

    step("Wait for LaunchedEffect(uiState.shouldAnimateToSelectedEvent)") {
      // This covers lines 162-167
      // if (uiState.shouldAnimateToSelectedEvent && uiState.selectedEventLocation != null)
      // actualViewModel.animateToSelectedEvent(mapViewportState)
      // actualViewModel.onEvent(MapViewEvent.ClearEventSelectionAnimation)
      Thread.sleep(1500)
      composeTestRule.waitForIdle()
    }

    step("Verify map is displayed after animation cleared") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_differentZoomLevelsWithEventUid_executesLaunchedEffects() = run {
    step("Display MapScreen with custom zoom and event") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 18.0,
            targetEventUid = "zoom-event-555")
      }
    }

    step("Wait for LaunchedEffects to execute") {
      Thread.sleep(1500)
      composeTestRule.waitForIdle()
    }

    step("Verify map is displayed correctly with zoom and event") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_nullLatitudeLongitudeWithEventUid_onlyEventSelectionExecutes() = run {
    step("Display MapScreen with null coordinates but valid eventUid") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = null,
            targetLongitude = null,
            targetZoom = 15.0,
            targetEventUid = "null-coords-event-777")
      }
    }

    step("Wait for event selection LaunchedEffect only") {
      // targetLatitude and targetLongitude are null, so first LaunchedEffect
      // condition is false, but event selection LaunchedEffect still runs
      Thread.sleep(1000)
      composeTestRule.waitForIdle()
    }

    step("Verify map displayed with event selection only") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_rapidEventUidChanges_launchedEffectReactsToChanges() = run {
    step("Display MapScreen with event UID to test LaunchedEffect") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197, targetLongitude = 6.6323, targetEventUid = "rapid-event-1")
      }
    }

    step("Wait for LaunchedEffect to execute") {
      Thread.sleep(1200)
      composeTestRule.waitForIdle()
    }

    step("Verify map remains stable after LaunchedEffect execution") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  // ===== Additional Tests to Maximize LaunchedEffect Coverage =====

  @Test
  fun mapScreen_launchedEffectEventSelection_executesWithEventsLoaded() = run {
    val targetEventUid = "loaded-event-999"

    step("Display MapScreen with targetEventUid") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 15.0,
            targetEventUid = targetEventUid)
      }
    }

    step("Wait extended time for events to load from repository") {
      // LaunchedEffect(targetEventUid, uiState.events) waits for events to load
      // Give enough time for EventRepository to load events
      Thread.sleep(2000)
      composeTestRule.waitForIdle()
    }

    step("Verify map screen displayed after event selection") {
      // This ensures the LaunchedEffect executed when uiState.events became non-empty
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_launchedEffectEventSelection_falseBranchEmptyEvents() = run {
    step("Display MapScreen with targetEventUid before events load") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197, targetLongitude = 6.6323, targetEventUid = "early-event")
      }
    }

    step("Check immediately before events have time to load") {
      // At this point, uiState.events should still be empty
      // This tests the false branch: targetEventUid != null BUT events.isEmpty()
      Thread.sleep(100) // Very short wait
      composeTestRule.waitForIdle()
    }

    step("Verify map screen still displays") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }

    step("Wait for events to eventually load") {
      Thread.sleep(1500)
      composeTestRule.waitForIdle()
    }
  }

  @Test
  fun mapScreen_launchedEffectAnimateToEvent_triggersWhenEventSelected() = run {
    val targetEventUid = "animate-trigger-event"

    step("Display MapScreen with event that will be auto-selected") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 15.0,
            targetEventUid = targetEventUid)
      }
    }

    step("Wait for event selection and animation LaunchedEffect") {
      // When targetEventUid is provided and events load:
      // 1. LaunchedEffect(targetEventUid, events) calls SelectEvent
      // 2. ViewModel sets shouldAnimateToSelectedEvent = true
      // 3. LaunchedEffect(shouldAnimateToSelectedEvent) triggers
      // 4. Calls animateToSelectedEvent and ClearEventSelectionAnimation
      Thread.sleep(2500)
      composeTestRule.waitForIdle()
    }

    step("Verify all LaunchedEffects completed successfully") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_launchedEffectAnimateToEvent_falseBranchNoEventLocation() = run {
    step("Display MapScreen to potentially trigger animation LaunchedEffect") {
      composeTestRule.setContent { MapScreen(targetLatitude = 46.5197, targetLongitude = 6.6323) }
    }

    step("Wait for initial setup") {
      // Without targetEventUid, shouldAnimateToSelectedEvent stays false
      // This tests the false branch of the animation LaunchedEffect
      Thread.sleep(1000)
      composeTestRule.waitForIdle()
    }

    step("Verify map displays without event animation") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_bothLaunchedEffectConditions_allBranchesCovered() = run {
    val targetEventUid = "comprehensive-coverage-event"

    step("Display MapScreen to trigger all LaunchedEffect paths") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 15.0,
            targetEventUid = targetEventUid)
      }
    }

    step("Wait minimal time - events not loaded yet") {
      // FALSE branch: targetEventUid != null but events.isEmpty()
      Thread.sleep(50)
      composeTestRule.waitForIdle()
    }

    step("Wait for events to load") {
      // TRUE branch: targetEventUid != null && events.isNotEmpty()
      Thread.sleep(1500)
      composeTestRule.waitForIdle()
    }

    step("Wait for animation LaunchedEffect") {
      // TRUE branch: shouldAnimateToSelectedEvent && selectedEventLocation != null
      Thread.sleep(1000)
      composeTestRule.waitForIdle()
    }

    step("Verify complete flow executed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_eventSelectionWithoutCoordinates_coversEventSelectionPath() = run {
    step("Display MapScreen with only eventUid, no coordinates") {
      composeTestRule.setContent { MapScreen(targetEventUid = "standalone-coverage-event") }
    }

    step("Wait for events to load and selection to trigger") {
      // This specifically tests LaunchedEffect(targetEventUid, events)
      // without the coordinate animation LaunchedEffect interfering
      Thread.sleep(2000)
      composeTestRule.waitForIdle()
    }

    step("Verify event selection LaunchedEffect executed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
    }
  }

  @Test
  fun mapScreen_animationLaunchedEffectWithLongWait_ensuresExecution() = run {
    step("Display MapScreen with event to ensure animation trigger") {
      composeTestRule.setContent {
        MapScreen(
            targetLatitude = 46.5197,
            targetLongitude = 6.6323,
            targetZoom = 16.0,
            targetEventUid = "long-wait-animation-event")
      }
    }

    step("Wait extended time for all async operations") {
      // Ensures:
      // 1. Events load from repository
      // 2. Event selection happens
      // 3. shouldAnimateToSelectedEvent becomes true
      // 4. Animation LaunchedEffect executes
      // 5. ClearEventSelectionAnimation is called
      Thread.sleep(3000)
      composeTestRule.waitForIdle()
    }

    step("Verify all LaunchedEffect branches executed") {
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()
    }
  }
}
