package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.location.LocationRepository
import com.github.se.studentconnect.ui.theme.AppTheme
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class LocationTextFieldTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockLocationRepository: LocationRepository
  private lateinit var testViewModel: LocationTextFieldViewModel

  @Before
  fun setUp() {
    mockLocationRepository = mockk()
    testViewModel = LocationTextFieldViewModel(mockLocationRepository)
  }

  @Test
  fun locationTextField_clearButtonClearsLocation() {
    var locationChanged: Location? = null
    val location = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")

    composeTestRule.setContent {
      AppTheme {
        LocationTextField(
            selectedLocation = location,
            onLocationChange = { locationChanged = it },
            locationTextFieldViewModel = testViewModel)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Clear location").performClick()
    composeTestRule.waitForIdle()

    assert(locationChanged == null)
  }

  @Test
  fun locationTextField_displaysCoordinatesWhenLocationHasNoName() {
    val location = Location(latitude = 46.5191, longitude = 6.5668, name = null)

    composeTestRule.setContent {
      AppTheme {
        LocationTextField(
            selectedLocation = location,
            onLocationChange = {},
            locationTextFieldViewModel = testViewModel)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("46.5191, 6.5668", substring = true).assertExists()
  }
}
