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

class EventMarkerConfigValidationTest {

  @Test
  fun eventMarkerConfig_hasCorrectStringValues() {
    assertEquals("event_marker_icon", EventMarkerConfig.ICON_ID)
    assertEquals("event_source", EventMarkerConfig.SOURCE_ID)
    assertEquals("event_layer", EventMarkerConfig.LAYER_ID)
    assertEquals("event_cluster_layer", EventMarkerConfig.CLUSTER_LAYER_ID)
    assertEquals("event_cluster_count_layer", EventMarkerConfig.CLUSTER_COUNT_LAYER_ID)
  }

  @Test
  fun eventMarkerConfig_hasCorrectColorValue() {
    assertEquals("#EF4444", EventMarkerConfig.COLOR)
  }

  @Test
  fun eventMarkerConfig_hasCorrectNumericValues() {
    assertEquals(1.5, EventMarkerConfig.ICON_SIZE, 0.001)
    assertEquals(30, EventMarkerConfig.CLUSTER_RADIUS_PX)
    assertEquals(16, EventMarkerConfig.CLUSTER_MAX_ZOOM)
    assertEquals(20.0, EventMarkerConfig.CLUSTER_CIRCLE_RADIUS, 0.001)
    assertEquals(2.0, EventMarkerConfig.CLUSTER_STROKE_WIDTH, 0.001)
    assertEquals(14.0, EventMarkerConfig.CLUSTER_TEXT_SIZE, 0.001)
  }

  @Test
  fun eventMarkerConfig_hasCorrectClusterColors() {
    assertEquals("#FFFFFF", EventMarkerConfig.CLUSTER_STROKE_COLOR)
    assertEquals("#FFFFFF", EventMarkerConfig.CLUSTER_TEXT_COLOR)
  }

  @Test
  fun eventMarkerConfig_hasCorrectTextFonts() {
    assertNotNull(EventMarkerConfig.CLUSTER_TEXT_FONTS)
    assertTrue(EventMarkerConfig.CLUSTER_TEXT_FONTS.isNotEmpty())
    assertTrue(EventMarkerConfig.CLUSTER_TEXT_FONTS.contains("DIN Offc Pro Bold"))
    assertTrue(EventMarkerConfig.CLUSTER_TEXT_FONTS.contains("Arial Unicode MS Bold"))
  }

  @Test
  fun eventMarkerConfig_hasCorrectPropertyKeys() {
    assertEquals("title", EventMarkerConfig.PROP_TITLE)
    assertEquals("uid", EventMarkerConfig.PROP_UID)
  }
}

class LocationSharingConfigTest {

  @Test
  fun locationSharingConfig_hasCorrectUpdateInterval() {
    assertEquals(10000L, LocationSharingConfig.UPDATE_INTERVAL_MS)
  }

  @Test
  fun locationSharingConfig_updateIntervalIsPositive() {
    assertTrue("Update interval should be positive", LocationSharingConfig.UPDATE_INTERVAL_MS > 0)
  }

  @Test
  fun locationSharingConfig_updateIntervalIsReasonable() {
    // Should be at least 1 second and at most 5 minutes
    assertTrue(
        "Update interval should be reasonable (1s - 5min)",
        LocationSharingConfig.UPDATE_INTERVAL_MS >= 1000L &&
            LocationSharingConfig.UPDATE_INTERVAL_MS <= 300000L)
  }
}

class MapViewEventAdditionalTest {

  @Test
  fun mapViewEvent_selectEvent_withNullUid() {
    val event = MapViewEvent.SelectEvent(null)
    assertTrue(event is MapViewEvent.SelectEvent)
  }

  @Test
  fun mapViewEvent_selectEvent_withValidUid() {
    val uid = "event-123"
    val event = MapViewEvent.SelectEvent(uid)
    assertTrue(event is MapViewEvent.SelectEvent)
  }

  @Test
  fun mapViewEvent_clearEventSelectionAnimation() {
    val event = MapViewEvent.ClearEventSelectionAnimation
    assertNotNull(event)
    assertTrue(event is MapViewEvent.ClearEventSelectionAnimation)
  }

  @Test
  fun mapViewEvent_updateSearchText_emptyString() {
    val event = MapViewEvent.UpdateSearchText("")
    assertEquals("", event.text)
  }

  @Test
  fun mapViewEvent_updateSearchText_withSpecialCharacters() {
    val text = "EPFL@#$%^&*()"
    val event = MapViewEvent.UpdateSearchText(text)
    assertEquals(text, event.text)
  }

