package com.github.se.studentconnect.ui.screen.map

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class MapUiComponentsTest {

  @Test
  fun padding_hasCorrectValues() {
    assertEquals(16.dp, Padding.CONTENT)
    assertEquals(8.dp, Padding.VERTICAL_SPACING)
  }

  @Test
  fun size_hasCorrectValues() {
    assertEquals(56.dp, Size.FAB)
    assertEquals(24.dp, Size.ICON)
    assertEquals(32.dp, Size.LARGE_ICON)
  }

  @Test
  fun corner_hasCorrectValues() {
    assertEquals(12.dp, Corner.RADIUS)
    assertEquals(16.dp, Corner.MAP_RADIUS)
  }

  @Test
  fun elevation_hasCorrectValues() {
    assertEquals(0.dp, Elevation.DEFAULT)
  }
}

class MapConfigurationUnitTest {

  @Test
  fun mapConfiguration_coordinates_hasCorrectValues() {
    assertEquals(6.6283, MapConfiguration.Coordinates.EPFL_LONGITUDE, 0.0001)
    assertEquals(46.5089, MapConfiguration.Coordinates.EPFL_LATITUDE, 0.0001)
  }

  @Test
  fun mapConfiguration_zoom_hasCorrectValues() {
    assertEquals(6.0, MapConfiguration.Zoom.INITIAL, 0.0001)
    assertEquals(10.0, MapConfiguration.Zoom.DEFAULT, 0.0001)
    assertEquals(10.0, MapConfiguration.Zoom.TARGET, 0.0001)
    assertEquals(10.0, MapConfiguration.Zoom.LOCATE_USER, 0.0001)
  }

  @Test
  fun mapConfiguration_animation_hasCorrectValues() {
    assertEquals(2000L, MapConfiguration.Animation.INITIAL_DURATION_MS)
    assertEquals(2500L, MapConfiguration.Animation.TARGET_DURATION_MS)
    assertEquals(1500L, MapConfiguration.Animation.LOCATE_USER_DURATION_MS)
  }

  @Test
  fun mapConfiguration_camera_hasCorrectValues() {
    assertEquals(0.0, MapConfiguration.Camera.BEARING, 0.0001)
    assertEquals(0.0, MapConfiguration.Camera.PITCH, 0.0001)
  }
}

class MapViewEventUnitTest {

  @Test
  fun mapViewEvent_toggleView_isCorrectType() {
    val event = MapViewEvent.ToggleView
    assertTrue(event is MapViewEvent.ToggleView)
  }

  @Test
  fun mapViewEvent_locateUser_isCorrectType() {
    val event = MapViewEvent.LocateUser
    assertTrue(event is MapViewEvent.LocateUser)
  }

  @Test
  fun mapViewEvent_clearError_isCorrectType() {
    val event = MapViewEvent.ClearError
    assertTrue(event is MapViewEvent.ClearError)
  }

  @Test
  fun mapViewEvent_clearLocationAnimation_isCorrectType() {
    val event = MapViewEvent.ClearLocationAnimation
    assertTrue(event is MapViewEvent.ClearLocationAnimation)
  }

  @Test
  fun mapViewEvent_updateSearchText_hasCorrectValue() {
    val text = "Test search"
    val event = MapViewEvent.UpdateSearchText(text)

    assertTrue(event is MapViewEvent.UpdateSearchText)
    assertEquals(text, event.text)
  }

  @Test
  fun mapViewEvent_setLocationPermission_hasCorrectValue() {
    val granted = true
    val event = MapViewEvent.SetLocationPermission(granted)

    assertTrue(event is MapViewEvent.SetLocationPermission)
    assertEquals(granted, event.granted)
  }

  @Test
  fun mapViewEvent_setTargetLocation_hasCorrectValues() {
    val latitude = 46.5089
    val longitude = 6.6283
    val zoom = 10.0
    val event = MapViewEvent.SetTargetLocation(latitude, longitude, zoom)

    assertTrue(event is MapViewEvent.SetTargetLocation)
    assertEquals(latitude, event.latitude, 0.0001)
    assertEquals(longitude, event.longitude, 0.0001)
    assertEquals(zoom, event.zoom, 0.0001)
  }
}

class MapUiStateUnitTest {

  @Test
  fun mapUiState_defaultValues() {
    val state = MapUiState()

    assertEquals("", state.searchText)
    assertTrue(state.isEventsView)
    assertFalse(state.hasLocationPermission)
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertNull(state.targetLocation)
    assertFalse(state.shouldAnimateToLocation)
  }

  @Test
  fun mapUiState_customValues() {
    val state =
        MapUiState(
            searchText = "EPFL",
            isEventsView = false,
            hasLocationPermission = true,
            isLoading = true,
            errorMessage = "Test error",
            targetLocation = null,
            shouldAnimateToLocation = true)

    assertEquals("EPFL", state.searchText)
    assertFalse(state.isEventsView)
    assertTrue(state.hasLocationPermission)
    assertTrue(state.isLoading)
    assertEquals("Test error", state.errorMessage)
    assertNull(state.targetLocation)
    assertTrue(state.shouldAnimateToLocation)
  }
}
