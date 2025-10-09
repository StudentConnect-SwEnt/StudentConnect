package com.github.se.studentconnect.ui.screen.map

import com.github.se.studentconnect.model.map.LocationConfig
import com.github.se.studentconnect.model.map.LocationRepository
import com.github.se.studentconnect.model.map.LocationResult
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MapViewModelTest {

    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: MapViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        locationRepository = mockk()
        viewModel = MapViewModel(locationRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun initialState_hasCorrectDefaults() {
        val state = viewModel.uiState.value
        
        assertEquals("", state.searchText)
        assertTrue(state.isEventsView)
        assertFalse(state.hasLocationPermission)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.targetLocation)
    }

    @Test
    fun toggleView_switchesEventsView() {
        viewModel.onEvent(MapViewEvent.ToggleView)
        
        assertFalse(viewModel.uiState.value.isEventsView)
        
        viewModel.onEvent(MapViewEvent.ToggleView)
        
        assertTrue(viewModel.uiState.value.isEventsView)
    }

    @Test
    fun updateSearchText_updatesState() {
        val searchText = "EPFL"
        
        viewModel.onEvent(MapViewEvent.UpdateSearchText(searchText))
        
        assertEquals(searchText, viewModel.uiState.value.searchText)
    }

    @Test
    fun setLocationPermission_granted_updatesStateAndClearsError() {
        viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        
        val state = viewModel.uiState.value
        assertTrue(state.hasLocationPermission)
        assertNull(state.errorMessage)
    }

    @Test
    fun setLocationPermission_denied_updatesStateAndSetsError() {
        viewModel.onEvent(MapViewEvent.SetLocationPermission(false))
        
        val state = viewModel.uiState.value
        assertFalse(state.hasLocationPermission)
        assertEquals(LocationConfig.PERMISSION_REQUIRED, state.errorMessage)
    }

    @Test
    fun clearError_removesErrorMessage() {
        // First set an error
        viewModel.onEvent(MapViewEvent.SetLocationPermission(false))
        assertNotNull(viewModel.uiState.value.errorMessage)
        
        // Then clear it
        viewModel.onEvent(MapViewEvent.ClearError)
        
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun setTargetLocation_updatesTargetLocation() {
        val latitude = 46.5089
        val longitude = 6.6283
        val zoom = 10.0
        
        viewModel.onEvent(MapViewEvent.SetTargetLocation(latitude, longitude, zoom))
        
        val targetLocation = viewModel.uiState.value.targetLocation
        assertNotNull(targetLocation)
        assertEquals(longitude, targetLocation!!.longitude(), 0.0001)
        assertEquals(latitude, targetLocation.latitude(), 0.0001)
    }

    @Test
    fun locateUser_noPermission_setsError() {
        viewModel.onEvent(MapViewEvent.SetLocationPermission(false))
        
        viewModel.onEvent(MapViewEvent.LocateUser)
        
        val state = viewModel.uiState.value
        assertEquals(LocationConfig.PERMISSION_REQUIRED_FOR_FEATURE, state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun locateUser_success_setsTargetLocationAndStopsLoading() = runTest {
        val mockLocation = mockk<android.location.Location> {
            every { latitude } returns 46.5089
            every { longitude } returns 6.6283
        }
        coEvery { locationRepository.getCurrentLocation() } returns LocationResult.Success(mockLocation)
        
        viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        viewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        val targetLocation = state.targetLocation
        assertNotNull(targetLocation)
        assertEquals(6.6283, targetLocation!!.longitude(), 0.0001)
        assertEquals(46.5089, targetLocation.latitude(), 0.0001)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun locateUser_error_setsErrorMessageAndStopsLoading() = runTest {
        val errorMessage = "GPS not available"
        coEvery { locationRepository.getCurrentLocation() } returns LocationResult.Error(errorMessage)
        
        viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        viewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(errorMessage, state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.targetLocation)
    }

    @Test
    fun locateUser_permissionDenied_setsPermissionError() = runTest {
        coEvery { locationRepository.getCurrentLocation() } returns LocationResult.PermissionDenied
        
        viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        viewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(LocationConfig.PERMISSION_REQUIRED_FOR_FEATURE, state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun locateUser_timeout_setsTimeoutError() = runTest {
        coEvery { locationRepository.getCurrentLocation() } returns LocationResult.Timeout
        
        viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        viewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(LocationConfig.LOCATION_TIMEOUT, state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun locateUser_locationDisabled_setsLocationDisabledError() = runTest {
        coEvery { locationRepository.getCurrentLocation() } returns LocationResult.LocationDisabled
        
        viewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        viewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(LocationConfig.LOCATION_DISABLED, state.errorMessage)
        assertFalse(state.isLoading)
    }


    @Test
    fun animateToTarget_callsMapViewportStateFlyTo() = runTest {
        val mapViewportState = mockk<MapViewportState>(relaxed = true)
        val latitude = 46.5089
        val longitude = 6.6283
        val zoom = 10.0
        
        coEvery { mapViewportState.flyTo(any(), any()) } just Runs
        
        viewModel.animateToTarget(mapViewportState, latitude, longitude, zoom)
        
        coVerify { mapViewportState.flyTo(any(), any()) }
    }

    @Test
    fun animateToUserLocation_withTargetLocation_callsMapViewportStateFlyTo() = runTest {
        val mapViewportState = mockk<MapViewportState>(relaxed = true)
        val targetPoint = Point.fromLngLat(6.6283, 46.5089)
        
        // Set target location first
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
    fun coordinates_hasCorrectEPFLValues() {
        assertEquals(6.6283, MapConfiguration.Coordinates.EPFL_LONGITUDE, 0.0001)
        assertEquals(46.5089, MapConfiguration.Coordinates.EPFL_LATITUDE, 0.0001)
    }

    @Test
    fun zoom_hasCorrectValues() {
        assertEquals(6.0, MapConfiguration.Zoom.INITIAL, 0.0001)
        assertEquals(10.0, MapConfiguration.Zoom.DEFAULT, 0.0001)
        assertEquals(10.0, MapConfiguration.Zoom.TARGET, 0.0001)
        assertEquals(10.0, MapConfiguration.Zoom.LOCATE_USER, 0.0001)
    }

    @Test
    fun animation_hasCorrectDurationValues() {
        assertEquals(2000L, MapConfiguration.Animation.INITIAL_DURATION_MS)
        assertEquals(2500L, MapConfiguration.Animation.TARGET_DURATION_MS)
        assertEquals(1500L, MapConfiguration.Animation.LOCATE_USER_DURATION_MS)
    }

    @Test
    fun camera_hasCorrectValues() {
        assertEquals(0.0, MapConfiguration.Camera.BEARING, 0.0001)
        assertEquals(0.0, MapConfiguration.Camera.PITCH, 0.0001)
    }
}

class MapUiStateTest {

    @Test
    fun mapUiState_defaultValues() {
        val state = MapUiState()
        
        assertEquals("", state.searchText)
        assertTrue(state.isEventsView)
        assertFalse(state.hasLocationPermission)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.targetLocation)
    }

    @Test
    fun mapUiState_customValues() {
        val targetLocation = Point.fromLngLat(6.6283, 46.5089)
        val state = MapUiState(
            searchText = "EPFL",
            isEventsView = false,
            hasLocationPermission = true,
            isLoading = true,
            errorMessage = "Test error",
            targetLocation = targetLocation
        )
        
        assertEquals("EPFL", state.searchText)
        assertFalse(state.isEventsView)
        assertTrue(state.hasLocationPermission)
        assertTrue(state.isLoading)
        assertEquals("Test error", state.errorMessage)
        assertEquals(targetLocation, state.targetLocation)
    }
}

class MapViewEventTest {

    @Test
    fun toggleView_isCorrectType() {
        val event = MapViewEvent.ToggleView
        assertTrue(event is MapViewEvent.ToggleView)
    }

    @Test
    fun locateUser_isCorrectType() {
        val event = MapViewEvent.LocateUser
        assertTrue(event is MapViewEvent.LocateUser)
    }

    @Test
    fun clearError_isCorrectType() {
        val event = MapViewEvent.ClearError
        assertTrue(event is MapViewEvent.ClearError)
    }

    @Test
    fun updateSearchText_hasCorrectValue() {
        val text = "Test search"
        val event = MapViewEvent.UpdateSearchText(text)
        
        assertTrue(event is MapViewEvent.UpdateSearchText)
        assertEquals(text, event.text)
    }

    @Test
    fun setLocationPermission_hasCorrectValue() {
        val granted = true
        val event = MapViewEvent.SetLocationPermission(granted)
        
        assertTrue(event is MapViewEvent.SetLocationPermission)
        assertEquals(granted, event.granted)
    }

    @Test
    fun setTargetLocation_hasCorrectValues() {
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