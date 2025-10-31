package com.github.se.studentconnect.ui.screen.map

import android.location.Location
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.repository.LocationConfig
import com.github.se.studentconnect.repository.LocationRepository
import com.github.se.studentconnect.repository.LocationResult
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MapViewModelTest {

  private lateinit var locationRepository: LocationRepository
  private lateinit var eventRepository: EventRepository
  private lateinit var friendsRepository:
      com.github.se.studentconnect.model.friends.FriendsRepository
  private lateinit var friendsLocationRepository:
      com.github.se.studentconnect.model.friends.FriendsLocationRepository
  private lateinit var viewModel: MapViewModel
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    locationRepository = mockk()
    eventRepository = mockk()
    friendsRepository = mockk()
    friendsLocationRepository = mockk()
    coEvery { eventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { friendsRepository.getFriends(any()) } returns emptyList()
    every { friendsLocationRepository.stopListening() } just Runs
    viewModel =
        MapViewModel(
            locationRepository, eventRepository, friendsRepository, friendsLocationRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  private fun assertState(
      searchText: String? = null,
      isEventsView: Boolean? = null,
      hasLocationPermission: Boolean? = null,
      isLoading: Boolean? = null,
      errorMessage: String? = null,
      targetLocation: Point? = null
  ) {
    val state = viewModel.uiState.value
    searchText?.let { assertEquals(it, state.searchText) }
    isEventsView?.let { assertEquals(it, state.isEventsView) }
    hasLocationPermission?.let { assertEquals(it, state.hasLocationPermission) }
    isLoading?.let { assertEquals(it, state.isLoading) }
    if (errorMessage != null) assertEquals(errorMessage, state.errorMessage)
    if (targetLocation != null) assertEquals(targetLocation, state.targetLocation)
  }

  @Test
  fun initialState_hasCorrectDefaults() {
    assertState(
        searchText = "",
        isEventsView = true,
        hasLocationPermission = false,
        isLoading = false,
        errorMessage = null,
        targetLocation = null)
  }

  @Test
  fun toggleView_switchesEventsView() {
    viewModel.onEvent(MapViewEvent.ToggleView)
    assertState(isEventsView = false)
    viewModel.onEvent(MapViewEvent.ToggleView)
    assertState(isEventsView = true)
  }

  @Test
  fun updateSearchText_updatesState() {
    viewModel.onEvent(MapViewEvent.UpdateSearchText("EPFL"))
    assertState(searchText = "EPFL")
  }

  @Test
  fun setLocationPermission_granted_updatesStateAndClearsError() {
    viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
    assertState(hasLocationPermission = true, errorMessage = null)
  }

  @Test
  fun setLocationPermission_denied_updatesStateAndSetsError() {
    viewModel.onEvent(MapViewEvent.SetLocationPermission(false))
    assertState(hasLocationPermission = false, errorMessage = LocationConfig.PERMISSION_REQUIRED)
  }

  @Test
  fun clearError_removesErrorMessage() {
    viewModel.onEvent(MapViewEvent.SetLocationPermission(false))
    assertNotNull(viewModel.uiState.value.errorMessage)
    viewModel.onEvent(MapViewEvent.ClearError)
    assertState(errorMessage = null)
  }

  @Test
  fun setTargetLocation_updatesTargetLocation() {
    viewModel.onEvent(MapViewEvent.SetTargetLocation(46.5089, 6.6283, 10.0))
    val target = viewModel.uiState.value.targetLocation
    assertNotNull(target)
    assertEquals(6.6283, target!!.longitude(), 0.0001)
    assertEquals(46.5089, target.latitude(), 0.0001)
  }

  @Test
  fun locateUser_noPermission_setsError() {
    viewModel.onEvent(MapViewEvent.SetLocationPermission(false))
    viewModel.onEvent(MapViewEvent.LocateUser)
    assertState(errorMessage = LocationConfig.PERMISSION_REQUIRED_FOR_FEATURE, isLoading = false)
  }

  @Test
  fun locateUser_success_setsTargetLocationAndStopsLoading() = runTest {
    val mockLocation =
        mockk<Location> {
          every { latitude } returns 46.5089
          every { longitude } returns 6.6283
        }
    coEvery { locationRepository.getCurrentLocation() } returns LocationResult.Success(mockLocation)
    viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
    viewModel.onEvent(MapViewEvent.LocateUser)
    advanceUntilIdle()
    val target = viewModel.uiState.value.targetLocation
    assertNotNull(target)
    assertEquals(6.6283, target!!.longitude(), 0.0001)
    assertEquals(46.5089, target.latitude(), 0.0001)
    assertState(isLoading = false, errorMessage = null)
  }

  @Test
  fun locateUser_variousErrors_setCorrectErrorMessages() = runTest {
    val testCases =
        listOf(
            LocationResult.Error("GPS not available") to "GPS not available",
            LocationResult.PermissionDenied to LocationConfig.PERMISSION_REQUIRED_FOR_FEATURE,
            LocationResult.Timeout to LocationConfig.LOCATION_TIMEOUT,
            LocationResult.LocationDisabled to LocationConfig.LOCATION_DISABLED)

    testCases.forEach { (result, expectedError) ->
      coEvery { locationRepository.getCurrentLocation() } returns result
      viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
      viewModel.onEvent(MapViewEvent.LocateUser)
      advanceUntilIdle()
      assertState(errorMessage = expectedError, isLoading = false)
    }
  }

  @Test
  fun animateToTarget_callsMapViewportStateFlyTo() = runTest {
    val mapViewportState = mockk<MapViewportState>(relaxed = true)
    coEvery { mapViewportState.flyTo(any(), any()) } just Runs
    viewModel.animateToTarget(mapViewportState, 46.5089, 6.6283, 10.0)
    coVerify { mapViewportState.flyTo(any(), any()) }
  }

  @Test
  fun animateToUserLocation_withTargetLocation_callsMapViewportStateFlyTo() = runTest {
    val mapViewportState = mockk<MapViewportState>(relaxed = true)
    viewModel.onEvent(MapViewEvent.SetTargetLocation(46.5089, 6.6283, 10.0))
    coEvery { mapViewportState.flyTo(any(), any()) } just Runs
    viewModel.animateToUserLocation(mapViewportState)
    coVerify { mapViewportState.flyTo(any(), any()) }
  }

  @Test
  fun animateToUserLocation_withoutTargetLocation_doesNotCallFlyTo() = runTest {
    val mapViewportState = mockk<MapViewportState>(relaxed = true)
    coEvery { mapViewportState.flyTo(any(), any()) } just Runs
    viewModel.animateToUserLocation(mapViewportState)
    coVerify(exactly = 0) { mapViewportState.flyTo(any(), any()) }
  }
}

class MapConfigurationTest {
  @Test
  fun allConstants_haveCorrectValues() {
    // Coordinates
    assertEquals(6.6283, MapConfiguration.Coordinates.EPFL_LONGITUDE, 0.0001)
    assertEquals(46.5089, MapConfiguration.Coordinates.EPFL_LATITUDE, 0.0001)
    // Zoom levels
    assertEquals(6.0, MapConfiguration.Zoom.INITIAL, 0.0001)
    assertEquals(10.0, MapConfiguration.Zoom.DEFAULT, 0.0001)
    assertEquals(10.0, MapConfiguration.Zoom.TARGET, 0.0001)
    assertEquals(10.0, MapConfiguration.Zoom.LOCATE_USER, 0.0001)
    // Animation durations
    assertEquals(2000L, MapConfiguration.Animation.INITIAL_DURATION_MS)
    assertEquals(2500L, MapConfiguration.Animation.TARGET_DURATION_MS)
    assertEquals(1500L, MapConfiguration.Animation.LOCATE_USER_DURATION_MS)
    // Camera settings
    assertEquals(0.0, MapConfiguration.Camera.BEARING, 0.0001)
    assertEquals(0.0, MapConfiguration.Camera.PITCH, 0.0001)
  }
}

class MapUiStateTest {
  @Test
  fun mapUiState_defaultAndCustomValues() {
    // Test defaults
    val defaultState = MapUiState()
    assertEquals("", defaultState.searchText)
    assertTrue(defaultState.isEventsView)
    assertFalse(defaultState.hasLocationPermission)
    assertFalse(defaultState.isLoading)
    assertNull(defaultState.errorMessage)
    assertNull(defaultState.targetLocation)
    assertTrue(defaultState.events.isEmpty())

    // Test custom values
    val targetLoc = Point.fromLngLat(6.6283, 46.5089)
    val event =
        com.github.se.studentconnect.model.event.Event.Public(
            uid = "test1",
            ownerId = "owner1",
            title = "Test Event",
            description = "Test",
            location =
                com.github.se.studentconnect.model.location.Location(46.5089, 6.6283, "EPFL"),
            start = com.google.firebase.Timestamp.now(),
            isFlash = false,
            subtitle = "Test")
    val customState =
        MapUiState(
            searchText = "EPFL",
            isEventsView = false,
            hasLocationPermission = true,
            isLoading = true,
            errorMessage = "Test error",
            targetLocation = targetLoc,
            shouldAnimateToLocation = false,
            events = listOf(event))

    assertEquals("EPFL", customState.searchText)
    assertFalse(customState.isEventsView)
    assertTrue(customState.hasLocationPermission)
    assertTrue(customState.isLoading)
    assertEquals("Test error", customState.errorMessage)
    assertEquals(targetLoc, customState.targetLocation)
    assertEquals(1, customState.events.size)
    assertEquals("Test Event", customState.events[0].title)
  }
}

class MapViewEventTest {
  @Test
  fun allEventTypes_haveCorrectTypesAndValues() {
    // Simple events
    assertTrue(MapViewEvent.ToggleView is MapViewEvent.ToggleView)
    assertTrue(MapViewEvent.LocateUser is MapViewEvent.LocateUser)
    assertTrue(MapViewEvent.ClearError is MapViewEvent.ClearError)

    // UpdateSearchText
    val searchEvent = MapViewEvent.UpdateSearchText("Test search")
    assertTrue(searchEvent is MapViewEvent.UpdateSearchText)
    assertEquals("Test search", searchEvent.text)

    // SetLocationPermission
    val permEvent = MapViewEvent.SetLocationPermission(true)
    assertTrue(permEvent is MapViewEvent.SetLocationPermission)
    assertEquals(true, permEvent.granted)

    // SetTargetLocation
    val targetEvent = MapViewEvent.SetTargetLocation(46.5089, 6.6283, 10.0)
    assertTrue(targetEvent is MapViewEvent.SetTargetLocation)
    assertEquals(46.5089, targetEvent.latitude, 0.0001)
    assertEquals(6.6283, targetEvent.longitude, 0.0001)
    assertEquals(10.0, targetEvent.zoom, 0.0001)
  }
}