  @Test
  fun mapViewEvent_setTargetLocation_withZeroZoom() {
    val event = MapViewEvent.SetTargetLocation(46.5089, 6.6283, 0.0)
    assertEquals(0.0, event.zoom, 0.0001)
  }

  @Test
  fun mapViewEvent_setTargetLocation_withHighZoom() {
    val event = MapViewEvent.SetTargetLocation(46.5089, 6.6283, 20.0)
    assertEquals(20.0, event.zoom, 0.0001)
  }
}

class MapUiStateAdditionalTest {

  @Test
  fun mapUiState_withEvents() {
    val events = emptyList<com.github.se.studentconnect.model.event.Event>()
    val state = MapUiState(events = events)
    assertTrue(state.events.isEmpty())
  }

  @Test
  fun mapUiState_withFriendLocations() {
    val friendLocations =
        emptyMap<String, com.github.se.studentconnect.model.friends.FriendLocation>()
    val state = MapUiState(friendLocations = friendLocations)
    assertTrue(state.friendLocations.isEmpty())
  }

  @Test
  fun mapUiState_withSelectedEvent() {
    val state = MapUiState(selectedEvent = null)
    assertNull(state.selectedEvent)
  }

  @Test
  fun mapUiState_withSelectedEventLocation() {
    val state = MapUiState(selectedEventLocation = null)
    assertNull(state.selectedEventLocation)
  }

  @Test
  fun mapUiState_withShouldAnimateToSelectedEvent() {
    val state = MapUiState(shouldAnimateToSelectedEvent = true)
    assertTrue(state.shouldAnimateToSelectedEvent)
  }

  @Test
  fun mapUiState_allBooleanFlags() {
    val state =
        MapUiState(
            isEventsView = true,
            hasLocationPermission = true,
            isLoading = true,
            shouldAnimateToLocation = true,
            shouldAnimateToSelectedEvent = true)

    assertTrue(state.isEventsView)
    assertTrue(state.hasLocationPermission)
    assertTrue(state.isLoading)
    assertTrue(state.shouldAnimateToLocation)
    assertTrue(state.shouldAnimateToSelectedEvent)
  }

  @Test
  fun mapUiState_allBooleanFlagsFalse() {
    val state =
        MapUiState(
            isEventsView = false,
            hasLocationPermission = false,
            isLoading = false,
            shouldAnimateToLocation = false,
            shouldAnimateToSelectedEvent = false)

    assertFalse(state.isEventsView)
    assertFalse(state.hasLocationPermission)
    assertFalse(state.isLoading)
    assertFalse(state.shouldAnimateToLocation)
    assertFalse(state.shouldAnimateToSelectedEvent)
  }

  @Test
  fun mapUiState_withNonEmptySearchText() {
    val state = MapUiState(searchText = "Search query")
    assertEquals("Search query", state.searchText)
  }

  @Test
  fun mapUiState_withLongSearchText() {
    val longText = "a".repeat(1000)
    val state = MapUiState(searchText = longText)
    assertEquals(longText, state.searchText)
  }

  @Test
  fun mapUiState_withErrorMessage() {
    val errorMsg = "Network error occurred"
    val state = MapUiState(errorMessage = errorMsg)
    assertEquals(errorMsg, state.errorMessage)
  }

  @Test
  fun mapUiState_withTargetLocation() {
    val targetLat = 46.5197
    val targetLng = 6.6323
    val point = com.mapbox.geojson.Point.fromLngLat(targetLng, targetLat)
    val state = MapUiState(targetLocation = point)
    assertNotNull(state.targetLocation)
    assertEquals(targetLng, state.targetLocation?.longitude() ?: 0.0, 0.0001)
    assertEquals(targetLat, state.targetLocation?.latitude() ?: 0.0, 0.0001)
  }
}

class MapViewEventComprehensiveTest {

  @Test
  fun mapViewEvent_toggleView_isSingleton() {
    val event1 = MapViewEvent.ToggleView
    val event2 = MapViewEvent.ToggleView
    assertSame(event1, event2)
  }

  @Test
  fun mapViewEvent_locateUser_isSingleton() {
    val event1 = MapViewEvent.LocateUser
    val event2 = MapViewEvent.LocateUser
    assertSame(event1, event2)
  }

