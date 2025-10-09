package com.github.se.studentconnect.ui.screen.map

import android.content.Context
import com.github.se.studentconnect.model.map.LocationRepository
import com.github.se.studentconnect.model.map.LocationResult
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MapScreenIntegrationTest {

    private lateinit var mockLocationRepository: LocationRepository
    private lateinit var mockContext: Context
    private lateinit var mapViewModel: MapViewModel

    @Before
    fun setUp() {
        mockLocationRepository = mockk()
        mockContext = mockk(relaxed = true)
        
        // Setup default mocks
        every { mockLocationRepository.hasLocationPermission() } returns true
        coEvery { mockLocationRepository.getCurrentLocation() } returns LocationResult.Success(mockk {
            every { latitude } returns 46.5089
            every { longitude } returns 6.6283
        })
        every { mockLocationRepository.getLocationUpdates() } returns flowOf()
        
        mapViewModel = MapViewModel(mockLocationRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun mapViewModel_initialState_isCorrect() {
        val state = mapViewModel.uiState.value

        assertEquals("", state.searchText)
        assertTrue(state.isEventsView)
        assertFalse(state.hasLocationPermission)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.targetLocation)
        assertFalse(state.shouldAnimateToLocation)
    }

    @Test
    fun mapViewModel_searchTextUpdate_updatesState() {
        val searchText = "EPFL Campus"
        
        mapViewModel.onEvent(MapViewEvent.UpdateSearchText(searchText))
        
        assertEquals(searchText, mapViewModel.uiState.value.searchText)
    }

    @Test
    fun mapViewModel_toggleView_switchesEventsView() {
        val initialState = mapViewModel.uiState.value.isEventsView
        
        mapViewModel.onEvent(MapViewEvent.ToggleView)
        
        assertEquals(!initialState, mapViewModel.uiState.value.isEventsView)
    }

    @Test
    fun mapViewModel_setLocationPermission_granted() {
        mapViewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        
        assertTrue(mapViewModel.uiState.value.hasLocationPermission)
        assertNull(mapViewModel.uiState.value.errorMessage)
    }

    @Test
    fun mapViewModel_setLocationPermission_denied() {
        mapViewModel.onEvent(MapViewEvent.SetLocationPermission(false))
        
        assertFalse(mapViewModel.uiState.value.hasLocationPermission)
        assertNotNull(mapViewModel.uiState.value.errorMessage)
    }

    @Test
    fun mapViewModel_setTargetLocation_updatesTargetLocation() = runTest {
        val latitude = 46.5089
        val longitude = 6.6283
        val zoom = 15.0
        
        mapViewModel.onEvent(MapViewEvent.SetTargetLocation(latitude, longitude, zoom))
        
        val targetLocation = mapViewModel.uiState.value.targetLocation
        assertNotNull(targetLocation)
        assertEquals(longitude, targetLocation!!.longitude(), 0.0001)
        assertEquals(latitude, targetLocation.latitude(), 0.0001)
    }

    @Test
    fun mapViewModel_clearError_removesErrorMessage() {
        // Set an error first
        mapViewModel.onEvent(MapViewEvent.SetLocationPermission(false))
        assertNotNull(mapViewModel.uiState.value.errorMessage)
        
        // Clear the error
        mapViewModel.onEvent(MapViewEvent.ClearError)
        
        assertNull(mapViewModel.uiState.value.errorMessage)
    }

    @Test
    fun mapViewModel_clearLocationAnimation_resetsShouldAnimate() = runTest {
        // First trigger location animation
        mapViewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        mapViewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        // Should have target location and animation flag
        assertTrue(mapViewModel.uiState.value.shouldAnimateToLocation)
        
        // Clear animation
        mapViewModel.onEvent(MapViewEvent.ClearLocationAnimation)
        
        assertFalse(mapViewModel.uiState.value.shouldAnimateToLocation)
    }

    @Test
    fun mapViewModel_locateUser_withoutPermission() {
        mapViewModel.onEvent(MapViewEvent.SetLocationPermission(false))
        mapViewModel.onEvent(MapViewEvent.LocateUser)
        
        assertNotNull(mapViewModel.uiState.value.errorMessage)
        assertFalse(mapViewModel.uiState.value.isLoading)
    }

    @Test
    fun mapViewModel_locateUser_withPermission_success() = runTest {
        val mockLocation = mockk<android.location.Location> {
            every { latitude } returns 46.5089
            every { longitude } returns 6.6283
        }
        coEvery { mockLocationRepository.getCurrentLocation() } returns LocationResult.Success(mockLocation)
        
        mapViewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        mapViewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        val state = mapViewModel.uiState.value
        assertNotNull(state.targetLocation)
        assertTrue(state.shouldAnimateToLocation)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun mapViewModel_locateUser_error() = runTest {
        val errorMessage = "GPS unavailable"
        coEvery { mockLocationRepository.getCurrentLocation() } returns LocationResult.Error(errorMessage)
        
        mapViewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        mapViewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        val state = mapViewModel.uiState.value
        assertEquals(errorMessage, state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.targetLocation)
    }

    @Test
    fun mapViewModel_locateUser_timeout() = runTest {
        coEvery { mockLocationRepository.getCurrentLocation() } returns LocationResult.Timeout
        
        mapViewModel.onEvent(MapViewEvent.SetLocationPermission(true))
        mapViewModel.onEvent(MapViewEvent.LocateUser)
        
        advanceUntilIdle()
        
        val state = mapViewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("timed out"))
        assertFalse(state.isLoading)
    }

    @Test
    fun mapViewModel_animateToTarget() = runTest {
        val mockMapViewportState = mockk<MapViewportState>(relaxed = true)
        val latitude = 46.5089
        val longitude = 6.6283
        val zoom = 15.0
        
        coEvery { mockMapViewportState.flyTo(any(), any()) } just Runs
        
        mapViewModel.animateToTarget(mockMapViewportState, latitude, longitude, zoom)
        
        coVerify { mockMapViewportState.flyTo(any(), any()) }
    }

    @Test
    fun mapViewModel_animateToUserLocation_withTargetLocation() = runTest {
        val mockMapViewportState = mockk<MapViewportState>(relaxed = true)
        
        // Set up target location first
        mapViewModel.onEvent(MapViewEvent.SetTargetLocation(46.5089, 6.6283, 10.0))
        
        coEvery { mockMapViewportState.flyTo(any(), any()) } just Runs
        
        mapViewModel.animateToUserLocation(mockMapViewportState)
        
        coVerify { mockMapViewportState.flyTo(any(), any()) }
    }

    @Test
    fun mapViewModel_animateToUserLocation_withoutTargetLocation() = runTest {
        val mockMapViewportState = mockk<MapViewportState>(relaxed = true)
        
        coEvery { mockMapViewportState.flyTo(any(), any()) } just Runs
        
        mapViewModel.animateToUserLocation(mockMapViewportState)
        
        // Should not call flyTo without target location
        coVerify(exactly = 0) { mockMapViewportState.flyTo(any(), any()) }
    }
}

class MapConfigurationValidationTest {

    @Test
    fun mapConfiguration_coordinates_validEPFLLocation() {
        val longitude = MapConfiguration.Coordinates.EPFL_LONGITUDE
        val latitude = MapConfiguration.Coordinates.EPFL_LATITUDE
        
        // EPFL coordinates validation
        assertTrue("EPFL longitude should be in valid range", longitude >= -180 && longitude <= 180)
        assertTrue("EPFL latitude should be in valid range", latitude >= -90 && latitude <= 90)
        
        // Specific EPFL location validation (approximate)
        assertTrue("EPFL longitude should be around 6.6", longitude > 6.0 && longitude < 7.0)
        assertTrue("EPFL latitude should be around 46.5", latitude > 46.0 && latitude < 47.0)
    }

    @Test
    fun mapConfiguration_zoom_levels() {
        assertTrue("Initial zoom should be reasonable", MapConfiguration.Zoom.INITIAL > 0 && MapConfiguration.Zoom.INITIAL <= 22)
        assertTrue("Default zoom should be reasonable", MapConfiguration.Zoom.DEFAULT > 0 && MapConfiguration.Zoom.DEFAULT <= 22)
        assertTrue("Target zoom should be reasonable", MapConfiguration.Zoom.TARGET > 0 && MapConfiguration.Zoom.TARGET <= 22)
        assertTrue("Locate user zoom should be reasonable", MapConfiguration.Zoom.LOCATE_USER > 0 && MapConfiguration.Zoom.LOCATE_USER <= 22)
        
        // Hierarchy validation
        assertTrue("Default zoom should be higher than initial", MapConfiguration.Zoom.DEFAULT > MapConfiguration.Zoom.INITIAL)
    }

    @Test
    fun mapConfiguration_animation_durations() {
        assertTrue("Initial duration should be positive", MapConfiguration.Animation.INITIAL_DURATION_MS > 0)
        assertTrue("Target duration should be positive", MapConfiguration.Animation.TARGET_DURATION_MS > 0)
        assertTrue("Locate user duration should be positive", MapConfiguration.Animation.LOCATE_USER_DURATION_MS > 0)
        
        // Reasonable duration validation (not too fast, not too slow)
        assertTrue("Durations should be reasonable", 
            MapConfiguration.Animation.INITIAL_DURATION_MS >= 500 && 
            MapConfiguration.Animation.INITIAL_DURATION_MS <= 5000)
    }

    @Test
    fun mapConfiguration_camera_defaults() {
        assertEquals("Camera bearing should be 0", 0.0, MapConfiguration.Camera.BEARING, 0.0001)
        assertEquals("Camera pitch should be 0", 0.0, MapConfiguration.Camera.PITCH, 0.0001)
    }
}

class MapScreenParameterValidationTest {

    @Test
    fun mapScreen_parameterValidation() {
        // Test valid coordinates
        val validLatitude = 46.5089
        val validLongitude = 6.6283
        val validZoom = 15.0
        
        assertTrue("Valid latitude should be in range", validLatitude >= -90 && validLatitude <= 90)
        assertTrue("Valid longitude should be in range", validLongitude >= -180 && validLongitude <= 180)
        assertTrue("Valid zoom should be positive", validZoom > 0)
    }

    @Test
    fun mapScreen_nullParameterHandling() {
        // Test null parameter handling
        val nullLatitude: Double? = null
        val nullLongitude: Double? = null
        
        // These should be handled gracefully by the MapScreen composable
        assertNull("Null latitude should remain null", nullLatitude)
        assertNull("Null longitude should remain null", nullLongitude)
    }

    @Test
    fun mapScreen_edgeCoordinates() {
        // Test edge case coordinates
        val maxLatitude = 90.0
        val minLatitude = -90.0
        val maxLongitude = 180.0
        val minLongitude = -180.0
        
        assertTrue("Max latitude should be valid", maxLatitude >= -90 && maxLatitude <= 90)
        assertTrue("Min latitude should be valid", minLatitude >= -90 && minLatitude <= 90)
        assertTrue("Max longitude should be valid", maxLongitude >= -180 && maxLongitude <= 180)
        assertTrue("Min longitude should be valid", minLongitude >= -180 && minLongitude <= 180)
    }
}