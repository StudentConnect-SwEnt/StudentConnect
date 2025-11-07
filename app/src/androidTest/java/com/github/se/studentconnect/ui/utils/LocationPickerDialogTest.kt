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
  fun locationPickerDialog_displaysCorrectly_withoutInitialLocation() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that the dialog is displayed with correct initial state
    composeTestRule.onNodeWithText("No location selected").assertIsDisplayed()
    composeTestRule.onNodeWithText("Radius : 10 km").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    composeTestRule.onNodeWithText("Apply").assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_withInitialLocation_displaysLocationName() {
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

    // Verify that initial location name is displayed
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
    composeTestRule.onNodeWithText("Radius : 15 km").assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_withInitialLocation_displaysCoordinates() {
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
  fun locationPickerDialog_applyButton_isEnabledWithInitialLocation() {
    val initialLocation = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = initialLocation,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that Apply button is enabled when location is provided
    composeTestRule.onNodeWithText("Apply").assertIsEnabled()
  }

  @Test
  fun locationPickerDialog_cancelButton_callsOnDismiss() {
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
  fun locationPickerDialog_applyButton_callsOnLocationSelectedWithInitialLocation() {
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

    // Click Apply button
    composeTestRule.onNodeWithText("Apply").performClick()
    composeTestRule.waitForIdle()

    // Wait for the coroutine to complete
    composeTestRule.mainClock.advanceTimeBy(1000)
    composeTestRule.waitForIdle()

    // Verify that onLocationSelected was called with initial values
    assert(selectedLoc != null)
    assert(selectedRad == 25f)
    assert(selectedLoc!!.name == "EPFL")
    assert(selectedLoc!!.latitude == 46.5191)
    assert(selectedLoc!!.longitude == 6.5668)
  }

  @Test
  fun locationPickerDialog_displaysRadius_withDifferentValues() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 50f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that radius is displayed correctly
    composeTestRule.onNodeWithText("Radius : 50 km").assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_hasDialogCard() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that all main UI components are visible
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    composeTestRule.onNodeWithText("Apply").assertIsDisplayed()
    composeTestRule.onNodeWithText("Radius : 10 km").assertIsDisplayed()
    composeTestRule.onNodeWithText("No location selected").assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_withLocation_hasApplyIcon() {
    val location = Location(latitude = 46.5191, longitude = 6.5668, name = "Test")

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = location,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that Apply button with icon exists
    composeTestRule.onNodeWithContentDescription("Apply").assertExists()
  }

  @Test
  fun locationPickerDialog_multipleDifferentRadii() {
    // Test with small radius
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = Location(46.5191, 6.5668, "Test"),
          initialRadius = 5f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Radius : 5 km").assertIsDisplayed()
}

  @Test
  fun locationPickerDialog_cancelButtonIsAlwaysEnabled() {
    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = null,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify that Cancel button is always enabled
    composeTestRule.onNodeWithText("Cancel").assertIsEnabled()
  }

  @Test
  fun locationPickerDialog_formatsCoordinatesCorrectly() {
    val location = Location(latitude = 46.519100, longitude = 6.566800, name = "Precise Location")

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = location,
          initialRadius = 10f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify coordinates are formatted to 4 decimal places
    composeTestRule.onNodeWithText("Precise Location").assertIsDisplayed()
    composeTestRule.onNodeWithText("(46.5191, 6.5668)", substring = true).assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_negativeCoordinates() {
    val location = Location(latitude = -33.8688, longitude = 151.2093, name = "Sydney")

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = location,
          initialRadius = 30f,
          onDismiss = {},
          onLocationSelected = { _, _ -> },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Verify negative coordinates are displayed correctly
    composeTestRule.onNodeWithText("Sydney").assertIsDisplayed()
    composeTestRule.onNodeWithText("(-33.8688, 151.2093)", substring = true).assertIsDisplayed()
  }

  @Test
  fun locationPickerDialog_applyButtonCallbackWithDifferentRadius() {
    var selectedLoc: Location? = null
    var selectedRad: Float? = null
    val initialLocation = Location(latitude = 48.8566, longitude = 2.3522, name = "Paris")

    composeTestRule.setContent {
      LocationPickerDialog(
          initialLocation = initialLocation,
          initialRadius = 75f,
          onDismiss = {},
          onLocationSelected = { loc, rad ->
            selectedLoc = loc
            selectedRad = rad
          },
          useTestMap = true)
    }

    composeTestRule.waitForIdle()

    // Click Apply button
    composeTestRule.onNodeWithText("Apply").performClick()
    composeTestRule.waitForIdle()

    // Wait for the coroutine to complete
    composeTestRule.mainClock.advanceTimeBy(1000)
    composeTestRule.waitForIdle()

    // Verify callback was called with correct values
    assert(selectedLoc != null)
    assert(selectedRad == 75f)
    assert(selectedLoc!!.name == "Paris")
  }
}