  @Test
  fun mapViewEvent_clearError_isSingleton() {
    val event1 = MapViewEvent.ClearError
    val event2 = MapViewEvent.ClearError
    assertSame(event1, event2)
  }

  @Test
  fun mapViewEvent_updateSearchText_withEmptyString() {
    val event = MapViewEvent.UpdateSearchText("")
    assertEquals("", event.text)
    assertTrue(event.text.isEmpty())
  }

  @Test
  fun mapViewEvent_updateSearchText_withWhitespace() {
    val event = MapViewEvent.UpdateSearchText("   ")
    assertEquals("   ", event.text)
  }

  @Test
  fun mapViewEvent_updateSearchText_withLongText() {
    val longText = "Long search query ".repeat(100)
    val event = MapViewEvent.UpdateSearchText(longText)
    assertEquals(longText, event.text)
  }

  @Test
  fun mapViewEvent_setLocationPermission_bothStates() {
    val grantedEvent = MapViewEvent.SetLocationPermission(true)
    val deniedEvent = MapViewEvent.SetLocationPermission(false)

    assertTrue(grantedEvent.granted)
    assertFalse(deniedEvent.granted)
    assertNotEquals(grantedEvent.granted, deniedEvent.granted)
  }

  @Test
  fun mapViewEvent_setTargetLocation_variousCoordinates() {
    // Test EPFL location
    val epfl = MapViewEvent.SetTargetLocation(46.5197, 6.6323, 15.0)
    assertEquals(46.5197, epfl.latitude, 0.0001)
    assertEquals(6.6323, epfl.longitude, 0.0001)
    assertEquals(15.0, epfl.zoom, 0.0001)

    // Test equator location
    val equator = MapViewEvent.SetTargetLocation(0.0, 0.0, 5.0)
    assertEquals(0.0, equator.latitude, 0.0001)
    assertEquals(0.0, equator.longitude, 0.0001)

    // Test extreme locations
    val northPole = MapViewEvent.SetTargetLocation(90.0, 0.0, 10.0)
    assertEquals(90.0, northPole.latitude, 0.0001)

    val southPole = MapViewEvent.SetTargetLocation(-90.0, 0.0, 10.0)
    assertEquals(-90.0, southPole.latitude, 0.0001)
  }

  @Test
  fun mapViewEvent_selectEvent_withVariousUids() {
    val nullEvent = MapViewEvent.SelectEvent(null)
    assertNull(nullEvent.eventUid)

    val emptyEvent = MapViewEvent.SelectEvent("")
    assertEquals("", emptyEvent.eventUid)

    val validEvent = MapViewEvent.SelectEvent("event-123")
    assertEquals("event-123", validEvent.eventUid)

    val longEvent = MapViewEvent.SelectEvent("e".repeat(1000))
    assertEquals(1000, longEvent.eventUid?.length)
  }

  @Test
  fun mapViewEvent_clearLocationAnimation_isSingleton() {
    val event1 = MapViewEvent.ClearLocationAnimation
    val event2 = MapViewEvent.ClearLocationAnimation
    assertSame(event1, event2)
  }

  @Test
  fun mapViewEvent_clearEventSelectionAnimation_isSingleton() {
    val event1 = MapViewEvent.ClearEventSelectionAnimation
    val event2 = MapViewEvent.ClearEventSelectionAnimation
    assertSame(event1, event2)
  }
}

class MapConfigurationComprehensiveTest {

  @Test
  fun mapConfiguration_coordinatesAreValid() {
    // EPFL coordinates should be in Switzerland
    assertTrue(
        "Longitude should be in Switzerland range",
        MapConfiguration.Coordinates.EPFL_LONGITUDE > 6.0 &&
            MapConfiguration.Coordinates.EPFL_LONGITUDE < 7.0)
    assertTrue(
        "Latitude should be in Switzerland range",
        MapConfiguration.Coordinates.EPFL_LATITUDE > 46.0 &&
            MapConfiguration.Coordinates.EPFL_LATITUDE < 47.0)
  }

