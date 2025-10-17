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
  fun mapScreen_displaysAllComponentsAndInteractions() = run {
    step("Display MapScreen") { composeTestRule.setContent { MapScreen() } }

    step("Verify all main components are displayed") {
      composeTestRule.onNodeWithTag(C.Tag.map_top_app_bar).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_search_field).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_container).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_screen).assertIsDisplayed()
      composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab).assertIsDisplayed()
      composeTestRule.onNodeWithText("Map").assertIsDisplayed()
      composeTestRule.onNodeWithText("Search locations...").assertIsDisplayed()
    }

    step("Verify search bar is interactive") {
      val searchField = composeTestRule.onNodeWithTag(C.Tag.map_search_field)
      searchField.performTextInput("Test location")
    }

    step("Verify toggle view FAB is clickable") {
      val toggleViewFab = composeTestRule.onNodeWithTag(C.Tag.map_toggle_view_fab)
      toggleViewFab.assertHasClickAction()
      toggleViewFab.performClick()
    }

    step("Verify location FAB is displayed and clickable when permission is granted") {
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
}
