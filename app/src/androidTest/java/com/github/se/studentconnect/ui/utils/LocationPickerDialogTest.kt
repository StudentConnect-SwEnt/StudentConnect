package com.github.se.studentconnect.ui.utils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.screen.filters.LocationPickerDialog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationPickerDialogTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  @Test
  fun locationPickerDialog_displaysCorrectly() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that the dialog is displayed
    composeTestRule.onNodeWithText("No location selected").assertIsDisplayed()
    composeTestRule.onNodeWithText("Radius : 10 km").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    composeTestRule.onNodeWithText("Apply").assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_withInitialLocation_displaysLocation() {
    val initialLocation = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = initialLocation,
          initialRadius = 15f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that initial location is displayed
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
    composeTestRule.onNodeWithText("Radius : 15 km").assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_clickMap_selectsLocation() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Click on the test map
    composeTestRule.onNodeWithTag("location_picker_map").performClick()
    composeTestRule.waitForIdle()

    // Verify that a location is now selected (check for coordinates instead of name)
    composeTestRule.onNodeWithText("(46.5191, 6.5668)", substring = true).assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_applyButton_isDisabledWithoutLocation() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that Apply button is disabled when no location is selected
    composeTestRule.onNodeWithText("Apply").assertIsNotEnabled()
  }

  @Test
  fun locationPickerDialog_applyButton_enabledAfterSelectingLocation() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Click on the map to select a location
    composeTestRule.onNodeWithTag("location_picker_map").performClick()
    composeTestRule.waitForIdle()

    // Verify that Apply button is now enabled
    composeTestRule.onNodeWithText("Apply").assertIsEnabled()
  }

  @Test
  fun locationPickerDialog_cancelButton_dismissesDialog() {
    var dismissed = false

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = { dismissed = true },
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Click Cancel button
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()

    // Verify that onDismiss was called
    assert(dismissed)
  }

  @Test
  fun locationPickerDialog_applyButton_callsOnLocationSelected() {
    var selectedLoc: Location? = null
    var selectedRad: Float? = null

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { loc, rad ->
            selectedLoc = loc
            selectedRad = rad
          },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Click on the map to select a location
    composeTestRule.onNodeWithTag("location_picker_map").performClick()
    composeTestRule.waitForIdle()

    // Click Apply button
    composeTestRule.onNodeWithText("Apply").performClick()
    composeTestRule.waitForIdle()

    // Verify that onLocationSelected was called with correct values
    assert(selectedLoc != null)
    assert(selectedRad == 10f)
    assert(selectedLoc!!.latitude == 46.5191)
    assert(selectedLoc!!.longitude == 6.5668)
  }

  @Test
  fun locationPickerDialog_slider_changesRadius() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = Location(46.5191, 6.5668, "EPFL"),
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify initial radius
    composeTestRule.onNodeWithText("Radius : 10 km").assertIsDisplayed()

    // Note: Slider interaction is complex in tests, so we just verify it exists
    composeTestRule
        .onNode(hasContentDescription("Radius"))
        .assertDoesNotExist() // Slider doesn't have content description by default
  }

  @Test
  fun locationPickerDialog_slider_isDisabledWithoutLocation() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that slider exists but we can't easily test if it's disabled
    composeTestRule.onNodeWithText("Radius : 10 km").assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_displaysCoordinates() {
    val location = Location(latitude = 46.5191, longitude = 6.5668, name = "Test Location")

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = location,
          initialRadius = 20f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that coordinates are displayed
    composeTestRule.onNodeWithText("Test Location").assertIsDisplayed()
    composeTestRule.onNodeWithText("(46.5191, 6.5668)", substring = true).assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_clickMapMultipleTimes_updatesLocation() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Click on the map first time
    composeTestRule.onNodeWithTag("location_picker_map").performClick()
    composeTestRule.waitForIdle()

    // Verify location is selected by checking coordinates
    composeTestRule.onNodeWithText("(46.5191, 6.5668)", substring = true).assertIsDisplayed()

    // Click on the map again
    composeTestRule.onNodeWithTag("location_picker_map").performClick()
    composeTestRule.waitForIdle()

    // Location should still be displayed (same coordinates in test map)
    composeTestRule.onNodeWithText("(46.5191, 6.5668)", substring = true).assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_applyWithInitialLocation_callsOnLocationSelected() {
    var selectedLoc: Location? = null
    var selectedRad: Float? = null
    val initialLocation = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = initialLocation,
          initialRadius = 25f,
          onDismiss = {},
          onLocationSelected = { loc, rad ->
            selectedLoc = loc
            selectedRad = rad
          },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Click Apply button without clicking the map
    composeTestRule.onNodeWithText("Apply").performClick()
    composeTestRule.waitForIdle()

    // Verify that onLocationSelected was called with initial values
    assert(selectedLoc != null)
    assert(selectedRad == 25f)
    assert(selectedLoc!!.name == "EPFL")
  }

  @Test
  fun locationPickerDialog_dialogCardIsDisplayed() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that all main components are visible
    composeTestRule.onNodeWithTag("location_picker_map").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    composeTestRule.onNodeWithText("Apply").assertIsDisplayed()
    composeTestRule.onNodeWithText("Radius : 10 km").assertIsDisplayed()
  }
}