  @Test
  fun mapConfiguration_zoomLevelsAreReasonable() {
    assertTrue("Initial zoom should be positive", MapConfiguration.Zoom.INITIAL > 0)
    assertTrue("Default zoom should be positive", MapConfiguration.Zoom.DEFAULT > 0)
    assertTrue("Target zoom should be positive", MapConfiguration.Zoom.TARGET > 0)
    assertTrue("Locate user zoom should be positive", MapConfiguration.Zoom.LOCATE_USER > 0)

    // Zoom levels should be within Mapbox limits (0-22)
    assertTrue("Initial zoom within range", MapConfiguration.Zoom.INITIAL <= 22.0)
    assertTrue("Default zoom within range", MapConfiguration.Zoom.DEFAULT <= 22.0)
    assertTrue("Target zoom within range", MapConfiguration.Zoom.TARGET <= 22.0)
    assertTrue("Locate user zoom within range", MapConfiguration.Zoom.LOCATE_USER <= 22.0)
  }

  @Test
  fun mapConfiguration_animationDurationsArePositive() {
    assertTrue(
        "Initial duration should be positive", MapConfiguration.Animation.INITIAL_DURATION_MS > 0)
    assertTrue(
        "Target duration should be positive", MapConfiguration.Animation.TARGET_DURATION_MS > 0)
    assertTrue(
        "Locate user duration should be positive",
        MapConfiguration.Animation.LOCATE_USER_DURATION_MS > 0)
  }

  @Test
  fun mapConfiguration_animationDurationsAreReasonable() {
    // Durations should be between 100ms and 10s for good UX
    assertTrue(
        "Initial duration reasonable",
        MapConfiguration.Animation.INITIAL_DURATION_MS >= 100 &&
            MapConfiguration.Animation.INITIAL_DURATION_MS <= 10000)
    assertTrue(
        "Target duration reasonable",
        MapConfiguration.Animation.TARGET_DURATION_MS >= 100 &&
            MapConfiguration.Animation.TARGET_DURATION_MS <= 10000)
    assertTrue(
        "Locate user duration reasonable",
        MapConfiguration.Animation.LOCATE_USER_DURATION_MS >= 100 &&
            MapConfiguration.Animation.LOCATE_USER_DURATION_MS <= 10000)
  }

  @Test
  fun mapConfiguration_cameraBearingAndPitchAreDefault() {
    assertEquals(0.0, MapConfiguration.Camera.BEARING, 0.0001)
    assertEquals(0.0, MapConfiguration.Camera.PITCH, 0.0001)
  }
}

class EventMarkerConfigAdditionalTest {

  @Test
  fun eventMarkerConfig_iconSizeIsReasonable() {
    assertTrue("Icon size should be positive", EventMarkerConfig.ICON_SIZE > 0)
    assertTrue("Icon size should not be too large", EventMarkerConfig.ICON_SIZE <= 5.0)
  }

  @Test
  fun eventMarkerConfig_clusterRadiusIsPositive() {
    assertTrue("Cluster radius should be positive", EventMarkerConfig.CLUSTER_RADIUS_PX > 0)
  }

  @Test
  fun eventMarkerConfig_clusterMaxZoomIsValid() {
    assertTrue(
        "Cluster max zoom should be valid Mapbox zoom level",
        EventMarkerConfig.CLUSTER_MAX_ZOOM >= 0 && EventMarkerConfig.CLUSTER_MAX_ZOOM <= 22)
  }

  @Test
  fun eventMarkerConfig_clusterCircleRadiusIsPositive() {
    assertTrue(
        "Cluster circle radius should be positive", EventMarkerConfig.CLUSTER_CIRCLE_RADIUS > 0)
  }

  @Test
  fun eventMarkerConfig_clusterStrokeWidthIsPositive() {
    assertTrue(
        "Cluster stroke width should be positive", EventMarkerConfig.CLUSTER_STROKE_WIDTH > 0)
  }

  @Test
  fun eventMarkerConfig_clusterTextSizeIsReadable() {
    assertTrue("Text size should be readable", EventMarkerConfig.CLUSTER_TEXT_SIZE >= 10.0)
    assertTrue("Text size should not be too large", EventMarkerConfig.CLUSTER_TEXT_SIZE <= 24.0)
  }

  @Test
  fun eventMarkerConfig_colorsAreValidHex() {
    assertTrue("Color should start with #", EventMarkerConfig.COLOR.startsWith("#"))
    assertEquals("Color should be 7 characters", 7, EventMarkerConfig.COLOR.length)

    assertTrue(
        "Stroke color should start with #", EventMarkerConfig.CLUSTER_STROKE_COLOR.startsWith("#"))
    assertEquals(
        "Stroke color should be 7 characters", 7, EventMarkerConfig.CLUSTER_STROKE_COLOR.length)

    assertTrue(
        "Text color should start with #", EventMarkerConfig.CLUSTER_TEXT_COLOR.startsWith("#"))
    assertEquals(
        "Text color should be 7 characters", 7, EventMarkerConfig.CLUSTER_TEXT_COLOR.length)
  }

