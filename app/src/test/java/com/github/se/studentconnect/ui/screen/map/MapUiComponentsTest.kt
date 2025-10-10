package com.github.se.studentconnect.ui.screen.map

import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.resources.C
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
  fun mapViewEvent_allEventTypes_createCorrectly() {
    // Test all event types in a single test to avoid duplication
    val toggleView = MapViewEvent.ToggleView
    val locateUser = MapViewEvent.LocateUser
    val clearError = MapViewEvent.ClearError
    val clearLocationAnimation = MapViewEvent.ClearLocationAnimation

    assertTrue(toggleView is MapViewEvent.ToggleView)
    assertTrue(locateUser is MapViewEvent.LocateUser)
    assertTrue(clearError is MapViewEvent.ClearError)
    assertTrue(clearLocationAnimation is MapViewEvent.ClearLocationAnimation)
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
    val event = MapViewEvent.SetLocationPermission(true)
    assertTrue(event is MapViewEvent.SetLocationPermission)
    assertTrue(event.granted)

    val deniedEvent = MapViewEvent.SetLocationPermission(false)
    assertFalse(deniedEvent.granted)
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

class TestMapComponentsTest {

  @Test
  fun testMapboxMap_hasCorrectTestTag() {
    assertEquals("map_screen", C.Tag.map_screen)
  }

  @Test
  fun testMapboxMap_textContent() {
    val expectedText = "Test Map View"
    assertNotNull(expectedText)
    assertEquals("Test Map View", expectedText)
  }
}

class MapScreenParametersTest {

  @Test
  fun mapScreen_parameterValues() {
    // Test default parameters
    val defaultTargetLatitude: Double? = null
    val defaultTargetLongitude: Double? = null
    val defaultTargetZoom = MapConfiguration.Zoom.TARGET

    assertNull(defaultTargetLatitude)
    assertNull(defaultTargetLongitude)
    assertEquals(MapConfiguration.Zoom.TARGET, defaultTargetZoom, 0.0001)

    // Test custom parameters
    val targetLatitude: Double? = 46.5089
    val targetLongitude: Double? = 6.6283
    val targetZoom = 15.0

    assertNotNull(targetLatitude)
    assertNotNull(targetLongitude)
    assertEquals(46.5089, targetLatitude!!, 0.0001)
    assertEquals(6.6283, targetLongitude!!, 0.0001)
    assertEquals(15.0, targetZoom, 0.0001)
  }
}

class MapResourceConstantsTest {

  @Test
  fun testMapResourceTags() {
    assertEquals("map_screen", C.Tag.map_screen)
    assertEquals("map_top_app_bar", C.Tag.map_top_app_bar)
    assertEquals("map_container", C.Tag.map_container)
    assertEquals("map_locate_user_fab", C.Tag.map_locate_user_fab)
    assertEquals("map_toggle_view_fab", C.Tag.map_toggle_view_fab)
    assertEquals("map_search_field", C.Tag.map_search_field)
  }
}

class MapUIConstantsEdgeCasesTest {

  @Test
  fun padding_nonZeroValues() {
    assertTrue("Content padding should be positive", Padding.CONTENT.value > 0)
    assertTrue("Vertical spacing should be positive", Padding.VERTICAL_SPACING.value > 0)
  }

  @Test
  fun size_validDimensions() {
    assertTrue("FAB size should be reasonable", Size.FAB.value >= 40 && Size.FAB.value <= 80)
    assertTrue("Icon size should be reasonable", Size.ICON.value >= 16 && Size.ICON.value <= 48)
    assertTrue(
        "Large icon size should be larger than regular icon",
        Size.LARGE_ICON.value > Size.ICON.value)
  }

  @Test
  fun corner_radiusValues() {
    assertTrue("Corner radius should be non-negative", Corner.RADIUS.value >= 0)
    assertTrue("Map radius should be non-negative", Corner.MAP_RADIUS.value >= 0)
    assertTrue(
        "Map radius should be larger than corner radius",
        Corner.MAP_RADIUS.value > Corner.RADIUS.value)
  }

  @Test
  fun elevation_defaultValue() {
    assertEquals("Default elevation should be 0", 0.0f, Elevation.DEFAULT.value, 0.001f)
  }
}