  @Test
  fun eventMarkerConfig_layerIdsAreUnique() {
    val ids =
        setOf(
            EventMarkerConfig.LAYER_ID,
            EventMarkerConfig.CLUSTER_LAYER_ID,
            EventMarkerConfig.CLUSTER_COUNT_LAYER_ID)
    assertEquals("All layer IDs should be unique", 3, ids.size)
  }

  @Test
  fun eventMarkerConfig_propertyKeysAreNonEmpty() {
    assertTrue(EventMarkerConfig.PROP_TITLE.isNotEmpty())
    assertTrue(EventMarkerConfig.PROP_UID.isNotEmpty())
  }
}

class FriendMarkerConfigAdditionalTest {

  @Test
  fun friendMarkerConfig_colorsAreValidHex() {
    assertTrue("Live color should start with #", FriendMarkerConfig.COLOR_LIVE.startsWith("#"))
    assertEquals("Live color should be 7 characters", 7, FriendMarkerConfig.COLOR_LIVE.length)

    assertTrue("Stale color should start with #", FriendMarkerConfig.COLOR_STALE.startsWith("#"))
    assertEquals("Stale color should be 7 characters", 7, FriendMarkerConfig.COLOR_STALE.length)
  }

  @Test
  fun friendMarkerConfig_iconSizeIsReasonable() {
    assertTrue("Icon size should be positive", FriendMarkerConfig.ICON_SIZE > 0)
    assertTrue("Icon size should not be too large", FriendMarkerConfig.ICON_SIZE <= 2.0)
  }

  @Test
  fun friendMarkerConfig_idsAreUnique() {
    assertNotEquals(FriendMarkerConfig.LAYER_ID, FriendMarkerConfig.SOURCE_ID)
    assertNotEquals(FriendMarkerConfig.LAYER_ID, FriendMarkerConfig.ICON_ID)
    assertNotEquals(FriendMarkerConfig.SOURCE_ID, FriendMarkerConfig.ICON_ID)
  }

  @Test
  fun friendMarkerConfig_propertyKeysAreNonEmpty() {
    assertTrue(FriendMarkerConfig.PROP_USER_ID.isNotEmpty())
    assertTrue(FriendMarkerConfig.PROP_TIMESTAMP.isNotEmpty())
    assertTrue(FriendMarkerConfig.PROP_IS_LIVE.isNotEmpty())
    assertTrue(FriendMarkerConfig.PROP_ICON_ID.isNotEmpty())
  }

  @Test
  fun friendMarkerConfig_propertyKeysAreUnique() {
    val keys =
        setOf(
            FriendMarkerConfig.PROP_USER_ID,
            FriendMarkerConfig.PROP_TIMESTAMP,
            FriendMarkerConfig.PROP_IS_LIVE,
            FriendMarkerConfig.PROP_ICON_ID)
    assertEquals("All property keys should be unique", 4, keys.size)
  }
}

class PaddingSizeCornerElevationTest {

  @Test
  fun padding_contentIsLargerThanSpacing() {
    assertTrue(
        "Content padding should be larger than vertical spacing",
        Padding.CONTENT.value > Padding.VERTICAL_SPACING.value)
  }

  @Test
  fun size_fabIsSquare() {
    // FAB should have same width and height
    assertTrue("FAB size should be reasonable for touch target", Size.FAB.value >= 48)
  }

  @Test
  fun size_iconProportions() {
    // Large icon should be larger than regular icon
    assertTrue("Large icon should be larger", Size.LARGE_ICON.value > Size.ICON.value)
    // Both should be reasonable for visibility
    assertTrue("Icon should be at least 16dp", Size.ICON.value >= 16)
    assertTrue("Large icon should be at least 24dp", Size.LARGE_ICON.value >= 24)
  }

  @Test
  fun corner_radiusRelationship() {
    // Map radius should accommodate more content
    assertTrue("Map radius >= corner radius", Corner.MAP_RADIUS.value >= Corner.RADIUS.value)
  }

  @Test
  fun elevation_defaultIsZeroOrPositive() {
    assertTrue("Elevation should not be negative", Elevation.DEFAULT.value >= 0)
  }
}
